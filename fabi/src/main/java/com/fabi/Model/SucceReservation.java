package com.fabi.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class SucceReservation extends Dialog {
    public SucceReservation(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.succes_reservation);
    }
    public void build()
    {
        show();
    }
}
