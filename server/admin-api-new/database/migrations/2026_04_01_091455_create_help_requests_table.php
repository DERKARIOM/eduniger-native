<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('help_requests', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            // Correctif (bloquait TOUTE migration ulterieure, y compris celles sans
            // rapport avec help_requests) : foreignId()->constrained('structures')
            // supposait une table 'structures' (minuscule, pluriel) avec un id
            // bigint unsigned - la table reelle s'appelle 'Structure' (majuscule,
            // singulier, schema importe directement en base sans migration Laravel,
            // comme Author/Book/Category) et sa colonne id est un INT signe (voir
            // base/eduniger.sql : "id int NOT NULL", pas "bigint unsigned"). MySQL
            // refuse une contrainte de cle etrangere entre deux types de colonnes
            // incompatibles, d'ou l'erreur 1824 "Failed to open the referenced
            // table 'structures'" au premier `php artisan migrate` reellement
            // execute sur le serveur.
            $table->integer('structure_id');
            $table->foreign('structure_id')->references('id')->on('Structure')->onDelete('cascade');
            $table->enum('type', ['problem', 'bug', 'improvement', 'service']);
            $table->string('title');
            $table->text('message');
            $table->enum('status', ['open', 'in_progress', 'resolved', 'closed'])->default('open');
            $table->enum('priority', ['low', 'medium', 'high'])->default('medium');
            $table->text('admin_response')->nullable();
            $table->timestamp('responded_at')->nullable();
            $table->timestamps();
        });
    }

    public function down()
    {
        Schema::dropIfExists('help_requests');
    }
};
