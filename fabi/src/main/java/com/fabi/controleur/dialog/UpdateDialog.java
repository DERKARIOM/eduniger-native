package com.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class UpdateDialog extends Dialog {
    public UpdateDialog(Activity activity)
    {
        super(activity,R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_update);
    }
    public void build()
    {
        show();
    }
}
