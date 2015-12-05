package org.qmsos.quakemo;

import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * 
 * Show earthquakes as list.
 *
 */
public class QuakeListFragment extends ListFragment 
implements OnSharedPreferenceChangeListener, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[] { QuakeProvider.KEY_SUMMARY }, new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);

		getLoaderManager().initLoader(0, null, this);

		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);

		getActivity().startService(new Intent(getActivity(), QuakeUpdateService.class));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_list_options, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		Intent i = new Intent(getActivity(), QuakeUpdateService.class);

		switch (item.getItemId()) {
		case (R.id.menu_refresh):
			i.putExtra(QuakeUpdateService.MANUAL_REFRESH, true);
			getActivity().startService(i);

			return true;
		case (R.id.menu_purge):
			i.putExtra(QuakeUpdateService.PURGE_DATABASE, true);
			getActivity().startService(i);

			return true;
		default:
			return false;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		ContentResolver resolver = getActivity().getContentResolver();

		Cursor result = resolver.query(
				ContentUris.withAppendedId(QuakeProvider.CONTENT_URI, id), null, null, null, null);

		if (result.moveToFirst()) {
			Date date = new Date(result.getLong(result.getColumnIndex(QuakeProvider.KEY_DATE)));

			String details = result.getString(result.getColumnIndex(QuakeProvider.KEY_DETAILS));

			double magnitude = result.getDouble(result.getColumnIndex(QuakeProvider.KEY_MAGNITUDE));

			String link = result.getString(result.getColumnIndex(QuakeProvider.KEY_LINK));

			double location_la = result.getDouble(result.getColumnIndex(QuakeProvider.KEY_LOCATION_LA));
			double location_lo = result.getDouble(result.getColumnIndex(QuakeProvider.KEY_LOCATION_LO));
			Location location = new Location("database");
			location.setLatitude(location_la);
			location.setLongitude(location_lo);

			Earthquake quake = new Earthquake(date, details, location, magnitude, link);

			DialogFragment dialog = QuakeDetailsDialog.newInstance(getActivity(), quake);
			dialog.show(getFragmentManager(), "dialog");
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { QuakeProvider.KEY_ID, QuakeProvider.KEY_SUMMARY };

		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		int minMagnitude = Integer.parseInt(prefs.getString(PrefActivity.PREF_MIN_MAG, "3"));

		String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;

		CursorLoader loader = new CursorLoader(
				getActivity(), QuakeProvider.CONTENT_URI, projection, where, null, null);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PrefActivity.PREF_MIN_MAG)) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

}
