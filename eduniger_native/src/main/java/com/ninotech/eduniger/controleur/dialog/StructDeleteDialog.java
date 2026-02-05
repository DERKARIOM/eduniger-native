package com.ninotech.eduniger.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.ninotech.eduniger.R;

public class StructDeleteDialog extends Dialog {
    public StructDeleteDialog(Activity activity)
    {
        super(activity,R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_structure_delete);
    }
    public void build()
    {
        show();
    }
}
