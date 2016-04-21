package com.ansonliu.shipmate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by m163876 on 3/1/2016.
 */
public class PhoneNumberDialogListenerFragment extends DialogFragment {

    PhoneNumberDialogListener myListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.phone_number_dialog, null);
        builder.setView(layout);
        builder.setTitle("Phone Number");
        setCancelable(false);

        final EditText numberEditText = (EditText) layout.findViewById(R.id.number_edit_text);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                myListener.onDialogSubmit(numberEditText.getText().toString());
            }

        });

        /*
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        */

        String passedNumber = getArguments().getString("phoneNumber");
        if (passedNumber != null)
            numberEditText.setText(passedNumber);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (numberEditText.getText().length() != 10) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });

        numberEditText.addTextChangedListener(new PhoneNumberTextWatcher(dialog));

        return dialog;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            myListener = (PhoneNumberDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PhoneNumberDialogListener");
        }
    }
}
