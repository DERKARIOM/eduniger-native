package com.ninotech.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.ninotech.fabi.R;

public class EmpreinteConfirmerDialog extends Dialog {
    public EmpreinteConfirmerDialog(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
        setContentView(R.layout.dialog_confirmer_empreinte);
    }
    public void build()
    {
        show();
    }
}
