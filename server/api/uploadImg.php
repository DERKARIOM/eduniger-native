<?php
// SECURITE : cet endpoint acceptait auparavant N'IMPORTE QUEL fichier (y compris un script PHP
// deguise en image) sous son nom d'origine, sans verification ni limite de taille — une faille
// critique d'upload arbitraire pouvant mener a une execution de code sur le serveur. Corrections
// apportees : verification que le fichier est reellement une image, whitelist d'extensions,
// nom de fichier genere aleatoirement cote serveur (le nom d'origine n'est jamais reutilise,
// ce qui empeche aussi tout "path traversal"), et limite de taille.

header('Content-Type: application/json; charset=UTF-8');

const UPLOAD_DIR = "/var/www/html/fabi/ressources/profile/";
const MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 Mo
const ALLOWED_EXTENSIONS = [
    IMAGETYPE_JPEG => 'jpg',
    IMAGETYPE_PNG  => 'png',
    IMAGETYPE_WEBP => 'webp',
];

if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Aucun fichier image valide reçu']);
    exit;
}

$fileTmp = $_FILES['image']['tmp_name'];
$fileSize = $_FILES['image']['size'];

if ($fileSize <= 0 || $fileSize > MAX_SIZE_BYTES) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Taille de fichier invalide (max 5 Mo)']);
    exit;
}

// Verifie que le contenu est reellement une image (empeche l'upload d'un script deguise en image)
$imageInfo = @getimagesize($fileTmp);
if ($imageInfo === false || !isset(ALLOWED_EXTENSIONS[$imageInfo[2]])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'error' => 'Le fichier envoyé n\'est pas une image valide (jpg, png ou webp)']);
    exit;
}

$extension = ALLOWED_EXTENSIONS[$imageInfo[2]];
// Nom de fichier genere aleatoirement cote serveur : le nom fourni par le client n'est
// jamais utilise directement (protection contre le path traversal et l'ecrasement de fichiers).
$fileName = bin2hex(random_bytes(16)) . '.' . $extension;
$destination = UPLOAD_DIR . $fileName;

if (!move_uploaded_file($fileTmp, $destination)) {
    http_response_code(500);
    echo json_encode(['success' => false, 'error' => "Échec de l'enregistrement du fichier"]);
    exit;
}

echo json_encode(['success' => true, 'file_name' => $fileName]);
