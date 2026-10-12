<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Connexion « Continuer avec Google » : identifiant Google stable (claim "sub" de
 * l'ID token) lie a un compte Elim. Nullable : les comptes par numero/mot de passe
 * n'en ont pas. Unique : un compte Google = un seul compte Elim.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasColumn('User', 'google_id')) {
            Schema::table('User', function (Blueprint $table) {
                $table->string('google_id', 64)->nullable()->unique()->after('email');
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasColumn('User', 'google_id')) {
            Schema::table('User', function (Blueprint $table) {
                $table->dropUnique(['google_id']);
                $table->dropColumn('google_id');
            });
        }
    }
};
