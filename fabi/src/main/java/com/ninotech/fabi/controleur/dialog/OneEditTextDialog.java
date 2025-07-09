package com.ninotech.fabi.controleur.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import com.ninotech.fabi.R;

public class OneEditTextDialog extends Dialog {
    public OneEditTextDialog(Activity activity)
    {
        super(activity, R.style.Dialog_fastpv);
        setContentView(R.layout.dialog_edit_text_one);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
    public void build()
    {
        show();
    }
}
