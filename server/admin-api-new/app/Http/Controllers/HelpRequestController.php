<?php

namespace App\Http\Controllers;

use App\Models\HelpRequest;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Log;
use App\Events\HelpTicketEvent;
use App\Events\NotificationEvent;

class HelpRequestController extends Controller
{
    /**
     * Liste des tickets.
     * - Super admin (role=2) voit tous les tickets, avec filtres optionnels.
     * - Agent normal voit uniquement ses propres tickets.
     */
    public function index(Request $request)
    {
        $user = Auth::user();

        // Autorisation centralisee dans HelpRequestPolicy::viewAny() (super admin
        // uniquement pour la vue "tous les tickets" avec filtres ; un agent normal
        // ne voit que ses propres tickets, filtre applique ci-dessous).
        $canViewAll = $user->can('viewAny', HelpRequest::class);

        $query = HelpRequest::with(['agent', 'structure']);

        if (!$canViewAll) {
            $query->where('agent_id', $user->id);
        } else {
            // Filtres pour super admin
            if ($request->filled('type')) {
                $query->where('type', $request->type);
            }
            if ($request->filled('status')) {
                $query->where('status', $request->status);
            }
            if ($request->filled('structure_id')) {
                $query->where('structure_id', $request->structure_id);
            }
            if ($request->filled('priority')) {
                $query->where('priority', $request->priority);
            }
        }

        $tickets = $query->latest()->get();

        return response()->json($tickets);
    }

    /**
     * Créer un nouveau ticket.
     */
    public function store(Request $request)
    {
        $user = Auth::user(); // struct_agent
        $this->authorize('create', HelpRequest::class);

        $data = $request->validate([
            'type' => 'required|in:problem,bug,improvement,service',
            'title' => 'required|string|max:255',
            'message' => 'required|string',
            'priority' => 'nullable|in:low,medium,high',
        ]);

        $ticket = HelpRequest::create([
            'agent_id' => $user->id,
            'structure_id' => $user->idStruct,
            'type' => $data['type'],
            'title' => $data['title'],
            'message' => $data['message'],
            'priority' => $data['priority'] ?? 'medium',
        ]);
        event(new HelpTicketEvent($ticket->toArray(), 'created'));
        event(new NotificationEvent([
            'user_id' => null, // pour tous les admins
            'type' => 'help_ticket',
            'title' => 'Nouveau ticket',
            'message' => 'Un nouveau ticket a été créé : ' . $ticket->title,
            'link' => '/help-requests/' . $ticket->id,
            'created_at' => now(),
        ]));
        return response()->json($ticket, 201);
    }

    /**
     * Afficher un ticket spécifique.
     */
    public function show(HelpRequest $helpRequest)
    {
        $this->authorize('view', $helpRequest);

        $helpRequest->load(['agent', 'structure']);
        return response()->json($helpRequest);
    }

    /**
     * Mettre à jour un ticket (réservé au super admin).
     */
    public function update(Request $request, HelpRequest $helpRequest)
{
    $this->authorize('update', $helpRequest);

    $data = $request->validate([
        'status' => 'sometimes|in:open,in_progress,resolved,closed',
        'priority' => 'sometimes|in:low,medium,high',
        'admin_response' => 'nullable|string',
    ]);

    if (isset($data['admin_response']) && $data['admin_response']) {
        $data['responded_at'] = now();
    }

    $helpRequest->update($data);

    // Émettre l'événement de mise à jour du ticket
    event(new HelpTicketEvent($helpRequest->toArray(), 'updated'));

    // Si une réponse admin a été ajoutée, notifier l'agent
    if ($request->filled('admin_response')) {
        event(new NotificationEvent([
            'user_id' => $helpRequest->agent_id,
            'type' => 'help_response',
            'title' => 'Réponse à votre ticket',
            'message' => 'Une réponse a été ajoutée à votre ticket : ' . $helpRequest->title,
            'link' => '/help-requests/' . $helpRequest->id,
            'created_at' => now(),
        ]));
    }

    return response()->json($helpRequest);
}

    /**
     * Supprimer un ticket (réservé au super admin).
     */
    public function destroy(HelpRequest $helpRequest)
    {
        $this->authorize('delete', $helpRequest);

        $helpRequest->delete();

        return response()->json(['message' => 'Ticket supprimé avec succès']);
    }
}