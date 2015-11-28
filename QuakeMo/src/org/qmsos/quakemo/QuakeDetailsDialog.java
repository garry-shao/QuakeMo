package org.qmsos.quakemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.dialog_details_title);

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_details, container, false);

		TextView textView = (TextView) view.findViewById(R.id.earthquakeDetailsTextView);

		String details = getArguments().getString(DIALOG_DETAILS);
		textView.setText(details);

		return view;
	}

}
