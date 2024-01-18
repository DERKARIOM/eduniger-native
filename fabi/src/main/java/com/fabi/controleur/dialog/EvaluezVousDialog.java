package com.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class EvaluezVousDialog extends Dialog {
    public EvaluezVousDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_evaluez_nous);
    }
    public void build()
    {
        show();
    }
}

