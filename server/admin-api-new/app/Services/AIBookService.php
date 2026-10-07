<?php

namespace App\Services;

use App\Models\Author;
use App\Models\Book;
use App\Models\Category;
use App\Models\StructBook;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use Smalot\PdfParser\Parser;
use getID3;
use Illuminate\Support\Facades\Log;

class AIBookService
{
    protected $apiKey;
    protected $apiVersion = 'v1';
    protected $currentModel = 'gemini-2.5-flash';

    public function __construct()
    {
        // Corrige lors de l'audit du deploiement : cette verification levait une
        // exception directement dans le constructeur, ce qui faisait planter
        // TOUTE instanciation de AIBookController (y compris `php artisan
        // route:list`, qui instancie les controleurs pour lister leurs
        // middlewares) des que GEMINI_API_KEY n'etait pas configuree, meme pour
        // une installation qui n'utilise pas du tout la generation IA. La
        // verification est deplacee dans callGemini(), au moment ou la cle est
        // reellement necessaire.
        $this->apiKey = env('GEMINI_API_KEY');
    }

    /**
     * Extrait les informations d'un livre.
     */
    public function extractBookInfo(?string $prompt, ?UploadedFile $pdf, ?UploadedFile $audio, ?UploadedFile $cover)
    {
        $context = '';

        if ($pdf) {
            $context .= $this->extractTextFromPdf($pdf);
        }

        if ($audio) {
            $audioMeta = $this->extractAudioMetadata($audio);
            $context .= "\nMétadonnées audio : " . json_encode($audioMeta, JSON_UNESCAPED_UNICODE);
        }

        // Récupérer les auteurs et catégories existants pour guider l'IA
        $existingAuthors = Author::select('name')->limit(50)->pluck('name')->toArray();
        $existingCategories = Category::select('title')->limit(50)->pluck('title')->toArray();

        $fullPrompt = $this->buildExtractionPrompt($prompt, $context, $existingAuthors, $existingCategories);
        $response = $this->callGemini($fullPrompt);

        if (!$response) {
            throw new \Exception('Erreur lors de l\'appel à l\'API Gemini');
        }

        $json = $this->cleanJson($response);
        $data = json_decode($json, true);

        if (json_last_error() !== JSON_ERROR_NONE) {
            Log::error('JSON decode error: ' . json_last_error_msg());
            throw new \Exception('La réponse de l\'IA n\'est pas un JSON valide');
        }

        // Vérifier les correspondances exactes avec les auteurs et catégories existants
        $data['author_suggestions'] = $this->suggestAuthor($data['author_name'] ?? null);
        $data['category_suggestions'] = $this->suggestCategories($data['categories'] ?? []);

        // Valeurs par défaut
        $data['title'] = $data['title'] ?? '';
        $data['author_name'] = $data['author_name'] ?? '';
        $data['description'] = $data['description'] ?? '';
        $data['categories'] = $data['categories'] ?? [];
        $data['is_physic'] = $data['is_physic'] ?? false;
        $data['is_audio'] = $data['is_audio'] ?? false;

        return $data;
    }

    /**
     * Crée le livre en base.
     */
    public function createBook($user, array $data, ?UploadedFile $cover, ?UploadedFile $pdfFile, ?UploadedFile $audioFile)
    {
        if (!$user->idStruct) {
            throw new \Exception('Utilisateur sans structure');
        }

        // Auteur
        $author = Author::firstOrCreate(
            ['name' => $data['author']],
            ['firstName' => '', 'biography' => '']
        );

        // Catégories
        $categoryIds = [];
        if (isset($data['categories']) && is_array($data['categories'])) {
            foreach ($data['categories'] as $catName) {
                if (!empty($catName)) {
                    $cat = Category::firstOrCreate(['title' => $catName]);
                    $categoryIds[] = $cat->idCategory;
                }
            }
        }

        $bookId = (string) Str::uuid();

        $coverPath = $cover ? $cover->store('covers', 'public') : null;
        $pdfPath = $pdfFile ? $pdfFile->store('books', 'public') : null;

        $book = Book::create([
            'idBook'        => $bookId,
            'title'         => $data['title'],
            'description'   => $data['description'] ?? '',
            'idAuthor'      => $author->idAuthor,
            'blanket'       => $coverPath,
            'electronic'    => $pdfPath,
            'isPhysic'      => $data['is_physic'] ?? false,
            'isAudio'       => $data['is_audio'] ?? false,
            'available'     => true,
            'size'          => 0,
            'nbrPage'       => $data['nbrPage'] ?? 0,
            'numberLike'    => 0,
            'numberNoLike'  => 0,
            'numberView'    => 0,
            'numberComment' => 0,
            'numberSubscribe' => 0
        ]);

        if (!empty($categoryIds)) {
            $book->categories()->sync($categoryIds);
        }

        StructBook::create([
            'idStruct' => $user->idStruct,
            'idBook'   => $bookId,
        ]);

        if ($audioFile && $data['is_audio']) {
            $audioPath = $audioFile->store('audios', 'public');
            $audioModel = new \App\Models\Audio();
            $audioModel->idAudio = (string) Str::uuid();
            $audioModel->idBook = $bookId;
            $audioModel->audio = $audioPath;
            $audioModel->title = $data['title'];
            $audioModel->size = $audioFile->getSize();
            $audioModel->maxtime = 0;
            $audioModel->save();
        }

        return $book;
    }

