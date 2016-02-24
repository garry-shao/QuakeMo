package org.qmsos.quakemo.fragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.R;
import org.qmsos.quakemo.map.CursorItemOverlay;
import org.qmsos.quakemo.map.TileFilesChecker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Show earthquakes on map.
 *
 *
 */
public class EarthquakeMap extends BaseLoaderFragment {

	private static final String KEY_CENTER = "KEY_CENTER";
	private static final String KEY_ZOOM_LEVEL = "KEY_ZOOM_LEVEL";

	// Confine zoom levels of MapView.
	private static final int ZOOM_LEVEL_MIN = 1;
	private static final int ZOOM_LEVEL_MAX = 4;
	
	/**
	 * The earthquake overlay on the map.
	 */
	private CursorItemOverlay mOverlay;

	/**
	 * Defined as there is only one view on this fragment.
	 */
	private MapView mMapView;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		TileFilesChecker.checkMapTileFiles(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_earthquake_map, container, false);
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mMapView = (MapView) view.findViewById(R.id.earthquake_map);
		mMapView.setMultiTouchControls(true);
		mMapView.setTileSource(TileFilesChecker.offlineTileSource(ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX));
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

		mOverlay = new CursorItemOverlay(getContext(), null);
		mMapView.getOverlays().add(mOverlay);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mMapView != null) {
			outState.putParcelable(KEY_CENTER, (GeoPoint) mMapView.getMapCenter());
			outState.putInt(KEY_ZOOM_LEVEL, mMapView.getZoomLevel());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mOverlay.swapCursor(data);
		mMapView.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mOverlay.swapCursor(null);
		mMapView.invalidate();
	}

}
