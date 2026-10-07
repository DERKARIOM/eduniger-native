# Rapport — Audit et restauration après changement de PC

**Date** : 4 octobre 2026
**Périmètre** : `C:\Users\derka\projets\eduniger-native` (application Android, API Laravel `eduniger-api`, back-office React `eduniger-admin`, API PHP historique `api/`)

---

## 1. Situation constatée sur le nouveau PC

| Élément | Ancien PC (d'après les rapports et l'historique Git) | Nouveau PC (avant restauration) |
|---|---|---|
| `eduniger_native/` (Android) | `master` @ `d22943f` (9 sept.) | **Identique** : clone de `origin/master` @ `d22943f`, aucun écart (vérifié fichier par fichier sur les tailles, fins de ligne CRLF prises en compte) |
| `api/` (API PHP historique, sous-module) | Version de septembre : JWT + refresh tokens, 34 endpoints protégés, ~24 injections SQL corrigées, secrets retirés | **Dossier vide**. Le sous-module n'a pas de `.gitmodules`. Le dépôt `DERKARIOM/eduniger-api` s'arrête au **8 mai 2026** (`8a64176`) : **aucune des modifications de septembre n'a été poussée** |
| `app-web/` (Laravel, ignoré par `.gitignore`) | Clone de `iMoubarak/eduniger-api` + correctifs | Remplacé par `eduniger-api/` (clone frais de `iMoubarak/eduniger-api`, `main` @ `94a1094`, 9 sept.) — la quasi-totalité du travail serveur y est bien poussée |
| `app-web/eduniger-admin/` (React) | Correctifs d'audit (4 sept.) + déploiement LAN (5-6 sept.) | `eduniger-admin/` **vide** : le checkout a échoué sous Windows (voir §3.1). `iMoubarak/eduniger-admin` s'arrête au **8 avril 2026** : **aucun correctif de septembre n'a été poussé** |

Branches examinées : `eduniger-native` → `master`, `Eduna` ; `iMoubarak/eduniger-api` → `main`, `production` ; `iMoubarak/eduniger-admin` → `main`, `production` ; `DERKARIOM/eduniger-api` → `master`.
Sources de référence : historique Git, `_app_web_audit_src.tar.gz` (sources du 4 sept.), `_app_web_lan_deploy.tar.gz` (5 sept.), `eduniger-admin-dist-lan-v2.tar.gz` (build compilé du 6 sept.), et les 4 rapports de l'ancien PC.

---

## 2. Modifications retrouvées et leur état