    private function callGemini(string $prompt): ?string
    {
        if (!$this->apiKey) {
            Log::error('Generation IA indisponible : GEMINI_API_KEY non configuree.');
            return null;
        }

        $modelsToTry = ['gemini-2.5-flash', 'gemini-2.5-pro', 'gemini-2.0-flash'];
        foreach ($modelsToTry as $model) {
            $url = "https://generativelanguage.googleapis.com/{$this->apiVersion}/models/{$model}:generateContent?key={$this->apiKey}";
            try {
                $response = Http::timeout(30)->post($url, [
                    'contents' => [['parts' => [['text' => $prompt]]]],
                    'generationConfig' => [
                        'temperature' => 0.2,
                        'maxOutputTokens' => 1024,
                        'topP' => 0.8,
                    ]
                ]);

                if ($response->successful()) {
                    $data = $response->json();
                    if (isset($data['candidates'][0]['content']['parts'][0]['text'])) {
                        Log::info("Model used: {$model}");
                        return $data['candidates'][0]['content']['parts'][0]['text'];
                    }
                }
            } catch (\Exception $e) {
                Log::warning("Model {$model} failed: " . $e->getMessage());
            }
        }
        return null;
    }

    private function extractTextFromPdf(UploadedFile $pdf): string
    {
        try {
            $parser = new Parser();
            $text = $parser->parseFile($pdf->getRealPath())->getText();
            return mb_substr($text, 0, 5000);
        } catch (\Exception $e) {
            Log::error('PDF extraction error: ' . $e->getMessage());
            return '';
        }
    }

    private function extractAudioMetadata(UploadedFile $audio): array
    {
        try {
            $getID3 = new getID3();
            $info = $getID3->analyze($audio->getRealPath());
            return [
                'title'    => $info['tags']['id3v2']['title'][0] ?? $info['tags']['id3v1']['title'][0] ?? null,
                'artist'   => $info['tags']['id3v2']['artist'][0] ?? $info['tags']['id3v1']['artist'][0] ?? null,
                'album'    => $info['tags']['id3v2']['album'][0] ?? $info['tags']['id3v1']['album'][0] ?? null,
                'duration' => $info['playtime_seconds'] ?? null,
            ];
        } catch (\Exception $e) {
            return [];
        }
    }

    private function buildExtractionPrompt(?string $prompt, string $context, array $existingAuthors, array $existingCategories): string
    {
        $authorsList = implode(', ', array_slice($existingAuthors, 0, 50));
        $categoriesList = implode(', ', array_slice($existingCategories, 0, 50));

        $instructions = <<<EOT
Tu es un assistant spécialisé dans l'extraction d'informations de livres pour une bibliothèque.

Voici les auteurs existants dans la base de données : {$authorsList}
Voici les catégories existantes : {$categoriesList}

À partir du prompt utilisateur et du contexte, retourne un objet JSON STRICT avec les champs suivants :
- title : titre du livre (string)
- author_name : nom de l'auteur (string) – **doit être choisi parmi les auteurs existants si possible**
- description : description ou résumé (string)
- categories : tableau de catégories (array of strings) – **doivent être choisis parmi les catégories existantes si possible**
- is_physic : boolean (livre physique ?)
- is_audio : boolean (livre audio ?)

RÈGLES IMPORTANTES :
- Utilise UNIQUEMENT les auteurs et catégories existants si une correspondance est pertinente.
- Si aucune correspondance exacte n'existe, laisse le nom tel quel (l'utilisateur pourra l'ajouter après).
- N'invente pas de données.
- Réponds UNIQUEMENT avec le JSON, sans texte supplémentaire.

Exemple :
{
    "title": "Le Petit Prince",
    "author_name": "Antoine de Saint-Exupéry",
    "description": "Un conte philosophique...",
    "categories": ["Roman", "Philosophique"],
    "is_physic": true,
    "is_audio": false
}
EOT;

        $userInput = "Prompt utilisateur : " . ($prompt ?? 'Aucun');
        $contextPart = "Contexte extrait : " . ($context ?: 'Aucun');

        return "$instructions\n\n$userInput\n\n$contextPart";
    }

    private function cleanJson(string $raw): string
    {
        $raw = preg_replace('/```json\s*|\s*```/', '', $raw);
        if (preg_match('/\{.*\}/s', $raw, $matches)) {
            return $matches[0];
        }
        return '{}';
    }

    private function suggestAuthor(?string $authorName): array
    {
        if (!$authorName) {
            return ['exists' => false, 'id' => null, 'name' => null];
        }
        $author = Author::where('name', 'LIKE', $authorName)->first();
        return $author
            ? ['exists' => true, 'id' => $author->idAuthor, 'name' => $author->name]
            : ['exists' => false, 'id' => null, 'name' => $authorName];
    }

    private function suggestCategories(array $categories): array
    {
        $suggestions = [];
        foreach ($categories as $catName) {
            $category = Category::where('title', 'LIKE', $catName)->first();
            $suggestions[] = [
                'name' => $catName,
                'exists' => (bool) $category,
                'id'    => $category->idCategory ?? null,
            ];
        }
        return $suggestions;
    }
} 