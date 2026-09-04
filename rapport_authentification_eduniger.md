# Rapport — Modernisation de l'authentification EduNiger

Ce rapport documente l'audit et la refonte du système d'authentification d'EduNiger, réalisés à la suite de l'audit technique global (voir `rapport_audit_eduniger.md`). Conformément à la consigne, ce travail n'est pas resté au stade de propositions : le code a été écrit, exécuté et testé contre une vraie base de données (copie locale de `base/eduniger.sql`, sur MariaDB 10.11 et PHP 8.4), puis porté sur vos fichiers réels.

## 1. Ce qui existait avant

Après lecture complète du code (recherche de `token`, `jwt`, `Authorization`, `login`, `logout`, `refresh`, des usages de `SharedPreferences`/SQLite locale, etc.), le constat était net : **il n'existait aucun système de session côté serveur**. Concrètement :

- Le mot de passe était haché en SHA-256 côté client (`PasswordUtil.hashPassword()`), envoyé tel quel au serveur, et comparé par simple égalité SQL (`WHERE password = :password`) — aucun hachage côté serveur, aucun sel.
- La quasi-totalité des ~50 endpoints "personnels" de `api/` (mes livres, mes réservations, mes notifications, mes structures, supprimer mon compte, changer mon mot de passe...) faisaient **confiance à un paramètre `idNumber`/`idUser` envoyé par le client**, sans aucune vérification cryptographique. N'importe qui, avec un simple `curl`, pouvait agir au nom de n'importe quel autre utilisateur.
- `DeleteAccountSyn.php` permettait de supprimer **n'importe quel compte** en connaissant juste son matricule.
- `UpdateSyn.php` acceptait un nom de colonne **et une valeur arbitraires** à écrire dans `User`, sans liste blanche ni paramètre lié — en théorie exploitable pour s'auto-promouvoir admin.
- `password_change.php` changeait un mot de passe sans aucune vérification autre que idNumber+email, et renvoyait le hash en clair.
- Une session "locale" existait bien (table SQLite `Session`), mais elle ne servait qu'à l'écran de verrouillage local (`LockActivity`) et stockait le hash du mot de passe en clair sur le disque.
- Un système de token **existe déjà** pour les agents de bibliothèque (Laravel Sanctum, table `personal_access_tokens`, modèle `StructAgent`) — mais il est totalement séparé et ne concerne pas les comptes `User` de l'app mobile, qui sont la cible de cette intervention.

## 2. Architecture mise en place

**Access Token (JWT, HS256, 15 min)** + **Refresh Token (opaque, 30 jours, avec rotation)**, entièrement neufs et sans dépendance externe (le dossier `api/` n'a pas de Composer/vendor — l'implémentation JWT est donc "maison", volontairement limitée à HS256 pour éliminer par construction les attaques `alg:none`/confusion d'algorithme).

```
Login (idNumber + password)
   │
   ├─ mot de passe vérifié (legacy SHA-256 OU bcrypt, migration transparente)
   ├─ anti brute-force (5 échecs → verrouillage 15 min)
   │
   └─ émission : Access Token (JWT, 15 min) + Refresh Token (aléatoire 256 bits, 30 jours)
                                  │
                                  ▼
Appel API protégé  ──Authorization: Bearer <access>──▶  AuthMiddleware::requireAuth()
                                  │
                          401 si absent/invalide/expiré
                          403 si rôle insuffisant
                                  │
                    access expiré → POST /refresh.php {refresh_token}
                                  │
                     ├─ valide  → nouveau couple (rotation : l'ancien refresh est révoqué)
                     └─ invalide/rejoué → 401 + code "invalid_refresh_token" → l'app doit
                                          arrêter tout nouvel essai et déconnecter l'utilisateur
```

**Rotation et détection de vol.** Chaque utilisation d'un refresh token le révoque et en émet un nouveau dans la même "famille". Si un refresh token *déjà révoqué* est présenté à nouveau (signe qu'il a été copié/volé), **toute la famille est révoquée immédiatement** — l'utilisateur légitime est lui aussi déconnecté à son prochain refresh, ce qui neutralise la copie volée en même temps. Ce comportement est vérifié par test (scénario 9b/12).

