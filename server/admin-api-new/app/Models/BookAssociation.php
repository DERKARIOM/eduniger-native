<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use App\Models\Structure;
use App\Models\Category;
use App\Models\Author;
use App\Models\Audio;
class BookAssociation extends Model
{
    protected $table = 'Book';
    protected $primaryKey = 'idBook';
    public $incrementing = false;
    protected $keyType = 'string';

    public function categories()
    {
        return $this->belongsToMany(
            Category::class,
            'BookCategory',
            'idBook',
            'idCategory'
        );
    }

    public function author()
    {
        return $this->belongsTo(Author::class, 'idAuthor');
    }

    public function structures()
    {
        return $this->belongsToMany(
            Structure::class,
            'StructBook',
            'idBook',
            'idStruct'
        );
    }
    public function audioFiles()
    {
        return $this->hasMany(Audio::class, 'idBook', 'idBook');
    }
}