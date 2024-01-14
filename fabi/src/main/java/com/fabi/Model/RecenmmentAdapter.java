package com.fabi.Model;

import android.content.Intent;
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

import com.example.fabi.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class RecenmmentAdapter extends RecyclerView.Adapter<RecenmmentAdapter.MyViewHolder> {
    List<Recenmment> mListRecenmment;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    private int mPosition;
    public RecenmmentAdapter(List<Recenmment> listRecenmment) {
        mListRecenmment = listRecenmment;
    }
    @Override
    public RecenmmentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_similaire,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Recenmment item = mListRecenmment.get(position);
        try {
            holder.display(mListRecenmment.get(position));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public int getItemCount() {
        return mListRecenmment.size();
    }


//    public void Remove(int position){
//        mListNotification.remove(position);
//        notifyItemRemoved(position);
//    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView mCouverture;
        MyViewHolder(View itemView){
            super(itemView);
            mCouverture = (ImageView) itemView.findViewById(R.id.couverteur_recent);
        }
        //        @Override
//        public void onCreateContextMenu(ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo){
////            menu.add(Menu.NONE,R.id.infoNotif,Menu.NONE,"Information");
////            menu.add(Menu.NONE,R.id.suppNotif,Menu.NONE,"Supprimer");
////            menu.add(Menu.NONE,R.id.inportanteNotif,Menu.NONE,"Message importants");
        void display(Recenmment recenmment) throws SQLException, IOException {
            Picasso.with(itemView.getContext())
                    .load("http://192.168.43.1:2222/fabi/couverture/" + recenmment.getCouverteur())
                    .placeholder(R.drawable.img_default_livre)
                    .error(R.drawable.img_default_livre)
                    .transform(new RoundedTransformation(15,4))
                    .resize(178,284)
                    .into(mCouverture);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloadAndOpenPDF(recenmment.getPDF());
                }
            });
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
