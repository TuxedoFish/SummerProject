package com.example.harry.umbrellafindr.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.harry.umbrellafindr.R;

public class ErrorAlertDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder failedToRegisterBuilder = new AlertDialog.Builder(getActivity());

            failedToRegisterBuilder.setMessage(R.string.failed_to_register);

            return failedToRegisterBuilder.create();
        }
}
