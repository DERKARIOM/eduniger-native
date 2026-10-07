<?php
/**
 * Vérification et hachage des mots de passe, avec migration progressive et transparente
 * de l'ancien format (SHA-256 non salé, calculé côté client Android avant envoi) vers
 * bcrypt (calculé et vérifié côté serveur, avec sel automatique).
 *
 * Pourquoi une migration "lazy" (au moment de la connexion) plutôt qu'un changement brutal :
 * le format actuel en base (SHA-256 non salé) ne peut PAS être converti directement en
 * bcrypt sans connaître le mot de passe en clair — hacher un hash n'est pas équivalent à
 * hacher le mot de passe original. La seule façon sûre de migrer est donc de re-hacher
 * avec bcrypt la prochaine fois que l'utilisateur se connecte avec succès (moment où le
 * serveur voit passer une preuve de connaissance du mot de passe). C'est le pattern
 * standard ("upgrade on login") recommandé par OWASP pour ce type de migration.
 *
 * IMPORTANT sur ce que représente "$submittedPassword" ici : l'app Android actuelle
 * calcule déjà SHA-256(motDePasse) côté client avant l'envoi (PasswordUtil.hashPassword())
 * — le serveur ne voit donc JAMAIS le mot de passe en clair, seulement cette empreinte.
 * Cette classe est volontairement agnostique de ce détail : elle hache/vérifie fidèlement
 * la chaîne qu'on lui donne, que ce soit ce SHA-256 client (comportement actuel, conservé
 * pour ne rien casser côté app existante) ou un futur mot de passe en clair si l'app est
 * un jour modifiée pour ne plus pré-hacher. Dans les deux cas, le stockage final en base
 * est du bcrypt (jamais la valeur reçue telle quelle), ce qui protège contre le vol de la
 * base même dans le scénario actuel où "submittedPassword" est un SHA-256 non salé.
 */
class PasswordHasher
{
    /**
     * Vérifie un mot de passe (ou son équivalent SHA-256 envoyé par l'app Android
     * actuelle) contre le hash stocké en base, quel que soit son format (legacy ou bcrypt).
     */
    public static function verify(string $submittedPassword, string $storedHash): bool
    {
        if (self::looksLikeBcrypt($storedHash)) {
            return password_verify($submittedPassword, $storedHash);
        }

        // Format legacy : le client envoie déjà un SHA-256 hexadécimal (voir
        // PasswordUtil.hashPassword côté Android) ; le serveur stocke tel quel et compare
        // en temps constant pour éviter les attaques par mesure de temps.
        return hash_equals($storedHash, $submittedPassword);
    }

    /**
     * Indique si le mot de passe stocké doit être re-haché en bcrypt (appelé uniquement
     * après une vérification réussie).
     */
    public static function needsRehash(string $storedHash): bool
    {
        return !self::looksLikeBcrypt($storedHash);
    }

    /**
     * Hache une valeur avec bcrypt (utilisé pour les nouvelles inscriptions et pour la
     * migration transparente au login).
     */
    public static function hash(string $password): string
    {
        return password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
    }

    private static function looksLikeBcrypt(string $hash): bool
    {
        return (bool) preg_match('/^\$2[axy]\$\d{2}\$/', $hash);
    }
}