### Application Android — complète
Toutes les modifications (audit technique, authentification par tokens, migration Sanctum, téléchargements avec reprise, « continuer l'écoute »…) sont dans `master` et présentes sur le PC. Rien à restaurer.

La branche **`Eduna`** (assistant IA Gemma/Kaggle, 5 actions, transcription audio — mai 2026) n'a jamais été fusionnée. Elle est en conflit avec `master` sur 5 fichiers (dont `ChatAiActivity.java`, `MessageAdapter.java`, `OnlineBookAdapter.java`) et réintroduirait `usesCleartextTraffic` à la place de `network_security_config`. **Non fusionnée** : décision produit à prendre (voir §6).

### API Laravel (`eduniger-api`) — presque complète
Présent sur `origin/main` : middlewares `role` et `structure.scope`, `StructAgentController` durci, routes protégées, `HelpRequestPolicy` corrigée, fichiers morts supprimés, migration dupliquée supprimée, `AIBookService` corrigé, gestion 401 JSON, connexion par téléphone, migrations prêts/structures, 10 tests d'autorisation.

**Manquant** (appliqué seulement sur le serveur du téléphone, jamais dans les sources) :
1. `config/database.php` — constante SSL compatible PHP 8.5 (sans elle, *toutes* les routes renvoient « headers already sent » sous PHP 8.5).
2. `config/cors.php` — motif `allowed_origins_patterns` pour les IP privées sur le port 8090 (accès depuis le Mac via 172.20.10.10).
3. `.env.example` — deux valeurs malformées (espaces non cités) qui cassent le parsing du `.env`.

### Back-office React (`eduniger-admin`) — entièrement perdu en source
Aucun des correctifs n'existait plus que dans le build compilé. Manquants :
- `Index.tsx` : hooks déclarés après des `return` anticipés (crash React possible).
- `AuthContext.tsx` : appel `/api/me` → `/api/api/me` (déconnexion silencieuse).
- `echo.ts` : `http://localhost:8000/broadcasting/auth` codé en dur.
- `src/lib/runtimeConfig.ts` : résolution dynamique des URLs API/stockage.
- Photos d'auteurs construites avec `…/api/storage/authors/…` (URL cassée).
- `VITE_APP_FILES_URL` utilisée mais jamais définie.
- 9 fichiers morts + `public/images.png:Zone.Identifier`.
- `router.php` pour servir la SPA avec `php -S`.
- `package-lock.json` mis à jour (l'actuel n'est plus synchronisé : `npm ci` échoue sur `yaml@2.9.1`).

### API PHP historique (`api/`) — perdue, non récupérable depuis Git
Les ~40 fichiers modifiés en septembre (`auth/Jwt.php`, `auth/AuthMiddleware.php`, `auth/TokenStore.php`, `auth/RateLimiter.php`, `auth/PasswordHasher.php`, `auth/Config.php`, `refresh.php`, `logout.php`, `login.php`, `register.php`, `password_change.php`, `UpdateSyn.php`, `DeleteAccountSyn.php`, `uploadImg.php`, `callOpenAi.php`…) n'existent **ni sur GitHub, ni dans les archives**. La seule copie connue est celle **déployée sur le serveur** (`/sdcard/projets/eduniger/api`, téléphone 172.20.10.10).

C'est critique : l'app Android actuelle appelle encore `refresh.php`, `logout.php`, `password_change.php`, `UpdateSyn.php`, `DeleteAccountSyn.php`, `NotificationSyn.php`, `update_token.php`… avec un jeton Bearer. La version de mai sur GitHub ne contient pas `refresh.php`/`logout.php`, ne vérifie pas les jetons, et contient encore les failles corrigées (injections SQL, `callOpenAi.php` avec clé en dur, `hash_password.php`).

### Configuration
- `.gitignore` ne couvrait pas la nouvelle disposition (`eduniger-api/`, `eduniger-admin/` à la racine au lieu de `app-web/`) : risque de committer deux dépôts entiers dans `eduniger-native`.
- `.gitmodules` absent → `git submodule update` impossible pour `api/`.
- `env.lan.template` : contenait encore `DB_HOST=localhost` / `DB_USERNAME=root` (corrigés sur le serveur en `127.0.0.1` / `eduniger_web`) **et le vrai mot de passe root MySQL**, dans un dépôt **public**.
- `.env` de `eduniger-api` : ignoré par Git, donc absent du nouveau PC (normal) ; à recréer depuis `env.lan.template`.

---

## 3. Modifications restaurées

### 3.1 `eduniger-admin` — branche `restauration-ancien-pc` (commit `2612279`)
Cause du dossier vide : le fichier `public/images.png:Zone.Identifier` (le `:` est interdit sous Windows) faisait échouer le checkout. Il est supprimé dans la branche restaurée, qui contient :

| Fichier | Modification |
|---|---|
| `src/pages/Index.tsx` | Retours anticipés déplacés après tous les hooks |
| `src/contexts/AuthContext.tsx` | `/api/me` → `/me` |
| `src/lib/runtimeConfig.ts` (nouveau) | `API_URL`, `STORAGE_URL`, `FILES_URL`, `PUBLIC_URL`, `REVERB_HOST` résolus à l'exécution ; une variable `VITE_APP_*` définie reste prioritaire (migration HTTPS) |
| `src/utils/api.ts` | `baseURL: API_URL` |
| `src/lib/echo.ts` | `authEndpoint: ${API_URL}/broadcasting/auth`, hôte Reverb dynamique, ports typés |
| `AuthorDetail.tsx`, `AuthorDialog.tsx`, `AuthorsList.tsx` | `${STORAGE_URL}/authors/…` |
| `QuickBookUpdateModal.tsx` | `FILES_URL` (variable désormais définie) |
| `public-lib/BookCard.tsx`, `BookDetailsDialog.tsx`, `AudioPlayer.tsx`, `hooks/useInstitutionTheme.tsx` | `STORAGE_URL` (cohérence) |
| `src/vite-env.d.ts` | Types des variables d'environnement |
| `.env.production` (nouveau) | URLs vides → résolution dynamique au build de production |
| `public/router.php` (nouveau) | Routeur SPA, copié dans `dist/` au build |
| `package-lock.json` | Version de l'ancien PC (synchronisée avec `package.json`) |
| 10 fichiers supprimés | `Admin.tsx`, `Help.tsx`, `PublicLibrary.tsx`, `Stats.tsx`, `library-original.ts`, `utils/axios.js`, `lib/axios.ts`, `logout.js`, `testaxios.js`, `images.png:Zone.Identifier` |

Livrée dans `restauration/eduniger-admin-restauration.bundle`, appliquée par `restauration/RESTAURER.bat` (voir §5 : les outils distants ne peuvent pas écrire dans `.git`).

### 3.2 `eduniger-api` — modifications écrites directement sur le PC (non commitées, sur `main`)
| Fichier | Modification |
|---|---|
| `config/database.php` | `PHP_VERSION_ID >= 80500 ? \Pdo\Mysql::ATTR_SSL_CA : PDO::MYSQL_ATTR_SSL_CA` (connexions mysql et mariadb) |
| `config/cors.php` | Motif d'origines : IP privées (192.168/16, 172.16-31, 10/8) + localhost, port 8090 uniquement |
| `.env.example` | `HETZNER_SFTP_ROOT` et `REMOTE_FILES_BASE_URL` corrigés |
| `routes/api.php` | **Bug trouvé pendant la restauration** : `Broadcast::routes(['prefix' => 'api'])` dans un fichier déjà préfixé publiait `/api/api/broadcasting/auth`, alors que `cors.php` et le Web attendent `/api/broadcasting/auth`. Les notifications temps réel du Web ne pouvaient donc pas s'authentifier. Préfixe en double retiré. |

### 3.3 `eduniger-native` — écrit directement sur le PC
- `.gitignore` : ajout de `eduniger-api/`, `eduniger-admin/`, `restauration/`.
- `env.lan.template` : `DB_HOST=127.0.0.1`, `DB_USERNAME=eduniger_web`, `DB_PASSWORD=A_RENSEIGNER` (mot de passe retiré) + commentaires.
- `.gitmodules` : ajouté par `RESTAURER.bat` (l'écriture directe de ce fichier est bloquée par les outils distants). Le sous-module n'est **volontairement pas** récupéré (voir §4).

---

## 4. Problèmes rencontrés / points d'attention

1. **API PHP `api/` de septembre : perdue côté sources.** À récupérer depuis le serveur **avant tout autre déploiement**, puis à pousser :
   ```bash
   rsync -avz -e "ssh -p 8080" --exclude='.git' cloud@172.20.10.10:/sdcard/projets/eduniger/api/ ./api-serveur/
   ```
   ⚠️ Ne pas exécuter la commande `rsync` du rapport d'authentification (sens PC → serveur) tant que `api/` n'a pas été récupéré : elle écraserait la seule copie à jour avec la version de mai.
2. **Secret exposé** : le mot de passe root MySQL figurait dans `env.lan.template` d'un dépôt public (il reste dans l'historique Git). → **Changer ce mot de passe.** `config/database.php` de `eduniger-api` contient aussi un mot de passe par défaut en dur (`DB_PASSWORD` de repli) — à retirer.
3. `.gitignorey` : copie avec une faute de frappe de `.gitignore` (contient `*..part_aa`, aussi mal écrit) — sans effet, à supprimer si inutile.
4. Lint `eduniger-admin` : 100 erreurs restantes (109 avant), toutes antérieures (`no-explicit-any` surtout). TypeScript : 4 erreurs antérieures dans des fichiers non modifiés (`@/types/structure`, `PdfBookViewer` introuvables, `ReservationStats`) contre 476 avant (la plupart venaient des fichiers morts supprimés).
5. Build Android impossible ici (dépôts Google/Maven inaccessibles depuis cet environnement). Le code mobile n'a pas été modifié et correspond exactement à `d22943f`.

---

## 5. Tests et commandes exécutés

| Commande | Résultat |
|---|---|
| `git log`/`diff`/`merge-tree` sur les 3 dépôts et 6 branches | Analyse §1-2 |
| `npm ci` (eduniger-admin, lockfile restauré) | ✅ 752 paquets |
| `tsc -p tsconfig.app.json --noEmit` | ✅ aucune erreur dans les fichiers modifiés |
| `eslint .` | 100 erreurs préexistantes (109 avant) |
| `npm run build` | ✅ built in 12,9 s ; `dist/router.php` présent ; 0 occurrence de `localhost:8000`/`192.168.49` dans le bundle |
| `composer install` + `php artisan test` (eduniger-api) | ✅ **12 tests réussis** (14 assertions) |
| `php artisan route:list` | ✅ 132 routes, `api/broadcasting/auth` présente |
| Test du motif CORS | ✅ 172.20.10.10:8090 et 192.168.49.1:8090 acceptés ; IP publique et autre port refusés |
| Simulation du `.bundle` sur une copie du dépôt du PC | ✅ fetch + checkout OK |

**Résultat final du build** : Web ✅, API Laravel ✅ (tests), Android : à compiler dans Android Studio (aucune modification apportée).

---

## 6. À faire de votre côté

1. Double-cliquer `restauration\RESTAURER.bat`, puis dans `eduniger-admin` : `npm ci && npm run build`.
2. Récupérer `api/` depuis le serveur (§4.1) et le pousser sur `DERKARIOM/eduniger-api`.
3. Relire puis committer/pousser : `eduniger-admin` (branche `restauration-ancien-pc`), `eduniger-api` (4 fichiers), `eduniger-native` (`.gitignore`, `.gitmodules`, `env.lan.template`).
4. Changer le mot de passe root MySQL.
5. Créer `eduniger-api/.env` depuis `env.lan.template` (+ `php artisan key:generate`).
6. Décider du sort de la branche `Eduna` (fusion manuelle à faire, ou abandon).
