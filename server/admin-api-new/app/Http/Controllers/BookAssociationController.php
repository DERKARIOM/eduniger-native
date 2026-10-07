<?php

namespace App\Http\Controllers;

use App\Models\BookAssociation;
use App\Models\Category;
use App\Models\Author;
use Illuminate\Http\Request;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Support\Facades\Log;
use Exception;

class BookAssociationController extends Controller
{
    /**
     * Récupère la liste des livres avec leurs relations (catégories, auteur, structures, fichiers audio).
     * Filtre optionnellement par identifiant de structure.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function getBooksWithAssociations(Request $request)
    {
        try {
            $structureId = $request->input('structureId');
            $categoryId  = $request->input('categoryId');
            $authorId    = $request->input('authorId');
            // Parametre "search" (additif, optionnel) : audit recherche livres - aucun
            // endpoint backend ne permettait jusqu'ici de chercher un livre par titre
            // (seuls des filtres par identifiant structureId/categoryId/authorId
            // existaient). Absent -> comportement 100% inchange pour les appelants
            // existants (Web Library.tsx, mobile) qui continuent a filtrer cote client.
            $search      = $request->input('search', $request->input('q'));
            $page        = $request->input('page');
            $limit       = $request->input('limit', 20);

            // Validation des paramètres (optionnels)
            if ($structureId !== null && !is_numeric($structureId)) {
                return response()->json([
                    'message' => 'L\'identifiant de la structure doit être un nombre valide.'
                ], 422);
            }
            if ($categoryId !== null && !is_numeric($categoryId)) {
                return response()->json([
                    'message' => 'L\'identifiant de la catégorie doit être un nombre valide.'
                ], 422);
            }
            if ($authorId !== null && !is_numeric($authorId)) {
                return response()->json([
                    'message' => 'L\'identifiant de l\'auteur doit être un nombre valide.'
                ], 422);
            }
            if ($page !== null && (!is_numeric($page) || (int) $page < 1)) {
                return response()->json([
                    'message' => 'Le numéro de page doit être un entier positif.'
                ], 422);
            }
            if ($limit !== null && (!is_numeric($limit) || (int) $limit < 1 || (int) $limit > 100)) {
                return response()->json([
                    'message' => 'La limite doit être comprise entre 1 et 100.'
                ], 422);
            }
            // categoryId/authorId numériques mais inexistants : 404 explicite plutôt
            // qu'une liste vide indiscernable d'une catégorie/auteur réellement sans
            // livre (cf. audit mobile "Categories"/"Auteurs" - tests "ID invalide" vs
            // "sans livre").
            if ($categoryId !== null && !Category::find($categoryId)) {
                return response()->json([
                    'message' => 'Catégorie non trouvée.'
                ], 404);
            }
            if ($authorId !== null && !Author::find($authorId)) {
                return response()->json([
                    'message' => 'Auteur non trouvé.'
                ], 404);
            }

            $query = BookAssociation::with([
                'categories',
                'author',
                'structures',
                'audioFiles',
            ]);

            if ($structureId) {
                $query->whereHas('structures', function ($q) use ($structureId) {
                    $q->where('id', $structureId);
                });
            }
            if ($categoryId) {
                // Colonne qualifiee obligatoire : "Category" et le pivot "BookCategory"
                // ont chacune une colonne idCategory, donc un where('idCategory', ...)
                // non qualifie est ambigu pour MySQL des que la jointure du pivot entre
                // en jeu (SQLSTATE[23000] 1052 "Column 'idCategory' in where clause is
                // ambiguous" - constate en test reel, cf. audit categories mobile).
                $query->whereHas('categories', function ($q) use ($categoryId) {
                    $q->where('Category.idCategory', $categoryId);
                });
            }
            if ($authorId) {
                // Colonne directe sur Book (pas de relation many-to-many ici) : un
                // simple where, pas de whereHas.
                $query->where('idAuthor', $authorId);
            }
            if ($search !== null && trim((string) $search) !== '') {
                $searchTerm = trim((string) $search);
                $query->where(function ($q) use ($searchTerm) {
                    $q->where('title', 'like', '%' . $searchTerm . '%')
                      ->orWhereHas('author', function ($qa) use ($searchTerm) {
                          $qa->where('name', 'like', '%' . $searchTerm . '%')
                             ->orWhere('firstName', 'like', '%' . $searchTerm . '%');
                      });
                });
            }

            $query->orderByDesc('idBook');

            // Pagination "opt-in" (page/limit) : si absents, comportement inchangé
            // (tableau complet, retro-compatible avec BooksFragment/CategoryActivity/
            // StructureActivity qui n'envoient ni page ni limit). Si présents, on
            // charge une page de plus que demandé pour déterminer s'il reste des
            // résultats (X-Has-More), sans requête COUNT séparée.
            $hasMore = null;
            if ($page !== null) {
                $page  = (int) $page;
                $limit = (int) $limit;
                $rows  = $query->skip(($page - 1) * $limit)->take($limit + 1)->get();
                $hasMore = $rows->count() > $limit;
                $books = $rows->slice(0, $limit)->values();
            } else {
                $books = $query->get();
            }

            $books = $books->map(function ($book) {
                $bookArray = $book->toArray();
                $bookArray['audioFiles'] = $bookArray['audio_files'] ?? [];
                unset($bookArray['audio_files']);
                return $bookArray;
            })->values();

            $response = response()->json($books, 200);
            if ($hasMore !== null) {
                $response->header('X-Has-More', $hasMore ? 'true' : 'false');
            }
            return $response;
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des livres avec associations : ' . $e->getMessage(), [
                'structureId' => $structureId ?? null,
                'categoryId'  => $categoryId ?? null,
                'authorId'    => $authorId ?? null,
                'search'      => $search ?? null,
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Impossible de récupérer la liste des livres.'
            ], 500);
        }
    }

    /**
     * Récupère les détails d'un livre spécifique avec ses relations.
     *
     * @param int $idBook
     * @return \Illuminate\Http\JsonResponse
     */
    public function getBookDetails($idBook)
{
    try {
        $book = BookAssociation::with([
            'categories',
            'author',
            'structures',
            'audioFiles',
        ])->findOrFail($idBook);

        $bookArray = $book->toArray();

        // Transform the snake_case audio_files to camelCase audioFiles
        $bookArray['audioFiles'] = $bookArray['audio_files'] ?? [];
        unset($bookArray['audio_files']);

        return response()->json($bookArray, 200);
    } catch (ModelNotFoundException $e) {
        Log::warning('Livre non trouvé : ID ' . $idBook);
        return response()->json(['message' => 'Livre non trouvé.'], 404);
    } catch (Exception $e) {
        Log::error('Erreur lors de la récupération du livre ID ' . $idBook . ' : ' . $e->getMessage());
        return response()->json(['message' => 'Impossible de récupérer les détails du livre.'], 500);
    }
}
}