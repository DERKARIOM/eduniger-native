<?php
use Illuminate\Support\Facades\Route;
use Illuminate\Http\Request;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\BookController;
use App\Http\Controllers\InvitationController;
use App\Http\Controllers\StructAgentController;
use App\Http\Controllers\StructureController;
use App\Models\Structure;
use App\Models\StructBook;
use App\Models\StructAgent;
use App\Http\Controllers\AuthorController;
use App\Http\Controllers\CategoryController;
use App\Http\Controllers\StudentController;
use App\Http\Controllers\BookAssociationController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\UserAuthController;
use App\Http\Controllers\LoandController;
use App\Http\Controllers\FileController;
use App\Http\Controllers\CsrfCookieController;
use App\Http\Controllers\StorageController;
use App\Http\Controllers\StructureBackupController;
use App\Http\Controllers\ReservationController;
use App\Http\Controllers\StructUserController;
use App\Http\Controllers\StructureNotificationController;
use App\Http\Controllers\AIBookController;
use App\Http\Controllers\BookInteractionController;
use App\Http\Controllers\HelpRequestController;
use Illuminate\Support\Facades\Broadcast;


Broadcast::routes(['prefix' => 'api', 'middleware' => ['auth:sanctum']]);


Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);
Route::post('/user/login', [UserAuthController::class, 'login']);
Route::post('/user/register', [UserAuthController::class, 'register']);

// Route PUBLIQUE (aucune authentification) : sert couvertures, PDF, audio et photos
// d'auteur pour les LECTEURS (catalogue public Web + application mobile), qui n'ont
// pas de compte agent et ne peuvent donc pas utiliser /secure. Verifie que le fichier
// correspond a un enregistrement reel (livre/auteur) plutot que de se reposer sur une
// authentification pour limiter l'acces - voir FileController::publicShow().
Route::get('/public/resource/{idStruct}/{type}/{filename}', [FileController::class, 'publicShow'])
    ->where('filename', '.*')
    ->where('idStruct', '[0-9]+');






