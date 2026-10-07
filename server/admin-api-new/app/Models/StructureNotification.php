<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class StructureNotification extends Model
{
    protected $table = 'StructureNotifications';
    protected $fillable = ['idStruct', 'title', 'message', 'type'];

    protected $casts = [
        'created_at' => 'datetime',
    ];

    public function structure()
    {
        return $this->belongsTo(Structure::class, 'idStruct');
    }

    public function reads()
    {
        return $this->hasMany(UserNotificationRead::class, 'notification_id');
    }
}