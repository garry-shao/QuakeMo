package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.R;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Show earthquakes as list.
 * 
 *
 */
public class QuakeListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	
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

		DialogFragment dialog = DetailsDialogFragment.newInstance(getContext(), id);
		dialog.show(getFragmentManager(), "dialog");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { QuakeProvider.KEY_ID, QuakeProvider.KEY_SUMMARY };

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		int minMagnitude = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_MINIMUM), "3"));
		String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;

		CursorLoader loader = new CursorLoader(
				getContext(), QuakeProvider.CONTENT_URI, projection, where, null, null);

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

}
