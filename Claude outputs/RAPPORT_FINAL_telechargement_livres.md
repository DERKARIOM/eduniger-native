# Rapport final — Audit et correction du téléchargement de livres (EduNiger Mobile)

Date : 9 septembre 2026
Périmètre : téléchargement, stockage local, « Mes téléchargements », lecture hors ligne des livres électroniques (PDF), Android (`eduniger_native`), backend Laravel (`app-web`).

---

## 1. Audit — problèmes détectés

| # | Problème | Cause | Criticité |
|---|---|---|---|
| 1 | Téléchargement PDF cassé pour presque tous les livres | `idStruct` codé en dur à `1` dans `PdfDownloadService`/`AudioDownloadService` : l'endpoint sécurisé `/api/public/resource/{idStruct}/...` vérifie que le fichier appartient à la structure indiquée — pour tout livre d'une structure ≠ 1, la vérification échouait (404) | **Critique** — fonctionnalité cœur inutilisable |
| 2 | Couverture de catégorie / photo d'auteur servies par l'ancien backend legacy | Migration Sanctum incomplète : ces 2 fichiers appelaient encore `Server.getUrlServer()` au lieu de l'endpoint public migré | Moyenne — incohérence, contenu potentiellement obsolète |
| 3 | Aucune trace locale d'un téléchargement avant sa fin | La ligne SQLite n'était insérée qu'après succès complet ; un téléchargement interrompu (app tuée) ne laissait aucune trace, aucun moyen de le détecter ni de le reprendre | **Critique** — Section 6/9 non respectées |
| 4 | Pas de reprise des téléchargements interrompus | `DownloadFile` chargeait via une simple requête HTTP sans en-tête `Range`, sans réutiliser un fichier partiel existant | Haute |
| 5 | Fichier jamais revalidé après téléchargement | Un téléchargement était considéré « réussi » dès la fin de la requête HTTP, sans vérifier taille/magic-bytes/checksum du fichier réellement écrit sur disque | Haute |
| 6 | `BookActivity.registerBroadcastReceivers()` : `NoSuchMethodError` potentiel | `registerReceiver(receiver, filter, int flags)` (3 arguments) n'existe que depuis l'API 33 (Tiramisu), mais était gardé par `Build.VERSION_CODES.O` (API 26) — crash runtime possible sur les appareils API 28-32, pourtant explicitement supportés (`minSdk 28`) | **Critique** (crash potentiel) |
| 7 | Double-tap sur « Télécharger » | Aucune garde : deux téléchargements pouvaient démarrer en parallèle vers le même fichier | Moyenne |
| 8 | L'UI ne reflétait pas un téléchargement en cours ou échoué au redémarrage de l'app | `configureElectronicFormat()` ne regardait que `isExist()`(=COMPLETED) ; un livre en `DOWNLOADING`/`FAILED` réaffichait « Format PDF » comme si rien n'avait été tenté | Haute |
| 9 | Suppression locale bloquée si un fichier manquait déjà | `ContainerActivity` exigeait que **les deux** fichiers (PDF + couverture) existent encore pour supprimer quoi que ce soit ; sinon, silence total, aucune mise à jour DB/UI possible | Haute (Section 14 non respectée) |
| 10 | `moveToFirst()` non vérifié dans la population de « Mes téléchargements » | Sur une liste vide, le `do/while` s'exécutait quand même une fois et levait une exception, absorbée par un `catch` englobant (fonctionnait « par accident », pas par conception) | Faible (robustesse/lisibilité) |
| 11 | « Mes téléchargements » ne pouvait pas afficher un état ni proposer de réessai | `ElectronicBook`/`ElectronicBookAdapter` ignoraient totalement le statut ; un clic ouvrait toujours le PDF, même absent/partiel | Haute (Section 4 non respectée) |
| 12 | `ElectronicDownloader.java`, `AudioDownloader.java` | Classes mortes (0 référence externe), doublons de logique de téléchargement jamais appelés | Faible (dette technique) |

