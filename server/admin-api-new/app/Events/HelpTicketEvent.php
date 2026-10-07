<?php

namespace App\Events;

use Illuminate\Broadcasting\Channel;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Contracts\Broadcasting\ShouldBroadcast;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class HelpTicketEvent implements ShouldBroadcast
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public $ticket;
    public $action; // 'created', 'updated', 'responded'

    public function __construct($ticket, $action)
    {
        $this->ticket = $ticket;
        $this->action = $action;
    }

    /**
     * Canaux de diffusion.
     * - Canal privé pour l'agent (propriétaire du ticket)
     * - Canal privé pour les administrateurs (tous les admins)
     */
    public function broadcastOn()
    {
        $channels = [
            new Channel('private-user.' . $this->ticket['agent_id']),
        ];

        // Si c'est une mise à jour admin, notifier tous les admins
        if (in_array($this->action, ['updated', 'responded'])) {
            $channels[] = new Channel('private-help.admin');
        }

        return $channels;
    }

    public function broadcastAs()
    {
        return 'help.ticket.' . $this->action;
    }

    public function broadcastWith()
    {
        return [
            'id' => $this->ticket['id'],
            'title' => $this->ticket['title'],
            'status' => $this->ticket['status'],
            'priority' => $this->ticket['priority'],
            'admin_response' => $this->ticket['admin_response'] ?? null,
        ];
    }
}