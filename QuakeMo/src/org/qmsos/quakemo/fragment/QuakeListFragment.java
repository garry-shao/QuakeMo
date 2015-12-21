package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.MainActivity;
import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.QuakeUpdateService;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.util.UtilCursorAdapter;

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
public class QuakeListFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final String KEY_RECYCLER_VIEW_STATE = "KEY_RECYCLER_VIEW_STATE";
	
	private UtilCursorAdapter adapter;
	private RecyclerView recyclerView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		recyclerView = (RecyclerView) inflater.inflate(R.layout.view_recycler, container, false);

		return recyclerView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		adapter = new UtilCursorAdapter(getContext(), null);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);
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
		
		LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof LinearLayoutManager) {
			outState.putParcelable(KEY_RECYCLER_VIEW_STATE, layoutManager.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		
		if (savedInstanceState != null) {
			Parcelable savedRecyclerViewState = savedInstanceState.getParcelable(KEY_RECYCLER_VIEW_STATE);
			LayoutManager layoutManager = recyclerView.getLayoutManager();
			if (layoutManager instanceof LinearLayoutManager) {
				layoutManager.onRestoreInstanceState(savedRecyclerViewState);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { QuakeProvider.KEY_ID, QuakeProvider.KEY_TIME, 
				QuakeProvider.KEY_MAGNITUDE, QuakeProvider.KEY_DETAILS };

		// Create search cursor.
		if (args != null && args.getString(MainActivity.KEY_QUERY) != null) {
			String query = args.getString(MainActivity.KEY_QUERY);
			
			String where = QuakeProvider.KEY_DETAILS + " LIKE \"%" + query + "%\"";
			String sortOrder = QuakeProvider.KEY_DETAILS + " COLLATE LOCALIZED ASC";

			return new CursorLoader(
				getContext(), QuakeProvider.CONTENT_URI, projection, where, null, sortOrder);
		} else {
		// Create data cursor.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
			int minMagnitude = Integer.parseInt(
					prefs.getString(getString(R.string.PREF_SHOW_MINIMUM), "3"));
		
			String where;
			boolean showAll = prefs.getBoolean(getString(R.string.PREF_SHOW_ALL), false);
			if (showAll) {
				where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
			} else {
				int range = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_RANGE), "1"));
				long startMillis = System.currentTimeMillis()
						- range * 24 * QuakeUpdateService.ONE_HOUR_IN_MILLISECONDS;
				
				where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude
						+ " AND " + QuakeProvider.KEY_TIME + " > " + startMillis;
			}

			return new CursorLoader(
					getContext(), QuakeProvider.CONTENT_URI, projection, where, null, null);
		}
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
