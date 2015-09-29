package org.qmsos.quakemo;

import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EarthquakeMapFragment extends Fragment implements LoaderCallbacks<Cursor> {
	EarthquakeOverlay earthquakeOverlay;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		earthquakeOverlay = new EarthquakeOverlay(getActivity(), null);

		MapView earthquakeMapView = ((EarthquakeActivity) getActivity()).mapView;
		earthquakeMapView.getOverlays().add(earthquakeOverlay);
		
		return earthquakeMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				EarthquakeProvider.KEY_ID, 
				EarthquakeProvider.KEY_LOCATION_LA, 
				EarthquakeProvider.KEY_LOCATION_LO,
		};
		
		String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + 
				((EarthquakeActivity)getActivity()).minMagnitude;
				
		CursorLoader loader = new CursorLoader(getActivity(), 
				EarthquakeProvider.CONTENT_URI, projection, where, null, null);
				
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		earthquakeOverlay.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		earthquakeOverlay.swapCursor(null);
	}

}
