<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Structure extends Model
{
    use HasFactory;
    
    protected $table = 'Structure';
    protected $primaryKey = 'id';
    protected $appends = ['logo_url', 'banner_url'];
    
    protected $fillable = [
        'id',
        'nameStruct', 
        'description', 
        'logo', 
        'banner', 
        'adhererNumber', 
        'bookNumber',
        'location',
        'storageLimit',
    ];
    public function setNameStructAttribute($value)
    {
        $this->attributes['nameStruct'] = $value;
        $this->timestamps = false; // Désactive la mise à jour automatique du timestamp
        $this->save();
        $this->timestamps = true; // Réactive les timestamps
    }
    // Accessor pour l'URL complète du logo
    public function getLogoUrlAttribute()
    {
        return $this->logo ? asset('storage/' . $this->logo) : null;
    }
    
    // Accessor pour l'URL complète de la bannière
    public function getBannerUrlAttribute()
    {
        return $this->banner ? asset('storage/' . $this->banner) : null;
    }

    public function books()
    {
        return $this->belongsToMany(Book::class, 'StructBook', 'idStruct', 'idBook', 'id', 'idBook');
    }
}