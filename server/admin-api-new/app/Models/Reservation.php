<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
class Reservation extends Model
{
    use HasFactory;
    
    protected $table = 'Reservation';
    protected $primaryKey = 'idReservation'; // ← Définir la clé primaire correcte
    
    protected $fillable = [
        'idReservation',
        'idNumber',
        'idBook',
        'date',
        'numberOfDay',
        'expireDate',
        'deliveryDate',
        'state',
        'treat',
        'view',
        'created_at',
        'updated_at',
        'idStruct',
    
    ];
    
    protected $casts = [
        'date' => 'datetime',
        'expireDate' => 'datetime',
        'deliveryDate' => 'datetime',
        'state' => 'integer',
        'treat' => 'boolean',
        'view' => 'boolean',
    ];
    
    public $incrementing = true; // Si idLoand est auto-incrémenté
    public $timestamps = false; // Si vous n'avez pas created_at/updated_at
}
