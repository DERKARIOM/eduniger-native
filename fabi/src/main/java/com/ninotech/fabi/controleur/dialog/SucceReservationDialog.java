package com.ninotech.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.ninotech.fabi.R;

public class SucceReservationDialog extends Dialog {
    public SucceReservationDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_succes_reservation);
    }
    public void build()
    {
        show();
    }
}
