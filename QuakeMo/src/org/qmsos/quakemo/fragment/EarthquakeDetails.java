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
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;

/**
 * Show a dialog of specific earthquake's details.
 *
 *
 */
public class EarthquakeDetails extends DialogFragment implements LoaderCallbacks<Cursor> {

	// Key used to retrieve earthquake id from bundle.
	private static final String KEY_EARTHQUAKE_ID = "KEY_EARTHQUAKE_ID";

	private OnLinkSelectedListener mListener;

	private String mMessage;
	private String mLink;

	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * 
	 * @param context
	 *            The context that this dialog within.
	 * @param earthquakeId
	 *            The id of this earthquake.
	 * @return The created dialog fragment instance.
	 */
	public static EarthquakeDetails newInstance(Context context, long earthquakeId) {
		Bundle args = new Bundle();
		args.putLong(KEY_EARTHQUAKE_ID, earthquakeId);

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Initialize here in order to prepare infos that are used in 
		// onCreateDialog(), remember onCreateDialog() is called after 
		// onCreate() and before onCreateView(). 
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title);

		if (mMessage != null) {
			builder.setMessage(mMessage);
		} else {
			builder.setMessage("Earthquake ID does not EXIST!");
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean linkEnabled = prefs.getBoolean(getString(R.string.PREF_LINK), false);
		if (linkEnabled) {
			builder.setPositiveButton(R.string.dialog_details_link, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mLink != null) {
						mListener.onLinkSelected(mLink);
					} else {
						mListener.onLinkInvalid();
					}
				}
			});
		}

		return builder.create();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long earthquakeId = getArguments().getLong(KEY_EARTHQUAKE_ID);
		Uri earthquakeUri = ContentUris.withAppendedId(Entity.CONTENT_URI, earthquakeId);
		
		return new CursorLoader(getContext(), earthquakeUri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && data.moveToFirst()) {
			long time = data.getLong(data.getColumnIndexOrThrow(Entity.TIME));
			double magnitude = data.getDouble(data.getColumnIndexOrThrow(Entity.MAGNITUDE));
			double depth = data.getDouble(data.getColumnIndexOrThrow(Entity.DEPTH));
			double longitude = data.getDouble(data.getColumnIndexOrThrow(Entity.LONGITUDE));
			double latitude = data.getDouble(data.getColumnIndexOrThrow(Entity.LATITUDE));
			String details = data.getString(data.getColumnIndexOrThrow(Entity.DETAILS));
			mLink = data.getString(data.getColumnIndexOrThrow(Entity.LINK));
			
			String lon = longitude > 0 ? 
					Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
			String lat = latitude > 0 ? 
					Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
			
			DateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss", Locale.US);
			
			mMessage = dataFormat.format(new Date(time)) + "\n\n" + 
					"Magnitude: " + magnitude + "\n\n" + 
					"Depth: " + depth + " km" + "\n\n" + 
					"Coord: " + lon + " " + lat + "\n\n" + 
					details;
			
			AlertDialog dialog = (AlertDialog) getDialog();
			dialog.setMessage(mMessage);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mLink = null;
		mMessage = null;
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
		
		/**
		 * Called when the link uri is invalid.
		 */
		void onLinkInvalid();
	}

}
