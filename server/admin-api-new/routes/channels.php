<?php

use Illuminate\Support\Facades\Broadcast;
use App\Models\StructAgent;

/*Broadcast::channel('App.Models.User.{id}', function ($user, $id) {
    return (int) $user->id === (int) $id;
});*/


// Canal privé pour un utilisateur (agent)
Broadcast::channel('private-user.{id}', function ($user, $id) {
    // $user est l'agent authentifié (via Sanctum)
    return (int) $user->id === (int) $id;
});

// Canal privé pour tous les administrateurs
Broadcast::channel('private-help.admin', function ($user) {
    // Uniquement les agents avec role = 2 (super admin)
    return $user->role === 2;
});