**Constat positif (aucune correction nécessaire) :**
- `FileController::publicShow` (backend) est déjà bien conçu : vérification d'appartenance du fichier à la structure, `sanitizePath()` (anti path-traversal), `BinaryFileResponse` avec ETag/Last-Modified/cache. Sécurisé pour un endpoint volontairement public (catalogue de livres, sans authentification par design).
- `PdfBoxViewerActivity` est déjà un lecteur PDF natif (`PdfRenderer`), entièrement hors-ligne, avec validation d'existence/lisibilité/corruption du fichier avant ouverture.
- Le stockage utilise déjà `getExternalFilesDir(null)` (stockage scoped), donc **aucune permission de stockage requise**, quelle que soit la version d'Android supportée (28-35).
- `PdfDownloadService`/`AudioDownloadService` sont déjà des Foreground Services (`dataSync`, `stopWithTask="false"`) : le téléchargement survit déjà à la mise en arrière-plan/verrouillage de l'écran, sans qu'un WorkManager soit nécessaire.

---

## 2. Corrections apportées

### 2.1 Backend (`app-web`)
**Aucune modification.** `FileController.php` et `routes/api.php` ont été audités en entier : l'endpoint public `/api/public/resource/{idStruct}/{type}/{filename}` couvre déjà tous les besoins (cover, pdf, category, profil) de façon sécurisée. Le problème n'était pas côté API mais dans la valeur d'`idStruct` envoyée par le mobile.

### 2.2 Mobile — fichiers réécrits en totalité
| Fichier | Contenu |
|---|---|
| `model/table/ElectronicTable.java` | Machine à états de téléchargement (`DOWNLOADING`/`COMPLETED`/`FAILED`, `PAUSED`/`CANCELLED` définis pour un usage futur). Nouvelles colonnes `statusElectronic`, `fileSizeElectronic`, `checksumElectronic`, `idStructElectronic` ajoutées via `PRAGMA table_info` + `ALTER TABLE ADD COLUMN` **idempotent, sans toucher au numéro de version SQLiteOpenHelper** (`data.db` est partagé avec `AudioTable`/`PlaybackTable`/`NotificationTable`/etc., chacune avec sa propre version — bumper la version aurait provoqué `Can't downgrade database` au premier accès d'une table sœur). Nouvelles méthodes `insertPending`, `markCompleted`, `markFailed`, `getStatus`. `getDataC`/`getDataA` filtrées sur `COMPLETED` pour ne pas faire apparaître de téléchargements en cours/échoués dans les listes par catégorie/auteur (qui n'ont pas de badge d'état). |
| `model/data/DownloadFile.java` | Téléchargement en **streaming** (jamais le fichier entier en mémoire), reprise HTTP Range (`Range: bytes=<offset>-`), gestion 206/200/erreur, validation finale (taille, magic-bytes `%PDF`, checksum SHA-256 recalculé sur le fichier disque), conservation du fichier partiel sur erreur transitoire vs suppression sur contenu confirmé invalide. |
| `model/service/PdfDownloadService.java` | Utilise l'`idStruct` réel du livre (nouveau paramètre `names[10]`), insère une ligne `DOWNLOADING` **avant** tout appel réseau, migre couverture de catégorie/photo d'auteur vers l'endpoint sécurisé, diffuse la progression en pourcentage **et** en octets. |

### 2.3 Mobile — fichiers modifiés (patch ciblé)
| Fichier | Modification |
|---|---|
| `model/service/AudioDownloadService.java` | `idStruct` réel (nouveau `names[13]`) au lieu de `1` ; adaptation à `DownloadFile.Result` (`.path`) ; signature de progression à 3 paramètres. |
| `controleur/activity/BookActivity.java` | (1) `idStruct` transmis à `PdfDownloadService` (`names[10]`) et `AudioDownloadService` (`names[13]`). (2) Garde anti double-tap dans `handlePdfDownload()`. (3) `registerBroadcastReceivers()` : garde corrigée `TIRAMISU` (au lieu de `O`) avec repli 2-arguments pour API < 33 (aligné sur le pattern déjà correct de `AudioPlayerActivity`). (4) `configureElectronicFormat()` reflète `DOWNLOADING`/`FAILED`/`COMPLETED` réels (bouton « Réessayer » si échec/interrompu). (5) `handleDownloadFinished()` affiche « Réessayer » (pas « Format PDF ») sur échec, pour cohérence. |
| `controleur/activity/ContainerActivity.java` | (1) Population de « Mes téléchargements » (case 1) : `moveToFirst()` vérifié explicitement (plus de dépendance à une exception absorbée), statut propagé sur chaque `ElectronicBook`. (2) Suppression : un fichier déjà absent n'empêche plus la suppression des autres/de la ligne DB (n'échoue que si un `delete()` qui aurait dû réussir échoue réellement). |
| `controleur/adapter/ElectronicBookAdapter.java` | Affiche l'état réel par livre (texte + comportement au clic) : `COMPLETED`/`null` → ouvre le PDF ; `DOWNLOADING` → toast informatif, pas de navigation ; `FAILED` → réouvre la fiche du livre (`BookActivity`, réutilise `handlePdfDownload()`/« Réessayer », **aucune logique de téléchargement dupliquée**). |
| `model/data/ElectronicBook.java` | Ajout du champ `status` (getter/setter). |
| `model/net/LoandSyncTask.java`, `model/service/MyFirebaseMessagingService.java` | Correction de compilation : ces deux fichiers (fonctionnalité « prêts », hors périmètre télé­chargement de livres) appelaient aussi `DownloadFile.start()` pour une couverture de prêt ; le changement de type de retour (`String` → `DownloadFile.Result`) cassait la compilation. Ajout de `.path`. **Aucune autre modification** — ces fichiers utilisent encore `idStruct=1` en dur pour cette couverture de prêt ; signalé en section 5 (hors périmètre de cet audit). |

