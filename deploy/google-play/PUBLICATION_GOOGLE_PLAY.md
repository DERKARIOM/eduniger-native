# Publication d'Elim sur Google Play

Application : **Elim**, identifiant `com.naniger.elim`, version 3.1.3 (versionCode 14). Elle cible Android 16 (API 36), comme Google Play l'exige depuis le 31/08/2026.

## 1. Ce qui est déjà conforme dans le code

| Exigence Google Play | Mise en œuvre |
|---|---|
| Niveau d'API cible | `targetSdk 36` / `compileSdk 36` |
| Permissions minimales | `CALL_PHONE` supprimée : les appels passent par le composeur (`ACTION_DIAL`). Aucune permission de localisation, contacts, SMS ou stockage étendu. |
| Suppression de compte **dans l'application** | Paramètres > Compte > Supprimer le compte → `DELETE /api/user/account`. Supprime le compte, les données associées et les sessions. |
| Suppression de compte **sur le web** | `https://elim.kawtal-consulting.com/suppression-compte.html` |
| Politique de confidentialité | `https://elim.kawtal-consulting.com/confidentialite.html`, accessible aussi dans l'application (Paramètres > Politique de confidentialité, et écran Conditions) |
| Contenu publié par les utilisateurs (commentaires) | Appui long sur un commentaire : **Signaler** ou **Masquer les commentaires de cet utilisateur**. Les signalements sont enregistrés dans la table `ContentReport`. |
| IA générative (assistant Eduna) | Appui long sur une réponse : **Signaler cette réponse** ou **Copier** |
| Connexion sécurisée | HTTPS uniquement en release (aucune exception HTTP). Mots de passe hachés, jeton chiffré sur le téléphone. |
| Écrans de développement | L'écran « Serveur connecté » est masqué dans la version release. |
| Journaux | Plus aucun jeton dans les logs |
| Marque | Plus aucune mention EduNiger visible. Icône sur fond bleu uni (la grille par défaut d'Android Studio a été retirée). |

## 2. Mise en ligne côté serveur (à faire avant l'envoi en revue)

### 2.1 API Laravel (PowerShell sur le PC)
```
cd C:\Users\derka\projets\elim\server\admin-api-new
$H="u883737194@185.224.138.118"
scp -P 65002 app\Http\Controllers\UserAuthController.php app\Http\Controllers\ContentReportController.php "${H}:~/elim-api/app/Http/Controllers/"
scp -P 65002 routes\api.php "${H}:~/elim-api/routes/"
scp -P 65002 database\migrations\2026_10_12_170000_create_content_report_table.php "${H}:~/elim-api/database/migrations/"
```
Ensuite, en SSH :
```
bash ~/elim-api/deploy/deploy_api.sh
cd ~/elim-api && php artisan route:list --path=user/account && php artisan route:list --path=reports
```

### 2.2 Pages web et fichiers publics
```
cd C:\Users\derka\projets\elim\deploy\hostinger\elim\public_html
$H="u883737194@185.224.138.118"; $D="~/domains/kawtal-consulting.com/public_html/elim"
scp -P 65002 confidentialite.html suppression-compte.html "${H}:${D}/"
ssh -p 65002 $H "mkdir -p ${D}/.well-known ${D}/eduniger/ressources/logo"
scp -P 65002 .well-known\assetlinks.json "${H}:${D}/.well-known/"
scp -P 65002 eduniger\ressources\logo\eduniger.png "${H}:${D}/eduniger/ressources/logo/"
```
Pour vérifier, ouvrez ces adresses dans un navigateur :
- `https://elim.kawtal-consulting.com/confidentialite.html`
- `https://elim.kawtal-consulting.com/suppression-compte.html`

`assetlinks.json` (pour les liens `https://elim.kawtal-consulting.com/open`) : remplacez `REMPLACER_PAR_SHA256_DE_LA_CLE_DE_SIGNATURE_PLAY` par l'empreinte SHA-256 indiquée dans Play Console > Intégrité de l'application > Signature d'application. Cette empreinte n'est disponible qu'après le premier envoi.

## 3. Clé de signature (une seule fois, à conserver précieusement)

Dans PowerShell, à la racine du projet :
```
keytool -genkeypair -v -keystore elim-upload.jks -alias elim -keyalg RSA -keysize 4096 -validity 10000
copy keystore.properties.example keystore.properties
```
Remplissez ensuite `keystore.properties` avec `storeFile=elim-upload.jks`, les deux mots de passe et `keyAlias=elim`.

- Sauvegardez `elim-upload.jks` et ses mots de passe à deux endroits sûrs, hors du dépôt Git. Le fichier `keystore` de l'ancienne application, présent dans le dépôt public, ne doit **pas** être utilisé.
- Activez **Play App Signing** (proposé par défaut) : Google conserve la clé de signature finale, et la vôtre ne sert qu'à envoyer les versions.
- Relevez les empreintes avec `.\gradlew.bat signingReport`, puis ajoutez dans Firebase (`elim-16f44` > application Android) :
  - le **SHA-1 de `elim-upload.jks`** ;
  - après le premier envoi, le **SHA-1 et le SHA-256 de la clé Play App Signing** (Play Console > Intégrité de l'application).

  Sans eux, « Continuer avec Google » échoue sur la version téléchargée depuis le Play Store.

Générez ensuite le bundle à envoyer :
```
.\gradlew.bat clean bundleRelease
```
Le fichier produit est `eduniger_native\build\outputs\bundle\release\elim-release.aab`.

## 4. Play Console : fiche et formulaires

### Fiche Play Store
- **Nom** : Elim, bibliothèque numérique
- **Description courte** (80 caractères max.) : `Livres, PDF et livres audio, avec un assistant IA pour apprendre partout.`
- **Description complète** : présenter la bibliothèque (livres numériques, audio, documents académiques), la lecture hors connexion, les structures (universités, bibliothèques) et l'assistant Eduna (résumés, explications, quiz).
- **Icône 512 × 512** : `deploy/google-play/icone-512.png`
- **Bannière 1024 × 500** : `deploy/google-play/banniere-1024x500.png`
- **Captures d'écran** : au moins 2 (téléphone), par exemple l'accueil, un livre, le lecteur audio et l'assistant Eduna. Prenez-les sur le téléphone, en mode sombre.
- **Catégorie** : Éducation · **Coordonnées** : contact@naniger.com
- **Politique de confidentialité** : `https://elim.kawtal-consulting.com/confidentialite.html`

### Accès à l'application (obligatoire : l'application exige un compte)
Créez un **compte de test** dédié (numéro et mot de passe), membre d'au moins une structure qui contient des livres. Indiquez ses identifiants dans *Contenu de l'appli > Accès à l'appli*. Sans ce compte, la revue est refusée.

### Sécurité des données (Data safety) : réponses
- Données collectées : **Oui**. Chiffrées en transit : **Oui**. Suppression possible sur demande : **Oui**, en indiquant l'adresse de la page de suppression.
- **Informations personnelles** : nom, adresse e-mail, ID utilisateur, numéro de téléphone. Collectées, non partagées. Finalité : fonctionnement de l'application, gestion du compte.
- **Photos** (photo de profil, facultative) : collectée. Finalités : fonctionnement de l'application, gestion du compte.
- **Messages** (commentaires, questions à l'assistant) : collectés. Les questions à l'IA sont transmises à Google Gemini pour générer la réponse ; dans le formulaire, il s'agit d'un **prestataire de services** et non d'un partage. Finalité : fonctionnement de l'application.
- **Activité dans l'application** (livres consultés, « j'aime », recherches) : collectée. Finalités : fonctionnement de l'application, personnalisation.
- **Identifiants de l'appareil** : jeton de notification FCM. Finalité : fonctionnement de l'application (notifications).
- **Non collectés** : localisation, contacts, données financières, santé, identifiant publicitaire. L'application ne contient pas de publicité.

### Autres déclarations
- **Classification du contenu** (questionnaire IARC) : catégorie Référence/Éducation. Contenu généré par les utilisateurs : **oui** (commentaires), avec signalement et masquage. Pas de violence ni de contenu pour adultes.
- **Public cible** : 13 ans et plus. Ne pas cocher les tranches de moins de 13 ans : cela soumettrait l'application au programme Familles.
- **Annonces** : non.
- **Services de premier plan** (Android 14+), à déclarer avec une courte vidéo de démonstration :
  - `mediaPlayback` : lecture des livres audio en arrière-plan, avec le contrôle dans la notification ;
  - `dataSync` : téléchargement des livres PDF et audio pour la lecture hors connexion.
- **Application d'actualités** : non. **Application gouvernementale** : non.
- **IA générative** : oui (assistant Eduna). Signalement intégré par appui long.

## 5. Avant l'envoi en production

1. `.\gradlew.bat clean installDebug`, puis testez sur le téléphone :
   - connexion par numéro et par Google, inscription, déconnexion ;
   - accueil (carrousel, Recommandé, structures), ouverture d'un livre, lecture d'un PDF et d'un audio (lecture en arrière-plan) ;
   - commentaires : envoi, **appui long → Signaler / Masquer** ;
   - Eduna : question, **appui long sur la réponse → Signaler** ;
   - **Paramètres > Politique de confidentialité** (ouvre la page web) ;
   - **Suppression du compte** avec un compte de test jetable.
2. Vérifiez l'affichage sous les barres système (Android 16 impose le plein écran) : rien ne doit passer sous la barre d'état ou la barre de navigation.
3. `bundleRelease`, puis envoi en **test interne** dans Play Console. Testez la version installée depuis le Play Store, dont la connexion Google.
4. Passez ensuite en production.

## 6. Points volontairement non traités

- `minifyEnabled` reste à `false`. L'activer réduit la taille et protège le code, mais demande des règles ProGuard (Glide, Picasso, OkHttp) et une série de tests. Ce n'est pas exigé par Google.
- La connexion Google utilise `play-services-auth` (GoogleSignIn), toujours accepté. Google recommande Credential Manager pour les nouveaux projets : migration possible plus tard.
- « Fabi by Fast » dans la liste des structures du formulaire « Ajouter un livre » est le nom d'une structure réelle : je l'ai laissé tel quel.