**Rôles.** Une colonne `User.role` (`USER`/`ADMIN`/`SUPER_ADMIN`) a été ajoutée, dérivée de l'ancien booléen `isAdmin` à la migration (conservé tel quel pour ne rien casser des usages existants). Le contrôle se fait uniquement côté serveur, à partir du rôle contenu dans le token signé — jamais à partir d'une valeur envoyée par le client.

## 3. Fichiers créés

| Fichier | Rôle |
|---|---|
| `api/auth/Jwt.php` | Encodage/décodage JWT HS256 "maison" (rejet strict de tout autre `alg`, `hash_equals()` pour la signature) |
| `api/auth/Config.php` | `AuthConfig` : durées de vie des tokens, seuils anti brute-force, et **secret JWT lu exclusivement via `getenv('JWT_SECRET')`** (jamais en dur — conforme à votre consigne explicite ; échec propre en 500 si absent) |
| `api/auth/PasswordHasher.php` | Vérification + migration transparente SHA-256 legacy → bcrypt |
| `api/auth/TokenStore.php` | Émission, rotation, révocation des tokens |
| `api/auth/AuthMiddleware.php` | `requireAuth([role])` : extrait le Bearer, vérifie, renvoie 401/403 ou les claims |
| `api/auth/RateLimiter.php` | Verrouillage anti brute-force par compte |
| `api/auth/migration_001_auth_tokens.sql` | Migration SQL (voir §4) |
| `api/refresh.php` | Nouvel endpoint : renouvellement de l'access token |
| `api/logout.php` | Nouvel endpoint : révocation du refresh token |
| Android : `TokenStore.java`, `model/net/AuthInterceptor.java`, `model/net/AuthAuthenticator.java`, `model/net/ApiClient.java` | Stockage sécurisé + injection automatique du Bearer + refresh automatique sur 401 (détail §6) |

## 4. Migration base de données

```sql
ALTER TABLE User
  ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER',
  ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN locked_until DATETIME NULL DEFAULT NULL;
UPDATE User SET role = 'ADMIN' WHERE isAdmin = 1;

CREATE TABLE RefreshToken (
  id INT AUTO_INCREMENT PRIMARY KEY,
  idUser VARCHAR(256) NOT NULL,
  tokenHash CHAR(64) NOT NULL,      -- SHA-256 du token, jamais la valeur en clair
  familyId CHAR(36) NOT NULL,
  createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expiresAt DATETIME NOT NULL,
  revokedAt DATETIME NULL DEFAULT NULL,
  replacedByHash CHAR(64) NULL DEFAULT NULL,
  userAgent VARCHAR(255) NULL DEFAULT NULL,
  ... FOREIGN KEY (idUser) REFERENCES User(idUser) ON DELETE CASCADE
);
```

Fichier : `api/auth/migration_001_auth_tokens.sql`. **À exécuter sur la base de production avant de déployer les nouveaux fichiers PHP** (les endpoints échoueront proprement, sans crasher, tant que ce n'est pas fait — mais l'authentification ne fonctionnera pas). Testée avec succès sur une copie locale de votre schéma réel.

## 5. Endpoints `api/` : ce qui a changé

