# Déploiement d'Elim en production — Hostinger (Business Web Hosting)

Domaine : **kawtal-consulting.com** · Offre : **Business Web Hosting** (hPanel, PHP, MySQL, SSH, cron)

## Architecture cible

| Sous-domaine | Rôle | Racine web |
|---|---|---|
| `elim.kawtal-consulting.com` | API PHP historique (`/eduniger/api/…`) et ressources (`/fabi/ressources/…`) | `~/domains/kawtal-consulting.com/public_html/elim` |
| `api.kawtal-consulting.com` | API Laravel 12 (`/api/…`) | `~/domains/kawtal-consulting.com/public_html/api` → lien symbolique vers `~/elim-api/public` |
| `admin.kawtal-consulting.com` | Back-office web (application React) | `~/domains/kawtal-consulting.com/public_html/admin` |
| `elim.kawtal-consulting.com/eduniger/ai/` | Assistant IA Eduna (PHP + API Google Gemini) | `…/public_html/elim/eduniger/ai/` |

Tout est hébergé chez Hostinger : plus aucune dépendance au serveur Hetzner (`78.46.46.154`). Les fichiers des livres sont dans `~/elim-api/storage/app/private/`.

Secrets (jamais dans `public_html`) : `~/private/`
- `elim-config.php` : accès MySQL + secret JWT de l'API historique
- `elim-16f44-service-account.json` : compte de service Firebase d'Elim
- `fabi-e56e6-service-account.json` : compte de service de l'ancienne application, facultatif
- `my.cnf` : identifiants pour la sauvegarde MySQL

> `~` = `/home/u883737194`. Sur Hostinger, les sous-domaines sont rangés dans `~/domains/kawtal-consulting.com/public_html/<sous-domaine>`. Le nom exact apparaît dans hPanel → Avancé → Accès SSH.

---

## Étape 1 — Sous-domaines et HTTPS
1. hPanel → **Domaines → Sous-domaines** : créer `elim`, `api` et `admin`.
2. hPanel → **Sécurité → SSL** : installer le certificat gratuit sur les trois sous-domaines.
3. Activer **Forcer le HTTPS** sur chacun.

## Étape 2 — PHP
hPanel → **Avancé → Configuration PHP**, pour chaque sous-domaine :
- **Version** : PHP **8.3**, ou 8.2 au minimum.
- **Extensions** : `pdo_mysql`, `mysqli`, `openssl`, `curl`, `mbstring`, `fileinfo`, `gd`, `zip`, `intl`, `bcmath`, `xml`, `ctype`, `tokenizer`.
- **Options** : `memory_limit = 256M`, `upload_max_filesize = 64M`, `post_max_size = 64M`, `max_execution_time = 120`.

## Étape 3 — Base de données
1. hPanel → **Bases de données → MySQL** : créer la base `uXXXXXXXX_elim` et l'utilisateur `uXXXXXXXX_elim`, avec un mot de passe fort. Noter les trois valeurs.
2. Sur le serveur actuel, **remettre MySQL en marche** : il refusait les connexions depuis le 2 octobre. Exporter ensuite la base :
   ```
   mysqldump -u root -p --single-transaction --routines eduniger > elim.sql
   ```
3. Importer l'export dans hPanel → phpMyAdmin → Importer, ou en SSH :
   ```
   mysql -u uXXXXXXXX_elim -p uXXXXXXXX_elim < elim.sql
   ```

## Étape 4 — Accès SSH
hPanel → **Avancé → Accès SSH** : activer l'accès, puis se connecter :
```
ssh -p 65002 uXXXXXXXX@<IP fournie par hPanel>
mkdir -p ~/private && chmod 700 ~/private
```

