package com.ninotech.eduniger.model.data;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile {

    private static final String TAG = "DownloadFile";

    private Context mContext;

    public DownloadFile(Context context) {
        this.mContext = context;
    }

    public String start(String fileUrl, String fileName, ProgressCallback callback) throws Exception {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String filePath = mContext.getExternalFilesDir(null) + "/" + fileName;

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "Début téléchargement");
        Log.d(TAG, "  URL      : " + fileUrl);
        Log.d(TAG, "  Fichier  : " + fileName);
        Log.d(TAG, "  Chemin   : " + filePath);

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();
            int fileLength = connection.getContentLength();

            Log.d(TAG, "  HTTP code    : " + responseCode);
            Log.d(TAG, "  Content-Type : " + contentType);
            Log.d(TAG, "  Content-Length : " + fileLength + " octets");

            // ── Vérification code HTTP ───────────────────────────────────────
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String msg = "Serveur a retourné HTTP " + responseCode
                        + " " + connection.getResponseMessage()
                        + " pour : " + fileUrl;
                Log.e(TAG, msg + " ⚠️");
                throw new Exception(msg);
            }

            // ── Vérification Content-Type pour les PDF ───────────────────────
            boolean isPdf = fileName.toLowerCase().endsWith(".pdf");
            if (isPdf && contentType != null) {
                boolean validContentType = contentType.contains("application/pdf")
                        || contentType.contains("application/octet-stream")
                        || contentType.contains("binary/octet-stream");

                if (!validContentType) {
                    String msg = "Content-Type inattendu pour un PDF : '"
                            + contentType + "' — le serveur renvoie probablement "
                            + "une page HTML d'erreur ou d'authentification ⚠️";
                    Log.e(TAG, msg);
                    // On continue quand même mais on loggue l'avertissement
                    // La vérification des magic bytes ci-dessous confirmera
                }
            }

            // ── Téléchargement ───────────────────────────────────────────────
            input = connection.getInputStream();
            output = new java.io.FileOutputStream(filePath);

            byte[] buffer = new byte[4096];
            long total = 0;
            int count;

            // Lire le premier bloc pour vérifier les magic bytes
            boolean headerChecked = false;

            while ((count = input.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new Exception("Téléchargement annulé");
                }

                // ── Vérification magic bytes sur le 1er bloc ─────────────────
                if (!headerChecked && isPdf && count >= 4) {
                    headerChecked = true;
                    String header = new String(buffer, 0, 4);
                    Log.d(TAG, "  Magic bytes (4 premiers octets) : '"
                            + header + "' (attendu : '%PDF')");

                    if (!header.startsWith("%PDF")) {
                        // Lire ce que le serveur a vraiment renvoyé (debug)
                        int previewLen = Math.min(count, 200);
                        String preview = new String(buffer, 0, previewLen);
                        Log.e(TAG, "  Contenu reçu (200 premiers chars) : " + preview + " ⚠️");

                        // Supprimer le fichier partiel
                        new File(filePath).delete();

                        throw new Exception(
                                "Le fichier reçu n'est pas un PDF valide. "
                                        + "Magic bytes : '" + header + "' au lieu de '%PDF'. "
                                        + "Le serveur a probablement renvoyé une erreur "
                                        + "d'authentification ou une page HTML. "
                                        + "Contenu reçu : " + preview.substring(0, Math.min(100, preview.length()))
                        );
                    }
                    Log.d(TAG, "  Magic bytes valides ✓ → fichier PDF confirmé");
                }

                total += count;
                output.write(buffer, 0, count);

                if (fileLength > 0 && callback != null) {
                    int progress = (int) (total * 100 / fileLength);
                    callback.onProgress(progress);
                }
            }

            // ── Vérification taille minimale ─────────────────────────────────
            output.flush();
            File downloadedFile = new File(filePath);
            long finalSize = downloadedFile.length();
            Log.d(TAG, "  Taille finale : " + finalSize + " octets");

            if (isPdf && finalSize < 1024) {
                downloadedFile.delete();
                throw new Exception(
                        "Fichier PDF suspect : taille trop petite ("
                                + finalSize + " octets). Téléchargement incomplet ou "
                                + "réponse d'erreur du serveur ⚠️"
                );
            }

            Log.d(TAG, "Téléchargement terminé avec succès ✓ : " + filePath);
            return filePath;

        } catch (Exception e) {
            // Nettoyer le fichier partiel en cas d'erreur
            File partial = new File(filePath);
            if (partial.exists()) {
                partial.delete();
                Log.w(TAG, "Fichier partiel supprimé : " + filePath);
            }
            Log.e(TAG, "Échec du téléchargement : " + e.getMessage(), e);
            throw e;

        } finally {
            try { if (output != null) output.close(); } catch (Exception ignored) {}
            try { if (input  != null) input.close();  } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
    }

    public interface ProgressCallback {
        void onProgress(int progress);
    }
}