<?php

namespace App\Http\Controllers;

use Exception;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Validator;

/**
 * Signalement d'un contenu inapproprie depuis l'application mobile
 * (commentaire d'un livre, reponse de l'assistant IA Eduna, utilisateur).
 * Exigence Google Play : tout contenu genere par les utilisateurs ou par une IA
 * doit pouvoir etre signale depuis l'application.
 */
class ContentReportController extends Controller
{
    private const REASONS = ['offensant', 'haineux', 'sexuel', 'violent', 'spam', 'faux', 'autre'];

    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'type'     => 'required|in:comment,ai,user',
            'targetId' => 'nullable|string|max:256',
            'reason'   => 'required|in:' . implode(',', self::REASONS),
            'excerpt'  => 'nullable|string|max:5000',
        ]);
        if ($validator->fails()) {
            return response()->json([
                'message' => 'Signalement invalide.',
                'errors'  => $validator->errors(),
            ], 422);
        }

        $idReporter = $request->user()->idUser;

        try {
            // Un meme utilisateur ne signale qu'une fois le meme contenu.
            $already = DB::table('ContentReport')
                ->where('idReporter', $idReporter)
                ->where('type', $request->type)
                ->where('targetId', $request->targetId)
                ->when($request->type === 'ai', fn ($q) => $q->where('excerpt', $request->excerpt))
                ->exists();

            if (!$already) {
                DB::table('ContentReport')->insert([
                    'idReporter' => $idReporter,
                    'type'       => $request->type,
                    'targetId'   => $request->targetId,
                    'reason'     => $request->reason,
                    'excerpt'    => $request->excerpt,
                    'status'     => 'pending',
                    'created_at' => now(),
                    'updated_at' => now(),
                ]);
                Log::warning('Contenu signale', [
                    'type' => $request->type, 'targetId' => $request->targetId, 'reason' => $request->reason,
                ]);
            }
        } catch (Exception $e) {
            Log::error('Erreur lors du signalement : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }

        return response()->json(['message' => 'Merci, votre signalement a été transmis à notre équipe.'], 201);
    }
}
