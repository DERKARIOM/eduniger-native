<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

/**
 * Table historique `Comment`. idComment est auto-incrémenté (NULL à l'insertion
 * côté ancien système) ; idNumber référence en réalité User.idUser.
 */
class Comment extends Model
{
    protected $table = 'Comment';
    protected $primaryKey = 'idComment';
    public $timestamps = false;

    protected $fillable = [
        'idNumber',
        'idBook',
        'date',
        'message',
    ];

    public function user()
    {
        return $this->belongsTo(User::class, 'idNumber', 'idUser');
    }
}
