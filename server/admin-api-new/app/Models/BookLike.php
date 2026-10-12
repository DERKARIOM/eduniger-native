<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Table historique `Like` (mot réservé SQL, d'où le nom de classe distinct).
 * Une ligne = un utilisateur a "aimé" un livre. Pas de clé auto-incrémentée :
 * l'unicité fonctionnelle est (idNumber, idBook).
 */
class BookLike extends Model
{
    protected $table = 'Like';
    public $incrementing = false;
    public $timestamps = false;
    protected $primaryKey = 'idNumber';
    protected $keyType = 'string';

    protected $fillable = [
        'idNumber',
        'idBook',
        'date',
    ];
}
