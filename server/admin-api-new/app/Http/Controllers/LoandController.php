<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Loand;
use App\Models\Reservation;
use App\Models\StructUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;
use Carbon\Carbon;
use App\Models\Book;
use Exception;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Validation\ValidationException;
use App\Http\Controllers\SendApiController;

/**
 * Class LoandController
 *
 * Gestion des emprunts (loans) dans le système de bibliothèque.
 *
 * Responsabilités :
 * - Création d’un emprunt à partir d’une réservation valide
 * - Gestion du retour des livres
 * - Consultation filtrée des emprunts
 * - Statistiques et données analytiques
 *
 * Contraintes métier :
 * - Un utilisateur doit appartenir à la structure
 * - Un livre ne peut pas être emprunté deux fois simultanément
 * - Une réservation ne peut être utilisée qu’une seule fois
 */
class LoandController extends Controller
{
    /**
     * Liste des emprunts avec filtres (structure obligatoire)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'idStruct' => 'required|integer|exists:Structure,id',
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'message' => 'ID de structure invalide.',
                    'errors' => $validator->errors()
                ], 422);
            }

            $query = Loand::where('idStruct', $request->idStruct);

            // Filtrage par statut
            if ($request->has('filter')) {
                switch ($request->filter) {
                    case 'active':
                        $query->where('closing', false);
                        break;

                    case 'returned':
                        $query->where('closing', true);
                        break;

                    case 'overdue':
                        $query->where('realReturnDate', '<', now())
                              ->where('closing', false);
                        break;
                }
            }

            // Recherche libre
            if ($request->has('search')) {
                $search = $request->search;
                $query->where(function ($q) use ($search) {
                    $q->where('idUser', 'LIKE', "%$search%")
                      ->orWhere('idAgentGiver', 'LIKE', "%$search%")
                      ->orWhere('idAgentRecover', 'LIKE', "%$search%")
                      ->orWhere('idReservation', 'LIKE', "%$search%");
                });
            }

            $loans = $query->orderBy('dateLoand', 'DESC')->paginate(20);

            return response()->json($loans, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des emprunts : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des emprunts.'], 500);
        }
    }

    /**
     * Affiche un emprunt spécifique
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        try {
            $loand = Loand::where('idLoand', $id)->first();

            if (!$loand) {
                return response()->json(['message' => 'Emprunt non trouvé'], 404);
            }

            return response()->json($loand, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de l\'emprunt ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les détails de l\'emprunt.'], 500);
        }
    }

    /**
     * Emprunts actifs (closing=false) de l'utilisateur courant, avec
     * titre/couverture du livre. Utilise pour la synchronisation a la demande
     * (ouverture de "Ma bibliotheque" cote mobile), en complement de la synchro
     * partielle declenchee par notification push (voir unread() ci-dessous).
     * Memes formats de date que unread() (voir sa note).
     */
    public function mine(Request $request)
    {
        try {
            $idUser = $request->user()->idUser;

            $loans = Loand::where('Loand.idUser', $idUser)
                ->where('Loand.closing', false)
                ->leftJoin('Book', 'Loand.idBook', '=', 'Book.idBook')
                ->select('Loand.*', 'Book.title as bookTitle', 'Book.blanket as bookCover')
                ->orderByDesc('Loand.dateLoand')
                ->get();

            $data = $loans->map(function (Loand $loand) {
                return [
                    'idLoand' => $loand->idLoand,
                    'idReservation' => $loand->idReservation,
                    'idBook' => $loand->idBook,
                    'idAgentGiver' => $loand->idAgentGiver,
                    'idAgentRecover' => $loand->idAgentRecover,
                    'dateLoand' => optional($loand->dateLoand)->format('Y-m-d H:i:s'),
                    'realReturnDate' => optional($loand->realReturnDate)->format('Y-m-d H:i:s'),
                    'actualReturnDate' => optional($loand->actualReturnDate)->format('Y-m-d H:i:s'),
                    'closing' => $loand->closing,
                    'view' => $loand->view,
                    'idStruct' => $loand->idStruct,
                    'idUser' => $loand->idUser,
                    'bookTitle' => $loand->bookTitle,
                    'bookCover' => $loand->bookCover,
                ];
            });

            return response()->json([
                'success' => true,
                'count' => $data->count(),
                'data' => $data,
            ]);
        } catch (Exception $e) {
            Log::error('Erreur lors de la recuperation des emprunts actifs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['success' => false, 'error' => 'Erreur serveur.'], 500);
        }
    }

    /**
     * Emprunts non vus (view=0) de l'utilisateur courant, avec titre/couverture
     * du livre associe. Remplace l'ancien get_unread_loands.php (deja durci pour
     * prendre l'identite du token verifie plutot qu'un champ envoye par le
     * client). Utilise par la synchronisation mobile declenchee par notification
     * push (type "5" / "loand").
     *
     * Note format des dates : renvoyees en 'Y-m-d H:i:s' (et non le format
     * ISO8601 par defaut d'Eloquent) car le code mobile existant
     * (ContainerActivity.converterDate) parse ce format precis avec un
     * SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
     */
    public function unread(Request $request)
    {
        try {
            $idUser = $request->user()->idUser;

            $loans = Loand::where('Loand.idUser', $idUser)
                ->where('Loand.view', false)
                ->leftJoin('Book', 'Loand.idBook', '=', 'Book.idBook')
                ->select('Loand.*', 'Book.title as bookTitle', 'Book.blanket as bookCover')
                ->orderByDesc('Loand.updated_at')
                ->get();

            $data = $loans->map(function (Loand $loand) {
                return [
                    'idLoand' => $loand->idLoand,
                    'idReservation' => $loand->idReservation,
                    'idBook' => $loand->idBook,
                    'idAgentGiver' => $loand->idAgentGiver,
                    'idAgentRecover' => $loand->idAgentRecover,
                    'dateLoand' => optional($loand->dateLoand)->format('Y-m-d H:i:s'),
                    'realReturnDate' => optional($loand->realReturnDate)->format('Y-m-d H:i:s'),
                    'actualReturnDate' => optional($loand->actualReturnDate)->format('Y-m-d H:i:s'),
                    'closing' => $loand->closing,
                    'view' => $loand->view,
                    'idStruct' => $loand->idStruct,
                    'idUser' => $loand->idUser,
                    'bookTitle' => $loand->bookTitle,
                    'bookCover' => $loand->bookCover,
                ];
            });

            return response()->json([
                'success' => true,
                'count' => $data->count(),
                'data' => $data,
            ]);
        } catch (Exception $e) {
            Log::error('Erreur lors de la recuperation des emprunts non vus : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['success' => false, 'error' => 'Erreur serveur.'], 500);
        }
    }

    /**
     * Marque un emprunt comme vu (view=1), strictement scope a l'utilisateur
     * courant (idUser pris du token, pas du client). Remplace l'ancien
     * mark_loand_viewed.php.
     */
    public function markViewed(Request $request, $id)
    {
        try {
            $idUser = $request->user()->idUser;

            $loand = Loand::where('idLoand', $id)->where('idUser', $idUser)->first();

            if (!$loand) {
                return response()->json([
                    'success' => false,
                    'error' => 'Emprunt introuvable pour cet utilisateur.',
                ], 404);
            }

            if ($loand->view) {
                return response()->json([
                    'success' => true,
                    'message' => 'Emprunt deja marque comme vu.',
                ]);
            }

            $loand->view = true;
            $loand->save();

            return response()->json([
                'success' => true,
                'message' => 'Emprunt marque comme vu.',
            ]);
        } catch (Exception $e) {
            Log::error('Erreur lors du marquage de l\'emprunt ' . $id . ' comme vu : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['success' => false, 'error' => 'Erreur serveur.'], 500);
        }
    }

    /**
     * Création d’un emprunt à partir d’une réservation
     *
     * Processus :
     * 1. Vérification de la réservation
     * 2. Vérification appartenance utilisateur
     * 3. Vérification disponibilité livre
     * 4. Création emprunt
     * 5. Mise à jour réservation
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'idStruct'      => 'required|integer|exists:Structure,id',
            'idReservation' => 'required|integer|exists:Reservation,idReservation',
            'idAgentGiver'  => 'required|string|max:256',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();

        try {
            // Verrouiller la réservation
            $reservation = Reservation::where('idReservation', $request->idReservation)
                ->lockForUpdate()
                ->first();

            if (!$reservation) {
                return response()->json(['message' => 'Réservation introuvable'], 404);
            }

            // Vérifier que l'utilisateur appartient à la structure
            $belongs = StructUser::where('idUser', $reservation->idNumber)
                ->where('idStruct', $request->idStruct)
                ->exists();

            if (!$belongs) {
                return response()->json([
                    'message' => 'Utilisateur non autorisé dans cette structure'
                ], 403);
            }

            // Vérifier la validité de la réservation
            // state: 1 = pending, 2 = approved, 3 = rejected, 4 = served, 5 = expired (à définir)
            if ($reservation->expireDate < now()) {
                return response()->json(['message' => 'Réservation expirée'], 400);
            }
            if ($reservation->state == 4) { // served
                return response()->json(['message' => 'Réservation déjà utilisée'], 409);
            }
            if ($reservation->state != 1 && $reservation->state != 2) {
                return response()->json(['message' => 'Réservation non valide pour un emprunt'], 400);
            }

            // Récupérer le livre associé
            $book = Book::find($reservation->idBook);
            if (!$book) {
                return response()->json(['message' => 'Livre introuvable'], 404);
            }
            if (!$book->isPhysic) {
                return response()->json(['message' => 'Ce livre n\'est pas physique'], 400);
            }
            if ($book->available <= 0) {
                return response()->json(['message' => 'Aucun exemplaire disponible'], 409);
            }

            // Vérifier qu'il n'y a pas d'emprunt actif pour ce livre
            // Note : la table Loand n'a pas idBook, donc on doit passer par les réservations
            // Solution : on cherche un emprunt non clos dont la réservation associée a le même idBook
            $activeLoan = Loand::where('closing', false)
                ->whereHas('reservation', function ($q) use ($reservation) {
                    $q->where('idBook', $reservation->idBook);
                })
                ->exists();

            if ($activeLoan) {
                return response()->json([
                    'message' => 'Livre déjà emprunté'
                ], 409);
            }

            // Décrémenter le stock
            //$book->available -= 1;
            $book->save();

            // Créer l'emprunt
            $loan = Loand::create([
                'idReservation' => $reservation->idReservation,
                'idAgentGiver'  => $request->idAgentGiver,
                'dateLoand'     => now(),
                'realReturnDate' => now()->addDays($reservation->numberOfDay), // champ numberOfDay
                'closing'       => false,
                'view'          => false,
                'idStruct'      => $request->idStruct,
                'idUser'        => $reservation->idNumber,
                'idBook'        => $reservation->idBook,
                // idAgentRecover, actualReturnDate sont null par défaut
            ]);

            // Mettre à jour la réservation
            $reservation->update([
                'state' => 4, // served
                'deliveryDate' => now(),
                'treat' => 1,   // ou true selon la colonne (tinyint)
            ]);

            DB::commit();
            $sendApiController = new SendApiController();
            $sendApiController->sendApiRequest($reservation->idNumber, $reservation->idBook, 5);
            return response()->json([
                'message' => 'Emprunt créé avec succès',
                'data' => $loan
            ], 201);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la création de l\'emprunt : ' . $e->getMessage(), [
                'data' => $request->all(),
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la création de l\'emprunt.'], 500);
        }
    }

    /**
     * Mise à jour d’un emprunt
     *
     * Cas principal : retour du livre
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        try {
            $loand = Loand::where('idLoand', $id)
                ->lockForUpdate()
                ->first();

            if (!$loand) {
                return response()->json(['message' => 'Emprunt non trouvé'], 404);
            }

            DB::beginTransaction();

            try {
                $data = [];

                // Gestion du retour
                if ($request->has('closing') && $request->closing === true) {
                    if (!$request->filled('idAgentRecover')) {
                        return response()->json([
                            'message' => 'Agent de retour requis'
                        ], 422);
                    }

                    // Vérifier que l'emprunt n'est pas déjà clos
                    if ($loand->closing) {
                        return response()->json(['message' => 'Emprunt déjà clôturé'], 400);
                    }

                    $data['closing'] = true;
                    $data['actualReturnDate'] = now();
                    $data['idAgentRecover'] = $request->idAgentRecover;

                    // Récupérer le livre via la réservation
                    $reservation = Reservation::find($loand->idReservation);
                    if ($reservation) {
                        $book = Book::find($reservation->idBook);
                        if ($book) {
                            // Incrémenter le stock
                            $book->available += 1;
                            $book->save();
                        }
                    }
                }

                // Mise à jour champ view
                if ($request->has('view')) {
                    $data['view'] = (bool) $request->view;
                }

                $loand->update($data);

                DB::commit();
                return response()->json([
                    'message' => 'Emprunt mis à jour',
                    'data' => $loand
                ], 200);

            } catch (Exception $e) {
                DB::rollBack();
                Log::error('Erreur lors de la mise à jour de l\'emprunt ID ' . $id . ' : ' . $e->getMessage(), [
                    'trace' => $e->getTraceAsString()
                ]);
                return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour.'], 500);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'emprunt ID ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Emprunt non trouvé.'], 404);
        }
    }

    /**
     * Suppression d’un emprunt
     *
     * Autorisé uniquement si l’emprunt est clôturé
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        try {
            $loand = Loand::where('idLoand', $id)->first();

            if (!$loand) {
                return response()->json(['message' => 'Introuvable'], 404);
            }

            if (!$loand->closing) {
                return response()->json([
                    'message' => 'Impossible de supprimer un emprunt actif'
                ], 400);
            }

            $loand->delete();

            return response()->json(['message' => 'Supprimé'], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la suppression de l\'emprunt ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de supprimer l\'emprunt.'], 500);
        }
    }

    /**
     * Statistiques globales des emprunts
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getStats(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $stats = [
                'totalLoans'    => Loand::where('idStruct', $idStruct)->count(),
                'activeLoans'   => Loand::where('idStruct', $idStruct)->where('closing', false)->count(),
                'returnedLoans' => Loand::where('idStruct', $idStruct)->where('closing', true)->count(),
                'overdueLoans'  => Loand::where('idStruct', $idStruct)
                                        ->where('realReturnDate', '<', now())
                                        ->where('closing', false)
                                        ->count(),
            ];

            return response()->json($stats, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des statistiques : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les statistiques.'], 500);
        }
    }

    /**
     * Statistiques globales des emprunts (par identifiant de structure)
     *
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function getStatsById($idStruct)
    {
        try {
            $stats = [
                'totalLoans'    => Loand::where('idStruct', $idStruct)->count(),
                'activeLoans'   => Loand::where('idStruct', $idStruct)->where('closing', false)->count(),
                'returnedLoans' => Loand::where('idStruct', $idStruct)->where('closing', true)->count(),
                'overdueLoans'  => Loand::where('idStruct', $idStruct)
                                        ->where('realReturnDate', '<', now())
                                        ->where('closing', false)
                                        ->count(),
            ];

            return response()->json($stats, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des statistiques pour la structure ID ' . $idStruct . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les statistiques.'], 500);
        }
    }

    /**
     * Distribution des emprunts par jour/mois
     *
     * Permet d'analyser la charge du système dans le temps
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getDistribution(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'idStruct' => 'required|integer|exists:Structure,id',
                'period'   => 'nullable|in:day,month'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'message' => 'Paramètres invalides',
                    'errors'  => $validator->errors()
                ], 422);
            }

            $period = $request->get('period', 'day');

            if ($period === 'month') {
                $format = '%Y-%m';
            } else {
                $format = '%Y-%m-%d';
            }

            $data = Loand::where('idStruct', $request->idStruct)
                ->select(
                    DB::raw("DATE_FORMAT(dateLoand, '$format') as period"),
                    DB::raw("COUNT(*) as total")
                )
                ->groupBy('period')
                ->orderBy('period', 'ASC')
                ->get();

            return response()->json($data, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération de la distribution des emprunts : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la distribution.'], 500);
        }
    }

    /**
     * Analyse des retours (à temps vs en retard)
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getReturnDistribution(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $onTime = Loand::where('idStruct', $idStruct)
                ->where('closing', true)
                ->whereColumn('actualReturnDate', '<=', 'realReturnDate')
                ->count();

            $late = Loand::where('idStruct', $idStruct)
                ->where('closing', true)
                ->whereColumn('actualReturnDate', '>', 'realReturnDate')
                ->count();

            return response()->json([
                'onTime' => $onTime,
                'late'   => $late
            ], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération de la distribution des retours : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les données.'], 500);
        }
    }

    /**
     * Top utilisateurs par nombre d'emprunts
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getTopUsers(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $data = Loand::where('idStruct', $idStruct)
                ->select('idUser', DB::raw('COUNT(*) as total'))
                ->groupBy('idUser')
                ->orderByDesc('total')
                ->limit(10)
                ->get();

            return response()->json($data, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération du top utilisateurs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les données.'], 500);
        }
    }

    /**
     * Top livres les plus empruntés
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getTopBooks(Request $request)
    {
        try {
            $idStruct = $request->idStruct;
            $limit = $request->get('limit', 10);

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $data = DB::table('Loand')
                ->join('Reservation', 'Loand.idReservation', '=', 'Reservation.idReservation')
                ->where('Loand.idStruct', $idStruct)
                ->select('Reservation.idBook', DB::raw('COUNT(*) as total'))
                ->groupBy('Reservation.idBook')
                ->orderByDesc('total')
                ->limit($limit)
                ->get();

            return response()->json($data, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération du top livres : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les données.'], 500);
        }
    }
}