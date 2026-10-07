<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Verifie que l'agent authentifie appartient bien a la structure ciblee par
 * la route (parametre passe en argument du middleware, ex: "idStruct" ou "id").
 * Un Super Admin (role === 2) n'est jamais cantonne a une structure et passe
 * toujours.
 *
 * Ajoute suite a l'audit de securite app-web : sans ce middleware, un agent
 * de la structure A pouvait manipuler les donnees (livres, adherents,
 * sauvegardes, fichiers proteges...) de n'importe quelle autre structure en
 * changeant simplement l'ID dans l'URL.
 *
 * Usage sur une route : ->middleware('structure.scope:idStruct')
 */
class EnsureAgentBelongsToStructure
{
    public function handle(Request $request, Closure $next, string $routeParam = 'idStruct'): Response
    {
        $user = $request->user();

        if (!$user) {
            return response()->json(['message' => 'Non authentifié'], 401);
        }

        // Super Admin : acces a toutes les structures.
        if ((int) $user->role === 2) {
            return $next($request);
        }

        $targetStructId = $request->route($routeParam);

        if ($targetStructId === null || (int) $user->idStruct !== (int) $targetStructId) {
            return response()->json(['message' => "Vous n'avez pas accès à cette structure"], 403);
        }

        return $next($request);
    }
}
