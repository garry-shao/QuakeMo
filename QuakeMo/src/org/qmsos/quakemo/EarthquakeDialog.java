package org.qmsos.quakemo;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EarthquakeDialog extends DialogFragment {
	private static final String DIALOG_STRING = "DIALOG_STRING";
	
	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * @param context The context that this dialog within.
	 * @param quake The particular earthquake to show. 
	 * @return
	 */
	public static EarthquakeDialog newInstance(Context context, EarthQuake quake) {
		EarthquakeDialog fragment = new EarthquakeDialog();
		Bundle args = new Bundle();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		String dateString = dateFormat.format(quake.getDate());
		String quakeText = dateString + "\n" + "Magnitude " + quake.getMagnitude() + 
				"\n" + quake.getDetails() + "\n" + quake.getLink();
		
		args.putString(DIALOG_STRING, quakeText);
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle("Earthquake Details");
		
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
