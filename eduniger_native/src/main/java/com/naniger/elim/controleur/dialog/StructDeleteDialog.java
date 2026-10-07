package com.naniger.elim.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.naniger.elim.R;

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
