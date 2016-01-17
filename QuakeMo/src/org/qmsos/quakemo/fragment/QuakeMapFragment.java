package org.qmsos.quakemo.fragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.MainActivity;
import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.QuakeUpdateService;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.util.UtilMapOverlay;

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
 * Show earthquakes on map.
 *
 *
 */
public class QuakeMapFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final String KEY_CENTER = "KEY_CENTER";
	private static final String KEY_ZOOM_LEVEL = "KEY_ZOOM_LEVEL";
	
	// Confine zoom levels of MapView.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;
	
	/**
	 * The earthquake overlay on the map.
	 */
	private UtilMapOverlay mapOverlay;

	/**
	 * Defined as there is only one view on this fragment.
	 */
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getContext());
		mapView.setMultiTouchControls(true);
		mapView.setUseDataConnection(false);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(ZOOM_LEVEL_MIN);
		mapView.setMaxZoomLevel(ZOOM_LEVEL_MAX);

		if (savedInstanceState != null) {
			GeoPoint center = savedInstanceState.getParcelable(KEY_CENTER);
			if (center != null) {
				mapView.getController().setCenter(center);
			}

			int zoomLevel = savedInstanceState.getInt(KEY_ZOOM_LEVEL);
			if (zoomLevel > 0) {
				mapView.getController().setZoom(zoomLevel);
			}
		} else {
			mapView.getController().setZoom(ZOOM_LEVEL_MIN);
		}

		mapOverlay = new UtilMapOverlay(getContext(), null);
		mapView.getOverlays().add(mapOverlay);

		return mapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_CENTER, (GeoPoint) mapView.getMapCenter());
		outState.putInt(KEY_ZOOM_LEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { QuakeProvider.KEY_ID, QuakeProvider.KEY_LATITUDE,
				QuakeProvider.KEY_LONGITUDE };

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
		
			int minMagnitude = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_MINIMUM), 
					getString(R.string.minimum_values_default)));

			String where;
			boolean showAll = prefs.getBoolean(getString(R.string.PREF_SHOW_ALL), false);
			if (showAll) {
				where = QuakeProvider.KEY_MAGNITUDE + " >= " + minMagnitude;
			} else {
				int range = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_RANGE), 
						getString(R.string.range_values_default)));
				long startMillis = System.currentTimeMillis()
						- range * 24 * QuakeUpdateService.ONE_HOUR_IN_MILLISECONDS;
				
				where = QuakeProvider.KEY_MAGNITUDE + " >= " + minMagnitude
						+ " AND " + QuakeProvider.KEY_TIME + " >= " + startMillis;
			}

			return new CursorLoader(
					getContext(), QuakeProvider.CONTENT_URI, projection, where, null, null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mapOverlay.swapCursor(data);
		mapView.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mapOverlay.swapCursor(null);
		mapView.invalidate();
	}

}
