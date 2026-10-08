package com.naniger.elim.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.naniger.elim.R;

public class DigitalPrintConfirmDialog extends Dialog {
    public DigitalPrintConfirmDialog(Activity activity)
    {
        super(activity, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
        setContentView(R.layout.dialog_confirmer_empreinte);
    }
    public void build()
    {
        show();
    }
}
