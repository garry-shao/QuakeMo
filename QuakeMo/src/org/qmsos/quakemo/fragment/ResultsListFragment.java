package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.ResultsActivity;
import org.qmsos.quakemo.data.Earthquake;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Search and display earthquakes stored in content provider.
 * 
 *
 */
public class ResultsListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

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
			query = args.getString(ResultsActivity.KEY_QUERY);
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
			long time = cursor.getLong(cursor.getColumnIndex(QuakeProvider.KEY_TIME));
			double magnitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_MAGNITUDE));
			double longitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LONGITUDE));
			double latitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LATITUDE));
			double depth = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_DEPTH));

			String details = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_DETAILS));
			String link = cursor.getString(cursor.getColumnIndex(QuakeProvider.KEY_LINK));

			Earthquake quake = new Earthquake(time, magnitude, longitude, latitude, depth);
			quake.setDetails(details);
			quake.setLink(link);

			return quake;
		} else {
			return null;
		}
	}

}
