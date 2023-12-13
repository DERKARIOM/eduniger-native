package com.fabi.Model;

import android.app.Activity;
import android.app.Dialog;

import com.example.fabi.R;

public class SucceSuggesion extends Dialog {
    public SucceSuggesion (Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.succes_suggestion);
    }
    public void build()
    {
        show();
    }
}
