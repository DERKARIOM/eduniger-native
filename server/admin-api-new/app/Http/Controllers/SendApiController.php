<?php

namespace App\Http\Controllers;

use Exception;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use App\Models\Book;
class SendApiController extends Controller
{
    //function pour retourner le type de notification et le message à envoyer à l'API externe
    public function TypeOfNotification($type)
    {
        switch ($type) {
            case 4:
                return (object)[
                    'title'  => 'Réservation approuvée',
                    'message' => 'Votre réservation a été approuvée pour le livre '
                ];
            case -4:
                return (object)[
                    'title' => 'Réservation refusée',
                    'message' => 'Votre réservation a été refusée pour le livre '
                ];
            case 5:
                return (object)[
                    'title' => 'Emprunt approuvé',
                    'message' => 'Votre emprunt a été approuvé pour le livre '
                ];
            case -5:
                return (object)[
                    'title' => 'Emprunt refusé',
                    'message' => 'Votre emprunt a été refusé pour le livre '
                ];
            default:
                Log::warning('Type de notification inconnu : ' . $type);
                return (object)['title' => '', 'message' => ''];
        }
    }
    public function sendApiRequest($idNumber,$idBook,$type)
    {
        //Chercher la couverture a partir de l'id du livre
        $book = Book::find($idBook);
        if (!$book) {
            Log::error('Livre non trouvé pour l\'ID : ' . $idBook);
            return;
        }
        
        // Préparer les données de formulaire
        $notification = $this->TypeOfNotification($type);
        $message = $notification->message . $book->title;
        $title = $notification->title;
        $formData = [
            'idNumber' => $idNumber,  // Identifiant de l'utilisateur
            'title'    => $title,  // Titre personnalisable
            'type'     => $type,  // Type de notification
            'message'  => $message , // Message descriptif
            'extraData' => $book->cover // Ajouter la couverture du livre
        ];
        Log::info($formData);

        try {
            // Envoyer la requête POST à l'API externe en form-data
            $response = Http::asForm()->post(config('services.legacy_api.url') . '/send_notification.php', $formData);

            // Vérifier la réponse (optionnel, pour le débogage)
            if ($response->successful()) {
                Log::info('Requête API envoyée avec succès', ['response' => $response->body()]);
            } else {
                Log::error('Échec de la requête API', ['status' => $response->status(), 'body' => $response->body()]);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'envoi de la requête API : ' . $e->getMessage());
        }
    }
    
}
