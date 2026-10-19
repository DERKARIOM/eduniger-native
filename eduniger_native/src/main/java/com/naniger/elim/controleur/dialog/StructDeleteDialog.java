package com.naniger.elim.controleur.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.naniger.elim.R;

/**
 * Confirmation « Quitter une structure » (mot de passe requis).
 * Mise en page : res/layout/dialog_structure_delete.xml (modes clair et sombre).
 */
public class StructDeleteDialog extends Dialog {

    private final EditText mPassword;
    private final TextView mError;

    public StructDeleteDialog(Activity activity) {
        super(activity, R.style.Dialog_fastpv);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_structure_delete);

        mPassword = findViewById(R.id.edit_text_dialog_struct_delete_password);
        mError = findViewById(R.id.text_view_dialog_structure_delete_err);

        // L'erreur disparaît dès que l'utilisateur corrige sa saisie.
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) { clearError(); }
        });

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            // Largeur : 90 % de l'écran, plafonnée à 400 dp (tablettes, paysage).
            DisplayMetrics dm = activity.getResources().getDisplayMetrics();
            int width = Math.min((int) (dm.widthPixels * 0.9f), (int) (400 * dm.density));
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    public void showError(@StringRes int message) {
        mPassword.setBackgroundResource(R.drawable.bg_dialog_input_error);
        mError.setText(message);
        mError.setVisibility(View.VISIBLE);
    }

    public void clearError() {
        if (mError.getVisibility() == View.VISIBLE) {
            mPassword.setBackgroundResource(R.drawable.bg_dialog_input);
            mError.setVisibility(View.GONE);
        }
    }

    public void build() {
        show();
    }
}
