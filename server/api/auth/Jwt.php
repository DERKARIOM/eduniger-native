<?php
/**
 * Implémentation JWT (JSON Web Token) minimale, sans dépendance externe, pour l'API EduNiger.
 *
 * Pourquoi une implémentation "maison" plutôt qu'une librairie (ex: firebase/php-jwt) ?
 * Le dossier api/ ne dispose d'aucun gestionnaire de dépendances (pas de composer.json,
 * pas de vendor/) : ajouter Composer uniquement pour ceci aurait été une dépendance non
 * justifiée pour ~80 lignes de code bien standard (HS256 = HMAC-SHA256, rien d'exotique).
 * Si le projet adopte Composer plus tard, cette classe peut être remplacée sans changer
 * l'API publique utilisée par le reste du code (encode/decode).
 *
 * Alogrithme : HS256 uniquement (HMAC-SHA256), volontairement — c'est l'algorithme
 * recommandé pour un secret partagé unique entre l'émetteur et le vérifieur (notre cas :
 * un seul serveur API). "alg": "none" et les algorithmes RS256/ES256 ne sont pas supportés
 * ici, ce qui élimine par construction toute une classe d'attaques classiques sur JWT
 * (falsification via alg=none, confusion RS256/HS256).
 */
class Jwt
{
    /**
     * Encode un tableau de claims en JWT signé HS256.
     */
    public static function encode(array $payload, string $secret): string
    {
        $header = ['typ' => 'JWT', 'alg' => 'HS256'];

        $segments = [];
        $segments[] = self::base64UrlEncode(json_encode($header, JSON_UNESCAPED_SLASHES));
        $segments[] = self::base64UrlEncode(json_encode($payload, JSON_UNESCAPED_SLASHES));

        $signingInput = implode('.', $segments);
        $signature = hash_hmac('sha256', $signingInput, $secret, true);
        $segments[] = self::base64UrlEncode($signature);

        return implode('.', $segments);
    }

    /**
     * Vérifie la signature, le format et l'expiration d'un JWT, et retourne ses claims.
     *
     * @throws JwtException si le token est invalide, mal signé, expiré ou malformé.
     */
    public static function decode(string $jwt, string $secret): array
    {
        $parts = explode('.', $jwt);
        if (count($parts) !== 3) {
            throw new JwtException('Format de token invalide');
        }
        [$headerB64, $payloadB64, $signatureB64] = $parts;

        $header = json_decode(self::base64UrlDecode($headerB64), true);
        if (!is_array($header) || !isset($header['alg'])) {
            throw new JwtException('En-tête de token invalide');
        }
        // Refus explicite de tout algorithme autre que HS256 (protection contre les
        // attaques "alg confusion" / "alg: none").
        if ($header['alg'] !== 'HS256') {
            throw new JwtException('Algorithme de signature non supporté');
        }

        $signingInput = $headerB64 . '.' . $payloadB64;
        $expectedSignature = hash_hmac('sha256', $signingInput, $secret, true);
        $actualSignature = self::base64UrlDecode($signatureB64);

        // hash_equals() : comparaison en temps constant, protège contre les attaques
        // par mesure de temps (timing attacks) sur la vérification de signature.
        if (!hash_equals($expectedSignature, $actualSignature)) {
            throw new JwtException('Signature invalide');
        }

        $payload = json_decode(self::base64UrlDecode($payloadB64), true);
        if (!is_array($payload)) {
            throw new JwtException('Payload de token invalide');
        }

        if (!isset($payload['exp']) || !is_int($payload['exp'])) {
            throw new JwtException('Le token ne contient pas de date d\'expiration');
        }
        if (time() >= $payload['exp']) {
            throw new JwtExpiredException('Token expiré');
        }

        return $payload;
    }

    private static function base64UrlEncode(string $data): string
    {
        return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
    }

    private static function base64UrlDecode(string $data): string
    {
        $padded = str_pad($data, strlen($data) % 4 === 0 ? strlen($data) : strlen($data) + (4 - strlen($data) % 4), '=');
        $decoded = base64_decode(strtr($padded, '-_', '+/'), true);
        if ($decoded === false) {
            throw new JwtException('Encodage base64url invalide');
        }
        return $decoded;
    }
}

class JwtException extends \Exception {}
class JwtExpiredException extends JwtException {}
