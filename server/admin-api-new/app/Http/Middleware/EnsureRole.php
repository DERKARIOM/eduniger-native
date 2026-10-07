<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Verifie que l'agent authentifie a l'un des roles autorises.
 * Roles StructAgent : 0 = Admin (de structure), 1 = Agent, 2 = Super Admin.
 * Usage sur une route : ->middleware('role:0,2') pour autoriser Admin ou Super Admin.
 *
 * Ajoute suite a l'audit de securite app-web : la quasi-totalite des routes
 * d'administration (structures, agents, notifications globales...) n'avait
 * aucun controle de role, seulement une verification d'authentification.
 */
class EnsureRole
{
    public function handle(Request $request, Closure $next, string ...$roles): Response
    {
        $user = $request->user();

        if (!$user) {
            return response()->json(['message' => 'Non authentifié'], 401);
        }

        if (!in_array((string) $user->role, $roles, true)) {
            return response()->json(['message' => 'Permission insuffisante'], 403);
        }

        return $next($request);
    }
}
