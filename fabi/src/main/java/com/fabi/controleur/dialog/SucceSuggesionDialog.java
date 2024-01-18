package com.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class SucceSuggesionDialog extends Dialog {
    public SucceSuggesionDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_succes_suggestion);
    }
    public void build()
    {
        show();
    }
}
