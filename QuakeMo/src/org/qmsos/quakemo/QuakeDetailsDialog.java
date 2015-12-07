package org.qmsos.quakemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * 
 * Show a dialog of specific earthquake's details.
 *
 */
public class QuakeDetailsDialog extends DialogFragment {

	/**
	 * Key of the details of earthquake passing in bundle object.
	 */
	private static final String DIALOG_DETAILS = "DIALOG_DETAILS";

	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * 
	 * @param context
	 *            The context that this dialog within.
	 * @param quake
	 *            The particular earthquake to show.
	 * @return The new formed details dialog.
	 */
	public static QuakeDetailsDialog newInstance(Context context, Earthquake quake) {
		Bundle args = new Bundle();
		args.putString(DIALOG_DETAILS, quake.getDialogDetails());

		QuakeDetailsDialog fragment = new QuakeDetailsDialog();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String details = getArguments().getString(DIALOG_DETAILS);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title).setMessage(details);
		
		return builder.create();
	}

}
