<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
class Book extends Model
{
    //
    use HasFactory;
    protected $table = "Book";
    protected $primaryKey = 'idBook';
    public $incrementing = false; 
    protected $fillable = [
        'idBook',
        'title',
        'description',
        'idAuthor',
        'blanket',
        'electronic',
        'isPhysic',
        'isAudio',
        'available',
        'size',
        'nbrPage',
        'numberLike',    
        'numberNoLike',   
        'numberView',   
        'numberComment',   
        'numberSubscribe'
    ];

    public function structures()
    {
        return $this->belongsToMany(Structure::class, 'StructBook', 'idBook', 'idStruct', 'idBook', 'id');
    }
    

}
