<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Vérifier si le paramètre password est présent
if (!isset($_GET['password'])) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => 'Le paramètre "password" est requis',
        'message' => 'Utilisation: ?password=votre_mot_de_passe'
    ]);
    exit;
}

$password = $_GET['password'];

// Vérifier que le mot de passe n'est pas vide
if (empty($password)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'error' => 'Le mot de passe ne peut pas être vide'
    ]);
    exit;
}

// Hasher le mot de passe avec SHA-256 (comme dans le code Java)
$hashedPassword = hash('sha256', $password);

// Retourner la réponse en JSON
echo json_encode([
    'success' => true,
    'original' => $password,
    'hashed' => $hashedPassword,
    'algorithm' => 'SHA-256'
], JSON_PRETTY_PRINT);
?>