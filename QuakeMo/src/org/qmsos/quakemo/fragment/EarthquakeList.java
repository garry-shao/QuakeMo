package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.EarthquakeProvider;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.util.IpcConstants;
import org.qmsos.quakemo.widget.RecyclerViewCursorAdapter;

import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Show earthquakes as list.
 * 
 *
 */
public class EarthquakeList extends Fragment implements LoaderCallbacks<Cursor> {

	private static final String KEY_RECYCLER_VIEW_STATE = "KEY_RECYCLER_VIEW_STATE";
	
	private RecyclerViewCursorAdapter mCursorAdapter;
	private RecyclerView mRecyclerView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRecyclerView = (RecyclerView) inflater.inflate(R.layout.view_recycler, container, false);

		return mRecyclerView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCursorAdapter = new RecyclerViewCursorAdapter(getContext(), null);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setAdapter(mCursorAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		LayoutManager layoutManager = mRecyclerView.getLayoutManager();
		if (layoutManager instanceof LinearLayoutManager) {
			outState.putParcelable(KEY_RECYCLER_VIEW_STATE, layoutManager.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		
		if (savedInstanceState != null) {
			Parcelable savedRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE);
			LayoutManager layoutManager = mRecyclerView.getLayoutManager();
			if (layoutManager instanceof LinearLayoutManager) {
				layoutManager.onRestoreInstanceState(savedRecyclerViewState);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_TIME, 
				EarthquakeProvider.KEY_MAGNITUDE, EarthquakeProvider.KEY_DETAILS };

		// Create search cursor.
		if (args != null && args.getString(IpcConstants.QUERY_CONTENT_KEY) != null) {
			String query = args.getString(IpcConstants.QUERY_CONTENT_KEY);
			
			String where = EarthquakeProvider.KEY_DETAILS + " LIKE \"%" + query + "%\"";
			String sortOrder = EarthquakeProvider.KEY_DETAILS + " COLLATE LOCALIZED ASC";

			return new CursorLoader(
				getContext(), EarthquakeProvider.CONTENT_URI, projection, where, null, sortOrder);
		} else {
		// Create data cursor.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
			int minMagnitude = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_MINIMUM), 
					getString(R.string.minimum_values_default)));
		
			String where;
			boolean showAll = prefs.getBoolean(getString(R.string.PREF_SHOW_ALL), false);
			if (showAll) {
				where = EarthquakeProvider.KEY_MAGNITUDE + " >= " + minMagnitude;
			} else {
				int range = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_RANGE), 
						getString(R.string.range_values_default)));
				long startMillis = System.currentTimeMillis() - range * AlarmManager.INTERVAL_DAY;
				
				where = EarthquakeProvider.KEY_MAGNITUDE + " >= " + minMagnitude
						+ " AND " + EarthquakeProvider.KEY_TIME + " >= " + startMillis;
			}

			return new CursorLoader(
					getContext(), EarthquakeProvider.CONTENT_URI, projection, where, null, null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

}
