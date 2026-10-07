<?php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Author extends Model
{
    protected $table = 'Author';       // ou 'authors' si tu as renommé la table
    protected $primaryKey = 'idAuthor';
    public $incrementing = true;
    protected $keyType = 'int';
    public $timestamps = false; // ou true si tu as created_at/updated_at
    protected $fillable = [
        'name', 'firstName', 'profile', 'level', 'profession','biography'
    ];
}
