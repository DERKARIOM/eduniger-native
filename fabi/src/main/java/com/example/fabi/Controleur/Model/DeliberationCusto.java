package com.example.fabi.Controleur.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class DeliberationCusto extends Dialog {
    public DeliberationCusto(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
        setContentView(R.layout.deliberation);
    }
    public void build()
    {
        show();
    }
}
