<?php
require_once __DIR__ . '/Config.php';

/**
 * Protection anti brute-force sur le login : verrouille temporairement un compte après
 * plusieurs échecs de connexion consécutifs. Utilise les colonnes
 * User.failed_login_attempts / User.locked_until (voir migration_001_auth_tokens.sql).
 *
 * Volontairement simple (basé sur le compte, pas sur l'IP) : bloquer par IP seule serait
 * inefficace derrière un NAT/opérateur mobile partagé (faux positifs) et contournable par
 * changement d'IP ; le verrouillage par compte protège directement la ressource visée
 * (le compte utilisateur) quelle que soit l'origine des tentatives.
 */
class LoginRateLimiter
{
    private PDO $pdo;

    public function __construct(PDO $pdo)
    {
        $this->pdo = $pdo;
    }

    /**
     * Retourne le nombre de secondes restant avant déverrouillage si le compte est
     * actuellement verrouillé, ou 0 s'il ne l'est pas (ou n'existe pas).
     */
    public function secondsUntilUnlock(string $idUser): int
    {
        $stmt = $this->pdo->prepare('SELECT locked_until FROM User WHERE idUser = :idUser');
        $stmt->execute([':idUser' => $idUser]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$row || $row['locked_until'] === null) {
            return 0;
        }

        $remaining = strtotime($row['locked_until']) - time();
        return $remaining > 0 ? $remaining : 0;
    }

    /**
     * À appeler après un échec d'authentification (mauvais mot de passe). Incrémente le
     * compteur et verrouille le compte si le seuil est atteint.
     */
    public function recordFailure(string $idUser): void
    {
        $stmt = $this->pdo->prepare(
            'UPDATE User SET failed_login_attempts = failed_login_attempts + 1 WHERE idUser = :idUser'
        );
        $stmt->execute([':idUser' => $idUser]);

        $stmt = $this->pdo->prepare('SELECT failed_login_attempts FROM User WHERE idUser = :idUser');
        $stmt->execute([':idUser' => $idUser]);
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($row && (int) $row['failed_login_attempts'] >= AuthConfig::MAX_FAILED_LOGIN_ATTEMPTS) {
            $lockedUntil = date('Y-m-d H:i:s', time() + AuthConfig::LOCKOUT_DURATION_SECONDS);
            $stmt = $this->pdo->prepare('UPDATE User SET locked_until = :lockedUntil WHERE idUser = :idUser');
            $stmt->execute([':lockedUntil' => $lockedUntil, ':idUser' => $idUser]);
        }
    }

    /**
     * À appeler après une authentification réussie : remet le compteur à zéro et lève un
     * éventuel verrou.
     */
    public function recordSuccess(string $idUser): void
    {
        $stmt = $this->pdo->prepare(
            'UPDATE User SET failed_login_attempts = 0, locked_until = NULL WHERE idUser = :idUser'
        );
        $stmt->execute([':idUser' => $idUser]);
    }
}
