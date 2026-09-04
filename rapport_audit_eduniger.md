# Rapport d'audit technique — EduNiger

**Date :** 4 septembre 2026
**Périmètre :** application Android native (`eduniger_native/`), API PHP (`api/`), backend Laravel (`app-web/`)
**Méthode :** analyse statique complète du code source (aucun accès à l'environnement d'exécution/serveur réel, aucun build Android n'a pu être lancé dans cet environnement — voir section « Tests effectués »)

---

## 0. Ce qu'est réellement le projet

Le dossier `eduniger-native` n'est **pas** une application React Native/Flutter : c'est une application **Android native en Java** (Gradle, sans Retrofit ni Volley — les appels réseau utilisent `OkHttp` et `HttpURLConnection` directement, à la main, écran par écran). Le projet regroupe trois sous-parties distinctes, chacune avec son propre dépôt Git :

- `eduniger_native/` — l'application Android (module Gradle `EduNiger`, package `com.ninotech.eduniger`)
- `api/` — l'API PHP « historique » consommée par l'app (fichiers PHP plats, PDO)
- `app-web/` — un backend/panneau d'administration **Laravel** séparé (`admin-api`), qui semble servir les fichiers (couvertures, PDF, audio) stockés dans `storage/`

Deux dossiers `app/` et `eduniger/` à la racine ne contiennent que des artefacts de build (`build/`) et aucun code source : ce sont des résidus, sans impact.

---

## 1. Centralisation de l'URL du serveur — fait, en un seul endroit

**Avant l'audit**, l'adresse `192.168.49.1:2222` (et une seconde adresse de production `78.46.46.154`) étaient codées en dur, de façon incohérente, dans **7 fichiers différents** :

| Fichier | Ce qui était codé en dur |
|---|---|
| `res/values/strings.xml` | déjà partiellement centralisé (`url_server`, `url_api`) |
| `model/data/Server.java` | valeurs par défaut (légitime, mais incomplet) |
| `model/service/AudioDownloadService.java` | `192.168.49.1:2222` × 4 |
| `model/data/AudioDownloader.java` | `192.168.49.1:2222` × 3 (classe inutilisée, voir §5) |
| `model/service/PdfDownloadService.java` | `78.46.46.154` × 2 (⚠️ IP de **production**, incohérent avec le reste du fichier) |
| `controleur/activity/ChatAiActivity.java` | `78.46.46.154` (endpoint IA) |
| `controleur/activity/ServerActivity.java` | `78.46.46.154` et `192.168.49.1:2222` × 3 (écran de changement de serveur) |

**Après l'audit**, `model/data/Server.java` est l'**unique point d'entrée** pour toute URL serveur de l'application, avec ses valeurs par défaut dans `res/values/strings.xml` :

```
url_server     → http://192.168.49.1:2222/eduniger/     (base API "eduniger")
url_api        → http://192.168.49.1:2222/eduniger/api/
url_host       → http://192.168.49.1:2222                (hôte nu, pour les téléchargements de fichiers)
url_host_prod  → http://78.46.46.154                      (hôte de stockage "production")
url_ai         → http://78.46.46.154/eduniger/ai/eduna_unified.php
```

Pour migrer vers `https://server.eduniger.com`, **il suffit désormais de modifier ces 5 lignes dans `strings.xml`** (ou les constantes en tête de `Server.java`) — plus aucun autre fichier à toucher. `Server.java` porte aussi désormais deux constantes publiques (`PRESET_PROD_FABI`, `PRESET_DEV_FABI`) utilisées par l'écran `ServerActivity` (sélecteur de serveur de test), qui n'a donc plus besoin de retaper les URLs.

`grep` de contrôle final : plus aucune occurrence de `192.168.49.1` ou `78.46.46.154` en dehors de `strings.xml` et `Server.java` dans tout `eduniger_native/`.

Une ressource `ip_eduna` existait déjà dans `strings.xml` mais n'était **utilisée nulle part** (tentative de centralisation abandonnée) : elle a été réactivée sous le nom `url_host_prod`.

**Point d'attention** : `PdfDownloadService.java` pointait en dur vers l'IP de production (`78.46.46.154`) pour les couvertures/PDF, alors que le reste du même fichier utilisait déjà `Server.getUrlServer()` (dynamique, donc le serveur configuré). C'est très probablement une incohérence involontaire (copier-coller), mais je l'ai traitée avec prudence : plutôt que de forcer ce flux sur le serveur dynamique (ce qui aurait changé le comportement par défaut), j'ai créé une constante dédiée `getUrlHostProd()` qui **conserve exactement le comportement actuel** tout en le centralisant. Si ce choix de toujours servir les PDF/couvertures depuis la production est intentionnel, tout va bien ; si c'était un bug, il suffit maintenant de changer une seule ligne.

---

## 2. Failles de sécurité critiques trouvées et corrigées

C'est le point le plus important de cet audit. **Trois secrets réels étaient exposés en clair dans le code source versionné dans Git :**

### 2.1 Mot de passe root MySQL exposé (CRITIQUE)
`api/eduna_unified` contenait :
```php
define('DB_USER', 'root');
define('DB_PASS', 'Password@uam2025');
```
→ Corrigé : lecture via variables d'environnement (`getenv('EDUNIGER_DB_USER')`, etc.), sans valeur de repli pour le mot de passe.

### 2.2 Le même mot de passe, exposé une seconde fois (CRITIQUE)
Le mot de passe `Password@uam2025` était **aussi** codé en dur dans le backend Laravel, à deux endroits, pour un compte NextCloud/WebDAV `root` :
- `app-web/config/filesystems.php`
- `app-web/app/Services/NextCloudStorage.php` (en valeur de repli d'un `env()`)

→ Corrigés tous les deux (lecture `env()` sans repli sur le mot de passe).

**⚠️ Action requise de votre part — je ne peux pas la faire à votre place :** ce mot de passe étant identique aux deux endroits, il s'agit très probablement de votre mot de passe root d'infrastructure réel. Il est exposé dans l'historique Git même après ma correction (supprimer un secret d'un fichier ne l'efface pas des anciens commits). **Changez ce mot de passe côté serveur MySQL et NextCloud dès que possible**, et envisagez un utilisateur MySQL dédié à l'application plutôt que `root`.

### 2.3 Clé API OpenAI exposée (CRITIQUE)
`api/callOpenAi.php` contenait une clé API OpenAI active en clair (`sk-proj-...`), plus deux anciennes clés en commentaire. Cet endpoint n'est **appelé par aucun code** de l'application (l'app utilise `eduna_unified.php`), mais il était néanmoins **accessible publiquement sans aucune authentification** — n'importe qui connaissant l'URL pouvait consommer votre quota OpenAI à volonté.

→ Corrigé : lecture via `getenv('OPENAI_API_KEY')`, avec une réponse d'erreur propre si la variable n'est pas définie.

**⚠️ Action requise : révoquez cette clé sur platform.openai.com et générez-en une nouvelle**, indépendamment de la correction de code.

### 2.4 Upload de fichier non restreint = exécution de code à distance possible (CRITIQUE)
`api/uploadImg.php` acceptait **n'importe quel fichier**, sous son nom d'origine, sans aucune vérification :
```php
move_uploaded_file($file_tmp, "/var/www/html/fabi/ressources/profile/".$file_name);
```
Un attaquant pouvait envoyer un fichier `shell.php` (déguisé en "image") et l'exécuter ensuite en visitant son URL — un scénario classique de prise de contrôle du serveur. Aucune limite de taille, aucun contrôle du type réel du fichier, et le nom fourni par le client permettait potentiellement un `../../` (traversée de répertoire).

→ Réécrit : vérification que le contenu est réellement une image (`getimagesize`), liste blanche d'extensions (jpg/png/webp), nom de fichier généré aléatoirement côté serveur (le nom du client n'est jamais réutilisé), limite de 5 Mo. J'ai vérifié que l'app Android (`LibraryFragment.java`) n'exploite pas le nom de fichier retourné par le serveur — ce changement est donc sans impact fonctionnel côté app.

### 2.5 Récepteurs de diffusion (broadcast) exposés à toute autre application (ÉLEVÉ)
17 appels à `registerReceiver(..., Context.RECEIVER_EXPORTED)` dans 13 fichiers, pour des diffusions **strictement internes** à l'app (progression de téléchargement, rafraîchissement d'écran, réponse du chatbot...). `RECEIVER_EXPORTED` autorise **n'importe quelle autre application installée sur le téléphone** à envoyer de fausses diffusions à EduNiger (par exemple, déclencher un faux "téléchargement terminé" ou perturber l'interface).

