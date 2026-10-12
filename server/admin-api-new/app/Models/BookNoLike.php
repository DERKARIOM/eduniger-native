<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Table historique `NoLike`. Une ligne = un utilisateur n'a "pas aimé" un livre.
 * Indépendante de `Like` (aucune contrainte croisée côté base, comme dans l'ancien système).
 */
class BookNoLike extends Model
{
    protected $table = 'NoLike';
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
