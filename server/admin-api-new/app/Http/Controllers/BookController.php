<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Book;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use App\Models\StructBook;
use Carbon\Carbon;
use App\Models\Structure;
use App\Models\Audio;
use App\Models\BookCategory;
use App\Models\StructUser;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Log;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Exception;

class BookController extends Controller
{
    /**
     * Livres "Recommandes" de l'utilisateur authentifie : livres presents dans
     * les structures auxquelles il adhere (table StructUser), toutes structures
     * confondues, sans doublon meme si un livre est present dans plusieurs de
     * ses structures. Remplace l'ancien recommended.php (backend JWT legacy,
     * incompatible avec Sanctum), qui n'etait donc plus jamais atteint depuis
     * la bascule de l'app mobile vers Sanctum.
     *
     * Un seul appel, calcule entierement cote serveur a partir de
     * l'utilisateur du token (jamais d'idUser/idStruct fournis par le client) :
     * pas de N+1 depuis le mobile (une requete par structure), conformement a
     * l'architecture attendue.
     *
     * Tri : par date d'association livre/structure la plus recente
     * (StructBook.date), donc les ajouts les plus recents dans les structures
     * de l'utilisateur remontent en premier - meme logique que l'ancien
     * recommended.php.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function recommendations(Request $request)
    {
        try {
            $idUser = $request->user()->idUser;

            $limit = (int) $request->query('limit', 6);
            if ($limit < 1) {
                $limit = 6;
            } elseif ($limit > 30) {
                $limit = 30;
            }

            $books = DB::table('Book')
                ->join('StructBook', 'Book.idBook', '=', 'StructBook.idBook')
                ->join('Structure', 'StructBook.idStruct', '=', 'Structure.id')
                ->join('StructUser', 'StructBook.idStruct', '=', 'StructUser.idStruct')
                ->leftJoin('BookCategory', 'Book.idBook', '=', 'BookCategory.idBook')
                ->leftJoin('Category', 'BookCategory.idCategory', '=', 'Category.idCategory')
                ->where('StructUser.idUser', $idUser)
                ->selectRaw("
                    Book.idBook,
                    Book.blanket,
                    Book.title as bookTitle,
                    Book.isPhysic,
                    Book.electronic,
                    Book.isAudio,
                    Book.numberLike,
                    Book.numberView,
                    COALESCE(GROUP_CONCAT(DISTINCT Category.title SEPARATOR ', '), '') as categoryTitle,
                    GROUP_CONCAT(DISTINCT Structure.id SEPARATOR ', ') as idStruct,
                    GROUP_CONCAT(DISTINCT Structure.nameStruct SEPARATOR ', ') as nameStruct,
                    MAX(StructBook.date) as lastDate
                ")
                ->groupBy('Book.idBook')
                ->orderByDesc('lastDate')
                ->limit($limit)
                ->get();

            return response()->json($books);
        } catch (Exception $e) {
            Log::error('Erreur lors de la recuperation des recommandations : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les recommandations.'], 500);
        }
    }

    /**
     * Récupère tous les livres appartenant à une structure spécifique.
     *
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function index($idStruct)
    {
        try {
            $books = Book::whereHas('structures', function ($query) use ($idStruct) {
                $query->where('idStruct', $idStruct);
            })->get();

            return response()->json($books, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des livres pour la structure ID ' . $idStruct . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer la liste des livres.'], 500);
        }
    }

    /**
     * Affiche les détails d'un livre spécifique.
     *
     * @param string $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function show(int $idStruct, $id)
    {
        // Bug trouve lors des tests API (phase 9 de l'audit) : cette methode ne
        // declarait qu'un seul parametre ($id) alors que la route est
        // /books/{idStruct}/{id} (deux segments). Laravel liait donc le premier
        // parametre de route (idStruct) a $id au lieu du second (l'identifiant
        // reel du livre) - la recherche Book::findOrFail($id) portait sur la
        // mauvaise valeur et echouait systematiquement (404 "Livre non trouve"
        // meme pour un livre qui existe reellement, confirme par update() qui,
        // lui, declare bien les deux parametres et fonctionne correctement).
        try {
            $book = Book::findOrFail($id);
            return response()->json($book, 200);
        } catch (ModelNotFoundException $e) {
            return response()->json(['message' => 'Livre non trouvé.'], 404);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'affichage du livre ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les détails du livre.'], 500);
        }
    }

    /**
     * Supprime un livre, ses fichiers associés et ses dépendances.
     *
     * @param int $idStruct
     * @param string $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy(int $idStruct, string $id)
    {
        DB::beginTransaction();
        try {
            $book = Book::findOrFail($id);

            // Supprimer la couverture
            if ($book->blanket !== 'default.png') {
                Storage::disk('private')->delete('structures/' . $idStruct . '/blankets/' . $book->blanket);
            }
            // Supprimer le PDF
            if ($book->electronic) {
                Storage::disk('private')->delete('structures/' . $idStruct . '/pdfs/' . $book->electronic);
            }

            // Supprimer les fichiers audio associés
            $audios = Audio::where('idBook', $id)->get();
            foreach ($audios as $audio) {
                Storage::disk('private')->delete('structures/' . $idStruct . '/audios/' . $audio->audio);
                $audio->delete();
            }

            BookCategory::where('idBook', $id)->delete();
            StructBook::where('idBook', $id)->delete();

            $book->delete();
            DB::commit();

            return response()->json(['message' => 'Livre supprimé avec succès'], 200);
        } catch (ModelNotFoundException $e) {
            DB::rollBack();
            return response()->json(['message' => 'Livre non trouvé.'], 404);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur suppression livre ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Échec de la suppression du livre.'], 500);
        }
    }

    /**
     * Récupère une structure avec la liste de ses livres.
     *
     * @param int $id
     * @return \Illuminate\Http\JsonResponse
     */
    public function getBooksByStructure($id)
    {
        try {
            $structure = Structure::with('books')->findOrFail($id);

            return response()->json([
                'structure' => $structure->nameStruct,
                'books' => $structure->books,
            ], 200);
        } catch (ModelNotFoundException $e) {
            return response()->json(['message' => 'Structure non trouvée.'], 404);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des livres pour la structure ID ' . $id . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de récupérer les livres de cette structure.'], 500);
        }
    }

    /**
     * Crée un nouveau livre et l'associe à une structure.
     *
     * @param Request $request
     * @param int $idStruct
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request, int $idStruct)
    {
        $validator = Validator::make($request->all(), [
            'idBook'        => 'nullable|string|max:36',
            'idStruct'      => 'required|integer|exists:Structure,id',
            'title'         => 'required|string|max:1000',
            'description'   => 'nullable|string',
            'idAuthor'      => 'required|integer|exists:Author,idAuthor',
            'blanket'       => 'nullable|file|mimes:jpg,jpeg,png|max:5120',
            'electronic'    => 'nullable|file|mimes:pdf',
            'isPhysic'      => 'required|boolean',
            'isAudio'       => 'required|boolean',
            'available'     => 'required|integer|min:1',
            'nbrPage'       => 'required|string',
            'categories'    => 'required|array|min:1',
            'categories.*'  => 'integer|exists:Category,idCategory',
            'audioFiles'    => 'nullable|array',
            'audioFiles.*.file'     => 'required_with:audioFiles|file|mimes:mp3,wav',
            'audioFiles.*.title'    => 'required_with:audioFiles|string|max:1000',
            'audioFiles.*.size'     => 'nullable|string',
            'audioFiles.*.maxTime'  => 'nullable|string',
        ], [
            'idBook.unique'         => 'Cet identifiant de livre existe déjà.',
            'available.min' => 'La disponibilité doit être au moins 1.',
            'title.required'       => 'Le titre est obligatoire.',
            'title.max'             => 'Le titre ne peut dépasser 256 caractères.',
            'idAuthor.required'    => 'L\'identifiant de l\'auteur est requis.',
            'idAuthor.exists'      => 'L\'auteur spécifié n\'existe pas.',
            'blanket.mimes'         => 'La couverture doit être un fichier JPG ou PNG.',
            'blanket.max'           => 'La couverture ne peut dépasser 5 Mo.',
            'electronic.mimes'      => 'Le fichier électronique doit être un PDF.',
            'electronic.max'        => 'Le PDF ne peut dépasser 20 Mo.',
            'isPhysic.required'     => 'Le champ format physique est requis.',
            'isAudio.required'      => 'Le champ format audio est requis.',
            'categories.required'   => 'Au moins une catégorie doit être sélectionnée.',
            'categories.*.exists'   => 'Une des catégories sélectionnées n\'existe pas.',
            'audioFiles.*.file.required_with' => 'Le fichier audio est requis lorsqu\'un audio est fourni.',
            'audioFiles.*.file.mimes' => 'Les fichiers audio doivent être au format MP3 ou WAV.',
            'audioFiles.*.file.max' => 'Chaque fichier audio ne peut dépasser 50 Mo.',
            'audioFiles.*.title.required_with' => 'Le titre de l\'audio est requis.',
        ]);

        if ($validator->fails()) {
            $firstError = $validator->errors()->first();
            return response()->json([
                'message' => $firstError,
                'errors'  => $validator->errors(),
            ], 422);
        }

        // Vérification de l'espace de stockage disponible
        try {
            $storageController = new StorageController();
            $storageResponse = $storageController->usage($idStruct);
            $storageData = json_decode($storageResponse->getContent(), true);
            $newFilesSize = 0;
            if ($request->hasFile('electronic')) {
                $newFilesSize += $request->file('electronic')->getSize();
            }
            if ($request->has('audioFiles')) {
                foreach ($request->input('audioFiles') as $index => $audioData) {
                    if ($request->hasFile("audioFiles.$index.file")) {
                        $audioFile = $request->file("audioFiles.$index.file");
                        $newFilesSize += $audioFile->getSize();
                    }
                }
            }
            if ($storageData['used'] + $newFilesSize > $storageData['storage_limit']) {
                return response()->json([
                    'message' => 'Espace de stockage insuffisant pour ce livre.'
                ], 400);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la vérification du stockage : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de vérifier l\'espace de stockage.'], 500);
        }

        DB::beginTransaction();

        try {
            $idBook = trim($request->input('idBook'));
            $title = trim($request->input('title'));
            $idAuthor = $request->input('idAuthor');
            $idStruct = $request->input('idStruct');
            $isPhysic = (bool)$request->input('isPhysic');
            $available = (int)$request->input('available');

            // Vérifier si le livre existe déjà (titre + auteur)
            $existingBook = Book::where('title', $title)
                                ->where('idAuthor', $idAuthor)
                                ->first();

            if ($existingBook) {
                $bookInStructure = StructBook::where('idBook', $existingBook->idBook)
                                            ->where('idStruct', $idStruct)
                                            ->exists();

                if ($bookInStructure) {
                    return response()->json([
                        'message' => 'Ce livre est déjà présent dans cette structure.'
                    ], 409);
                }

                StructBook::create([
                    'idStruct' => $idStruct,
                    'idBook'   => $existingBook->idBook,
                    'date'     => Carbon::now(),
                ]);

                if ($isPhysic) {
                    $existingBook->increment('available');
                }

                DB::commit();
                return response()->json([
                    'message' => 'Livre existant ajouté à la structure avec succès.',
                    'data'    => ['idBook' => $existingBook->idBook]
                ], 201);
            }

            // Générer un nouvel ID si nécessaire
            if (empty($idBook)) {
                $idBook = (string) Str::uuid();
            }

            // Traitement de la couverture
            $blanketName = 'default.png';
            if ($request->hasFile('blanket')) {
                $file = $request->file('blanket');
                $extension = $file->getClientOriginalExtension();
                $blanketName = $idBook . '_' . time() . '.' . $extension;
                if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/blankets', $file, $blanketName) === false) {
                    throw new Exception('Échec de l\'enregistrement de la couverture.');
                }
            }

            // Traitement du PDF
            $electronicName = null;
            $sizePdfMo = null;
            if ($request->hasFile('electronic')) {
                $file = $request->file('electronic');
                $sizePdf = $file->getSize();
                $sizePdfMo = round($sizePdf / (1024 * 1024), 2);
                $extension = $file->getClientOriginalExtension();
                $electronicName = $idBook . '_' . time() . '.' . $extension;
                if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/pdfs', $file, $electronicName) === false) {
                    throw new Exception('Échec de l\'enregistrement du fichier PDF.');
                }
            }

            // Création du livre
            $bookData = [
                'idBook'       => $idBook,
                'title'        => $title,
                'description'  => $request->filled('description') ? trim($request->input('description')) : null,
                'idAuthor'     => $idAuthor,
                'blanket'      => $blanketName,
                'electronic'   => $electronicName,
                'nbrPage'      => $request->input('nbrPage'),
                'size'         => $sizePdfMo,
                'isPhysic'     => $isPhysic,
                'isAudio'      => (bool)$request->input('isAudio'),
                'available'    => $isPhysic ? $available : 1,
            ];

            $book = Book::create($bookData);

            // Lier les catégories
            foreach ($request->input('categories', []) as $catId) {
                BookCategory::create([
                    'idBook'     => $idBook,
                    'idCategory' => $catId,
                    'date'       => Carbon::now(),
                ]);
            }

            // Traitement des fichiers audio
            if ($request->has('audioFiles')) {
                foreach ($request->input('audioFiles') as $index => $audioData) {
                    if ($request->hasFile("audioFiles.$index.file")) {
                        $audioFile = $request->file("audioFiles.$index.file");
                        $extension = $audioFile->getClientOriginalExtension();
                        $filename = $idBook . '_' . Str::uuid() . '.' . $extension;
                        if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/audios', $audioFile, $filename) === false) {
                            throw new Exception("Échec de l'enregistrement audio");
                        }
                        Audio::create([
                            'idBook'  => $idBook,
                            'audio'   => $filename,
                            'title'   => trim($audioData['title']),
                            'size'    => $audioData['size'] ?? null,
                            'maxTime' => $audioData['maxTime'] ?? null,
                        ]);
                    }
                }
            }

            // Lier le livre à la structure
            StructBook::create([
                'idStruct' => $idStruct,
                'idBook'   => $idBook,
                'date'     => Carbon::now(),
            ]);

            DB::commit();

            // Vérification post-transaction (optionnelle)
            if ($request->hasFile('blanket') && !Storage::disk('private')->exists('structures/' . $idStruct . '/blankets/' . $blanketName)) {
                Log::warning("Couverture non trouvée après transfert : {$blanketName}");
            }
            if ($request->hasFile('electronic') && !Storage::disk('private')->exists('structures/' . $idStruct . '/pdfs/' . $electronicName)) {
                Log::warning("PDF non trouvé après transfert : {$electronicName}");
            }
            if ($request->has('audioFiles')) {
                foreach ($request->input('audioFiles') as $index => $audioData) {
                    if (isset($audioData['title'])) {
                        $expectedAudio = Audio::where('idBook', $idBook)
                            ->where('title', trim($audioData['title']))
                            ->first();
                        if ($expectedAudio) {
                            $audioPath = 'structures/' . $idStruct . '/audios/' . $expectedAudio->audio;
                            if (!Storage::disk('private')->exists($audioPath)) {
                                Log::warning("Audio non trouvé après transfert : {$audioPath}");
                            }
                        }
                    }
                }
            }

            usleep(500000); // 0.5 seconde

            return response()->json([
                'message' => 'Livre créé et lié à la structure avec succès.',
                'data'    => ['idBook' => $idBook]
            ], 201);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la création du livre : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la création du livre : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur lors de la création du livre.'], 500);
        }
    }

    /**
     * Met à jour un livre existant.
     *
     * @param Request $request
     * @param int $idStruct
     * @param string $idBook
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, int $idStruct, $idBook)
    {
        try {
            $book = Book::find($idBook);
            if (!$book) {
                return response()->json(['message' => 'Livre non trouvé.'], 404);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la recherche du livre ID ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Livre non trouvé.'], 404);
        }

        $validator = Validator::make($request->all(), [
            'title'         => 'sometimes|required|string|max:1000',
            'description'   => 'nullable|string',
            'idAuthor'      => 'sometimes|required|integer|exists:Author,idAuthor',
            'blanket'       => 'nullable|file|mimes:jpg,jpeg,png',
            'electronic'    => 'nullable|file|mimes:pdf',
            'isPhysic'      => 'sometimes|required|boolean',
            'isAudio'       => 'sometimes|required|boolean',
            'nbrPage'       => 'sometimes|required|string',
            'available'     => 'sometimes|integer|min:0',
            'categories'    => 'sometimes|required|array|min:1',
            'categories.*'  => 'integer|exists:Category,idCategory',
            'removeAudioIds'=> 'nullable|array',
            'removeAudioIds.*' => 'integer|exists:Audio,idAudio',
            'audioFiles'    => 'nullable|array',
            'audioFiles.*.file'     => 'required_with:audioFiles|file|mimes:mp3,wav,mpeg',
            'audioFiles.*.title'    => 'required_with:audioFiles|string',
            'audioFiles.*.size'     => 'nullable|string|max:100',
            'audioFiles.*.maxTime'  => 'nullable|string|max:100',
        ], [
            'title.required'           => 'Le titre est obligatoire si fourni.',
            'title.max'                => 'Le titre ne peut dépasser 256 caractères.',
            'idAuthor.exists'          => 'L\'auteur spécifié n\'existe pas.',
            'blanket.mimes'            => 'La couverture doit être un fichier JPG ou PNG.',
            'electronic.mimes'         => 'Le fichier électronique doit être un PDF.',
            'electronic.max'           => 'Le PDF est trop lourd.',
            'available.min'            => 'La disponibilité ne peut être négative.',
            'isPhysic.required'        => 'Le champ format physique est requis si fourni.',
            'isAudio.required'         => 'Le champ format audio est requis si fourni.',
            'categories.*.exists'      => 'Une des catégories sélectionnées n\'existe pas.',
            'removeAudioIds.*.exists'  => 'Un des IDs audio à supprimer est invalide.',
            'audioFiles.*.file.mimes'  => 'Les fichiers audio doivent être au format MP3 ou WAV.',
            'audioFiles.*.file.max'    => 'Chaque fichier audio ne peut dépasser 50 Mo.',
            'audioFiles.*.title.required_with' => 'Le titre de l’audio est requis.',
        ]);

        if ($validator->fails()) {
            $firstError = $validator->errors()->first();
            return response()->json([
                'message' => $firstError,
                'errors'  => $validator->errors(),
            ], 422);
        }
 
        // Vérification de l'espace de stockage disponible
        try {
            $storageController = new StorageController();
            $storageResponse = $storageController->usage($idStruct);
            $storageData = json_decode($storageResponse->getContent(), true);
            $newFilesSize = 0;
            if ($request->hasFile('electronic')) {
                $newFilesSize += $request->file('electronic')->getSize();
            }
            if ($request->has('audioFiles')) {
                foreach ($request->input('audioFiles') as $index => $audioData) {
                    if ($request->hasFile("audioFiles.$index.file")) {
                        $audioFile = $request->file("audioFiles.$index.file");
                        $newFilesSize += $audioFile->getSize();
                    }
                }
            }
            if ($storageData['used'] + $newFilesSize > $storageData['storage_limit']) {
                return response()->json([
                    'message' => 'Espace de stockage insuffisant pour ces modifications.'
                ], 400);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de la vérification du stockage : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Impossible de vérifier l\'espace de stockage.'], 500);
        }

        DB::beginTransaction();

        try {
            $updatedFields = [];
            $finalIsPhysic = $book->isPhysic;

            if ($request->has('isPhysic')) {
                $finalIsPhysic = (bool)$request->input('isPhysic');
                $updatedFields['isPhysic'] = $finalIsPhysic;
            }

            if ($finalIsPhysic) {
                if ($request->filled('available')) {
                    $available = (int)$request->input('available');
                    if ($available < 0) {
                        return response()->json(['message' => 'La disponibilité ne peut être négative.'], 422);
                    }
                    $updatedFields['available'] = $available;
                }
            } else {
                $updatedFields['available'] = 1;
            }

            if ($request->filled('title')) {
                $updatedFields['title'] = trim($request->input('title'));
            }
            if ($request->filled('description')) {
                $updatedFields['description'] = trim($request->input('description'));
            }
            if ($request->filled('idAuthor')) {
                $updatedFields['idAuthor'] = $request->input('idAuthor');
            }
            if ($request->filled('nbrPage')) {
                $updatedFields['nbrPage'] = $request->input('nbrPage');
            }

            // Couverture
            if ($request->hasFile('blanket')) {
                $file = $request->file('blanket');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Fichier couverture invalide.'], 400);
                }
                $oldBlanket = $book->blanket;
                if ($oldBlanket && $oldBlanket !== 'default.png') {
                    Storage::disk('private')->delete('structures/' . $idStruct . '/blankets/' . $oldBlanket);
                }
                $extension = $file->guessExtension();
                $blanketName = $idBook . '_' . time() . '.' . $extension;
                if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/blankets', $file, $blanketName) === false) {
                    throw new Exception('Échec de l\'enregistrement de la couverture.');
                }
                $updatedFields['blanket'] = $blanketName;
            }

            // PDF
            if ($request->hasFile('electronic')) {
                $file = $request->file('electronic');
                if (!$file->isValid()) {
                    return response()->json(['message' => 'Fichier PDF invalide.'], 400);
                }
                $oldElectronic = $book->electronic;
                if ($oldElectronic) {
                    Storage::disk('private')->delete('structures/' . $idStruct . '/pdfs/' . $oldElectronic);
                }
                $extension = $file->getClientOriginalExtension();
                $electronicName = $idBook . '_' . time() . '.' . $extension;
                if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/pdfs', $file, $electronicName) === false) {
                    throw new Exception('Échec de l\'enregistrement du fichier PDF.');
                }
                $updatedFields['electronic'] = $electronicName;
                $sizePdf = $file->getSize();
                $updatedFields['size'] = round($sizePdf / (1024 * 1024), 2);
            }

            // Mise à jour du livre
            if (!empty($updatedFields)) {
                $book->update($updatedFields);
            }

            // Catégories
            if ($request->has('categories')) {
                BookCategory::where('idBook', $idBook)->delete();
                foreach ($request->input('categories', []) as $catId) {
                    BookCategory::create([
                        'idBook'     => $idBook,
                        'idCategory' => $catId,
                        'date'       => Carbon::now(),
                    ]);
                }
            }

            // Gestion des audios
            if ($request->has('isAudio') && !$request->input('isAudio')) {
                $audios = Audio::where('idBook', $idBook)->get();
                foreach ($audios as $audio) {
                    Storage::disk('private')->delete('structures/' . $idStruct . '/audios/' . $audio->audio);
                    $audio->delete();
                }
            } else {
                if ($request->has('removeAudioIds')) {
                    foreach ($request->input('removeAudioIds') as $audioId) {
                        $audio = Audio::find($audioId);
                        if ($audio && $audio->idBook === $idBook) {
                            Storage::disk('private')->delete('structures/' . $idStruct . '/audios/' . $audio->audio);
                            $audio->delete();
                        }
                    }
                }
                if ($request->has('audioFiles')) {
                    foreach ($request->input('audioFiles') as $index => $audioData) {
                        if ($request->hasFile("audioFiles.$index.file")) {
                            $audioFile = $request->file("audioFiles.$index.file");
                            $extension = $audioFile->getClientOriginalExtension();
                            $filename = $idBook . '_' . Str::uuid() . '.' . $extension;
                            if (Storage::disk('private')->putFileAs('structures/' . $idStruct . '/audios', $audioFile, $filename) === false) {
                                throw new Exception("Échec de l'enregistrement audio");
                            }
                            Audio::create([
                                'idBook'  => $idBook,
                                'audio'   => $filename,
                                'title'   => trim($audioData['title']),
                                'size'    => $audioData['size'] ?? null,
                                'maxTime' => $audioData['maxTime'] ?? null,
                            ]);
                        }
                    }
                }
            }

            DB::commit();

            return response()->json([
                'message' => 'Livre mis à jour avec succès.',
                'data'    => [
                    'idBook' => $book->idBook,
                    'title'  => $book->title,
                ]
            ], 200);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la mise à jour du livre ID ' . $idBook . ' : ' . $qe->getMessage(), [
                'trace' => $qe->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur de base de données.'], 500);
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour du livre ID ' . $idBook . ' : ' . $e->getMessage(), [
                'trace' => $e->getTraceAsString()
            ]);
            return response()->json(['message' => 'Erreur lors de la mise à jour du livre.'], 500);
        }
    }
}