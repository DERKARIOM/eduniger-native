<?php
/**
 * Eduna — assistant IA d'Elim. Version Hostinger, 100 % PHP.
 *
 * Remplace l'ancien service hébergé chez Hetzner (78.46.46.154), qui lançait des
 * scripts Python via shell_exec : cette fonction est interdite sur l'hébergement
 * mutualisé Hostinger. Le modèle est maintenant appelé directement en HTTPS
 * (API Google Gemini).
 *
 * Contrat inchangé pour l'application (ChatAiActivity) :
 *   POST  id_number|id_user, request|message|query, [id_book|book_id], [action]
 *   200 → {"success":true,"response":"…","session_id":"…","message_count":N[,"book","pages_analyzed"]}
 *   4xx/5xx → {"success":false,"error":"…"}
 *
 * Configuration (fichier privé ~/private/elim-config.php, chargé par connectBDD.php) :
 *   'gemini_api_key' => 'clé Google AI Studio'
 * Mémoire : tables EdunaSession / EdunaConversation (déjà présentes dans la base).
 */
header('Content-Type: application/json; charset=UTF-8');
header('X-Content-Type-Options: nosniff');

if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
    http_response_code(204);
    exit;
}

require_once __DIR__ . '/../connectBDD.php';

const EDUNA_HISTORY_LIMIT   = 10;    // échanges gardés en mémoire dans une session
const EDUNA_SESSION_HOURS   = 24;    // durée de vie d'une session sans activité
const EDUNA_MAX_MESSAGE     = 4000;  // caractères max d'une question
const EDUNA_PDF_PAGES       = 15;    // pages lues dans un livre
const EDUNA_PDF_CHARS       = 4000;  // caractères du livre envoyés au modèle
const EDUNA_RATE_LIMIT      = 30;    // messages max par utilisateur…
const EDUNA_RATE_WINDOW_MIN = 10;    // …sur cette fenêtre (minutes)
const EDUNA_MODELS          = ['gemini-2.5-flash', 'gemini-2.0-flash'];

function edunaFail(int $status, string $message): never
{
    http_response_code($status);
    echo json_encode(['success' => false, 'error' => $message], JSON_UNESCAPED_UNICODE);
    exit;
}

/** Racine du compte (/home/<utilisateur>), pour retrouver les fichiers de l'API Laravel. */
function edunaHome(): string
{
    return preg_match('#^(/home/[^/]+)#', __DIR__, $m) ? $m[1] : dirname(__DIR__, 5);
}

// ==================== SESSION ET MÉMOIRE ====================

function edunaSession(PDO $pdo, string $idUser): array
{
    $pdo->prepare('DELETE FROM EdunaSession WHERE last_interaction < DATE_SUB(NOW(), INTERVAL ' . EDUNA_SESSION_HOURS . ' HOUR)')
        ->execute();

    $stmt = $pdo->prepare('SELECT session_id, message_count FROM EdunaSession WHERE idUser = ? ORDER BY last_interaction DESC LIMIT 1');
    $stmt->execute([$idUser]);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($session) {
        $pdo->prepare('UPDATE EdunaSession SET message_count = message_count + 1 WHERE idUser = ? AND session_id = ?')
            ->execute([$idUser, $session['session_id']]);
        return ['session_id' => $session['session_id'], 'message_count' => (int) $session['message_count'] + 1];
    }

    $sessionId = uniqid($idUser . '_', true);
    $pdo->prepare('INSERT INTO EdunaSession (idUser, session_id, message_count) VALUES (?, ?, 1)')
        ->execute([$idUser, $sessionId]);
    return ['session_id' => $sessionId, 'message_count' => 1];
}

