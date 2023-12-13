package com.fabi.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class EvaluezVousCusto extends Dialog {
    public EvaluezVousCusto(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.evaluez_nous);
    }
    public void build()
    {
        show();
    }
}

