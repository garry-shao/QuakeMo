package org.qmsos.quakemo;

import java.util.Date;

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

public class EarthquakeSearchFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	
	public static final String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";
	
	private SimpleCursorAdapter adapter;
	
	@Override
	public void onResume() {
		super.onResume();
		
		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		adapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_1, null, 
				new String[] { EarthquakeProvider.KEY_SUMMARY }, 
				new int[] { android.R.id.text1 }, 0);
		setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor result = getActivity().getContentResolver().query(ContentUris.withAppendedId(
				EarthquakeProvider.CONTENT_URI, id), null, null, null, null);
		
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
			
			DialogFragment dialogFragment = EarthquakeDetailsDialog.newInstance(getActivity(), quake);
			dialogFragment.show(getFragmentManager(), "dialog");
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
				EarthquakeProvider.KEY_SUMMARY };
		
		String where = EarthquakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
		String sortOrder = EarthquakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";
		
		return new CursorLoader(getActivity(), EarthquakeProvider.CONTENT_URI, 
				projection, where, null, sortOrder);
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