function edunaHistory(PDO $pdo, string $idUser, string $sessionId): array
{
    $stmt = $pdo->prepare('SELECT user_message, assistant_response FROM EdunaConversation
                           WHERE idUser = ? AND session_id = ? ORDER BY created_at DESC LIMIT ' . EDUNA_HISTORY_LIMIT);
    $stmt->execute([$idUser, $sessionId]);
    return array_reverse($stmt->fetchAll(PDO::FETCH_ASSOC));
}

function edunaTooManyRequests(PDO $pdo, string $idUser): bool
{
    $stmt = $pdo->prepare('SELECT COUNT(*) FROM EdunaConversation
                           WHERE idUser = ? AND created_at > DATE_SUB(NOW(), INTERVAL ' . EDUNA_RATE_WINDOW_MIN . ' MINUTE)');
    $stmt->execute([$idUser]);
    return (int) $stmt->fetchColumn() >= EDUNA_RATE_LIMIT;
}

// ==================== LIVRE (PDF) ====================

/**
 * Texte du début d'un livre PDF, mis en cache sur disque : l'extraction n'est faite
 * qu'une fois par livre. Lecture avec smalot/pdfparser, déjà installé par l'API Laravel.
 */
function edunaBookText(PDO $pdo, string $idBook): ?array
{
    $stmt = $pdo->prepare('SELECT b.title, b.electronic, sb.idStruct FROM Book b
                           LEFT JOIN StructBook sb ON sb.idBook = b.idBook WHERE b.idBook = ? LIMIT 1');
    $stmt->execute([$idBook]);
    $book = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$book || empty($book['electronic']) || $book['electronic'] === 'null' || empty($book['idStruct'])) {
        return null;
    }

    $laravel  = edunaHome() . '/elim-api';
    $pdfPath  = $laravel . '/storage/app/private/structures/' . (int) $book['idStruct'] . '/pdfs/' . basename($book['electronic']);
    $cacheDir = $laravel . '/storage/app/private/ai-cache';
    $cache    = $cacheDir . '/book_' . sha1($idBook . '|' . $book['electronic']) . '.json';

    if (is_readable($cache)) {
        $cached = json_decode((string) file_get_contents($cache), true);
        if (is_array($cached)) {
            return ['title' => $book['title']] + $cached;
        }
    }
    if (!is_readable($pdfPath) || !is_readable($laravel . '/vendor/autoload.php')) {
        return ['title' => $book['title'], 'text' => '', 'pages' => 0];
    }

    $text = '';
    $pages = 0;
    try {
        require_once $laravel . '/vendor/autoload.php';
        $pdf = (new \Smalot\PdfParser\Parser())->parseFile($pdfPath);
        foreach (array_slice($pdf->getPages(), 0, EDUNA_PDF_PAGES) as $page) {
            $text .= $page->getText() . "\n";
            $pages++;
            if (mb_strlen($text) > EDUNA_PDF_CHARS * 2) {
                break;
            }
        }
    } catch (\Throwable $e) {
        error_log('eduna: lecture PDF impossible (' . $idBook . ') : ' . $e->getMessage());
    }
    $text = trim(preg_replace('/[ \t]+/', ' ', $text));
    $result = ['text' => mb_substr($text, 0, EDUNA_PDF_CHARS), 'pages' => $pages];

    if (!is_dir($cacheDir)) {
        @mkdir($cacheDir, 0775, true);
    }
    @file_put_contents($cache, json_encode($result, JSON_UNESCAPED_UNICODE));
    return ['title' => $book['title']] + $result;
}

// ==================== APPEL AU MODÈLE ====================

function edunaAsk(string $system, array $history, string $message): string
{
    $apiKey = getenv('GEMINI_API_KEY') ?: '';
    if ($apiKey === '') {
        error_log('eduna: gemini_api_key absente de ~/private/elim-config.php');
        edunaFail(503, "L'assistant n'est pas encore configuré. Réessayez plus tard.");
    }

    $contents = [];
    foreach ($history as $turn) {
        $contents[] = ['role' => 'user',  'parts' => [['text' => $turn['user_message']]]];
        $contents[] = ['role' => 'model', 'parts' => [['text' => $turn['assistant_response']]]];
    }
    $contents[] = ['role' => 'user', 'parts' => [['text' => $message]]];

    $payload = json_encode([
        'systemInstruction' => ['parts' => [['text' => $system]]],
        'contents'          => $contents,
        'generationConfig'  => ['temperature' => 0.7, 'maxOutputTokens' => 1500],
    ], JSON_UNESCAPED_UNICODE);

    foreach (EDUNA_MODELS as $model) {
        $ch = curl_init('https://generativelanguage.googleapis.com/v1beta/models/' . $model . ':generateContent');
        curl_setopt_array($ch, [
            CURLOPT_POST           => true,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_HTTPHEADER     => ['Content-Type: application/json', 'x-goog-api-key: ' . $apiKey],
            CURLOPT_POSTFIELDS     => $payload,
            CURLOPT_CONNECTTIMEOUT => 10,
            CURLOPT_TIMEOUT        => 45,
        ]);
        $raw  = curl_exec($ch);
        $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        $data = json_decode((string) $raw, true);
        $text = '';
        foreach (($data['candidates'][0]['content']['parts'] ?? []) as $part) {
            $text .= $part['text'] ?? '';
        }
        if ($code === 200 && trim($text) !== '') {
            return trim($text);
        }
        error_log("eduna: modèle $model indisponible (HTTP $code) " . substr((string) $raw, 0, 300));
    }
    edunaFail(502, "L'assistant est momentanément indisponible. Réessayez dans un instant.");
}

// ==================== TRAITEMENT ====================

if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
    edunaFail(405, 'Méthode non autorisée');
}

$idUser  = trim((string) ($_POST['id_user'] ?? $_POST['id_number'] ?? ''));
$message = trim((string) ($_POST['message'] ?? $_POST['request'] ?? $_POST['query'] ?? ''));
$idBook  = trim((string) ($_POST['id_book'] ?? $_POST['book_id'] ?? ''));

if ($idUser === '' || mb_strlen($idUser) > 64) {
    edunaFail(400, 'id_user requis');
}
if ($message === '') {
    edunaFail(400, 'Message requis');
}
if (mb_strlen($message) > EDUNA_MAX_MESSAGE) {
    edunaFail(413, 'Message trop long (' . EDUNA_MAX_MESSAGE . ' caractères maximum)');
}

try {
    if (edunaTooManyRequests($pdo, $idUser)) {
        edunaFail(429, 'Trop de messages en peu de temps. Patientez quelques minutes.');
    }

    $session = edunaSession($pdo, $idUser);
    $history = edunaHistory($pdo, $idUser, $session['session_id']);

    $system = "Tu es Eduna, l'assistant d'apprentissage de l'application Elim, une bibliothèque numérique "
            . "intelligente pour les élèves, étudiants et enseignants du Niger. "
            . "Réponds en français par défaut (ou dans la langue de l'utilisateur), de façon claire, "
            . "pédagogique et bienveillante. Explique les notions pas à pas, propose des exemples et, "
            . "si utile, de courtes questions pour vérifier la compréhension. "
            . "Si tu ne connais pas la réponse, dis-le honnêtement.";

    $book = null;
    if ($idBook !== '') {
        $book = edunaBookText($pdo, $idBook);
        if ($book && $book['text'] !== '') {
            $system .= "\n\nL'utilisateur pose une question sur le livre « " . $book['title'] . " ». "
                     . "Voici le début de son contenu ({$book['pages']} pages lues). Appuie-toi dessus :\n"
                     . $book['text'];
        } elseif ($book) {
            $system .= "\n\nL'utilisateur parle du livre « " . $book['title'] . " » (contenu non disponible).";
        }
    }

    $answer = edunaAsk($system, $history, $message);
    $bookContext = $book ? mb_substr('Livre: ' . $book['title'], 0, 500) : null;

    $pdo->prepare('INSERT INTO EdunaConversation (idUser, session_id, user_message, assistant_response, book_context) VALUES (?, ?, ?, ?, ?)')
        ->execute([$idUser, $session['session_id'], $message, $answer, $bookContext]);
    $pdo->prepare('UPDATE EdunaSession SET context = ? WHERE idUser = ? AND session_id = ?')
        ->execute([mb_substr('Dernier sujet: ' . ($bookContext ?? $message), 0, 500), $idUser, $session['session_id']]);

    $response = [
        'success'       => true,
        'response'      => $answer,
        'session_id'    => $session['session_id'],
        'message_count' => $session['message_count'],
    ];
    if ($book) {
        $response['book'] = $book['title'];
        $response['pages_analyzed'] = $book['pages'];
    }
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
} catch (\Throwable $e) {
    error_log('eduna: ' . $e->getMessage());
    edunaFail(500, 'Erreur interne du serveur');
}
