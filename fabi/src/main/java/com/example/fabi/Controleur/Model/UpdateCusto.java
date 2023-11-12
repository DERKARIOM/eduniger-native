package com.example.fabi.Controleur.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class UpdateCusto extends Dialog {
    public UpdateCusto (Activity activity)
    {
        super(activity,R.style.Dialog_fastpv);
        setContentView(R.layout.update);
    }
    public void build()
    {
        show();
    }
}
