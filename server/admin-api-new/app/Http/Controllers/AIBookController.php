<?php

namespace App\Http\Controllers;

use App\Services\AIBookService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class AIBookController extends Controller
{
    protected $aiBookService;

    public function __construct(AIBookService $aiBookService)
    {
        $this->aiBookService = $aiBookService;
    }

    public function extract(Request $request)
{
    try {
        $request->validate([
            'prompt' => 'nullable|string|max:2000',
            'pdf'    => 'nullable|file|mimes:pdf|max:102400', // 100MB
            'audio'  => 'nullable|file|mimes:mp3,wav,m4a|max:20480', // 20MB
            'cover'  => 'nullable|image|max:5120',
        ]);

        $result = $this->aiBookService->extractBookInfo(
            $request->input('prompt'),
            $request->file('pdf'),
            $request->file('audio'),
            $request->file('cover')
        );

        return response()->json($result);
        
    } catch (\Exception $e) {
        Log::error('Extraction error: ' . $e->getMessage());
        return response()->json([
            'message' => 'Erreur lors de l\'extraction des informations',
            'error' => $e->getMessage()
        ], 500);
    }
}

    public function store(Request $request)
    {
        Log::info('Début de la requête de création de livre', ['user_id' => $request->user() ? $request->user()->id : null]);

        try {
            $request->validate([
                'title'       => 'required|string|max:255',
                'author'      => 'required|string|max:255',
                'description' => 'nullable|string',
                'categories'  => 'array',
                'categories.*'=> 'string|max:100',
                'is_physic'   => 'boolean',
                'is_audio'    => 'boolean',
                'cover'       => 'nullable|image|max:5120',
                'pdf_file'    => 'nullable|file|mimes:pdf|max:20480',
                'audio_file'  => 'nullable|file|mimes:mp3,wav,m4a|max:20480',
            ]);
            Log::info('Validation de la requête de création réussie');
        } catch (\Illuminate\Validation\ValidationException $e) {
            Log::error('Erreur de validation dans store', ['errors' => $e->errors()]);
            return response()->json(['error' => 'Données invalides', 'details' => $e->errors()], 422);
        } catch (\Exception $e) {
            Log::error('Erreur inattendue lors de la validation dans store', ['error' => $e->getMessage()]);
            return response()->json(['error' => 'Erreur interne'], 500);
        }

        try {
            $book = $this->aiBookService->createBook(
                $request->user(),
                $request->only(['title', 'author', 'description', 'categories', 'is_physic', 'is_audio']),
                $request->file('cover'),
                $request->file('pdf_file'),
                $request->file('audio_file')
            );
            Log::info('Création du livre terminée avec succès', ['book_id' => $book->idBook]);
            return response()->json(['message' => 'Livre ajouté avec succès', 'book' => $book]);
        } catch (\Exception $e) {
            Log::error('Erreur lors de la création du livre', ['error' => $e->getMessage()]);
            return response()->json(['error' => 'Erreur lors de la création du livre'], 500);
        }
    }
}