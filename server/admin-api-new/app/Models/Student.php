<?php
namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Student extends Model
{
    protected $table = 'Student'; // ou 'students'
    protected $primaryKey = 'idNumber';
    public $incrementing = false; // si idNumber n'est pas auto-increment
    protected $keyType = 'string';
    public $timestamps = false; // ou true si tu as created_at/updated_at
    protected $fillable = [
        'idNumber', 'name', 'firstName', 'section', 'department', 'isDelegue'
    ];
}
