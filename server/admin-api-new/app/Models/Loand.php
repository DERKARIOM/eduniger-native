<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Loand extends Model
{
    use HasFactory;

    protected $table = 'Loand';
    protected $primaryKey = 'idLoand';
    public $timestamps = true; // ou false selon votre table

    protected $fillable = [
        'idReservation',
        'idBook', 
        'idAgentGiver',
        'idAgentRecover',
        'dateLoand',
        'realReturnDate',
        'actualReturnDate',
        'closing',
        'view',
        'idStruct',
        'idUser',
    ];

    protected $casts = [
        'dateLoand' => 'datetime',
        'realReturnDate' => 'datetime',
        'actualReturnDate' => 'datetime',
        'closing' => 'boolean',
        'view' => 'boolean',
    ];

    /**
     * Relation avec la réservation
     */
    public function reservation()
    {
        return $this->belongsTo(Reservation::class, 'idReservation', 'idReservation');
    }

    // Autres relations si besoin (book, user, structure...)
}