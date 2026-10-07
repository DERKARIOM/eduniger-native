<?php

namespace Tests\Feature;

use App\Models\StructAgent;
use App\Models\Structure;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Schema;
use Laravel\Sanctum\Sanctum;
use PHPUnit\Framework\Attributes\Test;
use Tests\TestCase;

/**
 * Verifie que les correctifs de securite apportes lors de l'audit du back-office
 * (middleware role / structure.scope, reecriture de StructAgentController) bloquent
 * bien les scenarios de contournement de permissions identifies pendant l'audit.
 *
 * Ce fichier cree ses propres tables (struct_agent / Structure) en base sqlite en
 * memoire au lieu de s'appuyer sur database/migrations : ce projet gere le schema
 * reel de ces tables hors Laravel (SQL applique directement en production), donc
 * ajouter de vraies migrations pour elles risquerait de faire echouer un futur
 * `php artisan migrate` sur la base de production ("table already exists"). Cette
 * approche isole donc totalement les tests de la base reelle.
 */
class BackofficeAuthorizationTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();

        if (!Schema::hasTable('Structure')) {
            Schema::create('Structure', function ($table) {
                $table->id();
                $table->string('nameStruct')->nullable();
                $table->text('description')->nullable();
                $table->string('logo')->nullable();
                $table->string('banner')->nullable();
                $table->integer('adhererNumber')->nullable();
                $table->integer('bookNumber')->nullable();
                $table->string('location')->nullable();
                $table->unsignedBigInteger('storageLimit')->nullable();
                $table->timestamps();
            });
        }

        if (!Schema::hasTable('Book')) {
            Schema::create('Book', function ($table) {
                // La cle primaire du modele Book est "idBook" (non auto-incrementee),
                // pas "id" : voir app/Models/Book.php ($primaryKey, $incrementing=false).
                $table->unsignedBigInteger('idBook')->primary();
                $table->string('title')->nullable();
                $table->timestamps();
            });
        }

        if (!Schema::hasTable('invitation_logs')) {
            Schema::create('invitation_logs', function ($table) {
                $table->id();
                $table->unsignedBigInteger('agent_id')->nullable();
                $table->string('generated_code')->nullable();
                $table->timestamp('expires_at')->nullable();
                $table->unsignedBigInteger('generated_by')->nullable();
                $table->timestamps();
            });
        }

        if (!Schema::hasTable('struct_agent')) {
            Schema::create('struct_agent', function ($table) {
                $table->id();
                $table->string('name')->nullable();
                $table->string('email')->unique();
                $table->string('phoneNumber')->nullable();
                $table->integer('role')->default(1); // 0 Admin, 1 Agent, 2 Super Admin
                $table->unsignedBigInteger('idStruct')->nullable();
                $table->boolean('isAuthorizedToCreateAccount')->default(true);
                $table->string('invitationCode')->nullable();
                $table->timestamp('codeExpiresAt')->nullable();
                $table->string('password');
                $table->rememberToken();
                $table->timestamps();
            });
        }
    }

    private function makeStructure(): Structure
    {
        return Structure::create([
            'nameStruct' => 'Bibliotheque Test',
            'description' => 'Structure de test',
        ]);
    }

    private function makeAgent(int $role, int $idStruct, ?int $id = null): StructAgent
    {
        return StructAgent::create([
            'id' => $id,
            'name' => 'Agent Test',
            'email' => 'agent' . uniqid() . '@example.com',
            'role' => $role,
            'idStruct' => $idStruct,
            'password' => 'secret123',
        ]);
    }

    #[Test]
    public function un_agent_role_1_ne_peut_pas_creer_de_structure(): void
    {
        $struct = $this->makeStructure();
        $agent = $this->makeAgent(role: 1, idStruct: $struct->id);

        Sanctum::actingAs($agent, [], 'sanctum');

        $response = $this->postJson('/api/structures', ['nameStruct' => 'Nouvelle structure']);

        $response->assertStatus(403);
    }

    #[Test]
    public function un_super_admin_peut_creer_une_structure(): void
    {
        $struct = $this->makeStructure();
        $superAdmin = $this->makeAgent(role: 2, idStruct: $struct->id);

        Sanctum::actingAs($superAdmin, [], 'sanctum');

        $response = $this->postJson('/api/structures', ['nameStruct' => 'Nouvelle structure']);

        $response->assertStatus(201);
    }

    #[Test]
    public function un_admin_de_structure_ne_peut_pas_modifier_une_autre_structure(): void
    {
        $structA = $this->makeStructure();
        $structB = $this->makeStructure();
        $adminA = $this->makeAgent(role: 0, idStruct: $structA->id);

        Sanctum::actingAs($adminA, [], 'sanctum');

        $response = $this->putJson("/api/structures/{$structB->id}", ['nameStruct' => 'Renommee']);

        $response->assertStatus(403);
    }

    #[Test]
    public function un_admin_de_structure_peut_modifier_sa_propre_structure(): void
    {
        $struct = $this->makeStructure();
        $admin = $this->makeAgent(role: 0, idStruct: $struct->id);

        Sanctum::actingAs($admin, [], 'sanctum');

        $response = $this->putJson("/api/structures/{$struct->id}", ['nameStruct' => 'Renommee']);

        $response->assertStatus(200);
    }

    #[Test]
    public function un_agent_ne_peut_pas_lister_les_agents_dune_autre_structure(): void
    {
        $structA = $this->makeStructure();
        $structB = $this->makeStructure();
        $this->makeAgent(role: 1, idStruct: $structB->id); // agent cible dans B
        $agentA = $this->makeAgent(role: 1, idStruct: $structA->id);

        Sanctum::actingAs($agentA, [], 'sanctum');

        $response = $this->getJson("/api/structure/{$structB->id}/agents");

        $response->assertStatus(403);
    }

    #[Test]
    public function un_super_admin_peut_lister_les_agents_de_nimporte_quelle_structure(): void
    {
        $structA = $this->makeStructure();
        $structB = $this->makeStructure();
        $this->makeAgent(role: 1, idStruct: $structB->id);
        $superAdmin = $this->makeAgent(role: 2, idStruct: $structA->id);

        Sanctum::actingAs($superAdmin, [], 'sanctum');

        $response = $this->getJson("/api/structure/{$structB->id}/agents");

        $response->assertStatus(200);
    }

    #[Test]
    public function un_admin_ne_peut_pas_creer_un_agent_super_admin(): void
    {
        $struct = $this->makeStructure();
        $admin = $this->makeAgent(role: 0, idStruct: $struct->id);

        Sanctum::actingAs($admin, [], 'sanctum');

        $response = $this->postJson('/api/agents', [
            'name' => 'Nouvel agent',
            'email' => 'nouvel.agent@example.com',
            'password' => 'secret123',
            'role' => 2,
            'idStruct' => $struct->id,
        ]);

        $response->assertStatus(403);
    }

    #[Test]
    public function un_admin_ne_peut_pas_creer_un_agent_dans_une_autre_structure(): void
    {
        $structA = $this->makeStructure();
        $structB = $this->makeStructure();
        $admin = $this->makeAgent(role: 0, idStruct: $structA->id);

        Sanctum::actingAs($admin, [], 'sanctum');

        $response = $this->postJson('/api/agents', [
            'name' => 'Nouvel agent',
            'email' => 'nouvel.agent2@example.com',
            'password' => 'secret123',
            'role' => 1,
            'idStruct' => $structB->id,
        ]);

        // Le controleur force idStruct a la structure de l'appelant plutot que de
        // faire confiance a la valeur envoyee dans le corps de la requete.
        $response->assertStatus(201);
        $this->assertDatabaseHas('struct_agent', [
            'email' => 'nouvel.agent2@example.com',
            'idStruct' => $structA->id,
        ]);
    }

    #[Test]
    public function une_requete_non_authentifiee_est_rejetee(): void
    {
        $response = $this->postJson('/api/structures', ['nameStruct' => 'X']);

        $response->assertStatus(401);
    }

    #[Test]
    public function get_book_by_id_utilise_bien_bookcontroller_et_non_structagentcontroller(): void
    {
        // Regression pour le bug trouve pendant l'audit : la route
        // GET /api/books/{idStruct}/{id} pointait vers StructAgentController::show
        // au lieu de BookController::show. On verifie ici que la reponse correspond
        // bien au comportement de BookController::show (404 "Livre non trouve"),
        // et pas a celui de StructAgentController::show (403/404 agent).
        $struct = $this->makeStructure();
        $agent = $this->makeAgent(role: 2, idStruct: $struct->id);

        Sanctum::actingAs($agent, [], 'sanctum');

        $response = $this->getJson("/api/books/{$struct->id}/999999");

        $response->assertStatus(404);
        $response->assertJson(['message' => 'Livre non trouvé.']);
    }
}
