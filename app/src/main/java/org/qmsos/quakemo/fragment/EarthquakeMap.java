package org.qmsos.quakemo.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.map.CustomItemizedOverlay;
import org.qmsos.quakemo.map.CustomTileSourceFactory;
import org.qmsos.quakemo.widget.CustomMapView;

/**
 * Show earthquakes on map.
 */
public class EarthquakeMap extends BaseLoaderFragment {

	private CustomMapView mMapView;

	// The earthquake overlay on the map.
	private CustomItemizedOverlay mOverlay;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		CustomTileSourceFactory.initiateOfflineMapTiles(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_earthquake_map, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mMapView = (CustomMapView) view.findViewById(R.id.earthquake_map);
		mMapView.setMultiTouchControls(true);
		mMapView.setTilesScaledToDpi(true);
		mMapView.setUseDataConnection(false);
		mMapView.setTileSource(CustomTileSourceFactory.offlineTileSource());

		mOverlay = new CustomItemizedOverlay(getContext(), null);
		mMapView.getOverlays().add(mOverlay);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		swapData(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		swapData(null);
	}

	/**
	 * Swap the cursor that contains earthquake data.
	 * 
	 * @param data
	 *            The cursor that contains earthquake data.
	 */
	private void swapData(Cursor data) {
		if (mOverlay != null) {
			mOverlay.swapCursor(data);
		}
		// Did not find a way to redraw Overlay, have to invalidate to whole view.
		if (mMapView != null) {
			mMapView.invalidate();
		}
	}

}
