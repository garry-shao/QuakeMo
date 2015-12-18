package org.qmsos.quakemo.fragment;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.MainActivity;
import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.QuakeUpdateService;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.util.UtilQuakeOverlay;

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
	private static final String KEY_ZOOMLEVEL = "KEY_ZOOMLEVEL";
	
	/**
	 * The earthquake overlay on the map.
	 */
	private UtilQuakeOverlay quakeOverlay;

	/**
	 * Defined as there is only one view on this fragment.
	 */
	private MapView mapView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getContext());
		mapView.setMultiTouchControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(1);

		if (savedInstanceState != null) {
			GeoPoint center = savedInstanceState.getParcelable(KEY_CENTER);
			if (center != null) {
				mapView.getController().setCenter(center);
			}

			int i = savedInstanceState.getInt(KEY_ZOOMLEVEL);
			if (i > 0) {
				mapView.getController().setZoom(i);
			}
		} else {
			mapView.getController().setZoom(1);
		}

		quakeOverlay = new UtilQuakeOverlay(getContext());
		mapView.getOverlays().add(quakeOverlay);
		
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
		outState.putInt(KEY_ZOOMLEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { QuakeProvider.KEY_ID, QuakeProvider.KEY_LATITUDE,
				QuakeProvider.KEY_LONGITUDE };

		// Create search cursor.
		if (args != null && args.getString(MainActivity.KEY_QUERY) != null) {
			String query = args.getString(MainActivity.KEY_QUERY);

			String where = QuakeProvider.KEY_SUMMARY + " LIKE \"%" + query + "%\"";
			String sortOrder = QuakeProvider.KEY_SUMMARY + " COLLATE LOCALIZED ASC";

			return new CursorLoader(
				getContext(), QuakeProvider.CONTENT_URI, projection, where, null, sortOrder);
		} else {
		// Create data cursor.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
			int minMagnitude = Integer.parseInt(
					prefs.getString(getString(R.string.PREF_SHOW_MINIMUM), "3"));
		
			int range = Integer.parseInt(prefs.getString(getString(R.string.PREF_SHOW_RANGE), "1"));
			long startMillis = System.currentTimeMillis()
					- range * 24 * QuakeUpdateService.ONE_HOUR_IN_MILLISECONDS;

			String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude
					+ " AND " + QuakeProvider.KEY_TIME + " > " + startMillis;
		
			return new CursorLoader(
					getContext(), QuakeProvider.CONTENT_URI, projection, where, null, null);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		quakeOverlay.setGeoPoints(parseGeoPoints(data));

		mapView.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		quakeOverlay.setGeoPoints(parseGeoPoints(null));

		mapView.invalidate();
	}

	/**
	 * Parse GeoPoints of earthquakes from cursor.
	 * 
	 * @param cursor
	 *            The cursor to parse.
	 * @return The parsed GeoPoints as list.
	 */
	private List<GeoPoint> parseGeoPoints(Cursor cursor) {
		LinkedList<GeoPoint> geoPoints = new LinkedList<GeoPoint>();

		if (cursor != null && cursor.moveToFirst()) {
			do {
				int LaIndex = cursor.getColumnIndexOrThrow(QuakeProvider.KEY_LATITUDE);
				int LoIndex = cursor.getColumnIndexOrThrow(QuakeProvider.KEY_LONGITUDE);

				GeoPoint geoPoint = new GeoPoint(cursor.getDouble(LaIndex), cursor.getDouble(LoIndex));

				geoPoints.add(geoPoint);
			} while (cursor.moveToNext());
		}

		return geoPoints;
	}

}
