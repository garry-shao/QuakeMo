package org.qmsos.quakemo.fragment;

import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.EarthquakeProvider;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.map.EarthquakeOverlay;
import org.qmsos.quakemo.map.MapTileChecker;
import org.qmsos.quakemo.util.IpcConstants;

import android.app.AlarmManager;
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
public class EarthquakeMap extends Fragment implements LoaderCallbacks<Cursor> {

	private static final String KEY_CENTER = "KEY_CENTER";
	private static final String KEY_ZOOM_LEVEL = "KEY_ZOOM_LEVEL";

	// Confine zoom levels of MapView.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;
	
	/**
	 * The earthquake overlay on the map.
	 */
	private EarthquakeOverlay mEarthquakeOverlay;

	/**
	 * Defined as there is only one view on this fragment.
	 */
	private MapView mMapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MapTileChecker.checkMapTileFiles(getContext());
		
		mMapView = new MapView(getContext());
		mMapView.setMultiTouchControls(true);
		mMapView.setTileSource(new XYTileSource(MapTileChecker.MAP_SOURCE, 
				ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX, 256, ".png", new String[] {}));
		mMapView.setUseDataConnection(false);
		mMapView.setTilesScaledToDpi(true);

		if (savedInstanceState != null) {
			GeoPoint center = savedInstanceState.getParcelable(KEY_CENTER);
			if (center != null) {
				mMapView.getController().setCenter(center);
			}

			int zoomLevel = savedInstanceState.getInt(KEY_ZOOM_LEVEL);
			if (zoomLevel > 0) {
				mMapView.getController().setZoom(zoomLevel);
			}
		} else {
			mMapView.getController().setZoom(ZOOM_LEVEL_MIN);
		}

		mEarthquakeOverlay = new EarthquakeOverlay(getContext(), null);
		mMapView.getOverlays().add(mEarthquakeOverlay);

		return mMapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_CENTER, (GeoPoint) mMapView.getMapCenter());
		outState.putInt(KEY_ZOOM_LEVEL, mMapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_LATITUDE,
				EarthquakeProvider.KEY_LONGITUDE };

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
		mEarthquakeOverlay.swapCursor(data);
		mMapView.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mEarthquakeOverlay.swapCursor(null);
		mMapView.invalidate();
	}

}