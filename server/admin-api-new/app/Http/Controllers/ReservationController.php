<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Reservation;
use App\Models\StructUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;
use App\Models\Book;
use App\Http\Controllers\SendApiController;
use Exception;

/**
 * Class ReservationController
 *
 * Gestion des réservations de livres.
 *
 * Responsabilités :
 * - Création de réservation
 * - Validation / traitement
 * - Expiration automatique
 * - Suivi des états
 * - Statistiques
 *
 * États possibles :
 * - pending  : en attente
 * - approved : validée
 * - rejected : refusée
 * - served   : utilisée (transformée en emprunt)
 * - expired  : expirée
 */
class ReservationController extends Controller
{
    /**
     * Liste des réservations avec filtres.
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
                    'message' => $validator->errors()->first(),
                    'errors'  => $validator->errors(),
                ], 422);
            }

            $query = Reservation::where('idStruct', $request->idStruct);

            // Filtrage par état (avec conversion)
            if ($request->has('state')) {
                $stateInt = $this->mapStateToInt($request->state);
                if ($stateInt !== null) {
                    $query->where('state', $stateInt);
                }
            }

            // Recherche
            if ($request->has('search')) {
                $search = $request->search;
                $query->where(function ($q) use ($search) {
                    $q->where('idNumber', 'LIKE', "%$search%")
                      ->orWhere('idBook', 'LIKE', "%$search%");
                });
            }

            $reservations = $query->orderBy('date', 'DESC')->paginate(20);

            return response()->json($reservations, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des réservations : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des réservations.'], 500);
        }
    }

    /**
     * Détails d’une réservation.
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($id)
    {
        try {
            $reservation = Reservation::where('idReservation', $id)->first();

            if (!$reservation) {
                return response()->json(['message' => 'Réservation introuvable'], 404);
            }

            return response()->json($reservation, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de la réservation ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les détails de la réservation.'], 500);
        }
    }

    /**
     * Création d’une réservation.
     *
     * Contraintes :
     * - utilisateur doit appartenir à la structure
     * - un livre ne peut pas être réservé plusieurs fois activement
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'idStruct'     => 'required|integer|exists:Structure,id',
            'idUser'       => 'required|string|max:256',
            'idBook'       => 'required|string|max:256',
            'numberOfDays' => 'required|integer|min:1|max:30'
        ]);
        

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();

        try {
            // Vérification appartenance utilisateur
            $belongs = StructUser::where('idUser', $request->idUser)
                ->where('idStruct', $request->idStruct)
                ->exists();

            if (!$belongs) {
                return response()->json([
                    'message' => 'Utilisateur non autorisé'
                ], 403);
            }

            // Vérification du livre
            $book = Book::find($request->idBook);
            if (!$book) {
                return response()->json(['message' => 'Livre introuvable'], 404);
            }
            if (!$book->isPhysic) {
                return response()->json(['message' => 'Ce livre n\'est pas physique, réservation impossible'], 400);
            }
            if ($book->available <= 0) {
                return response()->json(['message' => 'Aucun exemplaire disponible'], 409);
            }

            // Vérification réservation active existante
            $existing = Reservation::where('idBook', $request->idBook)
                ->where('idStruct', $request->idStruct)
                ->where('idNumber', $request->idUser)
                ->whereIn('state', ['pending', 'approved'])
                ->exists();

            if ($existing ) {
                return response()->json([
                    'message' => 'Livre déjà réservé'
                ], 409);
            }

            // Création
            $reservation = Reservation::create([
                'idNumber'     => $request->idUser,
                'idBook'       => $request->idBook,
                'idStruct'     => $request->idStruct,
                'numberOfDay'  => $request->numberOfDays,
                'state'        => $this->mapStateToInt('pending'),
                'treat'        => false,
                'expireDate'   => now()->addDays(2)
            ]);
            $book->available -= 1;
            $book->save();
            DB::commit();

            return response()->json([
                'message' => 'Réservation créée ',
                'data'    => $reservation
            ], 201);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la création de la réservation : ' . $e->getMessage(), [
                'data' => $request->all(),
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la création de la réservation.'], 500);
        }
    }

    /**
     * Validation / rejet d’une réservation.
     *
     * @param Request $request
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $id)
    {
        try {
            $reservation = Reservation::where('idReservation', $id)
                ->lockForUpdate()
                ->first();

            if (!$reservation) {
                return response()->json(['message' => 'Réservation introuvable'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de la réservation ID ' . $id . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Réservation introuvable.'], 404);
        }

        DB::beginTransaction();

        try {
            if ($request->state === 'approved') {
                $reservation->update([
                    'state' => $this->mapStateToInt('approved'),
                    'treat' => true,
                ]);
                $sendApiController = new SendApiController();
                $sendApiController->sendApiRequest($reservation->idNumber, $reservation->idBook, 4);

            }

            if ($request->state === 'rejected') {
                $reservation->update([
                    'state' => $this->mapStateToInt('rejected'),
                    'treat' => true
                ]);
                $sendApiController = new SendApiController();
                $sendApiController->sendApiRequest($reservation->idNumber, $reservation->idBook, -4);
            }

            DB::commit();

            return response()->json([
                'message' => 'Mise à jour effectuée',
                'data' => $reservation
            ], 200);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la mise à jour de la réservation ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour.'], 500);
        }
    }

    /**
     * Suppression d’une réservation.
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($id)
    {
        try {
            $reservation = Reservation::where('idReservation', $id)->first();

            if (!$reservation) {
                return response()->json(['message' => 'Réservation introuvable'], 404);
            }

            if (in_array($reservation->state, ['approved', 'served'])) {
                return response()->json([
                    'message' => 'Suppression interdite'
                ], 400);
            }

            $reservation->delete();

            return response()->json(['message' => 'Réservation supprimée'], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la suppression de la réservation ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de supprimer la réservation.'], 500);
        }
    }

    /**
     * Convertit un état texte en entier (stockage en base).
     *
     * @param string|null $state
     * @return int|null
     */
    private function mapStateToInt(?string $state): ?int
    {
        $map = [
            'pending'  => 1,
            'approved' => 2,
            'rejected' => 3,
            'served'   => 4,
            'expired'  => 5,
        ];
        return $map[$state] ?? null;
    }

