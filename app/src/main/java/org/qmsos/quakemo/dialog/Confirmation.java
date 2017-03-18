package org.qmsos.quakemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.qmsos.quakemo.R;

/**
 * Simple Material Design liked Dialog fragment, implements the listener inside
 * to get callback.
 */
public class Confirmation extends DialogFragment {

    private OnConfirmationSelectedListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (OnConfirmationSelectedListener) context;
        } catch (ClassCastException e) {
            String listenerName = OnConfirmationSelectedListener.class.getSimpleName();

            throw new ClassCastException(context.toString()
                    + " must implements "
                    + listenerName);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_confirmation_title);
        builder.setMessage(R.string.dialog_confirmation_message);
        builder.setNegativeButton(R.string.dialog_confirmation_negative, null);
        builder.setPositiveButton(R.string.dialog_confirmation_positive, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onConfirmationSelected();
            }
        });

        return builder.create();
    }

    /**
     * Interface for a callback to be invoked when the action is confirmed.
     */
    public interface OnConfirmationSelectedListener {
        /**
         * Called when the action is confirmed.
         */
        void onConfirmationSelected();
    }
}