### 2.4 Mobile — fichiers supprimés (nettoyage, Section 17)
- `model/data/ElectronicDownloader.java` — classe morte, 0 référence externe, entièrement remplacée par `PdfDownloadService` + `DownloadFile`.
- `model/data/AudioDownloader.java` — classe morte (1 seule référence, en commentaire), remplacée par `AudioDownloadService` + `DownloadFile`.

### 2.5 Nouveaux endpoints
**Aucun.** L'endpoint existant `/api/public/resource/{idStruct}/{type}/{filename}` couvrait déjà tous les besoins.

---

## 3. Fonctionnement final

```
Utilisateur tape "Télécharger"
  → handlePdfDownload() vérifie qu'aucun téléchargement n'est déjà en cours (garde double-tap)
  → startPdfDownloadService() lance PdfDownloadService avec l'idStruct réel du livre
  → PdfDownloadService insère une ligne DOWNLOADING en base AVANT le réseau
  → DownloadFile.start() : streaming vers /storage/app-external/<fichier>,
      reprise Range si un fichier partiel existe déjà, progression diffusée
      (pourcentage + octets) via broadcast
  → Fin de flux : validation (taille, magic-bytes %PDF, SHA-256)
  → Succès : ElectronicTable.markCompleted() (statut COMPLETED, taille, checksum)
      Échec  : ElectronicTable.markFailed() (statut FAILED, ligne conservée)
  → Broadcast ACTION_FINISH_DOWNLOAD → BookActivity met à jour le bouton
      ("Ouvrir" / "Réessayer")
  → Livre visible dans "Mes téléchargements" (ContainerActivity case 1)
      avec son état réel ; FAILED/DOWNLOADING affichés distinctement
  → Lecture hors ligne : PdfBoxViewerActivity ouvre le fichier LOCAL
      (PdfRenderer natif), aucun appel réseau/API
  → Suppression : fichiers supprimés s'ils existent + ligne DB supprimée
      + UI mise à jour immédiatement (le serveur n'est jamais touché)
```

---

## 4. Résultats des tests (Section 18/19)

