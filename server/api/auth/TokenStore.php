<?php
require_once __DIR__ . '/Jwt.php';
require_once __DIR__ . '/Config.php';

/**
 * Émission, rotation et révocation des tokens d'authentification.
 *
 *  - Access Token : un JWT signé (voir Jwt.php), à durée de vie courte
 *    (AuthConfig::ACCESS_TOKEN_TTL_SECONDS), jamais stocké en base — il se suffit à
 *    lui-même (stateless) et est vérifié uniquement via sa signature. C'est ce token qui
 *    est envoyé dans le header "Authorization: Bearer ..." de chaque appel API protégé.
 *
 *  - Refresh Token : une valeur aléatoire opaque (pas un JWT), à durée de vie longue
 *    (AuthConfig::REFRESH_TOKEN_TTL_SECONDS), dont seul le HASH SHA-256 est stocké en
 *    base (table RefreshToken) — jamais la valeur en clair, pour qu'une fuite de la base
 *    ne suffise pas à voler des sessions. Ce token n'est envoyé QUE lors de l'appel à
 *    refresh.php, jamais aux autres endpoints.
 *
 * Rotation + détection de réutilisation ("refresh token reuse detection") :
 * chaque utilisation d'un refresh token le révoque et en émet un nouveau dans la même
 * "famille" (familyId). Si un refresh token déjà révoqué est présenté à nouveau — signe
 * qu'il a été volé et qu'quelqu'un d'autre a déjà réussi une rotation avec la copie
 * légitime, ou l'inverse — TOUTE la famille est immédiatement révoquée : l'utilisateur
 * légitime sera lui aussi déconnecté à son prochain refresh et devra se reconnecter, ce
 * qui invalide le token volé en même temps.
 */
class TokenStore
{
    private PDO $pdo;

    public function __construct(PDO $pdo)
    {
        $this->pdo = $pdo;
    }

    /**
     * Émet un nouveau couple Access Token + Refresh Token pour un utilisateur, typiquement
     * après un login réussi. Démarre une nouvelle "famille" de refresh tokens.
     *
     * @return array{accessToken: string, refreshToken: string, expiresIn: int}
     */
    public function issueTokenPair(string $idUser, string $role): array
    {
        $familyId = self::generateUuidV4();
        return $this->issueAccessAndRefresh($idUser, $role, $familyId);
    }

    /**
     * Vérifie un refresh token présenté par le client et, s'il est valide, effectue la
     * rotation : révoque l'ancien, en émet un nouveau (même famille), et retourne un
     * nouveau couple de tokens. Retourne null si le refresh token est invalide, expiré,
     * révoqué (et déclenche alors la révocation de toute la famille), ou introuvable.
     *
     * @return array{accessToken: string, refreshToken: string, expiresIn: int}|null
     */
    public function rotate(string $presentedRefreshToken): ?array
    {
        $hash = self::hashToken($presentedRefreshToken);

        $stmt = $this->pdo->prepare(
            'SELECT rt.id, rt.idUser, rt.familyId, rt.expiresAt, rt.revokedAt, u.role, u.locked_until
             FROM RefreshToken rt
             JOIN User u ON u.idUser = rt.idUser
             WHERE rt.tokenHash = :hash'
        );
        $stmt->execute([':hash' => $hash]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$row) {
            // Token totalement inconnu (jamais émis, ou déjà purgé) : refus simple.
            return null;
        }

        if ($row['revokedAt'] !== null) {
            // Réutilisation d'un refresh token déjà consommé : signe probable de vol.
            // On révoque immédiatement toute la famille pour couper court à la session
            // potentiellement compromise, côté attaquant comme côté utilisateur légitime.
            $this->revokeFamily($row['familyId']);
            error_log("Réutilisation détectée d'un refresh token révoqué pour idUser={$row['idUser']} (famille {$row['familyId']}) : famille entière révoquée.");
            return null;
        }

        if (strtotime($row['expiresAt']) < time()) {
            return null;
        }

        // Révoque l'ancien token (rotation) puis émet le nouveau couple dans la même famille.
        $this->revokeSingle((int) $row['id']);

        return $this->issueAccessAndRefresh($row['idUser'], (string) $row['role'], $row['familyId']);
    }

    /**
     * Révoque un refresh token précis (déconnexion d'un seul appareil/session), utilisé
     * lors du logout.
     */
    public function revokeByRawToken(string $presentedRefreshToken): void
    {
        $hash = self::hashToken($presentedRefreshToken);
        $stmt = $this->pdo->prepare(
            'UPDATE RefreshToken SET revokedAt = NOW() WHERE tokenHash = :hash AND revokedAt IS NULL'
        );
        $stmt->execute([':hash' => $hash]);
    }

    /**
     * Révoque tous les refresh tokens actifs d'un utilisateur (déconnexion de toutes ses
     * sessions), utile par exemple après un changement de mot de passe.
     */
    public function revokeAllForUser(string $idUser): void
    {
        $stmt = $this->pdo->prepare(
            'UPDATE RefreshToken SET revokedAt = NOW() WHERE idUser = :idUser AND revokedAt IS NULL'
        );
        $stmt->execute([':idUser' => $idUser]);
    }

    private function revokeFamily(string $familyId): void
    {
        $stmt = $this->pdo->prepare(
            'UPDATE RefreshToken SET revokedAt = NOW() WHERE familyId = :familyId AND revokedAt IS NULL'
        );
        $stmt->execute([':familyId' => $familyId]);
    }

    private function revokeSingle(int $id): void
    {
        $stmt = $this->pdo->prepare('UPDATE RefreshToken SET revokedAt = NOW() WHERE id = :id');
        $stmt->execute([':id' => $id]);
    }

    private function issueAccessAndRefresh(string $idUser, string $role, string $familyId): array
    {
        $now = time();
        $accessToken = Jwt::encode([
            'sub' => $idUser,
            'role' => $role,
            'type' => 'access',
            'iat' => $now,
            'exp' => $now + AuthConfig::ACCESS_TOKEN_TTL_SECONDS,
        ], AuthConfig::jwtSecret());

        $refreshRaw = self::generateOpaqueToken();
        $refreshHash = self::hashToken($refreshRaw);
        $expiresAt = date('Y-m-d H:i:s', $now + AuthConfig::REFRESH_TOKEN_TTL_SECONDS);

        $stmt = $this->pdo->prepare(
            'INSERT INTO RefreshToken (idUser, tokenHash, familyId, expiresAt) VALUES (:idUser, :hash, :familyId, :expiresAt)'
        );
        $stmt->execute([
            ':idUser' => $idUser,
            ':hash' => $refreshHash,
            ':familyId' => $familyId,
            ':expiresAt' => $expiresAt,
        ]);

        return [
            'accessToken' => $accessToken,
            'refreshToken' => $refreshRaw,
            'expiresIn' => AuthConfig::ACCESS_TOKEN_TTL_SECONDS,
        ];
    }

    private static function generateOpaqueToken(): string
    {
        return bin2hex(random_bytes(32));
    }

    private static function hashToken(string $rawToken): string
    {
        return hash('sha256', $rawToken);
    }

    private static function generateUuidV4(): string
    {
        $data = random_bytes(16);
        $data[6] = chr(ord($data[6]) & 0x0f | 0x40);
        $data[8] = chr(ord($data[8]) & 0x3f | 0x80);
        return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($data), 4));
    }
}
