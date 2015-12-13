package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.data.Earthquake;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Show a dialog of specific earthquake's details.
 *
 *
 */
public class QuakeDetailsDialog extends DialogFragment {

	/**
	 * Follow keys are things need to be saved on configuration change.
	 */
	private static final String KEY_DETAILS = "KEY_DETAILS";
	private static final String KEY_LOCATION = "KEY_LOCATION";
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
		void onShowMap(Location location);
	}

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
		args.putString(KEY_DETAILS, quake.getDialogDetails());
		args.putParcelable(KEY_LOCATION, quake.getLocation());
		
		QuakeDetailsDialog fragment = new QuakeDetailsDialog();
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
		String details = getArguments().getString(KEY_DETAILS);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title).setMessage(details);
		if (mapEnabled) {
			builder.setPositiveButton(R.string.dialog_details_positive, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
					
					//Pass location info to the containing activity.
					Location location = getArguments().getParcelable(KEY_LOCATION);
					((ShowMapListener) getActivity()).onShowMap(location);
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
