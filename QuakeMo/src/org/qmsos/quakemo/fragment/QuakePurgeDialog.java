package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Simple Material Design liked Dialog fragment, implements the listener inside
 * to get callback.
 * 
 *
 */
public class QuakePurgeDialog extends DialogFragment {

	public interface ShowSnackbarListener {
		void onShowSnackbar();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_purge_title);
		builder.setMessage(R.string.dialog_purge_message);
		builder.setNegativeButton(R.string.dialog_negative_button, null);
		builder.setPositiveButton(R.string.dialog_positive_button, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((ShowSnackbarListener) getActivity()).onShowSnackbar();
			}
		});

		return builder.create();
	}

}
