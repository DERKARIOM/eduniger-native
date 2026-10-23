<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

/**
 * Bannière (image de couverture) du profil d'auteur, facultative.
 * Nom du fichier stocké dans le disque privé (author_covers/), servi par
 * GET /api/public/resource/0/author_cover/{fichier}.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasColumn('Author', 'cover')) {
            Schema::table('Author', function (Blueprint $table) {
                $table->string('cover', 256)->nullable()->after('profile');
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasColumn('Author', 'cover')) {
            Schema::table('Author', function (Blueprint $table) {
                $table->dropColumn('cover');
            });
        }
    }
};
