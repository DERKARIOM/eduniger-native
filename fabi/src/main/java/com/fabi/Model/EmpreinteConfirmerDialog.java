package com.fabi.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

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
