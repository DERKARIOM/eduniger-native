<?php

namespace App\Policies;

use App\Models\StructAgent;
use App\Models\HelpRequest;

class HelpRequestPolicy
{
    public function viewAny(StructAgent $user)
    {
        return $user->role === 2; // super admin
    }

    public function view(StructAgent $user, HelpRequest $helpRequest)
    {
        return $user->role === 2 || $user->id === $helpRequest->user_id;
    }

    public function create(StructAgent $user)
    {
        return in_array($user->role, [1, 2]); // agent ou super admin
    }

    public function update(StructAgent $user, HelpRequest $helpRequest)
    {
        return $user->role === 2;
    }

    public function delete(StructAgent $user, HelpRequest $helpRequest)
    {
        return $user->role === 2;
    }
}