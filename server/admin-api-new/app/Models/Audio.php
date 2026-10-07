<?php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Audio extends Model
{
    use HasFactory;
    protected $table = "Audio";
    protected $primaryKey = "idAudio";
    protected $fillable = [
        'idAudio',
        'idBook',
        'audio',
        'title',
        'size',
        'maxtime'
    ];

    //protected $appends = ['maxTime'];
    /*public function getMaxTimeAttribute()
    {
        return $this->maxtime;
    }*/
}