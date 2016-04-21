package com.ansonliu.shipmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by m163876 on 3/5/2016.
 */
public class CancelPickupDialogListenerFragment extends DialogFragment implements DialogInterface.OnClickListener {

    CancelPickupDialogListener myListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Cancel Pickup?");
        builder.setIcon(null);
        builder.setMessage(null);
        builder.setPositiveButton("Yes", this);
        builder.setNegativeButton("No", this);
      	setCancelable(false);
        return builder.create();
    }

    public void onClick(DialogInterface dialog, int id) {
        switch (id) {
            case Dialog.BUTTON_POSITIVE: myListener.onCancelPickupYes(); break;
        }
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            myListener = (CancelPickupDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CancelPickupDialogListener");
        }
    }
}
