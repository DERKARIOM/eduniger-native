# Rapport de déploiement — EduNiger Web (API + Frontend) sur réseau local

**Date** : 4-5 septembre 2026
**Cible** : téléphone Samsung, Termux + proot-distro (Ubuntu 22.04.5), réseau local (Wi-Fi hotspot + tethering USB/SSH)

---

## 1. Environnement détecté

Le téléphone héberge en réalité **deux environnements Linux distincts**, avec des systèmes de fichiers séparés bien qu'ils partagent le même noyau et le même stockage `/sdcard` :

| | Termux (l'app elle-même) | proot-distro (Ubuntu 22.04.5 LTS) |
|---|---|---|
| Accès | Directement dans l'app Termux | `ssh -p 8080 cloud@172.20.10.10` |
| PHP | Absent au départ, puis **8.5.1** (installé pendant l'audit) | 8.1.2 (préinstallé) |
| Node.js | Non utilisé pour ce déploiement | v20.20.2 / npm 10.8.2 (préinstallé) |
| Rôle final | Héberge l'API Laravel (port 8000) et le Web statique (port 8090) | Héberge Apache (API mobile legacy, port 2222) et MySQL/MariaDB |

**Interfaces réseau du téléphone** (deux adresses distinctes, joignables simultanément) :
- `wlan0` → `172.20.10.10/28` — tethering USB/SSH utilisé par le Mac
- `p2p-wlan0-0` → `192.168.49.1/24` — hotspot Wi-Fi réel, utilisé par les autres appareils du réseau local

Le serveur Laravel (`app-web`) exige PHP `^8.2` (`composer.lock` impose en réalité `>= 8.4.0`), incompatible avec le PHP 8.1.2 du proot-distro : c'est pourquoi tout le backend a été installé et exécuté **dans Termux**, dont le PHP 8.5.1 a été installé spécifiquement pour ce déploiement.

Une base de données `eduniger` préexistait (utilisée par l'API mobile historique), distincte d'une base `eduniger_test` trouvée dans un ancien déploiement Laravel abandonné (`admin-api`, non utilisé — voir section 12).

---

## 2. Dépendances

**API (`app-web`, Laravel 12)** : PHP ^8.2 (installé : 8.5.1), extensions PDO/pdo_mysql, Composer, Sanctum (auth par token Bearer, pas de session cookie SPA).

**Frontend (`eduniger-admin`, React 18 + Vite 5 + TypeScript)** : Node ≥ 18, npm. Scripts disponibles : `dev`, `build` (`vite build`), `build:dev`, `lint`, `preview`.

---

## 3. Commandes d'installation exécutées

Dans **Termux** :
```bash
pkg install php -y        # installe PHP 8.5.1
pkg install composer -y
cd /sdcard/projets/eduniger/admin-api-new
composer install --no-interaction --prefer-dist --optimize-autoloader --ignore-platform-req=php
php artisan key:generate --force
```

Le flag `--ignore-platform-req=php` contourne une contrainte de version trop restrictive de `nette/schema`/`nette/utils` (plafonnées à PHP 8.4 dans leur propre `composer.json`, alors que Termux fournit PHP 8.5.1) — ce sont de simples bibliothèques utilitaires, le risque est jugé faible et confirmé sans incident après coup.

Le frontend (`eduniger-admin`) a été **construit en dehors du téléphone** (environnement de développement) puis transféré déjà compilé — voir section 4 pour la justification.

---

## 4. Commande de build

```bash
npm install
npm run build
```

**Choix de construire hors du téléphone** : `npm run build` (Vite + TypeScript + ~750 paquets npm) est une opération lourde en CPU. Plutôt que de faire tourner cette étape sur le téléphone (contraire à la consigne de ne pas gaspiller ses ressources), le build de production a été réalisé une fois en environnement de développement, et seul le résultat final (dossier `dist/`, fichiers statiques HTML/CSS/JS, ~23 Mo) a été transféré sur le téléphone. Le téléphone n'exécute donc aucun outil de build, seulement un serveur de fichiers statiques.

---

## 5. Serveur web choisi

**Le serveur de développement intégré de PHP** (`php -S`), pour l'API **et** pour le Web statique — un seul et même outil, déjà nécessaire pour Laravel, réutilisé pour tout :

- **Apache** (déjà présent dans le proot-distro) a été écarté : il est lié à PHP 8.1 (trop ancien pour Laravel 12) et sert déjà l'API mobile historique sur le port 2222 — le reconfigurer aurait risqué de casser un service en production.
- **Node/`npx serve`** (disponible dans le proot-distro) a été écarté pour le Web statique : cela aurait exigé de télécharger un paquet npm supplémentaire sur le téléphone et de faire cohabiter deux runtimes (PHP + Node) pour un unique déploiement de test, alors que PHP seul suffit et est déjà en place.
- Le serveur intégré de PHP est explicitement recommandé par le propre README du projet Laravel pour un usage de développement/test, correspond à la contrainte de légèreté demandée, et ne consomme des ressources que lorsqu'une requête est traitée.

---

## 6. Port et configuration réseau

| Service | Port | Commande de démarrage |
|---|---|---|
| API Laravel | **8000** | `php -d display_errors=0 -d log_errors=1 -S 0.0.0.0:8000 -t public public/index.php` |
| Web statique (React) | **8090** | `php -S 0.0.0.0:8090 -t . router.php` |
| API mobile legacy (inchangée) | 2222 | Apache, proot-distro |

Les deux serveurs du déploiement sont liés à `0.0.0.0` (toutes les interfaces), donc joignables aussi bien via `192.168.49.1` (hotspot Wi-Fi) que via `172.20.10.10` (tethering) — voir section 7 pour la résolution dynamique qui exploite cette double joignabilité.

**Remarque technique importante** : `php artisan serve` a été délibérément évité au profit d'un appel direct à `php -S ... public/index.php`. En interne, `artisan serve` relance un nouveau processus PHP pour traiter les requêtes HTTP, **sans reprendre les options `-d` passées à la commande `artisan` elle-même** — un problème réel rencontré pendant l'audit (voir section 12).

---

## 7. Changements effectués dans le code

### Backend (`app-web`)
1. **`app/Services/AIBookService.php`** — la vérification de `GEMINI_API_KEY` était dans le constructeur et levait une exception dès l'instanciation du contrôleur (y compris pour `php artisan route:list`), cassant l'application entière si la clé IA n'était pas configurée. Déplacée dans `callGemini()`, au moment où la clé est réellement utilisée.
2. **`config/database.php`** — `PDO::MYSQL_ATTR_SSL_CA` est dépréciée depuis PHP 8.5 ; la notice de dépréciation, affichée dans le corps de la réponse HTTP, cassait l'envoi des en-têtes Laravel ("headers already sent"), rendant **toute** route de l'API inutilisable. Corrigé pour utiliser `Pdo\Mysql::ATTR_SSL_CA` sur PHP 8.5+ et l'ancienne constante sinon (compatible avec toutes les versions).
3. **`config/cors.php`** — voir section 8.
4. **Compte MySQL dédié** — un utilisateur `eduniger_web`@`%` a été créé spécifiquement pour l'application (au lieu d'utiliser `root`, dont le compte `@localhost` n'accepte l'authentification que par socket Unix local, pas par TCP). Bonne pratique de sécurité en plus d'être la seule solution qui fonctionne dans ce contexte réseau.

### Frontend (`eduniger-admin`)
5. **`src/lib/runtimeConfig.ts`** (nouveau fichier) — les URLs vers l'API et le stockage sont désormais calculées **dynamiquement à partir de l'hôte utilisé pour charger la page** (`window.location.hostname`) plutôt que figées à une IP au moment du build. Résultat : la même version compilée du Web fonctionne indifféremment via `192.168.49.1:8090` ou `172.20.10.10:8090` (ou toute autre interface future), l'API étant toujours contactée sur le même hôte, au port 8000. Si une variable d'environnement `VITE_APP_API_URL` est explicitement définie au build (cas de la future migration HTTPS, voir section 9), elle prend le pas sur ce comportement dynamique.
6. **Bug corrigé** — `AuthorDetail.tsx`, `AuthorDialog.tsx`, `AuthorsList.tsx` construisaient l'URL des photos d'auteurs avec `VITE_APP_API_URL` (qui inclut déjà `/api`), produisant une URL cassée `/api/storage/authors/...` au lieu de `/storage/authors/...`. Corrigé pour utiliser l'URL de stockage dédiée.
7. **Bug corrigé** — `src/lib/echo.ts` (configuration Laravel Echo / Reverb) contenait une URL `http://localhost:8000/broadcasting/auth` codée en dur. Remplacée par une construction dynamique cohérente avec le reste de l'application.
8. **Variable manquante** — `VITE_APP_FILES_URL`, utilisée dans `QuickBookUpdateModal.tsx` mais jamais déclarée ni définie, a été ajoutée.
9. **Nettoyage** — un fichier résiduel Windows (`public/images.png:Zone.Identifier`, une métadonnée NTFS accidentellement présente dans le dépôt, non référencée dans le code) a été supprimé : son nom contenant `:` faisait échouer l'extraction de l'archive sur le stockage Android.
10. **`dist/router.php`** (nouveau fichier) — routeur minimal pour le serveur intégré de PHP, nécessaire pour que les routes gérées côté client par React Router (ex. `/agents/5`) retournent bien `index.html` au lieu d'un 404.

---

## 8. Changements CORS

`config/cors.php` (API) a été mis à jour pour autoriser le Web à appeler l'API depuis n'importe quelle interface réseau du téléphone, sans avoir à lister chaque adresse IP à l'avance :

```php
'allowed_origins' => [
    'http://localhost:8080',
    'http://localhost:8081',
    'http://localhost:8085',
],

'allowed_origins_patterns' => [
    '#^http://(192\.168\.\d{1,3}\.\d{1,3}|172\.(1[6-9]|2\d|3[01])\.\d{1,3}\.\d{1,3}|10\.\d{1,3}\.\d{1,3}\.\d{1,3}|localhost|127\.0\.0\.1):8090$#',
],
```

Ce motif n'autorise **que des adresses IP privées** (192.168.x.x, 172.16-31.x.x, 10.x.x.x) sur le port 8090 précis du Web — jamais d'adresse publique.

---

## 9. URL Web finale et URL API finale

- **Web** : `http://192.168.49.1:8090` (ou `http://172.20.10.10:8090` depuis un appareil sur cette interface — les deux fonctionnent grâce à la résolution dynamique)
- **API** : `http://<même hôte>:8000/api` (déterminé automatiquement par le Web à l'exécution)

**Migration future vers HTTPS** (`https://server.eduniger.com`) : il suffira de renseigner `VITE_APP_API_URL`, `VITE_APP_STORAGE_URL`, etc. dans `.env.production` avec l'URL fixe du domaine avant de relancer `npm run build` — cela désactive automatiquement la résolution dynamique décrite en section 7, sans autre changement de code. Côté API, ajouter le domaine HTTPS à `allowed_origins` dans `config/cors.php`.

---

## 10. Tests effectués et résultats

| Test | Résultat |
|---|---|
| `curl http://127.0.0.1:8000/api/me` (non authentifié) | `401 {"message":"Unauthenticated."}` ✅ |
| `curl http://127.0.0.1:8000/api/websiteStructures` (public, interroge la base) | `200`, données réelles (structures OpenLab, Kit TD) ✅ |
| `curl http://192.168.49.1:8000/...` et `http://172.20.10.10:8000/...` | `200` sur les deux interfaces ✅ |
| `curl http://192.168.49.1:8090` et `http://172.20.10.10:8090` | `200`, page HTML servie ✅ |
| Recherche d'IP figées dans le build (`grep "192.168.49.1\|172.20.10.10" dist/assets/*.js`) | Aucune occurrence après le correctif de résolution dynamique ✅ |
| Build de production (`npm run build`) | Succès, ~23 Mo (6,7 Mo compressé) ✅ |
| Connexion réelle dans le navigateur (compte de test), depuis le téléphone | Connexion réussie, tableau de bord chargé avec données ✅ |
| Connexion réelle depuis un second appareil (Mac, via `172.20.10.10`) | Connexion réussie après ajustements CORS et résolution dynamique ✅ |
| Application mobile Android (API legacy port 2222, hors périmètre de ce déploiement) | Fonctionnelle (incident réseau ponctuel résolu, sans lien avec ce déploiement) ✅ |

---

## 11. Problèmes rencontrés et corrections

1. **PHP 8.1 du proot-distro incompatible avec Laravel 12** → PHP 8.5.1 installé dans Termux (environnement séparé), qui héberge finalement toute l'API.
2. **`php artisan serve` ne propage pas les options `-d` au processus qui traite réellement les requêtes** → contourné en appelant directement `php -S 0.0.0.0:PORT -t public public/index.php`.
3. **Notices de dépréciation PHP 8.5 (`PDO::MYSQL_ATTR_SSL_CA`) cassant l'envoi des en-têtes HTTP ("headers already sent")** → corrigé dans `config/database.php` (constante conditionnelle) et masqué au niveau PHP (`-d display_errors=0 -d log_errors=1`, pratique standard en production de toute façon).
4. **`server.php` absent du squelette Laravel déployé** → utilisation directe de `public/index.php` comme point d'entrée du serveur intégré.
5. **Connexion MySQL refusée pour `root@localhost` via TCP** → le compte `root` n'accepte l'authentification que par socket Unix local (comportement standard Debian/Ubuntu) ; création d'un compte dédié `eduniger_web`@`%` pour l'application.
6. **Confusion `DB_HOST=localhost` (déclenche une connexion par socket, invisible depuis Termux) vs `127.0.0.1` (TCP, fonctionne à travers les deux environnements)** → `.env` corrigé pour utiliser `127.0.0.1`.
7. **Fichier résiduel Windows (`images.png:Zone.Identifier`) bloquant l'extraction de l'archive sur le stockage Android** → supprimé du dépôt source.
8. **URL de l'API figée au build (`192.168.49.1`), invalidant tout test depuis une autre interface réseau (ex. le Mac via `172.20.10.10`)** → résolution dynamique de l'hôte introduite (section 7).
9. **Serveurs PHP tués par Android en arrière-plan** → résolu avec `termux-wake-lock` (section 13).
10. **Transferts de fichiers volumineux vers le Mac intermittents** → contournés en découpant les archives en morceaux de 2 Mo.

---

## 12. Nettoyage recommandé (non effectué, à valider avec vous)

Un ancien déploiement Laravel abandonné (`/sdcard/projets/eduniger/admin-api`) a été découvert pendant l'audit : il ne contient **aucun** des correctifs de sécurité de l'audit précédent (pas de middleware `role`/`structure.scope`), pointe vers une base `eduniger_test` obsolète, et contient deux fichiers volumineux inutiles (`eduniger-api.zip`, 81 Mo ; `storage/app.zip`, 24 Mo). Il n'a pas été utilisé ni modifié pendant ce déploiement (le nouveau dossier `admin-api-new` a été créé séparément), mais occupe de l'espace disque et représente un risque s'il était accidentellement exposé. Recommandation : le supprimer une fois ce déploiement validé — à faire vous-même ou sur demande explicite.

---

## 13. Procédure de redémarrage

À exécuter à chaque redémarrage du téléphone ou si les serveurs s'arrêtent (Termux fermé, veille prolongée) :

```bash
# 1. Empecher Android de tuer les processus en arriere-plan de Termux
termux-wake-lock

# 2. Demarrer l'API (port 8000)
cd /sdcard/projets/eduniger/admin-api-new
nohup php -d display_errors=0 -d log_errors=1 -S 0.0.0.0:8000 -t public public/index.php > storage/logs/artisan_serve.log 2>&1 &

# 3. Demarrer le Web (port 8090)
cd /sdcard/projets/eduniger/web-dist
nohup php -S 0.0.0.0:8090 -t . router.php > serve.log 2>&1 &

# 4. Verifier que tout repond
sleep 2
curl -s -o /dev/null -w "API: [HTTP %{http_code}]\n" http://127.0.0.1:8000/api/websiteStructures
curl -s -o /dev/null -w "Web: [HTTP %{http_code}]\n" http://127.0.0.1:8090
```

**Pour arrêter proprement** :
```bash
pkill -f " -S 0.0.0.0:8000"
pkill -f " -S 0.0.0.0:8090"
```

**Consommation de ressources** : `termux-wake-lock` maintient uniquement le CPU actif pour les tâches en arrière-plan (pas l'écran), impact batterie minime — c'est la solution la plus légère possible pour ce besoin, conformément à la consigne de ne pas gaspiller les ressources du téléphone. Sans elle, Android peut interrompre les serveurs après une mise en veille prolongée ; il suffit alors de relancer la procédure ci-dessus.

---

## Identifiants de test créés pendant l'audit

Un compte Super Admin dédié aux tests a été créé (à supprimer une fois les vérifications terminées) :
- Email : `test.deploiement@eduniger.local`
- Mot de passe : `TestDeploiement@2026`

Pour le supprimer : `php artisan tinker --execute="App\Models\StructAgent::where('email','test.deploiement@eduniger.local')->delete();"`
