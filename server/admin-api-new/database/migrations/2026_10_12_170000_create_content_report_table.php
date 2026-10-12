<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Signalements de contenus (politique Google Play « contenu genere par les
 * utilisateurs » et « IA generative ») : commentaires de livres et reponses de
 * l'assistant Eduna. Consultes et traites par l'equipe de moderation.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasTable('ContentReport')) {
            return;
        }
        Schema::create('ContentReport', function (Blueprint $table) {
            $table->bigIncrements('idReport');
            $table->string('idReporter', 256);          // User.idUser de l'auteur du signalement
            $table->string('type', 20);                 // comment | ai | user
            $table->string('targetId', 256)->nullable(); // idComment, idUser signale, idConversation...
            $table->string('reason', 50);
            $table->text('excerpt')->nullable();         // copie du contenu signale (preuve)
            $table->string('status', 20)->default('pending'); // pending | reviewed | removed
            $table->timestamps();
            $table->index(['type', 'targetId']);
            $table->index('status');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('ContentReport');
    }
};
