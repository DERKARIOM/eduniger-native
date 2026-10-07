<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
class StructBook extends Model
{
    //
    use HasFactory;
    protected $table = 'StructBook';
    protected $fillable = [
        'idStruct',
        'idBook',
        'date'
    ];
}
