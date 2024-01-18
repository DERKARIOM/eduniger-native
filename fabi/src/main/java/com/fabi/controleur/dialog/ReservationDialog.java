package com.fabi.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.example.fabi.R;

public class ReservationDialog extends Dialog {
    public ReservationDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_reservation);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.transparente));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
    public void build()
    {
        show();
    }
}

