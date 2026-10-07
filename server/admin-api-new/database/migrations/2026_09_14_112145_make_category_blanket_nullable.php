<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Corrige une incohérence réelle constatée dans l'audit "icônes de catégorie" :
 * la colonne `Category.blanket` était déclarée NOT NULL dans le schéma importé
 * (cf. base/eduniger.sql), alors que CategoryController::store() valide
 * explicitement l'icône comme facultative ('blanket' => 'nullable|file|...')
 * et tente d'insérer `null` quand aucun fichier n'est fourni.
 *
 * Résultat avant ce correctif : créer une catégorie SANS icône déclenchait une
 * QueryException (colonne NOT NULL) côté serveur - la catégorie n'était même
 * pas créée (500), pas seulement son icône manquante.
 *
 * Cette migration aligne le schéma réel sur ce que le code a toujours supposé :
 * une catégorie peut légitimement n'avoir aucune icône (le mobile affiche déjà
 * une icône par défaut via CategoryAdapter dans ce cas, cf. corrections mobiles
 * du même audit).
 */
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('Category', function (Blueprint $table) {
            $table->text('blanket')->nullable()->change();
        });
    }

    public function down(): void
    {
        Schema::table('Category', function (Blueprint $table) {
            $table->text('blanket')->nullable(false)->change();
        });
    }
};
