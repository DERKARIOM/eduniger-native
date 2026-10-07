<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class StructUser extends Model
{
    //
    use HasFactory;
    protected $table = 'StructUser';
    protected $primaryKey = ['idStruct', 'idUser'];
    public $incrementing = false;
    protected $keyType = 'string';
    protected $fillable = ['idStruct','idUser','date','isAdmin','registerNumber'];

    public function user()
    {
        return $this->belongsTo(User::class, 'idUser', 'idUser');
    }
    // Delete structure-user association
    public function deleteAssociation($idStruct, $idUser)
    {        return $this->where('idStruct', $idStruct)
                    ->where('idUser', $idUser)
                    ->delete(); 
    }
}
