# EduNiger Backend API

Backend de la plateforme **EduNiger** développé avec Laravel.

⚠️ **Ce dépôt est privé.**
Les informations sensibles (clés API, accès base de données, tokens, etc.) ne doivent jamais être commitées dans le repository.

---

# Prérequis

Avant de configurer le projet :

* PHP >= 8.1
* Composer
* MySQL ou MariaDB
* Git

---

# Installation

Cloner le dépôt :

```
git clone <repo-url>
```

Entrer dans le dossier backend :

```
cd backend
```

Installer les dépendances :

```
composer install
```

---

# Configuration de l'environnement

Créer le fichier `.env` :

```
cp .env.example .env
```

Configurer ensuite les variables nécessaires dans `.env`.

Exemple minimal :

```
APP_NAME=EduNiger
APP_ENV=local
APP_DEBUG=true
APP_URL=http://localhost:8000

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=eduniger
DB_USERNAME=root
DB_PASSWORD=
```

⚠️ **Ne jamais commit le fichier `.env`.**

---

# Génération de la clé Laravel

```
php artisan key:generate
```

---

# Migration de la base de données

```
php artisan migrate
```

---

# Lancer le serveur local

```
php artisan serve
```

Le backend sera disponible sur :

```
http://localhost:8000
```

---

# Sécurité du projet

Règles importantes :

* Ne jamais commit :

  * `.env`
  * fichiers contenant des clés API
  * dumps de base de données
* Toujours utiliser `.env.example`
* Utiliser des mots de passe forts pour la base de données
* Ne jamais exposer les routes d'administration publiquement.

---

# Structure principale

```
app/
routes/
config/
database/
storage/
```

---

# Commandes utiles

Vider le cache :

```
php artisan cache:clear
```

Recréer la base :

```
php artisan migrate:fresh
```

---

# API

Toutes les routes sont accessibles via :

```
/api
```

Exemple :

```
http://localhost:8000/api/structures
```

---

# Mainteneurs

Projet interne EduNiger / NinoTech.
Toute modification critique doit passer par une revue de code.
