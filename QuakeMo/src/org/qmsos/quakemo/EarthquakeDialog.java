package org.qmsos.quakemo;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Show a specific earthquake's detail as a dialog.
 *
 */
public class EarthquakeDialog extends DialogFragment {
	private static final String DIALOG_STRING = "DIALOG_STRING";
	
	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * @param context The context that this dialog within.
	 * @param quake The particular earthquake to show. 
	 * @return
	 */
	public static EarthquakeDialog newInstance(Context context, Earthquake quake) {
		EarthquakeDialog fragment = new EarthquakeDialog();
		Bundle args = new Bundle();
		
		args.putString(DIALOG_STRING, quake.getDialogDetails());
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.dialog_title);
		
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.earthquake_details, container, false);
		
		String details = getArguments().getString(DIALOG_STRING);
		
		TextView textView = (TextView) view.findViewById(R.id.quakeDetailsTextView);
		textView.setText(details);
		
		return view;
	}
	
}
