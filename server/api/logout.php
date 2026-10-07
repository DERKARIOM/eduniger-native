<?php
require_once("../connectBDD.php");
require_once(__DIR__ . "/auth/TokenStore.php");

/**
 * Déconnexion : révoque le refresh token côté serveur (le retire de la table
 * RefreshToken, ou plus précisément le marque révoqué), de sorte qu'il ne puisse plus
 * jamais être utilisé pour obtenir un nouvel access token, même s'il n'a pas encore
 * expiré. Le client doit, de son côté, supprimer les tokens de son stockage sécurisé.
 *
 * N'exige pas d'Authorization Bearer : au moment du logout, l'access token peut déjà
 * être expiré, et le seul élément nécessaire pour révoquer la session est le refresh
 * token lui-même (le connaître prouve qu'on est bien le détenteur de cette session).
 * Un refresh_token absent, déjà invalide ou déjà révoqué est traité comme un succès
 * silencieux (idempotent) : dans tous les cas, l'objectif du client ("ne plus être
 * connecté avec ce token") est déjà atteint.
 */
header('Content-Type: application/json; charset=UTF-8');

if (!empty($_POST['refresh_token'])) {
    try {
        (new TokenStore($pdo))->revokeByRawToken(trim($_POST['refresh_token']));
    } catch (Exception $e) {
        error_log($e->getMessage());
        // On ne bloque pas le logout côté client pour une erreur de révocation serveur.
    }
}

echo json_encode(['success' => true]);
