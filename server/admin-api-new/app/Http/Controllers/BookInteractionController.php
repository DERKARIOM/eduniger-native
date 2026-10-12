<?php

namespace App\Http\Controllers;

use App\Models\Book;
use App\Models\BookLike;
use App\Models\BookNoLike;
use App\Models\BookSubscription;
use App\Models\BookView;
use App\Models\Comment;
use Exception;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Validator;

/**
 * Class BookInteractionController
 *
 * Remplace les anciens endpoints legacy IsLike.php / insert_like.php,
 * IsNoLike.php / insert_no_like.php, IsSubscribeBook.php / insert_subscribe_book.php,
 * insert_view.php, ReceiveComments.php / SendComments.php.
 *
 * Chaque bascule (like/dislike/subscribe) est indépendante des autres, à l'identique
 * du comportement historique (aucune exclusion mutuelle côté serveur). Les compteurs
 * dénormalisés sur Book (numberLike, numberNoLike, numberSubscribe, numberView,
 * numberComment) sont maintenus ici de façon atomique.
 */
class BookInteractionController extends Controller
{
    private function findBookOrFail(string $idBook): ?Book
    {
        return Book::find($idBook);
    }

    // ==================== Like ====================

    public function likeStatus(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;

        $liked = BookLike::where('idNumber', $idNumber)
            ->where('idBook', $idBook)
            ->exists();

        return response()->json(['liked' => $liked], 200);
    }

