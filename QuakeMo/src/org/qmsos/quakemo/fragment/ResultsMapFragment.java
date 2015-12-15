package org.qmsos.quakemo.fragment;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.qmsos.quakemo.data.Earthquake;
import org.qmsos.quakemo.util.UtilQuakeOverlay;

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

	private UtilQuakeOverlay quakeOverlay;
	private MapView mapView;

	private Earthquake earthquake;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		if (savedInstanceState != null) {
			earthquake = savedInstanceState.getParcelable(KEY_EARTHQUAKE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity());
		mapView.setMultiTouchControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(1);

		quakeOverlay = new UtilQuakeOverlay(getContext());

		if (earthquake != null) {
			GeoPoint geoPoint = new GeoPoint(earthquake.getLatitude(), earthquake.getLongitude());
			List<GeoPoint> geoPoints = new LinkedList<GeoPoint>();
			geoPoints.add(geoPoint);
			quakeOverlay.setGeoPoints(geoPoints);

			mapView.getController().setCenter(geoPoint);
		}
		
		mapView.getOverlays().add(quakeOverlay);
		
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
		outState.putParcelable(KEY_EARTHQUAKE, earthquake);
		outState.putInt(KEY_ZOOMLEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	public Earthquake getEarthquake() {
		return earthquake;
	}

	public void setEarthquake(Earthquake earthquake) {
		this.earthquake = earthquake;
	}

}
