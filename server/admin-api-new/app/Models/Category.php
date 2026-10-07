<?php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Category extends Model
{
    protected $table = 'Category'; // ou 'categories' si tu as renommé
    protected $primaryKey = 'idCategory';
    public $incrementing = true;
    protected $keyType = 'int';
    public $timestamps = false; // si pas de created_at/updated_at
    protected $fillable = [
        'title', 'blanket', 'date', 'numberSubscribe'
    ];
}
