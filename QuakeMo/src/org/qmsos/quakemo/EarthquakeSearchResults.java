package org.qmsos.quakemo;

import java.util.Date;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class EarthquakeSearchResults extends ListActivity implements LoaderCallbacks<Cursor> {

	private static final String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";
	
	private SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new SimpleCursorAdapter(this, 
				android.R.layout.simple_list_item_1, null, 
				new String[] { EarthquakeProvider.KEY_SUMMARY }, 
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
		
		parseIntent(getIntent());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor result = getContentResolver().query(
				ContentUris.withAppendedId(EarthquakeProvider.CONTENT_URI, id), null, null, null, null);
		
		if (result.moveToFirst()) {
			Date date = new Date(result.getLong(
					result.getColumnIndex(EarthquakeProvider.KEY_DATE)));
			
			String details = result.getString(
					result.getColumnIndex(EarthquakeProvider.KEY_DETAILS));
			
			double magnitude = result.getDouble(
					result.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
			
			String link = result.getString(
					result.getColumnIndex(EarthquakeProvider.KEY_LINK));
			
			double location_la = result.getDouble(
					result.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LA));
			double location_lo = result.getDouble(
					result.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LO));
			Location location = new Location("db");
			location.setLatitude(location_la);
			location.setLongitude(location_lo);
			
			Earthquake quake = new Earthquake(date, details, location, magnitude, link);
			
			DialogFragment dialogFragment = EarthquakeDialog.newInstance(this, quake);
			dialogFragment.show(getFragmentManager(), "dialog");
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		parseIntent(getIntent());
	}

	/**
	 * Parse and process any search intent.
	 * @param intent The intent to parse.
	 */
	private void parseIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			
			Bundle args = new Bundle();
			args.putString(QUERY_EXTRA_KEY, searchQuery);
			
			getLoaderManager().restartLoader(0, args, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String query = "0";
		
		if (args != null) {
			query = args.getString(QUERY_EXTRA_KEY);
		}
		
		String[] projection = {
			EarthquakeProvider.KEY_ID, 
			EarthquakeProvider.KEY_SUMMARY	
		};
		String where = EarthquakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
		String[] whereArgs = null;
		String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";
		
		return new CursorLoader(this, EarthquakeProvider.CONTENT_URI, 
				projection, where, whereArgs, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
