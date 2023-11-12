package com.example.fabi.Controleur.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class EmpreinteConfirmerCusto extends Dialog {
    public EmpreinteConfirmerCusto(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
        setContentView(R.layout.confirmer_empreinte);
    }
    public void build()
    {
        show();
    }
}
