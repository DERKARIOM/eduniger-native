-- Migration: infrastructure d'authentification par tokens (Access + Refresh).
--
-- Compatible avec les données existantes :
--   - `User.isAdmin` (déjà utilisé par login.php / login_google.php / structure.php) est
--     CONSERVÉ tel quel pour ne rien casser côté app existante.
--   - `User.role` est une NOUVELLE colonne, dérivée de isAdmin au moment de la migration
--     (0 -> 'USER', 1 -> 'ADMIN'), qui prépare un vrai système de rôles multi-niveaux
--     (USER / ADMIN / SUPER_ADMIN) sans dépendre uniquement d'un booléen. Les futures
--     promotions vers SUPER_ADMIN se feront en mettant à jour cette colonne directement.
--   - Le contrôle des permissions se fait désormais UNIQUEMENT côté serveur (AuthMiddleware),
--     jamais en se fiant à une valeur envoyée par l'app mobile.

ALTER TABLE `User`
  ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER `isAdmin`,
  ADD COLUMN `failed_login_attempts` INT NOT NULL DEFAULT 0 AFTER `role`,
  ADD COLUMN `locked_until` DATETIME NULL DEFAULT NULL AFTER `failed_login_attempts`;

UPDATE `User` SET `role` = 'ADMIN' WHERE `isAdmin` = 1;

-- Refresh tokens : stockés SOUS FORME DE HASH (SHA-256) uniquement, jamais en clair,
-- afin qu'une fuite de la base ne permette pas de rejouer les tokens (même logique que
-- pour un mot de passe : on ne stocke jamais le secret lui-même).
--
-- `familyId` : identifiant de la "famille" de tokens issue d'un même login. Chaque
-- rotation (utilisation d'un refresh token pour en obtenir un nouveau) crée une nouvelle
-- ligne dans la même famille et révoque l'ancienne. Si un refresh token déjà révoqué/remplacé
-- est présenté à nouveau (signe probable de vol), TOUTE la famille est révoquée
-- immédiatement (déconnexion forcée de cette session), voir TokenStore::rotate().
CREATE TABLE IF NOT EXISTS `RefreshToken` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `idUser` VARCHAR(256) NOT NULL,
  `tokenHash` CHAR(64) NOT NULL,
  `familyId` CHAR(36) NOT NULL,
  `createdAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expiresAt` DATETIME NOT NULL,
  `revokedAt` DATETIME NULL DEFAULT NULL,
  `replacedByHash` CHAR(64) NULL DEFAULT NULL,
  `userAgent` VARCHAR(255) NULL DEFAULT NULL,
  UNIQUE KEY `uniq_token_hash` (`tokenHash`),
  KEY `idx_user` (`idUser`),
  KEY `idx_family` (`familyId`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`idUser`) REFERENCES `User` (`idUser`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
