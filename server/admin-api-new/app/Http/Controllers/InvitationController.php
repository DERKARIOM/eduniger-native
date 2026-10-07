<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\StructAgent;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Carbon\Carbon;
use App\Models\InvitationLog;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Exception;

class InvitationController extends Controller
{
    /**
     * Génère un code d'invitation pour un agent donné.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function generate(Request $request)
    {
        try {
            $request->validate([
                'email' => 'required|email|exists:struct_agent,email',
            ]);

            $agent = StructAgent::where('email', $request->email)->first();

            if (!$agent) {
                return response()->json(['message' => 'Agent non trouvé.'], 404);
            }

            $result = self::generateInvitationCode($agent);

            return response()->json([
                'message' => 'Code généré avec succès.',
                'invitation_code' => $result['code'],
                'expires_at' => $result['expires_at']->toDateTimeString(),
            ]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            // Les erreurs de validation sont automatiquement gérées par Laravel
            // mais nous pouvons les capturer pour les formater si nécessaire.
            return response()->json([
                'message' => 'Données invalides.',
                'errors' => $e->errors(),
            ], 422);
        } catch (Exception $e) {
            Log::error('Erreur lors de la génération du code d\'invitation : ' . $e->getMessage(), [
                'email' => $request->email ?? 'inconnu',
                'trace' => $e->getTraceAsString(),
            ]);
            return response()->json(['message' => 'Une erreur est survenue lors de la génération du code.'], 500);
        }
    }

    /**
     * Génère un code d'invitation pour un agent donné (méthode statique réutilisable).
     *
     * @param StructAgent $agent
     * @return array
     * @throws Exception
     */
    public static function generateInvitationCode(StructAgent $agent): array
    {
        DB::beginTransaction();
        try {
            $code = strtoupper(Str::random(4)) . '-' . random_int(1000, 9999);
            $expiresAt = Carbon::now()->addHours(24);

            $agent->update([
                'invitationCode' => $code,
                'codeExpiresAt' => $expiresAt,
                'isAuthorizedToCreateAccount' => true,
            ]);

            // Journalisation de l'invitation
            InvitationLog::create([
                'agent_id'       => $agent->id,
                'generated_code' => $code,
                'expires_at'     => $expiresAt,
                'generated_by'   => null, // null si pas connecté (ou à compléter)
            ]);

            DB::commit();

            return [
                'code' => $code,
                'expires_at' => $expiresAt,
            ];
        } catch (Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la génération du code d\'invitation pour l\'agent ID ' . $agent->id . ' : ' . $e->getMessage(), [
                'agent_id' => $agent->id,
                'trace' => $e->getTraceAsString(),
            ]);
            throw new Exception('Impossible de générer le code d\'invitation.');
        }
    }
}