    /**
     * Expiration automatique des réservations dépassées.
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function expireReservations()
    {
        try {
            $expired = Reservation::where('expireDate', '<', now())
                ->whereIn('state', [
                    $this->mapStateToInt('pending'),
                    $this->mapStateToInt('approved')
                ])
                ->update(['state' => $this->mapStateToInt('expired')]);

            return response()->json([
                'expiredCount' => $expired
            ], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'expiration des réservations : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de traiter les expirations.'], 500);
        }
    }

    /**
     * Statistiques globales des réservations.
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
                'total'    => Reservation::where('idStruct', $idStruct)->count(),
                'pending'  => Reservation::where('idStruct', $idStruct)->where('state', $this->mapStateToInt('pending'))->count(),
                'approved' => Reservation::where('idStruct', $idStruct)->where('state', $this->mapStateToInt('approved'))->count(),
                'rejected' => Reservation::where('idStruct', $idStruct)->where('state', $this->mapStateToInt('rejected'))->count(),
                'served'   => Reservation::where('idStruct', $idStruct)->where('state', $this->mapStateToInt('served'))->count(),
                'expired'  => Reservation::where('idStruct', $idStruct)->where('state', $this->mapStateToInt('expired'))->count(),
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
     * Distribution des réservations par date.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getDistribution(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $data = Reservation::where('idStruct', $idStruct)
                ->select(
                    DB::raw("DATE(date) as date"),
                    DB::raw("COUNT(*) as total")
                )
                ->groupBy('date')
                ->orderBy('date', 'ASC')
                ->get();

            return response()->json($data, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération de la distribution : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la distribution.'], 500);
        }
    }

    /**
     * Taux de conversion des réservations (servies / total).
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getConversionRate(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $total = Reservation::where('idStruct', $idStruct)->count();

            if ($total === 0) {
                return response()->json(['conversionRate' => 0], 200);
            }

            $served = Reservation::where('idStruct', $idStruct)
                ->where('state', $this->mapStateToInt('served'))
                ->count();

            $rate = ($served / $total) * 100;

            return response()->json([
                'conversionRate' => round($rate, 2)
            ], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du calcul du taux de conversion : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de calculer le taux de conversion.'], 500);
        }
    }

    /**
     * Top des livres les plus réservés.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getTopReservedBooks(Request $request)
    {
        try {
            $idStruct = $request->idStruct;

            if (!$idStruct) {
                return response()->json(['message' => 'ID de structure requis.'], 422);
            }

            $data = Reservation::where('idStruct', $idStruct)
                ->select('idBook', DB::raw('COUNT(*) as total'))
                ->groupBy('idBook')
                ->orderByDesc('total')
                ->limit(10)
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