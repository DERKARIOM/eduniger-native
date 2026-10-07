<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class HelpRequest extends Model
{
    use HasFactory;

    protected $fillable = [
        'agent_id', 'structure_id', 'type', 'title', 'message',
        'status', 'priority', 'admin_response', 'responded_at'
    ];

    protected $casts = [
        'responded_at' => 'datetime',
    ];

    public function agent()
    {
        return $this->belongsTo(StructAgent::class, 'agent_id');
    }

    public function structure()
    {
        return $this->belongsTo(Structure::class);
    }
}