    public function toggleLike(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;
        $book = $this->findBookOrFail($idBook);

        if (!$book) {
            return response()->json(['message' => 'Livre introuvable.'], 404);
        }

        try {
            $result = DB::transaction(function () use ($idNumber, $idBook, $book) {
                $existing = BookLike::where('idNumber', $idNumber)->where('idBook', $idBook)->first();

                if ($existing) {
                    $existing->delete();
                    $book->numberLike = max(0, (int) $book->numberLike - 1);
                    $liked = false;
                } else {
                    BookLike::create(['idNumber' => $idNumber, 'idBook' => $idBook, 'date' => now()]);
                    $book->numberLike = (int) $book->numberLike + 1;
                    $liked = true;
                }
                $book->save();

                return ['liked' => $liked, 'numberLike' => $book->numberLike, 'numberNoLike' => $book->numberNoLike];
            });

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du like du livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    // ==================== Dislike ====================

    public function dislikeStatus(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;

        $disliked = BookNoLike::where('idNumber', $idNumber)
            ->where('idBook', $idBook)
            ->exists();

        return response()->json(['disliked' => $disliked], 200);
    }

    public function toggleDislike(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;
        $book = $this->findBookOrFail($idBook);

        if (!$book) {
            return response()->json(['message' => 'Livre introuvable.'], 404);
        }

        try {
            $result = DB::transaction(function () use ($idNumber, $idBook, $book) {
                $existing = BookNoLike::where('idNumber', $idNumber)->where('idBook', $idBook)->first();

                if ($existing) {
                    $existing->delete();
                    $book->numberNoLike = max(0, (int) $book->numberNoLike - 1);
                    $disliked = false;
                } else {
                    BookNoLike::create(['idNumber' => $idNumber, 'idBook' => $idBook, 'date' => now()]);
                    $book->numberNoLike = (int) $book->numberNoLike + 1;
                    $disliked = true;
                }
                $book->save();

                return ['disliked' => $disliked, 'numberLike' => $book->numberLike, 'numberNoLike' => $book->numberNoLike];
            });

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors du dislike du livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    // ==================== Subscription (favoris) ====================

    public function subscriptionStatus(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;

        $subscribed = BookSubscription::where('idNumber', $idNumber)
            ->where('idBook', $idBook)
            ->exists();

        return response()->json(['subscribed' => $subscribed], 200);
    }

    public function toggleSubscription(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;
        $book = $this->findBookOrFail($idBook);

        if (!$book) {
            return response()->json(['message' => 'Livre introuvable.'], 404);
        }

        try {
            $result = DB::transaction(function () use ($idNumber, $idBook, $book) {
                $existing = BookSubscription::where('idNumber', $idNumber)->where('idBook', $idBook)->first();

                if ($existing) {
                    $existing->delete();
                    $book->numberSubscribe = max(0, (int) $book->numberSubscribe - 1);
                    $subscribed = false;
                } else {
                    BookSubscription::create(['idNumber' => $idNumber, 'idBook' => $idBook, 'date' => now()]);
                    $book->numberSubscribe = (int) $book->numberSubscribe + 1;
                    $subscribed = true;
                }
                $book->save();

                return ['subscribed' => $subscribed, 'numberSubscribe' => $book->numberSubscribe];
            });

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'abonnement au livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    // ==================== Vues ====================

    /**
     * Enregistre une vue pour l'utilisateur courant. Idempotent : une seule vue
     * comptabilisée par couple (idNumber, idBook), comme l'ancien système.
     */
    public function recordView(Request $request, string $idBook)
    {
        $idNumber = $request->user()->idUser;
        $book = $this->findBookOrFail($idBook);

        if (!$book) {
            return response()->json(['message' => 'Livre introuvable.'], 404);
        }

        try {
            $result = DB::transaction(function () use ($idNumber, $idBook, $book) {
                $alreadyViewed = BookView::where('idNumber', $idNumber)->where('idBook', $idBook)->exists();

                if (!$alreadyViewed) {
                    BookView::create(['idNumber' => $idNumber, 'idBook' => $idBook, 'date' => now()]);
                    $book->numberView = (int) $book->numberView + 1;
                    $book->save();
                }

                return ['recorded' => !$alreadyViewed, 'numberView' => $book->numberView];
            });

            return response()->json($result, 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'enregistrement de la vue du livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }

    // ==================== Commentaires ====================

    public function getComments(string $idBook)
    {
        try {
            $comments = Comment::with('user')
                ->where('idBook', $idBook)
                ->orderBy('idComment')
                ->get()
                ->map(function (Comment $comment) {
                    return [
                        'idComment' => $comment->idComment,
                        'idUser'    => $comment->idNumber,
                        'name'      => optional($comment->user)->name ?? '',
                        'firstName' => optional($comment->user)->firstName ?? '',
                        'message'   => $comment->message,
                        'date'      => $comment->date,
                    ];
                });

            return response()->json(['data' => $comments], 200);
        } catch (Exception $e) {
            Log::error('Erreur lors de la récupération des commentaires du livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Impossible de récupérer les commentaires.'], 500);
        }
    }

    public function postComment(Request $request, string $idBook)
    {
        $validator = Validator::make($request->all(), [
            'message' => 'required|string|max:1000',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'message' => $validator->errors()->first(),
                'errors'  => $validator->errors(),
            ], 422);
        }

        $idNumber = $request->user()->idUser;
        $book = $this->findBookOrFail($idBook);

        if (!$book) {
            return response()->json(['message' => 'Livre introuvable.'], 404);
        }

        try {
            $comment = DB::transaction(function () use ($idNumber, $idBook, $request, $book) {
                $comment = Comment::create([
                    'idNumber' => $idNumber,
                    'idBook'   => $idBook,
                    'date'     => now(),
                    'message'  => $request->input('message'),
                ]);
                $book->numberComment = (int) $book->numberComment + 1;
                $book->save();

                return $comment;
            });

            return response()->json([
                'message' => 'Commentaire ajouté.',
                'data'    => $comment,
            ], 201);
        } catch (Exception $e) {
            Log::error('Erreur lors de l\'ajout d\'un commentaire au livre ' . $idBook . ' : ' . $e->getMessage());
            return response()->json(['message' => 'Une erreur est survenue.'], 500);
        }
    }
}
