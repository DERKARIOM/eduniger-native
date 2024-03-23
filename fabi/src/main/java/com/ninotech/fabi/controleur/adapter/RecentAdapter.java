package com.ninotech.fabi.controleur.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.animation.RoundedTransformation;
import com.ninotech.fabi.model.data.SimilarBook;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.MyViewHolder> {
    List<SimilarBook> mListSimilarBook;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public RecentAdapter(List<SimilarBook> listSimilarBook) {
        mListSimilarBook = listSimilarBook;
    }
    @Override
    public RecentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_similar,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SimilarBook item = mListSimilarBook.get(position);
        try {
            holder.display(mListSimilarBook.get(position));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListSimilarBook.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private final ImageView mCoverImageView;
        MyViewHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.image_view_adapter_similar_cover);
        }
        void display(SimilarBook similarBook) throws SQLException, IOException {
            //mCoverImageView.setImageBitmap(similarBook.getCover());
            // Convertir le Bitmap en un fichier
            File file = bitmapToFile(itemView.getContext(), "image" + String.valueOf(similarBook.getId()) + ".png", similarBook.getCover());

// Charger le fichier avec Picasso
            Picasso.get().load(file)
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(15,4))
                    .resize(198,304)
                    .into(mCoverImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloadAndOpenPDF(similarBook.getPDF());
                }
            });
        }
        public File bitmapToFile(Context context, String filename, Bitmap bitmap) {
            // Créer un fichier dans le répertoire de cache de l'application
            File file = new File(context.getCacheDir(), filename);
            try {
                // Convertir le Bitmap en un fichier de sortie
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }
        private void downloadAndOpenPDF(String nomPdf) {
            new AsyncTask<Void, Void, File>() {
                @Override
                protected File doInBackground(Void... voids) {
                    try {
                        // URL du PDF distant
                        String pdfUrl = "http://192.168.43.1:2222/fabi/pdf/" + nomPdf;
                        URL url = new URL(pdfUrl);

                        // Ouvrir la connexion
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");

                        // Télécharger le PDF dans le répertoire de téléchargement
                        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS), nomPdf);

                        InputStream inputStream = urlConnection.getInputStream();
                        FileOutputStream outputStream = new FileOutputStream(pdfFile);

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.close();
                        inputStream.close();

                        return pdfFile;

                    } catch (IOException e) {
                        Log.e("DownloadTask", "Error while downloading PDF", e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(File pdfFile) {
                    super.onPostExecute(pdfFile);

                    if (pdfFile != null) {
                        // Ouvrir le PDF avec Adobe PDF Reader
                        openPDFWithAdobeReader(pdfFile);
                    } else {
                        Log.e("DownloadTask", "PDF file is null");
                    }
                }
            }.execute();
        }
        private void openPDFWithAdobeReader(File pdfFile) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri pdfUri = FileProvider.getUriForFile(itemView.getContext(),itemView.getContext().getPackageName() + ".provider", pdfFile);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(itemView.getContext(), "OpenPDF : " + e, Toast.LENGTH_LONG).show();
            }
        }

    }
}