Route::middleware('auth:sanctum')->group(function () {
    Route::get('/me', fn(Request $request) => $request->user());
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::post('/generate-invitation', [InvitationController::class, 'generate']);
});
Route::middleware('auth:sanctum')->group(function () {

    // User profile routes
    Route::get('/user/profile', [UserAuthController::class, 'me']);
    Route::put('/user/profile', [UserAuthController::class, 'updateProfile']);
    Route::post('/user/logout', [UserAuthController::class, 'logout']);

    // Invitation routes
    Route::prefix('agents')->group(function () {
                Route::post('/', [StructAgentController::class, 'store']);
                Route::get('/{id}', [StructAgentController::class, 'show']);
                Route::put('/{id}', [StructAgentController::class, 'update']);
                Route::delete('/{id}', [StructAgentController::class, 'destroy']);
                Route::get('/', [StructAgentController::class, 'index']);
    });

    // Superadmin routes
    Route::prefix('superadmin')->middleware('role:2')->group(function () {
        Route::put('/update-password', [AuthController::class, 'updatePassword']);
        Route::put('/me', [AuthController::class, 'updateProfile']);
    });
    
    // Structure routes
    Route::prefix('structures')->group(function () {
        Route::get('/', [StructureController::class, 'index']);
        Route::post('/', [StructureController::class, 'store'])->middleware('role:2');
        // Routes litterales AVANT le wildcard '/{id}' ci-dessous : sinon Laravel
        // matcherait '/structures/mine' en prenant "mine" comme valeur de {id}.
        Route::get('/mine', [StructureController::class, 'mine']);
        Route::get('/discover', [StructureController::class, 'discover']);
        Route::delete('/{id}', [StructureController::class, 'destroy'])->middleware('role:2');
        Route::put('/{id}', [StructureController::class, 'update'])->middleware(['role:0,2', 'structure.scope:id']);
        Route::get('/{id}', [StructureController::class, 'show']);
        // Adhesion/retrait en libre-service (n importe quel lecteur authentifie,
        // pas seulement les agents/admins de la structure -> pas de structure.scope ici).
        Route::post('/{id}/join', [StructureController::class, 'join']);
        Route::post('/{id}/leave', [StructureController::class, 'leave']);
    });
    Route::prefix('structure')->group(function () {
        Route::get('/{id}/books', [BookController::class, 'getBooksByStructure']);
        Route::get('/{id}/countbooks', [StructureController::class, 'countBooksInStructure']);
        Route::get('{id}/countagents', [StructAgentController::class, 'countAgentsInStructure']);
        // Corrige 2 bugs trouves lors de l'audit : (1) cette route pointait vers
        // StructAgentController::getAgentsByStructure, methode qui n'existe pas sur ce
        // controleur (elle est definie sur StructureController) -> l'appel plantait
        // systematiquement (erreur "method does not exist"). (2) une route dupliquee
        // identique juste en dessous (vers StructAgentController::getByStructure, qui,
        // elle, existe) etait donc totalement inatteignable (Laravel ne retient que la
        // premiere route enregistree pour une meme URI). On ne garde qu'une seule route
        // fonctionnelle, et on la protege : elle expose la liste des agents (nom, email,
        // telephone, invitationCode) d'une structure, ce qui doit rester reserve aux
        // agents de cette structure (ou au Super Admin).
        Route::get('/{id}/agents', [StructAgentController::class, 'getByStructure'])->middleware('structure.scope:id');
    });
    // Dans le groupe middleware auth:sanctum
    Route::prefix('structure/{idStruct}')->middleware('structure.scope:idStruct')->group(function () {
        // Utilisateurs de la structure
        Route::get('/users', [StructUserController::class, 'getUsersByStructure']);
        Route::get('/users/search', [StructUserController::class, 'searchUsers']);
        Route::get('/users/{idUser}', [StructUserController::class, 'checkUser']);
        Route::post('/users', [StructUserController::class, 'addUser']);
        Route::put('/users/{idUser}', [StructUserController::class, 'updateUserRole']);
        Route::delete('/users/{idUser}', [StructUserController::class, 'removeUser']);
        Route::get('/users/available', [StructUserController::class, 'getAvailableUsers']);
        Route::get('/users/count', [StructUserController::class, 'countUsersByStructure']);
    });

    // Book routes
    Route::prefix('books/{idStruct}')->middleware('structure.scope:idStruct')->group(function () {
        Route::get('/', [BookController::class, 'index']);
        Route::post('/', [BookController::class, 'store']);
        Route::get('/{id}', [BookController::class, 'show']);
        Route::put('/{id}', [BookController::class, 'update']);
        Route::delete('/{id}', [BookController::class, 'destroy']);
    })->where('idStruct', '[0-9]+');

    Route::get('/book/associations', [BookAssociationController::class, 'getBooksWithAssociations']);
    Route::get('/book/{idBook}/details', [BookAssociationController::class, 'getBookDetails']);

    // Interactions livre (like/dislike/abonnement/vues/commentaires)
    Route::prefix('book/{idBook}')->group(function () {
        Route::get('/like', [BookInteractionController::class, 'likeStatus']);
        Route::post('/like', [BookInteractionController::class, 'toggleLike']);
        Route::get('/dislike', [BookInteractionController::class, 'dislikeStatus']);
        Route::post('/dislike', [BookInteractionController::class, 'toggleDislike']);
        Route::get('/subscription', [BookInteractionController::class, 'subscriptionStatus']);
        Route::post('/subscription', [BookInteractionController::class, 'toggleSubscription']);
        Route::post('/view', [BookInteractionController::class, 'recordView']);
        Route::get('/comments', [BookInteractionController::class, 'getComments']);
        Route::post('/comments', [BookInteractionController::class, 'postComment']);
    });
    Route::get('/structures/{id}', [StructureController::class, 'show']);
    Route::prefix('authors')->group(function () {
        Route::get('/{idAuthor}', [AuthorController::class, 'show']);
        // Auteurs similaires (categories communes avec les livres de cet auteur) -
        // cf. audit mobile "Auteurs". Remplace l'ancien AuthorSimular.php.
        Route::get('/{idAuthor}/similar', [AuthorController::class, 'similar']);
        Route::post('/', [AuthorController::class, 'store']);
        Route::post('/{idAuthor}', [AuthorController::class, 'update']); // ou PUT/PATCH
        Route::delete('/{idAuthor}', [AuthorController::class, 'destroy']);
        Route::get('/', [AuthorController::class, 'index']);
    });

    // Category routes
    Route::prefix("categories")->group(function () {
        Route::get('/{idCategory}', [CategoryController::class, 'show']);
        Route::post('/', [CategoryController::class, 'store']);
        Route::put('/{idCategory}', [CategoryController::class, 'update']);
        Route::delete('/{idCategory}', [CategoryController::class, 'destroy']);
        Route::get('/', [CategoryController::class, 'index']);
    });


    // Student routes
    Route::prefix('students')->group(function () {
        Route::get('/{idNumber}', [StudentController::class, 'show']);
        Route::post('/', [StudentController::class, 'store']);
        Route::put('/{idNumber}', [StudentController::class, 'update']);
        Route::delete('/{idNumber}', [StudentController::class, 'destroy']);
        Route::get('/', [StudentController::class, 'index']);
    });

    // User routes
    Route::prefix('users')->group(function () {
        Route::get('/{id}', [UserController::class, 'show']);
        Route::post('/', [UserController::class, 'store']);
        Route::put('/{id}', [UserController::class, 'update']);
        Route::delete('/{id}', [UserController::class, 'destroy']);
        Route::get('/', [UserController::class, 'index']);
        
    });
    Route::get('/user/search', [UserController::class, 'search']);
    Route::get('/users/stats', [StructUserController::class, 'getMemberStats']);
    Route::get('/users/paginated', [StructUserController::class, 'getPaginatedUsers']);
    // Recommandations (accueil mobile) : livres des structures de l'utilisateur
    // authentifie, calcules cote serveur (voir BookController::recommendations).
    Route::get('/recommendations', [BookController::class, 'recommendations']);
    // Loan routes
    Route::prefix('loans')->group(function () {

        Route::get('/', [LoandController::class, 'index']);
        // Routes litterales AVANT le wildcard '/{id}' plus bas : sinon Laravel
        // matcherait '/loans/mine' ou '/loans/unread' en prenant "mine"/"unread"
        // comme valeur de {id}.
        Route::get('/mine', [LoandController::class, 'mine']);
        Route::get('/unread', [LoandController::class, 'unread']);
        Route::post('/{id}/mark-viewed', [LoandController::class, 'markViewed']);
        Route::get('/{id}', [LoandController::class, 'show']);
        Route::post('/', [LoandController::class, 'store']);
        Route::put('/{id}', [LoandController::class, 'update']);
        Route::delete('/{id}', [LoandController::class, 'destroy']);

        // Analytics
        Route::get('/stats/global', [LoandController::class, 'getStats']);
        Route::get('{idStruct}/stats/global', [LoandController::class, 'getStatsById']);

        Route::get('/stats/distribution', [LoandController::class, 'getDistribution']);
        Route::get('/stats/returns', [LoandController::class, 'getReturnDistribution']);
        Route::get('/stats/top-users', [LoandController::class, 'getTopUsers']);
        Route::get('/stats/top-books', [LoandController::class, 'getTopBooks']);
        Route::get('/stats/late-rate', [LoandController::class, 'getLateRate']);
        Route::get('/stats/agents', [LoandController::class, 'getAgentActivity']);
    });

    Route::prefix('reservations')->group(function () {

        Route::get('/', [ReservationController::class, 'index']);
        Route::get('/check', [ReservationController::class, 'checkExisting']);
        Route::post('/cancel', [ReservationController::class, 'cancelByBook']);
        Route::get('/{id}', [ReservationController::class, 'show']);
        Route::post('/', [ReservationController::class, 'store']);
        Route::put('/{id}', [ReservationController::class, 'update']);
        Route::delete('/{id}', [ReservationController::class, 'destroy']);

        Route::post('/expire', [ReservationController::class, 'expireReservations']);

        // Stats
        Route::get('/stats/global', [ReservationController::class, 'getStats']);
        Route::get('/stats/distribution', [ReservationController::class, 'getDistribution']);
        Route::get('/stats/conversion', [ReservationController::class, 'getConversionRate']);
        Route::get('/stats/top-books', [ReservationController::class, 'getTopReservedBooks']);
    });

    Route::get('/secure/{idStruct}/{type}/{filename}', [FileController::class, 'secure'])->where('filename', '.*')->where('idStruct', '[0-9]+')->middleware('structure.scope:idStruct');    // pour accepter les sous-dossiers éventuels
    Route::get('/storage/usage/{idStruct}', [StorageController::class, 'usage'])->where('idStruct', '[0-9]+')->middleware('structure.scope:idStruct');

    // Route pour la sauvergare des donnees de la structure
    Route::prefix('structure/{id}')->middleware('structure.scope:id')->group(function () {
    Route::get('/backups', [StructureBackupController::class, 'index']);
    Route::delete('/backups/{filename}', [StructureBackupController::class, 'destroy'])->where('filename', '.*');
    Route::get('/backups/{filename}/download', [StructureBackupController::class, 'download'])->where('filename', '.*');
    Route::post('/backup', [StructureBackupController::class, 'backup']); // si non existant
    });

    Route::get('/admin/notifications', [StructureNotificationController::class, 'allNotifications']);
    Route::prefix('notifications')->group(function () {
        Route::get('/', [StructureNotificationController::class, 'index']);
        Route::post('/', [StructureNotificationController::class, 'store'])->middleware('role:2');
        Route::put('{id}/read', [StructureNotificationController::class, 'markAsRead']);
        Route::delete('{id}', [StructureNotificationController::class, 'destroy'])->middleware('role:2');
    });
    // Routes pour l'IA 
    Route::post('/ai/extract-book', [AIBookController::class, 'extract']);
    Route::post('/ai/add-book', [AIBookController::class, 'store']);

    Route::apiResource('help-requests', HelpRequestController::class);

});
Route::get('/websiteStructures', [StructureController::class, 'index']);


//->middleware('auth:sanctum') 
    
    





    













