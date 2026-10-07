<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class UserNotificationRead extends Model
{
    protected $table = 'UserNotificationRead';
    protected $fillable = ['user_id', 'notification_id', 'read_at'];

    protected $casts = [
        'read_at' => 'datetime',
    ];

    public function user()
    {
        return $this->belongsTo(StructAgent::class, 'user_id');
    }

    public function notification()
    {
        return $this->belongsTo(StructureNotification::class, 'notification_id');
    }
}