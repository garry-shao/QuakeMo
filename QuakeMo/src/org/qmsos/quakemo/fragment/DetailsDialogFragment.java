package org.qmsos.quakemo.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.data.Earthquake;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Show a dialog of specific earthquake's details.
 *
 *
 */
public class DetailsDialogFragment extends DialogFragment {

	/**
	 * Follow keys are things need to be saved on configuration change.
	 */
	private static final String KEY_EARTHQUAKE = "KEY_EARTHQUAKE";
	private static final String KEY_ADDMAP = "KEY_ADDMAP";

	/**
	 * Flag on whether map button is shown on dialog.
	 */
	private boolean mapEnabled;
	
	/**
	 * Used by this dialog to communicate with the containing activity.
	 *
	 */
	public interface ShowMapListener {
		void onShowMap(Earthquake earthquake);
	}

	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * 
	 * @param context
	 *            The context that this dialog within.
	 * @param earthquake
	 *            The particular earthquake to show.
	 * @return The new formed details dialog.
	 */
	public static DetailsDialogFragment newInstance(Context context, Earthquake earthquake) {
		Bundle args = new Bundle();
		
		args.putParcelable(KEY_EARTHQUAKE, earthquake);
		
		DetailsDialogFragment fragment = new DetailsDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			mapEnabled = savedInstanceState.getBoolean(KEY_ADDMAP);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		arg0.putBoolean(KEY_ADDMAP, mapEnabled);

		super.onSaveInstanceState(arg0);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Earthquake earthquake = getArguments().getParcelable(KEY_EARTHQUAKE);

		DateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss", Locale.US);
		String details = dataFormat.format(new Date(earthquake.getTime())) + 
				"\n\n" + "Magnitude: " + earthquake.getMagnitude() + 
				"\n\n" + "Depth: " + earthquake.getDepth() + " km" + 
				"\n\n" + earthquake.getDetails() + 
				"\n\n" + earthquake.getLink();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title).setMessage(details);
		if (mapEnabled) {
			builder.setPositiveButton(R.string.dialog_details_positive, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ShowMapListener) getActivity()).onShowMap(earthquake);
				}
			});
		}
		
		return builder.create();
	}

	public boolean isMapEnabled() {
		return mapEnabled;
	}

	public void setMapEnabled(boolean mapEnabled) {
		this.mapEnabled = mapEnabled;
	}

}
