package com.ansonliu.shipmate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

/**
 * Created by m163876 on 3/1/2016.
 */
public class PhoneNumberTextWatcher implements TextWatcher {
    private AlertDialog dialog;
    public PhoneNumberTextWatcher(AlertDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 10)
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        else
            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1,
                                  int arg2, int arg3) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {}
}
