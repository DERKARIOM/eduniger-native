<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Author;
use App\Models\Book;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Illuminate\Support\Facades\Log;
use Exception;

class AuthorController extends Controller
{
    /**
     * Affiche la liste des auteurs avec recherche optionnelle.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            if ($request->query('search')) {
                $search = $request->query('search');
                // Sécurisation : utilisation de whereRaw avec binding pour éviter l'injection SQL
                $authors = Author::whereRaw("CONCAT(firstName, ' ', name) LIKE ?", ["%$search%"])
                    ->orWhere('name', 'LIKE', "%$search%")
                    ->orWhere('firstName', 'LIKE', "%$search%")
                    ->get();
            } else {
                $authors = Author::all();
            }

            return response()->json($authors, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des auteurs : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des auteurs.'], 500);
        }
    }

    /**
     * Affiche un auteur spécifique par son ID.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($idAuthor)
    {
        // Identifiant strictement numérique : evite une requete inutile (et un 500
        // possible) pour /api/authors/abc ; meme reponse qu'un auteur inexistant.
        if (!ctype_digit((string) $idAuthor)) {
            return response()->json(['message' => 'Auteur non trouvé.'], 404);
        }
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
            $authorData = $author->toArray();

            // Compteurs historiques (tous les livres de l'auteur, utilises par le Web
            // d'administration) : meme semantique qu'avant, mais calcules en UNE requete
            // au lieu de quatre.
            $all = DB::table('Book')
                ->where('idAuthor', $idAuthor)
                ->selectRaw("COUNT(*) AS total,
                    SUM(CASE WHEN electronic IS NOT NULL THEN 1 ELSE 0 END) AS electronic,
                    SUM(CASE WHEN isPhysic = 1 THEN 1 ELSE 0 END) AS physical,
                    SUM(CASE WHEN isAudio = 1 THEN 1 ELSE 0 END) AS audio")
                ->first();
            $authorData['books_count'] = (int) ($all->total ?? 0);
            $authorData['books_count_electronic'] = (int) ($all->electronic ?? 0);
            $authorData['books_count_physical'] = (int) ($all->physical ?? 0);
            $authorData['books_count_audio'] = (int) ($all->audio ?? 0);

            // Profil public (application mobile) : seuls les livres PUBLIES, c'est-a-dire
            // rattaches a au moins une structure (StructBook) - ce sont les seuls que le
            // lecteur peut ouvrir (couverture et fichiers sont stockes par structure).
            // Memes regles que /api/book/associations?authorId=..&published=1, donc les
            // chiffres affiches correspondent exactement a la bibliographie listee.
            $published = DB::table('Book')
                ->where('Book.idAuthor', $idAuthor)
                ->whereExists(function ($q) {
                    $q->select(DB::raw(1))->from('StructBook')->whereColumn('StructBook.idBook', 'Book.idBook');
                })
                ->selectRaw("COUNT(*) AS total,
                    SUM(CASE WHEN electronic IS NOT NULL AND electronic <> '' AND electronic <> 'null' THEN 1 ELSE 0 END) AS electronic,
                    SUM(CASE WHEN isAudio = 1 THEN 1 ELSE 0 END) AS audio,
                    SUM(CASE WHEN isPhysic = 1 THEN 1 ELSE 0 END) AS physical,
                    MIN(created_at) AS first_added_at,
                    MAX(created_at) AS last_added_at")
                ->first();
            $electronic = (int) ($published->electronic ?? 0);
            $audio = (int) ($published->audio ?? 0);
            $physical = (int) ($published->physical ?? 0);
            $authorData['public_stats'] = [
                'publications'   => (int) ($published->total ?? 0),
                'electronic'     => $electronic,
                'audio'          => $audio,
                'physical'       => $physical,
                'formats'        => ($electronic > 0 ? 1 : 0) + ($audio > 0 ? 1 : 0) + ($physical > 0 ? 1 : 0),
                'first_added_at' => $published->first_added_at ?? null,
                'last_added_at'  => $published->last_added_at ?? null,
            ];

            // Repartition par categorie (livres publies), une seule requete groupee.
            $authorData['categories'] = DB::table('Book')
                ->join('BookCategory', 'BookCategory.idBook', '=', 'Book.idBook')
                ->join('Category', 'Category.idCategory', '=', 'BookCategory.idCategory')
                ->where('Book.idAuthor', $idAuthor)
                ->whereExists(function ($q) {
                    $q->select(DB::raw(1))->from('StructBook')->whereColumn('StructBook.idBook', 'Book.idBook');
                })
                ->groupBy('Category.idCategory', 'Category.title')
                ->orderByDesc(DB::raw('COUNT(DISTINCT Book.idBook)'))
                ->limit(12)
                ->get([
                    'Category.idCategory as idCategory',
                    'Category.title as title',
                    DB::raw('COUNT(DISTINCT Book.idBook) as books_count'),
                ])
                ->map(fn ($c) => [
                    'idCategory'  => (int) $c->idCategory,
                    'title'       => $c->title,
                    'books_count' => (int) $c->books_count,
                ])
                ->values();

            return response()->json($authorData, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les informations de l\'auteur.'], 500);
        }
    }

    /**
     * Auteurs "similaires" à un auteur donné : auteurs ayant au moins un livre
     * dans une catégorie commune avec les livres de cet auteur, classés par
     * nombre de livres partageant une catégorie commune (pertinence).
     *
     * Remplace l'ancienne logique (AuthorSimular.php), qui renvoyait en réalité
     * les 10 auteurs au "level" le plus élevé - identique pour tout auteur
     * consulté, sans aucun rapport avec ses livres ou catégories réels.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function similar($idAuthor)
    {
        if (!ctype_digit((string) $idAuthor)) {
            return response()->json(['message' => 'Auteur non trouvé.'], 404);
        }
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }

            $categoryIds = DB::table('BookCategory')
                ->whereIn('idBook', DB::table('Book')->where('idAuthor', $idAuthor)->pluck('idBook'))
                ->pluck('idCategory')
                ->unique();

            if ($categoryIds->isEmpty()) {
                // Aucune catégorie connue pour les livres de cet auteur : pas de base
                // de comparaison pertinente -> liste vide plutôt que des suggestions
                // arbitraires (cf. audit mobile "Auteurs").
                return response()->json([], 200);
            }

            $bookIdsInSharedCategories = DB::table('BookCategory')
                ->whereIn('idCategory', $categoryIds)
                ->pluck('idBook')
                ->unique();

            // Un auteur par nombre de livres (distincts) partageant une catégorie
            // avec l'auteur consulté - c'est le signal de pertinence principal.
            $sharedCounts = DB::table('Book')
                ->select('idAuthor', DB::raw('COUNT(DISTINCT idBook) as shared_books_count'))
                ->whereIn('idBook', $bookIdsInSharedCategories)
                ->where('idAuthor', '!=', $idAuthor)
                ->whereNotNull('idAuthor')
                ->groupBy('idAuthor')
                ->orderByDesc('shared_books_count')
                ->limit(10)
                ->pluck('shared_books_count', 'idAuthor');

            if ($sharedCounts->isEmpty()) {
                return response()->json([], 200);
            }

            $booksCounts = DB::table('Book')
                ->select('idAuthor', DB::raw('COUNT(*) as cnt'))
                ->whereIn('idAuthor', $sharedCounts->keys())
                ->groupBy('idAuthor')
                ->pluck('cnt', 'idAuthor');

            $authors = Author::whereIn('idAuthor', $sharedCounts->keys())
                ->get(['idAuthor', 'name', 'firstName', 'profile', 'profession']);

            // Categories effectivement partagees (pour expliquer la recommandation :
            // « Sciences · Mathématiques »). Une seule requete groupee, pas de N+1.
            $sharedTitles = DB::table('Book')
                ->join('BookCategory', 'BookCategory.idBook', '=', 'Book.idBook')
                ->join('Category', 'Category.idCategory', '=', 'BookCategory.idCategory')
                ->whereIn('Book.idAuthor', $sharedCounts->keys())
                ->whereIn('BookCategory.idCategory', $categoryIds)
                ->groupBy('Book.idAuthor', 'Category.idCategory', 'Category.title')
                ->orderByDesc(DB::raw('COUNT(DISTINCT Book.idBook)'))
                ->get(['Book.idAuthor as idAuthor', 'Category.title as title'])
                ->groupBy('idAuthor')
                ->map(fn ($rows) => $rows->pluck('title')->filter()->take(3)->values());

            $result = $authors->map(function ($a) use ($sharedCounts, $booksCounts, $sharedTitles) {
                return [
                    'idAuthor'           => $a->idAuthor,
                    'name'               => $a->name,
                    'firstName'          => $a->firstName,
                    'profile'            => $a->profile,
                    'profession'         => $a->profession,
                    'books_count'        => $booksCounts[$a->idAuthor] ?? 0,
                    'shared_books_count' => $sharedCounts[$a->idAuthor] ?? 0,
                    'shared_categories'  => $sharedTitles[$a->idAuthor] ?? [],
                ];
            })->sortByDesc('shared_books_count')->values();

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du calcul des auteurs similaires pour l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les auteurs similaires.'], 500);
        }
    }

    /**
     * Crée un nouvel auteur.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'fullName'   => 'required|string|max:512',
            'profile'    => 'nullable|file|mimes:jpg,jpeg,png|max:8000', // 8 Mo
            'cover'      => 'nullable|file|mimes:jpg,jpeg,png,webp|max:5120', // bannière, 5 Mo
            'level'      => 'nullable|integer',
            'profession' => 'nullable|string|max:1000',
            'biography'  => 'nullable|string',
        ], [
            'fullName.required' => 'Le nom complet est obligatoire.',
            'profile.mimes'     => 'La photo de profil doit être au format JPG ou PNG.',
            'profile.max'       => 'La photo de profil ne peut dépasser 8 Mo.',
            'cover.mimes'       => 'La bannière doit être au format JPG, PNG ou WEBP.',
            'cover.max'         => 'La bannière ne peut dépasser 5 Mo.',
            'level.integer'     => 'Le niveau doit être un nombre entier.',
            'profession.max'    => 'La profession ne peut dépasser 1000 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $fullName = trim($request->input('fullName'));
            $parts = explode(' ', $fullName);
            $firstName = array_shift($parts);
            $name = implode(' ', $parts);

            $data = [
                'name'       => $name,
                'firstName'  => $firstName,
                'level'      => $request->filled('level') ? (int)$request->input('level') : 0,
                'profession' => $request->filled('profession') ? trim($request->input('profession')) : null,
                'biography'  => $request->filled('biography') ? trim($request->input('biography')) : null,
            ];

            // Gestion du fichier de profil
            if ($request->hasFile('profile')) {
                $file = $request->file('profile');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Le fichier de profil est invalide.'], 400);
                }

                $extension = $file->getClientOriginalExtension();
                $filename = 'author_' . Str::uuid()->toString() . '.' . $extension;

                // Stockage dans le disque 'private' (à adapter selon votre configuration)
                $stored = Storage::disk('private')->putFileAs('profils', $file, $filename);
                if ($stored === false) {
                    throw new Exception('Échec de l\'enregistrement du fichier');
                }

                $data['profile'] = $filename;
            } else {
                $data['profile'] = 'user.png';
            }

            if ($request->hasFile('cover')) {
                $data['cover'] = $this->storeCover($request->file('cover'));
            }

            $author = Author::create($data);

            DB::commit();
            return response()->json([
                'message' => 'Auteur créé avec succès.',
                'data'    => $author,
            ], 201);

        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la création d\'un auteur : ' . $qe->getMessage(), [
                'data' => $data ?? [],
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la création de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la création d\'un auteur : ' . $e->getMessage(), [
                'data' => $data ?? [],
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la création de l\'auteur.'], 500);
        }
    }

    /**
     * Met à jour un auteur existant.
     *
     * @param Request $request
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Impossible de localiser l\'auteur.'], 500);
        }

        $validator = Validator::make($request->all(), [
            'fullName'   => 'sometimes|required|string|max:512',
            'profile'    => 'nullable|file|mimes:jpg,jpeg,png|max:5120',
            'cover'      => 'nullable|file|mimes:jpg,jpeg,png,webp|max:5120',
            'remove_cover' => 'nullable|boolean',
            'level'      => 'nullable|integer',
            'profession' => 'nullable|string|max:1000',
            'biography'  => 'nullable|string',
        ], [
            'fullName.required' => 'Le nom complet est obligatoire.',
            'profile.mimes'     => 'La photo de profil doit être JPG ou PNG.',
            'profile.max'       => 'La photo de profil ne peut dépasser 5 Mo.',
            'cover.mimes'       => 'La bannière doit être au format JPG, PNG ou WEBP.',
            'cover.max'         => 'La bannière ne peut dépasser 5 Mo.',
            'level.integer'     => 'Le niveau doit être un nombre entier.',
            'profession.max'    => 'La profession ne peut dépasser 1000 caractères.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => 'Validation échouée.',
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $updatedFields = [];

            if ($request->filled('fullName')) {
                $fullName = trim($request->input('fullName'));
                $parts = explode(' ', $fullName);
                $firstName = array_shift($parts);
                $name = implode(' ', $parts);

                $updatedFields['name'] = $name;
                $updatedFields['firstName'] = $firstName;
            }
            if ($request->filled('level')) {
                $updatedFields['level'] = (int)$request->input('level');
            }
            if ($request->filled('profession')) {
                $updatedFields['profession'] = trim($request->input('profession'));
            }
            if ($request->filled('biography')) {
                $updatedFields['biography'] = trim($request->input('biography'));
            }

            // Gestion du nouveau fichier de profil
            if ($request->hasFile('profile')) {
                $file = $request->file('profile');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Le fichier de profil est invalide.'], 400);
                }

                // Suppression de l'ancien fichier s'il existe
                if ($author->profile && $author->profile !== 'user.png') {
                    $oldPath = 'profils/' . $author->profile;
                    if (Storage::disk('private')->exists($oldPath)) {
                        Storage::disk('private')->delete($oldPath);
                    }
                }

                $extension = $file->getClientOriginalExtension();
                $filename = 'author_' . Str::uuid()->toString() . '.' . $extension;
                $stored = Storage::disk('private')->putFileAs('profils', $file, $filename);
                if ($stored === false) {
                    throw new Exception('Échec de l\'enregistrement du nouveau fichier de profil');
                }

                $updatedFields['profile'] = $filename;
            }

            // Bannière : nouvelle image (l'ancienne est supprimée) ou retrait explicite.
            if ($request->hasFile('cover')) {
                $this->deleteCover($author->cover);
                $updatedFields['cover'] = $this->storeCover($request->file('cover'));
            } elseif ($request->boolean('remove_cover')) {
                $this->deleteCover($author->cover);
                $updatedFields['cover'] = null;
            }

            if (!empty($updatedFields)) {
                $author->update($updatedFields);
            }

            DB::commit();
            return response()->json([
                'message' => 'Auteur mis à jour avec succès.',
                'data'    => $author->fresh(),
            ], 200);

        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la mise à jour de l\'auteur ID ' . $idAuthor . ' : ' . $qe->getMessage(), [
                'updatedFields' => $updatedFields ?? [],
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la mise à jour de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'updatedFields' => $updatedFields ?? [],
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la mise à jour de l\'auteur.'], 500);
        }
    }

    /**
     * Supprime un auteur ainsi que son fichier de profil associé.
     *
     * @param int $idAuthor
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($idAuthor)
    {
        try {
            $author = Author::find($idAuthor);
            if (!$author) {
                return response()->json(['message' => 'Auteur non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Impossible de localiser l\'auteur.'], 500);
        }

        DB::beginTransaction();
        try {
            // Suppression du fichier de profil s'il existe
            if ($author->profile && $author->profile !== 'user.png') {
                $filePath = 'profils/' . $author->profile;
                if (Storage::disk('private')->exists($filePath)) {
                    Storage::disk('private')->delete($filePath);
                }
            }

            $this->deleteCover($author->cover);

            $author->delete();
            DB::commit();
            return response()->json(['message' => 'Auteur supprimé avec succès.'], 200);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur SQL lors de la suppression de l\'auteur ID ' . $idAuthor . ' : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données lors de la suppression de l\'auteur.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la suppression de l\'auteur ID ' . $idAuthor . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la suppression de l\'auteur.'], 500);
        }
    }

    /**
     * Enregistre la bannière d'un auteur dans le disque privé (author_covers/).
     * Nom aléatoire : jamais le nom d'origine (pas de collision ni de chemin injecté).
     */
    private function storeCover($file): string
    {
        if (!$file->isValid()) {
            throw new Exception('Le fichier de bannière est invalide.');
        }
        $filename = 'cover_' . Str::uuid()->toString() . '.' . strtolower($file->getClientOriginalExtension());
        if (Storage::disk('private')->putFileAs('author_covers', $file, $filename) === false) {
            throw new Exception('Échec de l\'enregistrement de la bannière');
        }
        return $filename;
    }

    private function deleteCover(?string $cover): void
    {
        if ($cover && Storage::disk('private')->exists('author_covers/' . $cover)) {
            Storage::disk('private')->delete('author_covers/' . $cover);
        }
    }
}
