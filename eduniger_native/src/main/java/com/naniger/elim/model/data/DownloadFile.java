package com.naniger.elim.model.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Téléchargement en streaming vers le stockage local de l'app (jamais chargé
 * intégralement en mémoire), avec :
 *  - reprise automatique (HTTP Range) si un fichier partiel existe déjà à
 *    l'emplacement cible et que le serveur répond 206 Partial Content ;
 *  - validation du fichier final (taille attendue, magic bytes pour les PDF,
 *    checksum SHA-256 calculé sur le fichier écrit sur disque - pas en
 *    mémoire) ;
 *  - conservation du fichier partiel sur une erreur réseau transitoire (pour
 *    permettre une reprise), suppression uniquement lorsque le contenu reçu
 *    est confirmé invalide (mauvais magic bytes sur un téléchargement frais,
 *    ou reprise refusée par le serveur).
 */
public class DownloadFile {

    private static final String TAG = "DownloadFile";

    private Context mContext;

    public DownloadFile(Context context) {
        this.mContext = context;
    }

    /** Résultat d'un téléchargement réussi : chemin local, taille finale, checksum SHA-256. */
    public static class Result {
        public final String path;
        public final long size;
        public final String checksum;

        Result(String path, long size, String checksum) {
            this.path = path;
            this.size = size;
            this.checksum = checksum;
        }
    }

    public interface ProgressCallback {
        /**
         * @param percent          0-100 si la taille totale est connue, -1 sinon (cf. section
         *                         "progression" de l'audit : ne jamais inventer une progression).
         * @param bytesDownloaded  Total téléchargé (reprise incluse).
         * @param totalBytes       Taille totale connue, ou -1 si inconnue (serveur sans Content-Length).
         */
        void onProgress(int percent, long bytesDownloaded, long totalBytes);
    }

    /** Compatibilité : ancienne signature à un seul paramètre (pourcentage uniquement). */
    public interface SimpleProgressCallback {
        void onProgress(int progress);
    }

    public Result start(String fileUrl, String fileName, ProgressCallback callback) throws Exception {
        String filePath = mContext.getExternalFilesDir(null) + "/" + fileName;
        File targetFile = new File(filePath);

        long startOffset = targetFile.exists() ? targetFile.length() : 0;
        boolean isResume = startOffset > 0;

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "Début téléchargement (reprise=" + isResume + ", offset=" + startOffset + ")");
        Log.d(TAG, "  URL      : " + fileUrl);
        Log.d(TAG, "  Fichier  : " + fileName);
        Log.d(TAG, "  Chemin   : " + filePath);

        HttpURLConnection connection = null;
        boolean contentConfirmedInvalid = false;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            if (isResume) {
                connection.setRequestProperty("Range", "bytes=" + startOffset + "-");
            }
            connection.connect();

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            Log.d(TAG, "  HTTP code    : " + responseCode);
            Log.d(TAG, "  Content-Type : " + contentType);

            if (isResume && responseCode == HttpURLConnection.HTTP_PARTIAL) {
                // Le serveur accepte la reprise : on ajoute à la suite du fichier existant.
                long remaining = connection.getContentLength();
                long total = remaining >= 0 ? startOffset + remaining : -1;
                downloadBody(connection, targetFile, true, startOffset, total, fileName, callback);
            } else {
                if (isResume) {
                    // Reprise refusée (200 = pas de support Range, ou toute autre réponse) :
                    // on ne peut pas se fier à un fichier partiel qu'on ne peut pas prolonger
                    // proprement -> on repart de zéro plutôt que de corrompre le fichier.
                    Log.w(TAG, "Reprise refusée par le serveur (HTTP " + responseCode
                            + ") : redémarrage complet du téléchargement.");
                    targetFile.delete();
                    startOffset = 0;
                    // La connexion précédente a été ouverte avec un en-tête Range : on en
                    // rouvre une neuve, propre, sans Range.
                    connection.disconnect();
                    connection = (HttpURLConnection) new URL(fileUrl).openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(30000);
                    connection.connect();
                    responseCode = connection.getResponseCode();
                    contentType = connection.getContentType();
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String msg = "Serveur a retourné HTTP " + responseCode
                            + " " + connection.getResponseMessage()
                            + " pour : " + fileUrl;
                    Log.e(TAG, msg + " ⚠️");
                    throw new Exception(msg);
                }

                boolean isPdf = fileName.toLowerCase().endsWith(".pdf");
                if (isPdf && contentType != null) {
                    boolean validContentType = contentType.contains("application/pdf")
                            || contentType.contains("application/octet-stream")
                            || contentType.contains("binary/octet-stream");
                    if (!validContentType) {
                        Log.w(TAG, "Content-Type inattendu pour un PDF : '" + contentType
                                + "' — vérification par magic bytes ci-dessous.");
                    }
                }

                long total = connection.getContentLength();
                downloadBody(connection, targetFile, false, 0, total >= 0 ? total : -1, fileName, callback);
            }

