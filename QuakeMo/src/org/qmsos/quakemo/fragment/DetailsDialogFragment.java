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
import android.database.Cursor;
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
		String dialogDetails;

		final long id = getArguments().getLong(KEY_EARTHQUAKE);

		Cursor cursor = getContext().getContentResolver()
				.query(ContentUris.withAppendedId(QuakeProvider.CONTENT_URI, id), null, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				long time = cursor.getLong(cursor.getColumnIndex(QuakeProvider.KEY_TIME));
				double magnitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_MAGNITUDE));
				double depth = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_DEPTH));
				String details = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_DETAILS));
				String link = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_LINK));

				DateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss", Locale.US);
				dialogDetails = dataFormat.format(new Date(time)) + 
						"\n\n" + "Magnitude: " + magnitude + 
						"\n\n" + "Depth: " + depth + " km" + 
						"\n\n" + details + "\n\n" + link;
			} else {
				dialogDetails = "earthquake ID does not EXIST!";
			}
		} finally {
			cursor.close();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.dialog_details_title).setMessage(dialogDetails);

		return builder.create();
	}

}