## Étape 5 — API PHP historique (`elim.kawtal-consulting.com`)
1. Envoyer dans `~/domains/kawtal-consulting.com/public_html/elim/`, par le **Gestionnaire de fichiers** ou en SFTP :
   - `.htaccess`, depuis `deploy/hostinger/elim/public_html/.htaccess` ;
   - `eduniger/connectBDD.php`, depuis `deploy/hostinger/elim/public_html/eduniger/connectBDD.php` ;
   - `eduniger/api/`, avec **tout le contenu de `server/api/` sauf `hash_password.php`**, qui ne doit pas être envoyé ;
   - `fabi/ressources/` : copie du dossier `/var/www/html/fabi/ressources/` de l'ancien serveur (photos de profil, couvertures).
2. Créer `~/private/elim-config.php` à partir de `deploy/hostinger/private/elim-config.example.php`. Y mettre les identifiants MySQL de l'étape 3 et le **même `JWT_SECRET` que l'ancien serveur**, sinon tous les utilisateurs sont déconnectés.
3. Déposer `~/private/elim-16f44-service-account.json` : Firebase → Paramètres du projet → Comptes de service → Générer une nouvelle clé privée.
4. Protéger les secrets :
   ```
   chmod 600 ~/private/*
   ```
5. Test :
   ```
   curl -s "https://elim.kawtal-consulting.com/eduniger/api/category.php?id_number=test"
   ```
   Le serveur doit répondre en JSON. Une erreur « Configuration serveur manquante » signifie que `~/private/elim-config.php` est absent ou illisible.

## Étape 6 — API Laravel (`api.kawtal-consulting.com`)
1. Envoyer le dossier `server/admin-api-new/` **sans `vendor/` ni `.env`** dans `~/elim-api/`. Le plus simple est de le compresser en zip, de l'envoyer avec le Gestionnaire de fichiers, puis de cliquer sur « Extraire ».
2. Créer `elim-api/.env` à partir de `deploy/hostinger/api/env.production.example`. **Reprendre l'`APP_KEY` actuelle.**
3. Faire pointer la racine du sous-domaine vers `public/` :
   ```
   cd ~/domains/kawtal-consulting.com/public_html
   rm -rf api && ln -s ~/elim-api/public api
   ```
4. Copier `deploy/hostinger/api/deploy_api.sh` dans `elim-api/deploy/`, puis lancer :
   ```
   bash ~/elim-api/deploy/deploy_api.sh
   ```
   Le script installe les dépendances (`composer install --no-dev`), lance les migrations, crée le lien `storage:link`, met en place les caches de production et règle les droits.