→ Corrigé partout en `Context.RECEIVER_NOT_EXPORTED` (déjà la valeur correcte utilisée — comme seule exception — dans `AudioPlayerService.java`, ce qui confirme qu'il s'agissait d'une erreur systématique et non d'un choix voulu). Fichiers concernés : `CategoryActivity`, `AudioPlayerActivity`, `MainActivity`, `BookActivity`, `AuthorActivity`, `StructureActivity`, `SearchActivity`, `ChatAdapter`, `ChatBotFragment`, `StructureFragment`, `BooksFragment`, `HomeFragment`, `CategoryFragment`.

### 2.6 Fuite d'informations via les messages d'erreur (MOYEN)
**30 fichiers** de `api/` renvoyaient directement le message d'exception PHP brut au client (`echo $e->getMessage();`), pouvant exposer la structure de la base de données, des chemins serveur, etc. en cas d'erreur.

→ Corrigé partout : le détail de l'erreur est désormais journalisé côté serveur (`error_log`) et le client ne reçoit plus qu'un message générique (`"error"` ou `{"error": "Une erreur est survenue."}`, en conservant exactement le même format de réponse — texte brut ou JSON — qu'avant, pour ne rien casser côté app).

### 2.7 Sauvegarde automatique Android non filtrée (MOYEN)
`android:allowBackup="true"` était actif sans aucune règle d'exclusion : la base SQLite locale (`data.db`, qui contient les identifiants de session, mots de passe hashés et jetons) pouvait être extraite via la sauvegarde cloud Google ou `adb backup` sur un appareil déverrouillé.

→ Corrigé : `data_extraction_rules.xml` et `backup_rules.xml` excluent désormais `data.db` et les préférences `server_prefs`.

### 2.8 Trafic HTTP en clair autorisé pour n'importe quel hôte (MOYEN — préparation HTTPS)
`android:usesCleartextTraffic="true"` autorisait du HTTP non chiffré vers **n'importe quel serveur**, pas seulement les serveurs de test connus.

→ Remplacé par une `network_security_config.xml` : le HTTP en clair n'est désormais autorisé que pour les deux hôtes de test connus (`192.168.49.1`, `78.46.46.154`) ; **tout le reste, y compris votre futur `https://server.eduniger.com`, sera chiffré par défaut**, sans qu'il y ait quoi que ce soit à changer dans ce fichier lors de la bascule.
⚠️ Effet de bord à connaître : le champ « serveur personnalisé » de `ServerActivity` (saisie libre d'une URL de test) ne fonctionnera plus en HTTP pour une IP autre que ces deux-là ; ajoutez-la dans `network_security_config.xml` si nécessaire pour vos tests.

### 2.9 Non corrigé, à traiter avec une vraie migration (recommandation)
Les mots de passe utilisateurs sont hashés en **SHA-256 sans sel** (`hash_password.php`, cohérent avec le hash déjà effectué côté Java avant l'envoi), et comparés par simple égalité SQL dans `login.php`. C'est nettement plus faible que `bcrypt`/`Argon2` en cas de fuite de la base. Je n'ai **pas** touché à ce mécanisme : changer l'algorithme casserait l'authentification de tous les comptes existants sans une vraie stratégie de migration (par exemple : re-hasher en bcrypt au prochain login réussi). Je recommande de traiter ce chantier séparément.
`api/hash_password.php` est par ailleurs un endpoint de debug accessible publiquement, qui accepte un mot de passe en paramètre **GET** (donc journalisé en clair dans les logs du serveur web) et retourne le mot de passe en clair **et** son hash dans la réponse. Je recommande de le supprimer ou de le protéger — je ne l'ai pas fait car je ne suis pas certain qu'il ne soit pas encore utilisé par un outil interne.
Enfin, `res/values/strings.xml` du côté app, et `app-web/.env.example` contiennent une `APP_KEY` Laravel qui ressemble à une vraie clé générée (pas un simple exemple) : vérifiez qu'elle n'est pas la clé réellement utilisée en production ; si c'est le cas, régénérez-la (`php artisan key:generate`).

---

## 3. Stabilité — l'application ne remontait pas les échecs de téléchargement

En creusant les flux de téléchargement (couverture, audio, PDF), j'ai trouvé un bug de fiabilité réel : en cas d'échec réseau, **l'erreur était avalée silencieusement** (`catch (Exception e) { e.printStackTrace(); }`), sans aucun retour à l'utilisateur :

- La notification restait bloquée sur « Téléchargement en cours... » indéfiniment.
- L'écran (`BookActivity`) affichait quand même **« Téléchargé avec succès »**, transformait le bouton en « Lire »/« Ouvrir », et pour le PDF tentait d'ouvrir un fichier qui n'existait pas réellement (`ElectronicDownloader`, dans le cas de la variante `AsyncTask`, insérait même des chemins `null` en base après un échec, en affichant quand même le message de succès).

→ Corrigé dans `AudioDownloadService.java`, `PdfDownloadService.java`, `AudioDownloader.java`, `ElectronicDownloader.java` et `BookActivity.java` : en cas d'échec, une notification d'échec est affichée, un signal `success=false` est envoyé, et l'écran repasse sur l'état « non téléchargé » avec un message d'erreur clair au lieu du faux message de succès. Le paramètre `success` a une valeur par défaut de `true` pour ne rien casser sur un éventuel appelant existant qui ne l'enverrait pas.

`DownloadFile.java` (le cœur du téléchargement, utilisé par tous ces flux) est en revanche déjà bien écrit : timeouts corrects (15 s connexion / 30 s lecture), vérification du code HTTP, vérification des « magic bytes » pour les PDF, nettoyage des fichiers partiels en cas d'erreur. Aucune correction nécessaire ici.

---

## 4. Performance — appels réseau

- **Aucune bibliothèque réseau centralisée** (pas de Retrofit/Volley) : chaque écran fait ses propres appels avec OkHttp ou `HttpURLConnection`.
- **34 instanciations distinctes de `new OkHttpClient()`** réparties dans **26 fichiers**. Chaque `OkHttpClient` possède son propre pool de connexions et son propre pool de threads : les créer à la volée à chaque écran/appel est un vrai gaspillage de ressources (mémoire, connexions TCP non réutilisées, pas de timeout par défaut cohérent puisque chaque instance utilise les valeurs par défaut d'OkHttp sans configuration explicite).
  **Je n'ai pas appliqué cette correction** : elle toucherait 26 fichiers et 34 sites d'appel, et je n'ai pas pu compiler/tester le projet dans cet environnement (voir §7) pour vérifier l'absence de régression sur un changement aussi large. Je recommande de créer une classe `NetworkClient` avec un `OkHttpClient` unique (singleton, avec des timeouts explicites) et de migrer les écrans progressivement, en testant à chaque étape.
- Aucun mécanisme de cache HTTP ni de déduplication des requêtes identiques n'a été trouvé (par exemple, plusieurs écrans rechargent les mêmes données de structure/catégories sans les partager). C'est un chantier plus lourd, hors du périmètre des corrections directes de cet audit — je le signale comme piste d'amélioration.

---

## 5. Autres constats notables

- **Code mort** : les classes `model/data/AudioDownloader.java` et `model/data/ElectronicDownloader.java` (implémentations `AsyncTask`, API dépréciée depuis longtemps) ne sont **instanciées nulle part** dans le code — elles sont intégralement remplacées par `AudioDownloadService`/`PdfDownloadService`. Je les ai tout de même corrigées (URL centralisée + gestion d'échec) par cohérence et au cas où elles seraient réactivées, mais je recommande de les supprimer si elles sont bien confirmées inutilisées.
- **Bug de copier-coller dans `ServerActivity.java`** (écran de changement de serveur) : sélectionner le préréglage « production » (`78.46.46.154`) enregistrait en réalité l'URL de **développement** (`192.168.49.1:2222`) — les deux branches du `switch` pointaient vers la même valeur. Corrigé.
- `ChatAiActivity.java` envoie systématiquement un identifiant utilisateur codé en dur (`ID_NUMBER = "94961793"`) à l'assistant IA, quel que soit l'utilisateur réellement connecté. Je n'ai pas touché à ce point (comportement métier, pas un problème de sécurité en soi), mais si l'historique de conversation IA est censé être personnel à chaque utilisateur, cela mérite vérification — tous les utilisateurs partagent actuellement la même session IA côté serveur.
- Toutes les requêtes SQL de `api/` utilisent des **requêtes préparées PDO** (aucune concaténation directe de `$_GET`/`$_POST` trouvée) — bon niveau de protection contre les injections SQL, à l'exception du fichier `eduna_unified` (voir ci-dessous).
- `api/eduna_unified` utilise `mysqli` avec des requêtes préparées `mysqli_prepare`/`bind_param` — cohérent, mais c'est le seul fichier de l'API à ne pas utiliser PDO comme le reste ; à surveiller si vous unifiez un jour la couche d'accès aux données.
- Les fichiers PHP renvoient systématiquement `Access-Control-Allow-Origin: *` (CORS ouvert à tous). Pour une API consommée uniquement par l'app mobile (pas de navigateur/cookies impliqués), ce n'est pas critique, mais ce n'est pas nécessaire non plus — à restreindre si un jour un client web utilisant des cookies de session est ajouté.

---

## 6. Fichiers modifiés

**Application Android (`eduniger_native/`, 11 fichiers) :**
`model/data/Server.java`, `res/values/strings.xml`, `model/service/AudioDownloadService.java`, `model/service/PdfDownloadService.java`, `model/data/AudioDownloader.java`, `model/data/ElectronicDownloader.java`, `controleur/activity/ChatAiActivity.java`, `controleur/activity/ServerActivity.java`, `controleur/activity/BookActivity.java`, `AndroidManifest.xml`, `res/xml/data_extraction_rules.xml`, `res/xml/backup_rules.xml` (nouveau : `res/xml/network_security_config.xml`)

Correctif `RECEIVER_NOT_EXPORTED` (13 fichiers) : `CategoryActivity.java`, `AudioPlayerActivity.java`, `MainActivity.java`, `AuthorActivity.java`, `StructureActivity.java`, `SearchActivity.java`, `ChatAdapter.java`, `ChatBotFragment.java`, `StructureFragment.java`, `BooksFragment.java`, `HomeFragment.java`, `CategoryFragment.java` (+ `BookActivity.java` déjà compté ci-dessus)

**API PHP (`api/`, 33 fichiers modifiés) :**
`eduna_unified` (identifiants DB), `callOpenAi.php` (clé OpenAI), `uploadImg.php` (réécrit), et 30 fichiers pour la fuite de messages d'erreur : `Author.php`, `AuthorAudioBook.php`, `AuthorPDFBook.php`, `AuthorPhysicBook.php`, `AuthorSimular.php`, `CategoryStrut.php`, `FabiolaBook.php`, `PreRegister.php`, `ReceiveComments.php`, `RegisterAuthor.php`, `Reservation.php`, `SendComments.php`, `SimilarBook.php`, `StructBookMore.php`, `StructCategoryIn.php`, `Structure2.php`, `StructureMore.php`, `Suggestion.php`, `Tones.php`, `add_book.php`, `author_book.php`, `author_top.php`, `book.php`, `books.php`, `category.php`, `category_in.php`, `ranking.php`, `register.php`, `structure_more.php`.

**Backend Laravel (`app-web/`, 2 fichiers) :** `config/filesystems.php`, `app/Services/NextCloudStorage.php`

**⚠️ Point technique à vérifier de votre côté :** `api/eduna_unified` était en réalité un **lien symbolique** vers `/var/www/html/eduniger/ai/eduna_unified.php` sur votre machine (hors du dossier connecté à cette session). Le pont de fichiers utilisé pour cet audit a pu lire son contenu à travers le lien, mais l'écriture l'a transformé en fichier normal dans votre dépôt `api/` — **je n'ai pas pu confirmer que ma correction a bien atteint le fichier réel servi par votre serveur PHP local à ce chemin**. Merci de vérifier directement `/var/www/html/eduniger/ai/eduna_unified.php` sur votre machine et d'y appliquer la même correction (retirer `DB_PASS='Password@uam2025'` et `DB_USER='root'` en dur, utiliser `getenv()`) si ce n'est pas déjà le cas — c'est le fichier le plus critique de tout cet audit.

**Effet de bord sans gravité :** en parcourant `api/`, les bits d'exécution (`755`) de plusieurs fichiers PHP non modifiés par moi sont apparus comme changés (`755` → `644`) dans `git status` — probablement un artefact du pont de fichiers utilisé par cette session, sans rapport avec le contenu. Sans conséquence sur le fonctionnement (PHP n'a pas besoin du bit d'exécution servi par Apache/PHP-FPM), mais à savoir si vous voyez ces fichiers listés comme modifiés dans Git alors que vous ne les avez pas touchés.

---

## 7. Tests effectués

Cet environnement ne dispose pas du SDK Android ni d'un serveur PHP pour exécuter le projet : je n'ai donc **pas pu compiler l'application ni lancer les tests** de bout en bout. Les vérifications suivantes ont été faites à la place :

- Recherche exhaustive (`grep`) de toute IP/URL serveur codée en dur, avant/après correction, sur l'ensemble du dépôt.
- Recherche exhaustive de secrets exposés (clés API, mots de passe) sur l'ensemble du dépôt, avant/après correction.
- Vérification de l'équilibre des accolades sur tous les fichiers Java modifiés.
- Relecture ligne à ligne de chaque modification pour vérifier la cohérence avec le code environnant (imports, types, contexte `this`/`mContext`) et l'absence de régression sur le chemin « succès » existant.
- Vérification manuelle que le nom de fichier retourné par `uploadImg.php` n'est utilisé nulle part côté app (upload « fire-and-forget »), avant de changer sa génération.

**Recommandation avant mise en production de ces changements :** compilez le module `eduniger_native` (`./gradlew :eduniger_native:compileDebugJavaWithJavac` puis un build complet) et testez manuellement : connexion, téléchargement audio et PDF (y compris un test en coupant le réseau pour vérifier le nouveau message d'échec), changement de serveur dans les paramètres, et upload de photo de profil.

---

## 8. Ce qu'il reste à faire (par ordre de priorité)

1. **Faire tourner** — révoquer/changer immédiatement : le mot de passe MySQL/NextCloud `Password@uam2025`, et la clé API OpenAI exposée.
2. Vérifier et corriger `/var/www/html/eduniger/ai/eduna_unified.php` directement sur votre serveur (voir §6).
3. Décider d'un plan de migration du hachage des mots de passe (SHA-256 non salé → bcrypt/Argon2) avec ré-hachage progressif au login.
4. Protéger ou supprimer `api/hash_password.php` (endpoint de debug public).
5. Centraliser le client OkHttp (26 fichiers, 34 sites d'appel) une fois que vous pouvez builder/tester le projet.
6. Confirmer si `AudioDownloader.java`/`ElectronicDownloader.java` (code mort) peuvent être supprimés.
7. Vérifier le comportement voulu de `ChatAiActivity` (identifiant utilisateur en dur) et de `PdfDownloadService` (hôte de stockage figé en production).
