package com.ansonliu.shipmate;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.os.Bundle;

/**
 * Created by m163876 on 4/19/2016.
 */
public class InstructionDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Instructions");
        builder.setMessage("When you request a pickup, a phone call will be initiated to the SHIPMATE duty phone.\nYou must talk to the driver to verify that you are a real person and your location.");
        builder.setPositiveButton("Okay", null);
        builder.setCancelable(false);
        return builder.create();
    }
}