5. Test :
   ```
   curl -s https://api.kawtal-consulting.com/api/structures -H "Accept: application/json"
   ```
   La réponse attendue est un JSON (401 si l'authentification est requise). Une page HTML d'erreur signifie un problème de configuration : voir `elim-api/storage/logs/`.

## Étape 7 — Tâches planifiées
hPanel → **Avancé → Tâches cron** : ajouter les trois lignes de `deploy/hostinger/cron.txt` (planificateur Laravel, file d'attente, sauvegarde MySQL quotidienne) en remplaçant `uXXXXXXXX`.

Pour la sauvegarde, créer `~/private/my.cnf` :
```
[client]
user=uXXXXXXXX_elim
password=MOT_DE_PASSE
```
puis :
```
chmod 600 ~/private/my.cnf
```

## Étape 8 — Back-office (`admin.kawtal-consulting.com`)
Build : `deploy/hostinger/elim-admin.zip` (sources `eduniger-admin`, branche `prod-hostinger`, `npm run build` avec `.env.production`).
API appelée : `https://api.kawtal-consulting.com/api`. Temps réel désactivé (`VITE_REALTIME_ENABLED=false`, pas de Reverb sur l'offre Business).

1. hPanel : sous-domaine `admin` créé, SSL installé, HTTPS forcé. Noter son dossier racine.
2. Envoi et extraction (PowerShell puis SSH) :
   ```
   scp -P 65002 deploy\hostinger\elim-admin.zip u883737194@185.224.138.118:elim-admin.zip
   ssh -p 65002 u883737194@185.224.138.118
   D=~/domains/kawtal-consulting.com/public_html/admin   # dossier racine du sous-domaine
   mkdir -p $D && cd $D && rm -rf assets index.html && unzip -oq ~/elim-admin.zip && rm ~/elim-admin.zip
   ```
3. CORS côté API : `.env` de l'API → `CORS_ALLOWED_ORIGINS=https://admin.kawtal-consulting.com`, et `config/cors.php` corrigé (une 2e clé `allowed_origins` codée en dur annulait la valeur du `.env`). Envoyer `config/cors.php`, puis `bash ~/elim-api/deploy/deploy_api.sh`.
4. Test : `https://admin.kawtal-consulting.com` → page « Connexion » Elim, connexion avec un compte administrateur.

## Étape 9 — Application Android
Déjà fait dans le code (branche `feat/prod-hostinger`) :
- les adresses de production sont dans `res/values/strings.xml` ;
- le HTTP en clair est interdit en release, sans exception ;
- les adresses locales restent autorisées dans les builds debug ;
- `versionCode` est passé à 14 ;
- la signature de release est lue depuis `keystore.properties`, qui n'est pas versionné.

À faire :
1. Créer la clé de signature **une seule fois**. La conserver avec ses mots de passe dans au moins deux endroits sûrs : sans elle, aucune mise à jour Play Store n'est possible.
   ```
   keytool -genkeypair -v -keystore elim-release.jks -alias elim -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Copier `keystore.properties.example` en `keystore.properties`, puis le remplir.
3. Relever les empreintes avec `.\gradlew.bat signingReport` et ajouter le **SHA-1 et le SHA-256 de release** dans Firebase (`elim-16f44`). Retélécharger ensuite `google-services.json`.
4. Générer le bundle :
   ```
   .\gradlew.bat bundleRelease
   ```
   Il est produit dans `eduniger_native/build/outputs/bundle/release/`.

> `app_version` reste à `3.1.3`, car le serveur compare cette valeur à la table `Version` pour forcer les mises à jour. Pour passer en 4.0.0, changer **en même temps** `app_version`, `versionName` et la ligne de la table `Version`.

## Étape 10 — Recette avant ouverture
- [ ] Connexion par matricule et mot de passe, puis déconnexion et reconnexion
- [ ] Accueil : structures, recommandations, catégories
- [ ] Ouverture d'un livre, téléchargement d'un PDF, lecture d'un audio
- [ ] Photo de profil : envoi et affichage (`/fabi/ressources/profile/`)
- [ ] Notification envoyée depuis le back-office et reçue sur le téléphone
- [ ] Connexion au back-office et liste des livres et utilisateurs
- [ ] Test en **4G** (pas en Wi-Fi local)

## Étape 11 — Après la mise en ligne
- Surveillance : UptimeRobot (gratuit) sur `https://elim.kawtal-consulting.com/eduniger/api/category.php?id_number=test` et `https://api.kawtal-consulting.com/up`.
- Garder l'ancien serveur **en lecture seule** une semaine, puis l'arrêter.
- Les sauvegardes quotidiennes de Hostinger sont actives. Vérifier aussi `~/backups/`.

## Points connus, à traiter ensuite
- **Connexion Google** : corrigée. L'application appelle `POST /api/user/login/google` (Laravel), qui fait vérifier l'ID token par Google. `login_google.php` n'est plus utilisé et peut être supprimé du serveur.
- **Endpoints absents du serveur** : l'application appelle `AddBook.php`, `Category.php`, `CategoryIn.php`, `GradeSyn.php` et `upload.php`, qui n'existent pas dans `server/api/`. Les fichiers présents s'appellent `add_book.php`, `category.php`… et Linux distingue les majuscules. Ces fonctions ne marchaient déjà pas sur l'ancien serveur.
- **Ancienne clé de signature** : le fichier `keystore` est encore versionné dans le dépôt GitHub public. Ne pas l'utiliser pour Elim, et le retirer de l'historique Git.
