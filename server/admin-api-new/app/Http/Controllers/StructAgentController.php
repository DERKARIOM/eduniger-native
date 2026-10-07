<?php

namespace App\Http\Controllers;

use App\Models\StructAgent;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;
use Carbon\Carbon;
use App\Models\Structure;
use App\Http\Controllers\InvitationController;

/**
 * SECURITE (audit app-web) : avant correction, aucune methode de ce controleur
 * ne verifiait le role ni l'appartenance a une structure de l'agent appelant.
 * N'importe quel agent authentifie (meme role 1 "Agent", le plus bas) pouvait :
 *  - creer un nouvel agent avec le role de son choix, y compris role=2
 *    "Super Admin", pour n'importe quelle structure (escalade de privileges) ;
 *  - modifier le mot de passe/email/structure de N'IMPORTE QUEL autre agent,
 *    y compris un Super Admin (prise de controle de compte) ;
 *  - supprimer n'importe quel agent, y compris un Super Admin (deni de service).
 * Corrige ci-dessous : role 0 (Admin de structure) et role 2 (Super Admin)
 * seuls autorises a gerer des agents ; un Admin de structure est cantonne a
 * SA PROPRE structure et ne peut pas creer/promouvoir un Super Admin.
 */
class StructAgentController extends Controller
{
    //  Lister les agents (Super Admin : tous ; Admin de structure : uniquement les siens)
    public function index()
    {
        $user = Auth::user();

        $query = StructAgent::with('structure');
        if ((int) $user->role !== 2) {
            $query->where('idStruct', $user->idStruct);
        }

        return response()->json($query->get());
    }

    // Ajouter un nouvel agent
    public function store(Request $request)
    {
        $actor = Auth::user();

        if (!in_array((int) $actor->role, [0, 2], true)) {
            return response()->json(['message' => 'Permission insuffisante pour créer un agent.'], 403);
        }

        $request->validate([
            'name'        => 'required|string|max:256',
            'email'       => 'required|email|unique:struct_agent,email',
            'phoneNumber' => 'nullable|string|max:100',
            'role'        => 'required|in:0,1,2', // 0: Admin, 1: Agent, 2: Super Admin
            'idStruct'    => 'nullable|exists:Structure,id',
            'password'    => 'nullable|string|min:8',
        ]);

        // Un Admin de structure (role 0) ne peut créer des agents que pour SA
        // propre structure, et ne peut jamais créer/promouvoir un Super Admin.
        $targetIdStruct = $request->idStruct;
        $targetRole = (int) $request->role;
        if ((int) $actor->role === 0) {
            $targetIdStruct = $actor->idStruct;
            if ($targetRole === 2) {
                return response()->json(['message' => "Vous ne pouvez pas créer de compte Super Admin."], 403);
            }
        }

        $agent = StructAgent::create([
            'name'        => $request->name,
            'email'       => $request->email,
            'phoneNumber' => $request->phoneNumber,
            'role'        => $targetRole,
            'idStruct'    => $targetIdStruct,
            'isAuthorizedToCreateAccount' => true,
            'password' => Hash::make($request->password ?? null), // Mot de passe par défaut si non fourni
        ]);

        // Génération automatique via InvitationController
        $invitation = InvitationController::generateInvitationCode($agent);

        return response()->json([
            'message' => 'Agent créé avec succès.',
            'agent' => $agent,
            'invitation_code' => $invitation['code'],
            'expires_at' => $invitation['expires_at']->toDateTimeString(),
        ], 201);
    }

    // Modifier un agent
    public function update(Request $request, $id)
    {
        $actor = Auth::user();
        $agent = StructAgent::findOrFail($id);

        if (!in_array((int) $actor->role, [0, 2], true)) {
            return response()->json(['message' => 'Permission insuffisante pour modifier un agent.'], 403);
        }
        // Un Admin de structure ne peut modifier que les agents de SA structure.
        if ((int) $actor->role === 0 && (int) $actor->idStruct !== (int) $agent->idStruct) {
            return response()->json(['message' => "Vous n'avez pas accès à cet agent."], 403);
        }

        $request->validate([
            'name'       => 'required|string|max:256',
            'email'      => ['nullable','email', Rule::unique('struct_agent')->ignore($agent->id)],
            'phoneNumber'=> 'nullable|string|max:100',
            'idStruct'   => 'required|exists:Structure,id',
            'password'  => 'nullable|string|min:8|confirmed',
        ]);
        // Un Admin de structure ne peut pas déplacer un agent vers une autre structure.
        if ((int) $actor->role === 0 && (int) $request->idStruct !== (int) $actor->idStruct) {
            return response()->json(['message' => "Vous ne pouvez pas déplacer cet agent vers une autre structure."], 403);
        }
        // Vérification de l'existence de la structure
        $structure = Structure::findOrFail($request->idStruct);
        // Vérification de l'appartenance de l'agent à la structure
        if ($structure->id !== $agent->idStruct) {
            return response()->json(['message' => 'L\'agent n\'appartient pas à cette structure.'], 403);
        }
        // Mise à jour de l'agent
        if ($request->has('password') && !empty($request->password)) {
            $request->merge(['password' => Hash::make($request->password)]);
        } else {
            $request->merge(['password' => $agent->password]); // Conserver l'ancien mot de passe si non fourni
        }
        $agent->update($request->only(['name', 'email', 'phoneNumber', 'idStruct','password']));

        return response()->json(['message' => 'Agent mis à jour', 'agent' => $agent]);
    }

    //  Supprimer un agent
    public function destroy($id)
    {
        $actor = Auth::user();
        $agent = StructAgent::findOrFail($id);

        if (!in_array((int) $actor->role, [0, 2], true)) {
            return response()->json(['message' => 'Permission insuffisante pour supprimer un agent.'], 403);
        }
        if ((int) $actor->role === 0 && (int) $actor->idStruct !== (int) $agent->idStruct) {
            return response()->json(['message' => "Vous n'avez pas accès à cet agent."], 403);
        }

        $agent->delete();

        return response()->json(['message' => 'Agent supprimé']);
    }

    //  Afficher un agent spécifique
    public function show($id)
    {
        $actor = Auth::user();
        $agent = StructAgent::with('structure')->findOrFail($id);

        if ((int) $actor->role !== 2 && (int) $actor->idStruct !== (int) $agent->idStruct) {
            return response()->json(['message' => "Vous n'avez pas accès à cet agent."], 403);
        }

        return response()->json($agent);
    }
    public function countAgentsInStructure($id)
    {
        $count = StructAgent::where('idStruct', $id)->count();
        return response()->json(['count' => $count]);
    }
    // StructAgentController.php

    public function getByStructure($id)
    {
        $agents = StructAgent::where('idStruct', $id)->get();
        return response()->json($agents);
    }
    
}
