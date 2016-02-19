package org.qmsos.quakemo.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.provider.EarthquakeContract.Entity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Show a dialog of specific earthquake's details.
 *
 *
 */
public class EarthquakeDetails extends DialogFragment {

	private static final String TAG = EarthquakeDetails.class.getSimpleName();
	
	/**
	 * Key used to retrieve earthquake id from bundle.
	 */
	private static final String KEY_EARTHQUAKE = "KEY_EARTHQUAKE";

	private OnLinkSelectedListener mListener;

	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * 
	 * @param context
	 *            The context that this dialog within.
	 * @param id
	 *            The id of this earthquake.
	 * @return The created dialog fragment instance.
	 */
	public static EarthquakeDetails newInstance(Context context, long id) {
		Bundle args = new Bundle();
		args.putLong(KEY_EARTHQUAKE, id);

		EarthquakeDetails fragment = new EarthquakeDetails();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (OnLinkSelectedListener) activity;
		} catch (ClassCastException e) {
			String listenerName = OnLinkSelectedListener.class.getSimpleName();
			
			throw new ClassCastException(activity.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = null;
		String link = null;
		Cursor cursor = null;
		try {
			final long id = getArguments().getLong(KEY_EARTHQUAKE);
			cursor = getContext().getContentResolver().query(
					ContentUris.withAppendedId(Entity.CONTENT_URI, id), null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long time = cursor.getLong(cursor.getColumnIndexOrThrow(Entity.TIME));
				double magnitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Entity.MAGNITUDE));
				double depth = cursor.getDouble(cursor.getColumnIndexOrThrow(Entity.DEPTH));
				double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Entity.LONGITUDE));
				double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Entity.LATITUDE));
				String details = cursor.getString(cursor.getColumnIndexOrThrow(Entity.DETAILS));
				link = cursor.getString(cursor.getColumnIndexOrThrow(Entity.LINK));
				
				String lon = 
						longitude > 0 ? Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
				String lat = 
						latitude > 0 ? Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
				
				DateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss", Locale.US);
				
				message = dataFormat.format(new Date(time)) + "\n\n" + 
						"Magnitude: " + magnitude + "\n\n" + 
						"Depth: " + depth + " km" + "\n\n" + 
						"Coord: " + lon + " " + lat + "\n\n" + 
						details;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Columns do not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title);

		if (message != null) {
			builder.setMessage(message);
		} else {
			builder.setMessage("earthquake ID does not EXIST!");
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean linkEnabled = prefs.getBoolean(getString(R.string.PREF_LINK), false);
		if (linkEnabled && link != null) {
			final String fLink = link;
			builder.setPositiveButton(R.string.dialog_details_link, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mListener.onLinkSelected(fLink);
				}
			});
		}

		return builder.create();
	}

	/**
	 * Interface for a callback to be invoked when open link action is called.
	 * 
	 *
	 */
	public interface OnLinkSelectedListener {
		/**
		 * Called when open link action is executed.
		 * 
		 * @param link
		 *            The link string.
		 */
		void onLinkSelected(String link);
	}

}
