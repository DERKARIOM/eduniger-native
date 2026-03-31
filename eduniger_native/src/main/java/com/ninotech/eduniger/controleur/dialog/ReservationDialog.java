package com.ninotech.eduniger.controleur.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ninotech.eduniger.R;

/**
 * ReservationDialog — BottomSheetDialog dark.
 *
 * Le layout est gonflé dès le constructeur via inflate(),
 * ce qui permet d'appeler findViewById() AVANT show()/build(),
 * exactement comme l'ancien Dialog.
 *
 * Aucun changement requis dans BookActivity.java.
 */
public class ReservationDialog extends BottomSheetDialog {

    private final View mRootView;

    public ReservationDialog(Context context) {
        super(context, R.style.BottomSheetDarkTheme);
        // Gonfler le layout immédiatement — findViewById disponible dès la construction
        mRootView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reservation_bottom_sheet, null, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Réutiliser la vue déjà gonflée dans le constructeur
        setContentView(mRootView);

        if (getWindow() != null) {
            getWindow().setDimAmount(0.6f);
        }
    }

    /**
     * Override findViewById pour chercher dans mRootView en priorité.
     * Garantit que ça fonctionne AVANT et APRÈS show().
     */
    @Override
    public <T extends View> T findViewById(int id) {
        T view = mRootView.findViewById(id);
        if (view != null) return view;
        return super.findViewById(id);
    }

    /** Compatibilité avec le code existant : build() → show() */
    public void build() {
        show();
    }

    // cancel() et dismiss() sont hérités de BottomSheetDialog — rien à changer dans BookActivity
}