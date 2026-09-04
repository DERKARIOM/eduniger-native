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

Fichier : `api/auth/migration_001_auth_tokens.sql`. **Déjà exécutée sur votre base réelle** (via phpMyAdmin, en votre présence) : les colonnes `role`/`failed_login_attempts`/`locked_until` existaient déjà sur `User` (probable tentative précédente), et la table `RefreshToken` a été créée et vérifiée (`DESCRIBE RefreshToken` conforme à la définition ci-dessus). Aucun compte n'a actuellement `isAdmin = 1` dans votre base — voir la note de promotion en fin de section 9.

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
- **Client partagé** : `ApiClient.java`, timeouts explicites (connexion 15s / lecture-écriture 20s), et une méthode `ApiClient.newBuilder(context)` pour les écrans ayant besoin de timeouts spécifiques (gros envois de fichiers) tout en héritant de l'intercepteur/authenticator.
- **Migration complète des ~34 points d'appel existants** : les 31 instanciations directes de `new OkHttpClient()` réparties dans 26 fichiers (15 activités, 2 adapters, 6 fragments, 1 service, 1 worker) ont été remplacées par `ApiClient.getInstance(context)` — ou `ApiClient.newBuilder(context)` pour `RegisterAuthorActivity`, qui a besoin de timeouts longs pour l'upload de fichiers volumineux. Détail dans la table ci-dessous. Le seul `new OkHttpClient()` restant dans le code (hors `ApiClient.java` lui-même) est volontaire : `AuthAuthenticator.mRefreshClient`, un client minimal dédié à l'appel de `refresh.php`, qui doit rester indépendant du client principal pour éviter une boucle de renouvellement sur lui-même.

| Fichier | Contexte utilisé | Occurrences |
|---|---|---|
| `CategoryActivity.java` | `getApplicationContext()` (classes internes `AsyncTask`) | 2 |
| `AddBookActivity.java`, `ChangePasswordActivity.java`, `SuggestionActivity.java`, `ChangeEmailActivity.java`, `PreRegistrationActivity.java` | `getApplicationContext()` (classes internes `AsyncTask`) | 1 chacun |
| `ChatAiActivity.java`, `BookActivity.java`, `AccountActivity.java`, `RegisterActivity.java`, `LoginActivity.java`, `AuthorActivity.java`, `StructureActivity.java`, `SearchActivity.java` | `this` (appel direct dans une méthode de l'activité) | 1 chacun |
| `RegisterAuthorActivity.java` | `ApiClient.newBuilder(this)` + timeouts longs conservés | 1 |
| `SettingAdapter.java`, `StructureAdapter.java` | `itemView.getContext()` (classes internes du `ViewHolder`) | 3 et 2 |
| `ChatBotFragment.java`, `StructureFragment.java`, `BooksFragment.java`, `HomeFragment.java`, `CategoryFragment.java` | `requireContext()` | 1, 2, 1, 1, 1 |
| `LibraryFragment.java` | `getContext()` (cohérent avec le reste de la méthode) | 1 |
| `MyFirebaseMessagingService.java` | `getApplicationContext()` | 2 |
| `NetworkCheckWorker.java` | `context` (paramètre du constructeur) | 1 |

Vérification effectuée : `grep` de contrôle confirmant zéro `new OkHttpClient()` actif restant en dehors des deux cas volontaires ci-dessus, import `ApiClient` ajouté partout où nécessaire (aucun doublon), et comptage d'accolades équilibré sur les 26 fichiers modifiés.
- `LoginActivity.java` : sauvegarde désormais les tokens reçus après connexion ; **suppression de `Log.d("310726", jsonData)`**, qui écrivait l'intégralité de la réponse de login (mot de passe haché, et désormais les tokens) dans les logs système — faille explicitement à corriger d'après votre cahier des charges ; ajout d'un état "compte verrouillé" (`accountLocked`) distinct du mot de passe incorrect.
- `MainActivity.java` : la déconnexion révoque désormais le refresh token côté serveur (best-effort, en tâche de fond) en plus du nettoyage de session local.
- `build.gradle` : ajout de `androidx.security:security-crypto` et d'une dépendance OkHttp explicite (elle n'était utilisée que via résolution transitive non garantie jusqu'ici).

**Point important à noter malgré tout** : après une inscription réussie, `RegisterActivity` appelle `mAccount.login()` qui ne fait qu'une écriture SQLite locale (pas d'appel réseau) — un utilisateur nouvellement inscrit n'obtient donc ses tokens qu'à sa PROCHAINE connexion explicite via `LoginActivity`. Ce comportement pré-existant n'a pas été modifié dans ce chantier (voir §8).

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
- **`RegisterActivity` n'obtient pas de tokens immédiatement après inscription** (voir §6) — nécessite une connexion explicite juste après.
- **Aucun compte n'a actuellement `role = 'ADMIN'` dans votre base** (voir §9) — à corriger manuellement si vous voulez pouvoir utiliser les fonctions d'administration.
- Aucun secret n'a été trouvé en dur dans les nouveaux fichiers (`JWT_SECRET` exclusivement via variable d'environnement, conformément à votre consigne — voir §11 pour comment elle a été configurée sur votre serveur).
- ✅ ~~Compilation Android non vérifiée~~ : compilée avec succès dans Android Studio après correction d'un bug réel (voir §11), et connexion confirmée fonctionnelle de bout en bout sur votre appareil réel contre votre serveur réel.

