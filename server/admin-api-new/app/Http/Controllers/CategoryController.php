<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Category;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Carbon\Carbon;
use Illuminate\Support\Facades\Log;
use Exception;

class CategoryController extends Controller
{
    /**
     * Récupère la liste de toutes les catégories.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function index(Request $request)
    {
        try {
            $categories = Category::all();
            return response()->json($categories, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des catégories : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des catégories.'], 500);
        }
    }

    /**
     * Affiche les détails d'une catégorie spécifique.
     *
     * @param int $idCategory
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($idCategory)
    {
        try {
            $category = Category::find($idCategory);
            if (!$category) {
                return response()->json(['message' => 'Catégorie non trouvée.'], 404);
            }
            return response()->json($category, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage de la catégorie ID ' . $idCategory . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les détails de la catégorie.'], 500);
        }
    }

    /**
     * Crée une nouvelle catégorie.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'title'   => 'required|string|max:256',
            'blanket' => 'nullable|file|mimes:jpg,jpeg,png|max:5120',
        ], [
            'title.required' => 'Le titre est obligatoire.',
            'title.max'      => 'Le titre ne peut dépasser 256 caractères.',
            'blanket.mimes'  => 'L’image de la catégorie doit être au format JPG ou PNG.',
            'blanket.max'    => 'L’image de la catégorie ne peut pas dépasser 5 Mo.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $data = [
                'title'           => trim($request->input('title')),
                'date'            => Carbon::now(),
                'numberSubscribe' => 0,
            ];

            // Traitement de l'image
            if ($request->hasFile('blanket')) {
                $file = $request->file('blanket');
                if (!$file->isValid()) {
                    return response()->json([
                        'message' => 'Le fichier image est invalide.',
                    ], 400);
                }
                $extension = $file->getClientOriginalExtension();
                $filename = 'category_' . Str::uuid()->toString() . '.' . $extension;

                if (Storage::disk('private')->putFileAs('categories', $file, $filename) === false) {
                    throw new Exception('Échec de l’enregistrement de l’image de la catégorie.');
                }

                $data['blanket'] = $filename;
            } else {
                $data['blanket'] = null;
            }

            $category = Category::create($data);

            DB::commit();
            return response()->json([
                'message' => 'Catégorie créée avec succès.',
                'data'    => $category,
            ], 201);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la création de catégorie : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Erreur de base de données lors de la création de la catégorie.',
            ], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la création de catégorie : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Erreur inattendue lors de la création de la catégorie.',
            ], 500);
        }
    }

    /**
     * Met à jour une catégorie existante.
     *
     * @param Request $request
     * @param int $idCategory
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $idCategory)
    {
        try {
            $category = Category::find($idCategory);
            if (!$category) {
                return response()->json(['message' => 'Catégorie non trouvée.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de la catégorie ID ' . $idCategory . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Catégorie non trouvée.'], 404);
        }

        $validator = Validator::make($request->all(), [
            'title'   => 'sometimes|required|string|max:256',
            'blanket' => 'nullable|file|mimes:jpg,jpeg,png|max:5120',
        ], [
            'title.required' => 'Le titre est obligatoire si fourni.',
            'title.max'      => 'Le titre ne peut dépasser 256 caractères.',
            'blanket.mimes'  => 'L’image de la catégorie doit être au format JPG ou PNG.',
            'blanket.max'    => 'L’image de la catégorie ne peut pas dépasser 5 Mo.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $updatedFields = [];

            if ($request->filled('title')) {
                $updatedFields['title'] = trim($request->input('title'));
            }

            // Traitement de la nouvelle image
            if ($request->hasFile('blanket')) {
                $file = $request->file('blanket');
                if (!$file->isValid()) {
                    return response()->json([
                        'message' => 'Le fichier image est invalide.',
                    ], 400);
                }

                // Suppression de l'ancienne image si elle existe
                if ($category->blanket) {
                    Storage::disk('private')->delete('categories/' . $category->blanket);
                }

                $extension = $file->getClientOriginalExtension();
                $filename = 'category_' . Str::uuid()->toString() . '.' . $extension;

                if (Storage::disk('private')->putFileAs('categories', $file, $filename) === false) {
                    throw new Exception('Échec de l’enregistrement de la nouvelle image de catégorie.');
                }

                $updatedFields['blanket'] = $filename;
            }

            if (!empty($updatedFields)) {
                $category->update($updatedFields);
            }

            DB::commit();
            return response()->json([
                'message' => 'Catégorie mise à jour avec succès.',
                'data'    => $category->fresh(),
            ], 200);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la mise à jour de catégorie ID ' . $idCategory . ' : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Erreur de base de données lors de la mise à jour de la catégorie.',
            ], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour de catégorie ID ' . $idCategory . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Erreur inattendue lors de la mise à jour de la catégorie.',
            ], 500);
        }
    }

    /**
     * Supprime une catégorie.
     *
     * @param int $idCategory
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($idCategory)
    {
        try {
            $category = Category::find($idCategory);
            if (!$category) {
                return response()->json(['message' => 'Catégorie non trouvée.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche de la catégorie ID ' . $idCategory . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Catégorie non trouvée.'], 404);
        }

        DB::beginTransaction();
        try {
            // Suppression de l'image associée
            if ($category->blanket) {
                Storage::disk('private')->delete('categories/' . $category->blanket);
            }

            $category->delete();
            DB::commit();

            return response()->json(['message' => 'Catégorie supprimée avec succès.'], 200);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la suppression de la catégorie ID ' . $idCategory . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json([
                'message' => 'Erreur lors de la suppression de la catégorie.',
            ], 500);
        }
    }
}