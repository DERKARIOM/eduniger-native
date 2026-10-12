#!/usr/bin/env bash
# Mise à jour de l'API Laravel d'Elim sur Hostinger (à lancer en SSH).
#   bash ~/elim-api/deploy/deploy_api.sh
# Prérequis : le code est à jour dans ~/elim-api (hors public_html ; le sous-domaine
#             api.kawtal-consulting.com pointe vers ~/elim-api/public par un lien symbolique)
#             (git pull ou envoi SFTP) et le fichier .env est rempli.
set -euo pipefail

APP_DIR="$HOME/elim-api"
cd "$APP_DIR"

echo "→ Mode maintenance"
php artisan down --retry=30 || true

echo "→ Dépendances (sans les outils de développement)"
composer install --no-dev --optimize-autoloader --no-interaction

echo "→ Migrations en attente (la base importée contient déjà les tables)"
php artisan migrate:status | tail -n 5
php artisan migrate --force

echo "→ Lien public vers storage/"
php artisan storage:link || true

echo "→ Caches de production"
php artisan config:cache
php artisan route:cache
php artisan view:cache
php artisan event:cache || true

echo "→ Droits d'écriture"
chmod -R ug+rwX storage bootstrap/cache

echo "→ Remise en ligne"
php artisan up
echo "✔ Déploiement terminé"
