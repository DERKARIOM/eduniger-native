package com.ninotech.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.ninotech.fabi.R;

public class SimpleOkDialog extends Dialog {
    public SimpleOkDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_simple_ok);
    }
    public void build()
    {
        show();
    }
}
