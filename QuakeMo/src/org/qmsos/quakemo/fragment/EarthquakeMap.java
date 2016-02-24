package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;
import org.qmsos.quakemo.map.CursorItemOverlay;
import org.qmsos.quakemo.map.TileFilesChecker;
import org.qmsos.quakemo.widget.CustomMapView;

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

	/**
	 * The earthquake overlay on the map.
	 */
	private CursorItemOverlay mOverlay;

	private CustomMapView mMapView;

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
		
		mMapView = (CustomMapView) view.findViewById(R.id.earthquake_map);
		mMapView.setMultiTouchControls(true);
		mMapView.setTileSource(TileFilesChecker.offlineTileSource());
		mMapView.setUseDataConnection(false);
		mMapView.setTilesScaledToDpi(true);

		mOverlay = new CursorItemOverlay(getContext(), null);
		mMapView.getOverlays().add(mOverlay);
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
