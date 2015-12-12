package org.qmsos.quakemo.fragment;

import java.util.Date;

import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.data.Earthquake;

import android.content.ContentUris;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class QuakeSearchFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	/**
	 * Key used to get query string from incoming intent.
	 */
	public static final String KEY_QUERY_EXTRA = "KEY_QUERY_EXTRA";

	private SimpleCursorAdapter adapter;

	@Override
	public void onResume() {
		super.onResume();

		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_1, null,
				new String[] { QuakeProvider.KEY_SUMMARY }, new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor result = getContext().getContentResolver().query(
				ContentUris.withAppendedId(QuakeProvider.CONTENT_URI, id), null, null, null, null);
		Earthquake earthquake = queryForQuake(result);
		result.close();
		
		if (earthquake != null) {
			DialogFragment dialog = QuakeDetailsDialog.newInstance(getContext(), earthquake);
			((QuakeDetailsDialog) dialog).setMapEnabled(true);
			
			dialog.show(getFragmentManager(), "dialog");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String query = "0";

		if (args != null) {
			query = args.getString(KEY_QUERY_EXTRA);
		}

		String[] projection = { QuakeProvider.KEY_ID, QuakeProvider.KEY_SUMMARY };

		String where = QuakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
		String sortOrder = QuakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

		return new CursorLoader(
				getContext(), QuakeProvider.CONTENT_URI, projection, where, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	/**
	 * Help method to get the FIRST Earthquake in cursor out from content provider.
	 * @param cursor
	 *            The query cursor.
	 * @return The first earthquake or NULL if not available.
	 */
	private Earthquake queryForQuake(Cursor cursor) {
		if (cursor.moveToFirst()) {
			Date date = new Date(cursor.getLong(cursor.getColumnIndex(QuakeProvider.KEY_DATE)));

			String details = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_DETAILS));

			double magnitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_MAGNITUDE));

			String link = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_LINK));

			double latitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LOCATION_LA));
			double longitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LOCATION_LO));
			Location location = new Location("database");
			location.setLatitude(latitude);
			location.setLongitude(longitude);

			Earthquake quake = new Earthquake(date, details, location, magnitude, link);

			return quake;
		} else {
			return null;
		}
	}

}