**Nouveaux/réécrits pour émettre des tokens** : `login.php`, `login_google.php`, `refresh.php`, `logout.php`, `register.php` (hache désormais le mot de passe en bcrypt à l'inscription).

**Protégés par `AuthMiddleware::requireAuth()`, identité prise du token (plus jamais du client)** — 34 fichiers : `DeleteAccountSyn`, `UpdateSyn`, `password_change` (durci autrement, voir ci-dessous), `NotificationSyn`, `adherer_struct`, `detach_struct`, `update_token`, `get_unread_loands`, `mark_loand_viewed`, `send_notification` (réservé ADMIN), `books`, `recommended`, `ranking`, `Structure2`, `StructureMore`, `structure`, `structure_more`, `structure_top`, `StructCategoryIn`, `category_in`, `IsLike`, `IsNoLike`, `IsSubscribeBook`, `insert_like`, `insert_no_like`, `insert_subscribe_book`, `insert_view`, `LoandClosing`, `LoandSyn`, `NotifService`, `ReservationService`, `Reservation`, `SendComments`, `Suggestion`, `add_book` (réservé ADMIN).

**Protégés par authentification simple (endpoints "catalogue" consultés uniquement par l'app connectée, sans donnée personnelle en jeu)** : `Author`, `AuthorSimular`, `author_top`, `author_book`, `AuthorAudioBook`, `AuthorPDFBook`, `AuthorPhysicBook`, `StructBookMore`, `Tones`, `SimilarBook`, `ReceiveComments`.

**Corrections de sécurité trouvées en cours de route et corrigées** (au-delà du périmètre strict "authentification", mais découvertes en creusant le même code) :
- **Injection SQL réelle** dans ~24 fichiers qui appelaient `$pdo->prepare()` sur une chaîne SQL **déjà construite par concaténation** — la protection de `prepare()` y était donc purement cosmétique. Tous corrigés avec de vrais paramètres liés (`IsLike`, `insert_like`, `LoandClosing`, `LoandSyn`, `NotifService`, `ReservationService`, `Reservation`, `SendComments`, `PreRegister`, `ChangeEmail`, `Suggestion`, `StructBookMore`, `Tones`, `author_book`, `AuthorAudioBook/PDF/Physic`, `SimilarBook`, `add_book`, etc.). C'était une lacune de mon précédent audit (qui n'avait vérifié que la *présence* de `prepare()`, pas l'absence de paramètres liés) — merci de noter cette correction méthodologique.
- `password_change.php` : mot de passe désormais haché en bcrypt, plus de fuite du hash dans la réponse, anti brute-force ajouté.
- `UpdateSyn.php` : liste blanche stricte des colonnes modifiables (`name`, `firstName`, `email`, `password`), fin de l'écriture SQL arbitraire.
- `login_google.php` : deux bugs bloquants pré-existants corrigés (insertion sans `idUser`, colonne `token` inexistante au lieu de `fcm_token`) ; la vérification cryptographique du token Google reste toutefois **non fonctionnelle** faute de la librairie `google/apiclient` (absente, `vendor/` inexistant dans `api/`) — voir §8.

**Code mort découvert et documenté (non supprimé, juste sécurisé)** : `NotifService.php` (point d'appel Android entièrement commenté), `ChangeEmail.php` et `add_book.php` (aucun appelant trouvé), et surtout : **`ReceiveComments.php`/`SendComments.php` référencent une table `Comment` inexistante, et `NotificationSyn.php` une table `NotifView` inexistante**, dans `base/eduniger.sql` — ces fonctionnalités (commentaires sur un livre, notifications) sont donc **actuellement non fonctionnelles en production**, indépendamment de ce chantier. Elles échouaient probablement déjà silencieusement (erreur SQL non gérée) ; elles échouent maintenant proprement (message générique, rien ne remonte au client), mais restent à corriger côté schéma — décision produit hors périmètre de cette intervention.

## 6. Côté application Android

- **Stockage sécurisé** : `TokenStore.java`, basé sur `EncryptedSharedPreferences` (AES-256, clé Android Keystore) — remplace tout stockage en clair pour les nouveaux tokens.
- **Injection automatique du Bearer** : `AuthInterceptor.java`.
- **Renouvellement automatique sur 401** : `AuthAuthenticator.java`, avec garde anti-boucle explicite (abandon après une seule tentative de refresh par requête — empêche tout `401 → refresh → 401 → refresh → ...` infini) et déconnexion locale propre (`TokenStore.clear()`) si le refresh échoue.
- **Client partagé** : `ApiClient.java`, timeouts explicites (connexion 15s / lecture-écriture 20s).
- `LoginActivity.java` : sauvegarde désormais les tokens reçus après connexion ; **suppression de `Log.d("310726", jsonData)`**, qui écrivait l'intégralité de la réponse de login (mot de passe haché, et désormais les tokens) dans les logs système — faille explicitement à corriger d'après votre cahier des charges ; ajout d'un état "compte verrouillé" (`accountLocked`) distinct du mot de passe incorrect.
- `MainActivity.java` : la déconnexion révoque désormais le refresh token côté serveur (best-effort, en tâche de fond) en plus du nettoyage de session local.
- `build.gradle` : ajout de `androidx.security:security-crypto` et d'une dépendance OkHttp explicite (elle n'était utilisée que via résolution transitive non garantie jusqu'ici).

**Important — travail restant côté mobile, à faire avant déploiement** : ce chantier a mis en place l'infrastructure (stockage, intercepteur, refresh automatique) et l'a branchée sur le flux de connexion/déconnexion, mais **n'a pas migré les ~34 instanciations directes de `new OkHttpClient()` déjà présentes dans le projet** (une par écran, déjà cataloguées dans l'audit précédent) vers `ApiClient.getInstance(context)`. Tant que cette migration écran par écran n'est pas faite, ces appels existants n'enverront pas le header `Authorization` et **recevront systématiquement 401** sur tous les endpoints nouvellement protégés listés au §5. C'est un travail mécanique mais volumineux qu'il n'était pas raisonnable de faire à l'aveugle (aucun SDK Android disponible dans cet environnement pour compiler/vérifier chaque écran) — voir §9 pour la stratégie de déploiement recommandée. Egalement à noter : après une inscription réussie, `RegisterActivity` appelle `mAccount.login()` qui ne fait qu'une écriture SQLite locale (pas d'appel réseau) — un utilisateur nouvellement inscrit n'obtient donc ses tokens qu'à sa PROCHAINE connexion explicite via `LoginActivity`.

**Avertissement sur les changements Android** : comme pour l'audit précédent, cet environnement ne dispose pas du SDK Android/Gradle — ces fichiers ont été relus attentivement (accolades équilibrées vérifiées mécaniquement, API `EncryptedSharedPreferences`/OkHttp `Authenticator` standard et bien documentée) mais **n'ont pas pu être compilés**. Une compilation Gradle réelle avant publication est indispensable.

## 7. Tests exécutés (les 15 scénarios demandés, contre une vraie base MariaDB)

Environnement : PHP 8.4 (`php -S`) + MariaDB 10.11, schéma réel importé depuis `base/eduniger.sql`, utilisateur DB dédié `eduniger_app` (droits limités, pas root). Script : `run_scenarios.sh`, exécuté via de vraies requêtes HTTP (`curl`).

| # | Scénario | Résultat |
|---|---|---|
| 1 | Inscription | ✅ `ok`, mot de passe stocké en bcrypt |
| 2 | Connexion, identifiants corrects | ✅ retourne `accessToken` + `refreshToken` |
| 3 | Connexion, mauvais mot de passe | ✅ `incorrectPassword` |
| 4 | Accès API protégée, token valide | ✅ HTTP 200 |
| 5 | Accès sans token | ✅ HTTP 401 |
| 6 | Token invalide (signature falsifiée) | ✅ HTTP 401 |
| 7 | Token expiré | ✅ HTTP 401 + `code: token_expired` |
| 8 | Renouvellement, refresh token valide | ✅ nouveau couple émis (rotation), et l'utilisateur reste authentifié après (8b) |
| 9 | Renouvellement, refresh token invalide | ✅ HTTP 401 + `code: invalid_refresh_token` |
| 10 | Refresh token expiré | ✅ HTTP 401 |
| 11 | Déconnexion | ✅ `success:true`, le refresh token est ensuite rejeté |
| 12 | Réutilisation d'un ancien refresh token | ✅ HTTP 401, **et toute la famille de tokens est révoquée** (vérifié séparément en 9b) |
| 13 | Rôle insuffisant (USER sur endpoint ADMIN) | ✅ HTTP 403 ; rôle ADMIN → autorisé (13b) |
| 14 | Serveur indisponible | ✅ vérifié que chaque endpoint échoue proprement (pas de crash serveur, testé avec des requêtes malformées) — le comportement client (timeout OkHttp) est à vérifier manuellement en conditions réelles |
| 15 | Perte de connexion pendant un refresh | ⚠️ non simulable côté serveur seul ; couvert côté conception par `AuthAuthenticator` (l'échec réseau du refresh est traité comme un échec → déconnexion locale propre, pas de boucle) |

**20/20 vérifications automatisées passées** (certains scénarios comportaient plusieurs assertions). Vérifications complémentaires effectuées : migration transparente d'un mot de passe legacy SHA-256 vers bcrypt au premier login (confirmé en base), verrouillage effectif après 5 échecs puis déverrouillage après succès, `DeleteAccountSyn.php` ne peut plus supprimer que son propre compte même en fournissant délibérément l'identifiant d'un autre utilisateur, aucune erreur fatale PHP journalisée sur l'ensemble des tests (y compris avec des requêtes volontairement malformées). Les 66 fichiers de `api/` passent `php -l` sans erreur.

## 8. Problèmes de sécurité restants (non résolus, à traiter séparément)

- **`login_google.php`** : la vérification cryptographique du token Google (signature RS256, JWKS) n'est pas fonctionnelle — la librairie `google/apiclient` n'est pas installée (`api/` n'a pas de Composer). L'endpoint échoue désormais proprement (503) plutôt que de planter, mais la connexion Google reste inopérante tant que cette dépendance n'est pas ajoutée. Hors périmètre de "Access/Refresh Token" strictement parlant.
- **Tables manquantes** (`Comment`, `NotifView`, `StudentAccount`) : voir §5, fonctionnalités déjà non opérationnelles avant cette intervention.
- **Retrofit Android incomplet** : voir §6, ~34 écrans à migrer vers `ApiClient` avant que la protection par token ne soit effective de bout en bout.
- Aucun secret n'a été trouvé en dur dans les nouveaux fichiers (`JWT_SECRET` exclusivement via variable d'environnement, conformément à votre consigne).

## 9. Stratégie de déploiement recommandée

Le système actuel a une contrainte incontournable : dès que `AuthMiddleware::requireAuth()` est actif sur un endpoint, tout appareil exécutant l'**ancienne** version de l'app (qui n'envoie pas de Bearer token) recevra 401 sur cet endpoint. Le projet dispose déjà d'un levier prêt à l'emploi pour gérer cette transition proprement : le mécanisme de version (`Version.idVersion`, déjà vérifié par `login.php`/`register.php`, renvoyant `expiresVersion` → l'app affiche un dialogue de mise à jour obligatoire).

Séquence recommandée :
1. Exécuter la migration SQL (§4) sur la base de production.
2. Déployer les fichiers `api/` de ce chantier (ils sont rétrocompatibles pour le login : les champs ajoutés à la réponse sont additifs, les anciens champs comme `password` sont conservés — voir note ci-dessous).
3. Terminer la migration Android (retrofit des ~34 écrans vers `ApiClient`, compilation, tests manuels réels), publier une nouvelle version de l'app.
4. Une fois cette version suffisamment diffusée, **incrémenter `Version.idVersion`** en base : les appareils encore sur l'ancienne version seront alors bloqués à l'écran de connexion avec un message de mise à jour, au lieu de rencontrer des 401 confus sur certains écrans.

**Note sur le champ `password` renvoyé par `login.php`** : il est conservé dans la réponse uniquement parce que `LoginActivity.java` (ligne ~476) le lit encore pour l'écran de verrouillage local (`LockActivity`). C'est un problème résiduel d'exposition de données déjà signalé dans l'audit précédent ; le supprimer casserait immédiatement cette fonctionnalité pour toute la base installée. À traiter en migrant `LockActivity` vers un stockage sécurisé dédié (ne nécessitant plus de recevoir le hash du serveur), puis en retirant ce champ de la réponse.

## 10. Récapitulatif — durées de vie et stockage

| | Valeur | Justification |
|---|---|---|
| Access Token | 15 min, JWT HS256, stateless | Court : limite la fenêtre d'exploitation d'un vol |
| Refresh Token | 30 jours, opaque, haché (SHA-256) en base | Rotation à chaque usage, révocation immédiate en cas de réutilisation détectée |
| Verrouillage compte | 5 échecs → 15 min | Anti brute-force sans pénaliser un oubli ponctuel |
| Stockage mobile | EncryptedSharedPreferences (AES-256, Keystore) | Remplace tout stockage en clair |

---

Ce système est fonctionnel et testé côté serveur de bout en bout. La condition pour qu'il protège réellement l'application en production est la finalisation du chantier Android décrite au §6/§9 — je reste disponible pour l'enchaîner.
