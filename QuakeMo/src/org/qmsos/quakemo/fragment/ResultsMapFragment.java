package org.qmsos.quakemo.fragment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.QuakeProvider;
import org.qmsos.quakemo.util.UtilSimpleOverlay;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A utility map fragment shows only ONE earthquake.
 * 
 *
 */
public class ResultsMapFragment extends Fragment {

	private static final String KEY_CENTER = "KEY_CENTER";
	private static final String KEY_EARTHQUAKE = "KEY_EARTHQUAKE";
	private static final String KEY_ZOOMLEVEL = "KEY_ZOOMLEVEL";

	private UtilSimpleOverlay quakeOverlay;
	private MapView mapView;

	/**
	 * Create a new instance of map fragment that shows particular earthquake.
	 * 
	 * @param context
	 *            The context that this fragment within.
	 * @param id
	 *            The id of this earthquake.
	 * @return The created fragment instance.
	 */
	public static ResultsMapFragment newInstance(Context context, long id) {
		Bundle args = new Bundle();
		args.putLong(KEY_EARTHQUAKE, id);

		ResultsMapFragment fragment = new ResultsMapFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity());
		mapView.setMultiTouchControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(1);


		long id = getArguments().getLong(KEY_EARTHQUAKE);
		Cursor cursor = getContext().getContentResolver()
				.query(ContentUris.withAppendedId(QuakeProvider.CONTENT_URI, id), null, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				double latitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LATITUDE));
				double longitude = cursor.getDouble(cursor.getColumnIndex(QuakeProvider.KEY_LONGITUDE));
				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				
				quakeOverlay = new UtilSimpleOverlay(getContext(), geoPoint);
				mapView.getOverlays().add(quakeOverlay);
				mapView.getController().setCenter(geoPoint);
			}
		} finally {
			cursor.close();
		}

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
			mapView.getController().setZoom(3);
		}

		return mapView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_CENTER, (GeoPoint) mapView.getMapCenter());
		outState.putInt(KEY_ZOOMLEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

}
