package com.example.fabi.Controleur.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class InfoNoteCusto extends Dialog {
    public InfoNoteCusto(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
        setContentView(R.layout.info_note);
    }
    public void build()
    {
        show();
    }
}
