<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Table historique `Comment` (commentaires des livres), utilisee par
 * BookInteractionController et par l'ancien SendComments.php
 * (INSERT INTO Comment VALUES (NULL, :idNumber, :idBook, NOW(), :message)).
 * Absente de certains exports de la base : creee seulement si elle n'existe pas,
 * sans jamais toucher a une table existante.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasTable('Comment')) {
            return;
        }
        Schema::create('Comment', function (Blueprint $table) {
            $table->bigIncrements('idComment');
            $table->string('idNumber', 256);
            $table->string('idBook', 256);
            $table->dateTime('date')->nullable();
            $table->text('message');
            $table->index('idBook');
        });
    }

    public function down(): void
    {
        // Volontairement vide : ne jamais supprimer des commentaires existants.
    }
};
