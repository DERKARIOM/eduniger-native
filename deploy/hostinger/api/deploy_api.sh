#!/usr/bin/env bash
# Mise à jour de l'API Laravel d'Elim sur Hostinger (à lancer en SSH).
#   bash ~/elim-api/deploy/deploy_api.sh
# Prérequis : le code est à jour dans ~/elim-api (hors public_html ; le sous-domaine
#             api.kawtal-consulting.com pointe vers ~/elim-api/public par un lien symbolique)
#             (git pull ou envoi SFTP) et le fichier .env est rempli.
set -euo pipefail

APP_DIR="$HOME/elim-api"
cd "$APP_DIR"

# En cas d'échec, ne pas laisser l'API bloquée en mode maintenance
trap 'echo "✘ Échec du déploiement : remise en ligne de la version précédente"; php artisan up' ERR

echo "→ Mode maintenance"
php artisan down --retry=30 || true

echo "→ Nettoyage des caches venus d'un autre environnement"
rm -f bootstrap/cache/*.php
# Linux distingue la casse : le fichier doit porter le nom exact de la classe
if [ -f app/Services/NextCloudStorage.php ] && [ ! -f app/Services/NextcloudStorage.php ]; then
    mv app/Services/NextCloudStorage.php app/Services/NextcloudStorage.php
fi

echo "→ Dépendances (sans les outils de développement)"
# Hostinger désactive proc_open : Composer ne peut pas lancer les scripts post-install,
# on les exécute donc nous-mêmes juste après.
composer install --no-dev --optimize-autoloader --no-interaction --no-scripts
php artisan package:discover --ansi

echo "→ Migrations en attente (la base importée contient déjà les tables)"
php artisan migrate:status | tail -n 5
php artisan migrate --force

echo "→ Lien public vers storage/"
# La fonction PHP symlink() est désactivée chez Hostinger : lien créé par le shell.
[ -L public/storage ] || ln -s ../storage/app/public public/storage

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
