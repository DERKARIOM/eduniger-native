<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Student;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\QueryException;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Auth;

class StudentController extends Controller
{
    /**
     * Vérifie que l'utilisateur est authentifié.
     *
     * @return \Illuminate\Http\JsonResponse|null
     */
    private function checkAuth()
    {
        if (!Auth::check()) {
            return response()->json(['message' => 'Non authentifié.'], 401);
        }
        return null;
    }

    /**
     * Récupère la liste de tous les étudiants.
     *
     * @return \Illuminate\Http\JsonResponse
     */
    public function index()
    {
        //$authError = $this->checkAuth();
       // if ($authError) return $authError;

        try {
            $students = Student::all();
            return response()->json($students, 200);
        } catch (\Exception $e) {
            Log::error('Erreur lors de la récupération des étudiants : ' . $e->getMessage());
            return response()->json(['message' => 'Erreur lors de la récupération des étudiants.'], 500);
        }
    }

    /**
     * Affiche les détails d'un étudiant spécifique.
     *
     * @param string $idNumber
     * @return \Illuminate\Http\JsonResponse
     */
    public function show($idNumber)
    {
        //$authError = $this->checkAuth();
        //if ($authError) return $authError;

        try {
            $student = Student::find($idNumber);
            if (!$student) {
                return response()->json(['message' => 'Étudiant non trouvé.'], 404);
            }
            return response()->json($student, 200);
        } catch (\Exception $e) {
            Log::error('Erreur lors de la récupération de l\'étudiant : ' . $e->getMessage());
            return response()->json(['message' => 'Erreur lors de la récupération de l\'étudiant.'], 500);
        }
    }

    /**
     * Crée un nouvel étudiant.
     *
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function store(Request $request)
    {
        //$authError = $this->checkAuth();
        //if ($authError) return $authError;

        $validator = Validator::make($request->all(), [
            'idNumber'   => 'required|string|max:256|unique:Student,idNumber',
            'name'       => 'required|string|max:256',
            'firstName'  => 'required|string|max:256',
            'section'    => 'nullable|string|max:256',
            'department' => 'nullable|string|max:256',
            'isDelegue'  => 'required|boolean',
        ], [
            'idNumber.required'  => 'L’identifiant de l’étudiant est obligatoire.',
            'idNumber.unique'    => 'Cet identifiant d’étudiant existe déjà.',
            'name.required'      => 'Le nom est obligatoire.',
            'firstName.required' => 'Le prénom est obligatoire.',
            'section.max'        => 'La section ne peut dépasser 256 caractères.',
            'department.max'     => 'Le département ne peut dépasser 256 caractères.',
            'isDelegue.required' => 'Le statut délégué est requis.',
            'isDelegue.boolean'  => 'Le statut délégué doit être vrai ou faux.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $data = [
                'idNumber'   => trim($request->input('idNumber')),
                'name'       => trim($request->input('name')),
                'firstName'  => trim($request->input('firstName')),
                'section'    => $request->filled('section') ? trim($request->input('section')) : null,
                'department' => $request->filled('department') ? trim($request->input('department')) : null,
                'isDelegue'  => (bool)$request->input('isDelegue'),
            ];

            $student = Student::create($data);

            DB::commit();
            return response()->json([
                'message' => 'Étudiant créé avec succès.',
                'data'    => $student,
            ], 201);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la création de l\'étudiant : ' . $qe->getMessage());
            return response()->json(['message' => 'Erreur de base de données lors de la création de l’étudiant.'], 500);
        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la création de l\'étudiant : ' . $e->getMessage());
            return response()->json(['message' => 'Erreur inattendue lors de la création de l’étudiant.'], 500);
        }
    }

    /**
     * Met à jour les informations d'un étudiant existant.
     *
     * @param Request $request
     * @param string $idNumber
     * @return \Illuminate\Http\JsonResponse
     */
    public function update(Request $request, $idNumber)
    {
        //$authError = $this->checkAuth();
        //if ($authError) return $authError;

        $student = Student::find($idNumber);
        if (!$student) {
            return response()->json(['message' => 'Étudiant non trouvé.'], 404);
        }

        $validator = Validator::make($request->all(), [
            'name'       => 'sometimes|required|string|max:256',
            'firstName'  => 'sometimes|required|string|max:256',
            'section'    => 'nullable|string|max:256',
            'department' => 'nullable|string|max:256',
            'isDelegue'  => 'sometimes|required|boolean',
        ], [
            'name.required'      => 'Le nom est obligatoire si fourni.',
            'firstName.required' => 'Le prénom est obligatoire si fourni.',
            'section.max'        => 'La section ne peut dépasser 256 caractères.',
            'department.max'     => 'Le département ne peut dépasser 256 caractères.',
            'isDelegue.required' => 'Le statut délégué est requis si fourni.',
            'isDelegue.boolean'  => 'Le statut délégué doit être vrai ou faux.',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        DB::beginTransaction();
        try {
            $updatedFields = [];
            if ($request->filled('name')) {
                $updatedFields['name'] = trim($request->input('name'));
            }
            if ($request->filled('firstName')) {
                $updatedFields['firstName'] = trim($request->input('firstName'));
            }
            if ($request->has('isDelegue')) {
                $updatedFields['isDelegue'] = (bool)$request->input('isDelegue');
            }
            if ($request->has('section')) {
                $updatedFields['section'] = $request->filled('section') ? trim($request->input('section')) : null;
            }
            if ($request->has('department')) {
                $updatedFields['department'] = $request->filled('department') ? trim($request->input('department')) : null;
            }

            if (!empty($updatedFields)) {
                $student->update($updatedFields);
            }

            DB::commit();
            return response()->json([
                'message' => 'Étudiant mis à jour avec succès.',
                'data'    => $student->fresh(),
            ], 200);
        } catch (QueryException $qe) {
            DB::rollBack();
            Log::error('Erreur base de données lors de la mise à jour de l\'étudiant : ' . $qe->getMessage());
            return response()->json(['message' => 'Erreur de base de données lors de la mise à jour de l’étudiant.'], 500);
        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Erreur inattendue lors de la mise à jour de l\'étudiant : ' . $e->getMessage());
            return response()->json(['message' => 'Erreur inattendue lors de la mise à jour de l’étudiant.'], 500);
        }
    }

    /**
     * Supprime un étudiant.
     *
     * @param string $idNumber
     * @return \Illuminate\Http\JsonResponse
     */
    public function destroy($idNumber)
    {
        //$authError = $this->checkAuth();
        //if ($authError) return $authError;

        $student = Student::find($idNumber);
        if (!$student) {
            return response()->json(['message' => 'Étudiant non trouvé.'], 404);
        }

        DB::beginTransaction();
        try {
            $student->delete();
            DB::commit();
            return response()->json(['message' => 'Étudiant supprimé avec succès.'], 200);
        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Erreur lors de la suppression de l\'étudiant : ' . $e->getMessage());
            return response()->json(['message' => 'Erreur lors de la suppression de l’étudiant.'], 500);
        }
    }
}