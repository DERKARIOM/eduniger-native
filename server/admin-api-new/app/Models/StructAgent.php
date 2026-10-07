<?php

namespace App\Models;

use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Sanctum\HasApiTokens;
use Illuminate\Notifications\Notifiable;

class StructAgent extends Authenticatable
{
    use HasApiTokens, Notifiable;

    protected $table = 'struct_agent';

    protected $fillable = [
        'name',
        'email',
        'phoneNumber',
        'role',
        'idStruct',
        'isAuthorizedToCreateAccount',
        'invitationCode',
        'codeExpiresAt',
        'password',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected $casts = [
        'isAuthorizedToCreateAccount' => 'boolean',
        'codeExpiresAt' => 'datetime',
        'idStruct' => 'integer',
        'role' => 'integer', // 0: Admin, 1: Agent, 2: Super Admin
        'password' => 'hashed',
    ];

    public function structure()
    {
        return $this->belongsTo(Structure::class, 'idStruct');
    }
}
