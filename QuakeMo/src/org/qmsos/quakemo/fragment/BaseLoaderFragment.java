package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.contract.ProviderContract.Entity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;

/**
 * Abstract class that used as template class, implementing loader callback.
 * 
 *
 */
public abstract class BaseLoaderFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final long INTERVAL_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { Entity.ID, Entity.TIME, Entity.MAGNITUDE, Entity.DETAILS, 
				Entity.LATITUDE, Entity.LONGITUDE };

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		String where;
		boolean showAll = prefs.getBoolean(getString(R.string.PREF_DISPLAY_ALL), false);
		if (showAll) {
			where = null;
		} else {
			int range = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_DISPLAY_RANGE), 
					getString(R.string.default_pref_range_value)));
			
			long startMillis = System.currentTimeMillis() - range * INTERVAL_DAY_IN_MILLIS;
			
			where = Entity.TIME + " >= " + startMillis;
		}
		
		return new CursorLoader(getContext(), Entity.CONTENT_URI, projection, where, null, null);
	}

}
