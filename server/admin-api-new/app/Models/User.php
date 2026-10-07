<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $table = 'User';
    protected $primaryKey = 'idUser';
    public $incrementing = false;
    protected $keyType = 'string';

    protected $fillable = [
        'idUser',
        'name',
        'firstName',
        'email',
        'phoneNumber',
        'profession',
        'profile',
        'isAdmin',
        'password',
        'fcm_token',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected $casts = [
        'isAdmin' => 'boolean',
        'profession' => 'integer',
    ];
}
