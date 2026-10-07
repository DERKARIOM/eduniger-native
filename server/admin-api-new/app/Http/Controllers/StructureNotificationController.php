<?php

namespace App\Http\Controllers;

use App\Models\StructureNotification;
use App\Models\UserNotificationRead;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class StructureNotificationController extends Controller
{
    /**
     * Récupère les notifications pour l'utilisateur connecté.
     */
    public function index()
    {
        $user = Auth::user(); // vient de struct_agent
        $structureId = $user->idStruct;

        // Notifications destinées à sa structure + celles pour toutes
        $notifications = StructureNotification::where(function ($query) use ($structureId) {
            $query->where('idStruct', $structureId)
                  ->orWhereNull('idStruct');
        })->orderBy('created_at', 'desc')->get();

        // Ajouter le statut de lecture
        $notifications->each(function ($notification) use ($user) {
            $read = UserNotificationRead::where('user_id', $user->id)
                ->where('notification_id', $notification->id)
                ->first();
            $notification->read = $read && $read->read_at !== null;
            $notification->read_at = $read ? $read->read_at : null;
        });

        return response()->json($notifications);
    }

    /**
     * Envoie une nouvelle notification (réservé au super admin).
     */
    public function store(Request $request)
    {
        $user = Auth::user();
        if ($user->role !== 2) {
            return response()->json(['message' => 'Accès interdit'], 403);
        }

        $request->validate([
            'idStruct' => 'nullable|exists:Structure,id',
            'title' => 'required|string|max:255',
            'message' => 'required|string',
            'type' => 'required|in:info,warning,success,system',
        ]);

        $notification = StructureNotification::create([
            'idStruct' => $request->idStruct,
            'title' => $request->title,
            'message' => $request->message,
            'type' => $request->type,
        ]);

        return response()->json($notification, 201);
    }

    /**
     * Marque une notification comme lue.
     */
    public function markAsRead($id)
    {
        $user = Auth::user();
        $notification = StructureNotification::findOrFail($id);

        // Vérifier que l'utilisateur a le droit de voir cette notification
        if ($notification->idStruct !== null && $notification->idStruct !== $user->idStruct) {
            return response()->json(['message' => 'Non autorisé'], 403);
        }

        $read = UserNotificationRead::firstOrCreate(
            ['user_id' => $user->id, 'notification_id' => $id],
            ['read_at' => now()]
        );

        if (!$read->read_at) {
            $read->update(['read_at' => now()]);
        }

        return response()->json(['message' => 'Marquée comme lue']);
    }

    /**
     * Supprime une notification (super admin uniquement).
     */
    public function destroy($id)
    {
        $user = Auth::user();
        if ($user->role !== 2) {
            return response()->json(['message' => 'Accès interdit'], 403);
        }

        $notification = StructureNotification::findOrFail($id);
        $notification->delete();

        return response()->json(['message' => 'Notification supprimée']);
    }
    

/**
 * Récupère toutes les notifications (pour super admin uniquement)
 */
public function allNotifications()
{
    $user = Auth::user();
    
    // Vérifier que l'utilisateur est super admin
    if ($user->role !== 2) {
        return response()->json(['message' => 'Accès interdit'], 403);
    }

    $notifications = StructureNotification::with('structure') // Pour avoir le nom de la structure
        ->orderBy('created_at', 'desc')
        ->get();

    // Ajouter le nom de la structure pour l'affichage
    $notifications->each(function ($notification) {
        $notification->structure_name = $notification->structure ? $notification->structure->nameStruct : 'Toutes les structures';
    });

    return response()->json($notifications);
}
}