**Limite importante et transparente : cet environnement d'exécution n'a ni appareil Android/émulateur, ni accès réseau sortant général** (sandbox de développement isolée). Il ne m'est donc pas possible d'exécuter moi-même les 15 scénarios sur un vrai appareil, ni de builder l'APK. Ce que j'ai concrètement fait :
- Vérification statique complète du code (accolades/parenthèses équilibrées sur chaque fichier modifié, recherche exhaustive de tous les appels à `DownloadFile.start()` dans tout le projet pour m'assurer qu'aucun appelant n'a été cassé par le changement de type de retour — 2 appelants oubliés initialement, dans `LoandSyncTask`/`MyFirebaseMessagingService`, corrigés en section 2.3).
- Relecture ligne à ligne de chaque zone modifiée après patch.
- Recherche exhaustive de code mort avant suppression (`grep` sur tout `src/`).

Les tests marqués ⏳ nécessitent un rebuild + exécution sur appareil/émulateur par vos soins (commandes fournies en section 6).

| Test | Résultat | Commentaire |
|---|---|---|
| Téléchargement d'un petit livre | ⏳ | Code streaming vérifié statiquement ; à confirmer sur appareil |
| Téléchargement d'un gros livre (plusieurs centaines de Mo) | ⏳ | Streaming par blocs de 8 Ko, jamais chargé entier en mémoire — vérifié dans le code |
| Interruption du téléchargement | ⏳ | Fichier partiel conservé sur erreur transitoire (vérifié dans le code) |
| Reprise du téléchargement | ⏳ | `Range: bytes=<offset>-` implémenté ; **support serveur non vérifié en direct** (voir section 6, commande curl à exécuter) |
| Fermeture de l'app pendant le téléchargement | ⏳ | Ligne `DOWNLOADING` déjà en base avant le réseau → détectable au redémarrage |
| Redémarrage de l'app | ⏳ | `configureElectronicFormat()` lit désormais le statut réel (corrigé) |
| Désactivation du réseau après téléchargement | ⏳ | `PdfBoxViewerActivity` n'appelle jamais l'API, confirmé par lecture de code (pré-existant, non modifié) |
| Ouverture hors ligne | ⏳ | Idem — lecteur déjà 100% local |
| Suppression d'un livre | ⏳ | Bug de blocage sur fichier manquant corrigé ; à confirmer en conditions réelles |
| Re-téléchargement d'un livre | ⏳ | `insertPending` réinitialise proprement ; `handlePdfDownload` accepte "Format PDF" et "Réessayer" |
| Téléchargement de plusieurs livres | ⏳ | Chaque ligne est indexée par `(idNumber, idBook)` (UNIQUE), pas de collision attendue |
| Espace disque insuffisant | ⏳ | `FileOutputStream` lèvera une `IOException` → capturée, fichier partiel conservé, statut `FAILED` — logique vérifiée, comportement réel non observé |
| URL invalide | ⏳ | `MalformedURLException`/HTTP non-200 → capturée, statut `FAILED` |
| Utilisateur non authentifié | N/A | L'endpoint de téléchargement est **volontairement public** (catalogue), comme le reste de l'app ; l'authentification n'intervient pas à ce niveau (confirmé par lecture de `routes/api.php` : route hors du groupe `auth:sanctum`) |
| Téléchargement d'une ressource hors structure de l'utilisateur | ✅ (vérifié par lecture de code, backend inchangé) | `FileController::publicShow` vérifie que le fichier appartient réellement à la structure `idStruct` demandée (`whereHas('structures', ...)`) — c'était déjà correct côté backend ; le bug (section 1, item 1) était que le mobile envoyait le mauvais `idStruct`, pas une faille de sécurité |
| Web (admin ajoute un livre) → API → DB → Mobile → téléchargement → lecture hors ligne | ⏳ | Chaîne analysée bout en bout (aucune modification requise côté API/DB pour ce flux) ; exécution réelle à faire par vos soins |

---

## 5. Points sortis du périmètre (documentés, non traités)

- **Section 15 (détection de nouvelle version serveur)** : non implémentée. Les colonnes `checksumElectronic`/`fileSizeElectronic` existent déjà et permettraient une comparaison future (ETag ou `updated_at` côté API), mais aucune API de « métadonnées de livre » n'expose aujourd'hui cette information — nécessiterait une décision produit avant implémentation.
- **`PAUSED`/`CANCELLED`** : définis dans `ElectronicTable` pour un usage futur, mais aucune action UI (bouton pause/annuler) ne les déclenche actuellement.
- **Barre de progression en octets ("25 Mo / 50 Mo")** : l'infrastructure existe (broadcast `bytesDownloaded`/`totalBytes`), mais aucun `TextView` ne l'affiche actuellement — nécessiterait une modification de layout XML, non faite pour rester dans le périmètre défini.
- **Déduplication couverture de catégorie/photo d'auteur** entre livres partageant le même auteur/la même catégorie : actuellement re-téléchargée par livre (inefficacité mineure de bande passante/stockage, pas un bug).
- **Audit complet de la fonctionnalité Audio** : seul le bug `idStruct=1` a été corrigé par cohérence (bug identique trouvé à côté de celui du PDF) ; aucun audit complet de la fonctionnalité audio n'a été mené (hors périmètre "livres").
- **`idStruct=1` en dur dans `LoandSyncTask.java`/`MyFirebaseMessagingService.java`** (téléchargement de la couverture d'un prêt) : même classe de bug que celui corrigé en section 1, mais dans la fonctionnalité « prêts », hors périmètre de cet audit. Signalé pour une correction future si pertinent.
- **`Build.VERSION_CODES.O` au lieu de `TIRAMISU`** pour `registerReceiver(..., int flags)` : le même bug que celui corrigé dans `BookActivity` existe aussi dans `CategoryActivity`, `MainActivity`, `AuthorActivity`, `StructureActivity`, `SearchActivity`, `ChatAdapter`, `ChatBotFragment`, `StructureFragment`, `BooksFragment`, `HomeFragment`, `CategoryFragment` — corrigé uniquement dans `BookActivity` (chaîne de téléchargement des livres). Recommandé : une passe dédiée sur ces fichiers, hors périmètre du présent audit.

---

## 6. Commandes à exécuter de votre côté

**1) Vérifier le support HTTP Range en direct** (nécessaire pour confirmer la reprise de téléchargement) :
```bash
curl -I -H "Range: bytes=0-100" https://<votre-host-prod>/api/public/resource/<idStruct>/pdf/<filename>
# Attendu : HTTP/1.1 206 Partial Content, avec Content-Range et Content-Length
```

**2) Rebuild de l'application mobile** (Android Studio, ou en ligne de commande depuis `eduniger_native/`) :
```bash
./gradlew clean assembleDebug
# puis installer sur un appareil/émulateur couvrant API 28 et API 33+ (pour valider le fix TIRAMISU)
```

**3) Exécuter les 15 scénarios de la section 18 et le test Web→API→Mobile de la section 19** sur un appareil réel, en couvrant au minimum : un appareil API 28-32 (validation du fix `registerReceiver`), et un test réseau coupé après téléchargement (avion) pour la lecture hors ligne.

---

## 7. Résumé

Le téléchargement de livres était fondamentalement cassé (mauvais `idStruct`) pour la quasi-totalité des structures. Corrigé, avec en plus : reprise de téléchargement (Range), validation réelle du fichier après écriture disque, machine à états persistée dès le début du téléchargement, correction d'un crash potentiel (`registerReceiver`), garde anti double-tap, suppression locale qui ne se bloque plus sur un fichier manquant, et « Mes téléchargements » qui reflète enfin l'état réel de chaque livre (avec retry). Aucune modification backend n'était nécessaire ; deux fichiers hors périmètre (`LoandSyncTask`, `MyFirebaseMessagingService`) ont dû être corrigés pour rester compilables suite au changement de signature de `DownloadFile.start()`. Les tests d'exécution réelle restent à votre charge (sandbox sans appareil ni réseau sortant général) ; commandes et scénarios fournis ci-dessus.