## 9. Stratégie de déploiement recommandée

Le système actuel a une contrainte incontournable : dès que `AuthMiddleware::requireAuth()` est actif sur un endpoint, tout appareil exécutant l'**ancienne** version de l'app (qui n'envoie pas de Bearer token) recevra 401 sur cet endpoint. Le projet dispose déjà d'un levier prêt à l'emploi pour gérer cette transition proprement : le mécanisme de version (`Version.idVersion`, déjà vérifié par `login.php`/`register.php`, renvoyant `expiresVersion` → l'app affiche un dialogue de mise à jour obligatoire).

Séquence recommandée :
1. ✅ Migration SQL (§4) exécutée sur la base de production.
2. ✅ Fichiers `api/` déployés sur votre serveur réel (voir §11 pour le détail — chemin réel, secret JWT, cache).
3. ✅ Migration Android terminée et **compilée avec succès** (voir §11) ; connexion testée et fonctionnelle sur un appareil réel. Il reste à faire passer manuellement les 14 autres scénarios du tableau ci-dessus directement dans l'app avant publication (déconnexion, renouvellement, accès sans droits, etc.), puis publier la nouvelle version.
4. Une fois cette version suffisamment diffusée, **incrémenter `Version.idVersion`** en base : les appareils encore sur l'ancienne version seront alors bloqués à l'écran de connexion avec un message de mise à jour, au lieu de rencontrer des 401 confus sur certains écrans.
5. Promouvoir au moins un compte en `role = 'ADMIN'` (ou `'SUPER_ADMIN'`) si vous avez besoin d'accéder aux fonctions d'administration (`send_notification.php`, `add_book.php`) :
   ```sql
   UPDATE `User` SET `role` = 'ADMIN', `isAdmin` = 1 WHERE `email` = 'votre-email@exemple.com';
   ```

**Note sur le champ `password` renvoyé par `login.php`** : il est conservé dans la réponse uniquement parce que `LoginActivity.java` (ligne ~476) le lit encore pour l'écran de verrouillage local (`LockActivity`). C'est un problème résiduel d'exposition de données déjà signalé dans l'audit précédent ; le supprimer casserait immédiatement cette fonctionnalité pour toute la base installée. À traiter en migrant `LockActivity` vers un stockage sécurisé dédié (ne nécessitant plus de recevoir le hash du serveur), puis en retirant ce champ de la réponse.

