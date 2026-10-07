<?php

namespace App\Events;

use Illuminate\Broadcasting\Channel;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Contracts\Broadcasting\ShouldBroadcast;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class NotificationEvent implements ShouldBroadcast
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public $notification;

    public function __construct($notification)
    {
        $this->notification = $notification;
    }

    /**
     * Le canal sur lequel l'événement sera diffusé.
     * Utilise un canal privé pour l'utilisateur cible.
     */
    public function broadcastOn()
    {
        return new Channel('private-user.' . $this->notification['user_id']);
    }

    /**
     * Nom de l'événement côté frontend.
     */
    public function broadcastAs()
    {
        return 'notification.received';
    }

    /**
     * Données supplémentaires envoyées.
     */
    public function broadcastWith()
    {
        return [
            'id' => $this->notification['id'],
            'type' => $this->notification['type'],
            'title' => $this->notification['title'],
            'message' => $this->notification['message'],
            'link' => $this->notification['link'],
            'created_at' => $this->notification['created_at'],
        ];
    }
}