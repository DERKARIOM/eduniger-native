<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class InvitationLog extends Model
{
    protected $fillable = [
        'agent_id',
        'generated_code',
        'expires_at',
        'generated_by',
    ];

    public function agent()
    {
        return $this->belongsTo(StructAgent::class, 'agent_id');
    }

    public function admin()
    {
        return $this->belongsTo(User::class, 'generated_by');
    }
}