## 11. Déploiement réel sur votre serveur — ce qui a été trouvé et corrigé

Après la migration Android (§6), la compilation réelle dans Android Studio a révélé un problème que je ne pouvais pas voir sans SDK : la version de `androidx.security:security-crypto` déclarée dans `build.gradle` (`1.1.0-alpha06`) a supprimé la classe `MasterKeys` au profit d'une nouvelle classe `MasterKey` (Builder). `TokenStore.java` a été corrigé pour utiliser la nouvelle API (`MasterKey.Builder(...).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()` puis `EncryptedSharedPreferences.create(context, fileName, masterKey, ...)`), sans changement de comportement (même chiffrement AES-256/Keystore).

Une fois l'app compilée, un premier test de connexion réel a échoué (`JSONException: No value for name`) — pas un bug de code, mais un **problème de déploiement**, diagnostiqué avec vous en direct :

- **Topologie réelle du serveur** : `172.20.10.10:2222` est un serveur Apache 2.4/Ubuntu (probablement un environnement Termux/proot sur un appareil Android, vu le chemin `/sdcard/...`) sur votre réseau local. Le `DocumentRoot` du VirtualHost écoutant sur le port 2222 est `/var/www/html`, et `/var/www/html/eduniger` est un **lien symbolique vers `/sdcard/projets/eduniger`** — c'est donc **`/sdcard/projets/eduniger/api`** le vrai chemin à utiliser pour tout déploiement futur, PAS `/var/www/eduniger/api` (un premier essai y avait été fait par erreur, sans effet puisque ce dossier n'est pas servi ; il a été supprimé).
- **PHP tourne en mod_php** (pas de PHP-FPM), avec **OPcache actif** — après tout déploiement de nouveaux fichiers PHP, un redémarrage d'Apache est nécessaire pour que les changements soient réellement pris en compte (sinon l'ancien bytecode compilé peut continuer à être servi).
- **`JWT_SECRET` a été configuré** via `SetEnv JWT_SECRET "..."` ajouté dans `/etc/apache2/sites-enabled/000-default.conf` (à l'intérieur du bloc `<VirtualHost *:2222>`), suivi d'un `systemctl restart apache2`.
- Un dossier `.git` s'était retrouvé copié par erreur sur le serveur lors du premier essai de déploiement (risque de fuite du code source si exposé publiquement) ; il a été supprimé avec le reste du mauvais dossier.

**Aide-mémoire pour un futur déploiement** :
```bash
rsync -avz -e "ssh -p 8080" --exclude='.git' --exclude='.DS_Store' \
  ~/projets/eduniger-native/api/ cloud@172.20.10.10:/sdcard/projets/eduniger/api/
ssh -p 8080 cloud@172.20.10.10 "apache2ctl configtest && sudo systemctl restart apache2"
```

Après ces corrections, la connexion a été testée avec succès depuis l'app sur un appareil réel, contre le serveur réel — confirmation concrète, au-delà des tests automatisés du §7, que la chaîne complète (app Android → Apache/PHP → MariaDB) fonctionne.

## 10. Récapitulatif — durées de vie et stockage

| | Valeur | Justification |
|---|---|---|
| Access Token | 15 min, JWT HS256, stateless | Court : limite la fenêtre d'exploitation d'un vol |
| Refresh Token | 30 jours, opaque, haché (SHA-256) en base | Rotation à chaque usage, révocation immédiate en cas de réutilisation détectée |
| Verrouillage compte | 5 échecs → 15 min | Anti brute-force sans pénaliser un oubli ponctuel |
| Stockage mobile | EncryptedSharedPreferences (AES-256, Keystore) | Remplace tout stockage en clair |

---

Ce système est fonctionnel et testé côté serveur de bout en bout. La condition pour qu'il protège réellement l'application en production est la finalisation du chantier Android décrite au §6/§9 — je reste disponible pour l'enchaîner.
