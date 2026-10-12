<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Table historique `SubscribeBook`. Une ligne = un utilisateur "suit" un livre.
 */
class BookSubscription extends Model
{
    protected $table = 'SubscribeBook';
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
