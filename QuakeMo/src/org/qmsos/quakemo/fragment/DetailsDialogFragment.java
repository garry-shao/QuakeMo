package org.qmsos.quakemo.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.R;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Show a dialog of specific earthquake's details.
 *
 *
 */
public class DetailsDialogFragment extends DialogFragment {

	/**
	 * Key used to retrieve earthquake id from bundle.
	 */
	private static final String KEY_EARTHQUAKE = "KEY_EARTHQUAKE";

	/**
	 * Create a new instance of dialog that shows particular earthquake details.
	 * 
	 * @param context
	 *            The context that this dialog within.
	 * @param id
	 *            The id of this earthquake.
	 * @return The created dialog fragment instance.
	 */
	public static DetailsDialogFragment newInstance(Context context, long id) {
		Bundle args = new Bundle();
		args.putLong(KEY_EARTHQUAKE, id);

		DetailsDialogFragment fragment = new DetailsDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title);
		
		final long id = getArguments().getLong(KEY_EARTHQUAKE);

		Cursor cursor = getContext().getContentResolver()
				.query(ContentUris.withAppendedId(QuakeProvider.CONTENT_URI, id), null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			long time = cursor.getLong(cursor.getColumnIndex(QuakeProvider.KEY_TIME));
			double magnitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_MAGNITUDE));
			double depth = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_DEPTH));
			double longitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LONGITUDE));
			double latitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LATITUDE));
			String details = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_DETAILS));
			final String link = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_LINK));

			String lon = longitude > 0 ? Math.abs(longitude) + "\u00b0E" : Math.abs(longitude) + "\u00b0W";
			String lat = latitude > 0 ? Math.abs(latitude) + "\u00b0N" : Math.abs(latitude) + "\u00b0S";
			
			DateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss", Locale.US);
			String dialogDetails = dataFormat.format(new Date(time)) + 
					"\n\n" + "Magnitude: " + magnitude + 
					"\n\n" + "Depth: " + depth + " km" + 
					"\n\n" + "Coord: " + lon + " " + lat + 
					"\n\n" + details;
			
			builder.setMessage(dialogDetails);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			boolean linkEnabled = prefs.getBoolean(getString(R.string.PREF_LINK), false);
			if (linkEnabled) {
				builder.setPositiveButton(R.string.dialog_details_link, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(link));
						startActivity(intent);
					}
				});
			}
		} else {
			builder.setMessage("earthquake ID does not EXIST!");
		}
		cursor.close();

		return builder.create();
	}
}
