<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Table historique `View`. Une ligne = un utilisateur a consulté un livre
 * (une seule fois par couple idNumber/idBook, comme dans l'ancien système).
 */
class BookView extends Model
{
    protected $table = 'View';
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
