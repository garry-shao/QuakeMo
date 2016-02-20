package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.provider.EarthquakeContract.Entity;
import org.qmsos.quakemo.widget.CursorRecyclerViewAdapter;

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
	
	private CursorRecyclerViewAdapter mCursorAdapter;
	private RecyclerView mRecyclerView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_earthquake_list, container, false);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mCursorAdapter = new CursorRecyclerViewAdapter(getContext(), null);
		
		mRecyclerView = (RecyclerView) view.findViewById(R.id.earthquake_list);
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
		String[] projection = { Entity.ID, Entity.TIME, Entity.MAGNITUDE, Entity.DETAILS };

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		int minMagnitude = Integer.parseInt(prefs.getString(
				getString(R.string.PREF_SHOW_MINIMUM), 
				getString(R.string.minimum_values_default)));
		
		String where;
		boolean showAll = prefs.getBoolean(getString(R.string.PREF_SHOW_ALL), false);
		if (showAll) {
			where = Entity.MAGNITUDE + " >= " + minMagnitude;
		} else {
			int range = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_SHOW_RANGE), 
					getString(R.string.range_values_default)));
			
			long startMillis = System.currentTimeMillis() - range * AlarmManager.INTERVAL_DAY;
			
			where = Entity.MAGNITUDE + " >= " + minMagnitude + " AND " + 
					Entity.TIME + " >= " + startMillis;
		}
		
		return new CursorLoader(getContext(), Entity.CONTENT_URI, projection, where, null, null);
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