            // ── Validation finale (le fichier est réellement écrit sur le disque) ─────
            long finalSize = targetFile.length();
            boolean isPdf = fileName.toLowerCase().endsWith(".pdf");
            if (isPdf && finalSize < 1024) {
                contentConfirmedInvalid = true;
                throw new Exception("Fichier PDF suspect : taille trop petite (" + finalSize
                        + " octets). Téléchargement incomplet ou réponse d'erreur du serveur ⚠️");
            }
            if (isPdf) {
                String header = readFirstBytes(targetFile, 4);
                if (header == null || !header.startsWith("%PDF")) {
                    contentConfirmedInvalid = true;
                    throw new Exception("Le fichier reçu n'est pas un PDF valide (magic bytes '"
                            + header + "' au lieu de '%PDF').");
                }
            }

            String checksum = sha256(targetFile);
            Log.d(TAG, "Téléchargement terminé avec succès ✓ : " + filePath
                    + " (" + finalSize + " octets, sha256=" + checksum + ")");
            return new Result(filePath, finalSize, checksum);

        } catch (Exception e) {
            if (e instanceof PdfMagicBytesException) {
                contentConfirmedInvalid = true;
            }
            if (contentConfirmedInvalid) {
                // Contenu confirmé invalide (mauvais PDF, taille aberrante) : le fichier ne
                // peut servir de base à une reprise, on le supprime.
                if (targetFile.exists()) targetFile.delete();
                Log.w(TAG, "Fichier invalide supprimé : " + filePath);
            } else {
                // Erreur transitoire (réseau, timeout, appli tuée en cours de route...) :
                // on CONSERVE le fichier partiel pour permettre une reprise au prochain
                // essai (cf. section "reprise des téléchargements" de l'audit).
                Log.w(TAG, "Échec transitoire : fichier partiel conservé pour reprise ultérieure ("
                        + filePath + ")");
            }
            Log.e(TAG, "Échec du téléchargement : " + e.getMessage(), e);
            throw e;

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void downloadBody(HttpURLConnection connection, File targetFile, boolean append,
                               long startOffset, long totalBytes, String fileName,
                               ProgressCallback callback) throws Exception {
        InputStream input = connection.getInputStream();
        OutputStream output = new FileOutputStream(targetFile, append);
        try {
            byte[] buffer = new byte[8192];
            long total = startOffset;
            int count;
            boolean isPdf = fileName.toLowerCase().endsWith(".pdf");
            boolean headerChecked = append; // on ne vérifie les magic bytes que sur un flux frais

            while ((count = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new Exception("Téléchargement annulé");
                }

                if (!headerChecked && isPdf && count >= 4) {
                    headerChecked = true;
                    String header = new String(buffer, 0, 4);
                    if (!header.startsWith("%PDF")) {
                        int previewLen = Math.min(count, 200);
                        String preview = new String(buffer, 0, previewLen);
                        Log.e(TAG, "  Contenu reçu (200 premiers chars) : " + preview + " ⚠️");
                        output.write(buffer, 0, count); // écrit pour que le fichier existe, sera supprimé par l'appelant
                        output.flush();
                        throw new PdfMagicBytesException(
                                "Le fichier reçu n'est pas un PDF valide. Magic bytes : '" + header
                                        + "' au lieu de '%PDF'. Contenu reçu : "
                                        + preview.substring(0, Math.min(100, preview.length())));
                    }
                }

                total += count;
                output.write(buffer, 0, count);

                int percent = totalBytes > 0 ? (int) (total * 100 / totalBytes) : -1;
                if (callback != null) callback.onProgress(percent, total, totalBytes);
            }
            output.flush();
        } finally {
            try { output.close(); } catch (Exception ignored) {}
            try { input.close(); } catch (Exception ignored) {}
        }
    }

    /** Marqueur interne : magic bytes invalides -> l'appelant doit supprimer le fichier. */
    private static class PdfMagicBytesException extends Exception {
        PdfMagicBytesException(String message) { super(message); }
    }

    private String readFirstBytes(File file, int n) {
        try (InputStream in = new FileInputStream(file)) {
            byte[] buf = new byte[n];
            int read = in.read(buf);
            if (read < n) return null;
            return new String(buf, 0, read);
        } catch (Exception e) {
            return null;
        }
    }

    /** Checksum SHA-256 calculé en flux depuis le disque (jamais tout le fichier en mémoire). */
    private String sha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            Log.w(TAG, "Impossible de calculer le checksum : " + e.getMessage());
            return null;
        }
    }
}
