package org.qmsos.quakemo;

import org.osmdroid.views.MapView;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * Show earthquakes on map.
 *
 */
public class QuakeMapFragment extends Fragment implements LoaderCallbacks<Cursor> {
	
	/**
	 * The earthquake overlay on the map.
	 */
	private QuakeMapOverlay quakeMapOverlay;
	
	/**
	 * Defined as there is only one view on this fragment.
	 */
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity().getApplicationContext());
		mapView.setMultiTouchControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(1);
		
		mapView.getController().setZoom(1);
		
		return mapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		quakeMapOverlay = new QuakeMapOverlay(getActivity(), null);
		mapView.getOverlays().add(quakeMapOverlay);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				QuakeProvider.KEY_ID, 
				QuakeProvider.KEY_LOCATION_LA, 
				QuakeProvider.KEY_LOCATION_LO };
		
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		int minMagnitude = Integer.parseInt(
				prefs.getString(PrefActivity.PREF_MIN_MAG, "3"));
		String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
				
		CursorLoader loader = new CursorLoader(getActivity(), 
				QuakeProvider.CONTENT_URI, projection, where, null, null);
				
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		quakeMapOverlay.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		quakeMapOverlay.swapCursor(null);
	}

}
