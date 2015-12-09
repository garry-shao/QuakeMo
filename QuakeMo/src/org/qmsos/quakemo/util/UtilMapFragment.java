package org.qmsos.quakemo.util;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.location.Location;
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
public class UtilMapFragment extends Fragment {

	private static final String KEY_GEOPOINT = "KEY_GEOPOINT";
	private static final String KEY_CENTER = "KEY_CENTER";
	private static final String KEY_ZOOMLEVEL = "KEY_ZOOMLEVEL";

	private UtilQuakeOverlay quakeOverlay;
	private MapView mapView;

	/**
	 * The ONLY GeoPoint to display.
	 */
	private GeoPoint geoPoint;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity());
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
			mapView.getController().setCenter(geoPoint);
			mapView.getController().setZoom(3);
		}

		quakeOverlay = new UtilQuakeOverlay(getContext());
		if (geoPoint != null) {
			List<GeoPoint> geoPoints = new LinkedList<GeoPoint>();
			geoPoints.add(geoPoint);
			quakeOverlay.setGeoPoints(geoPoints);
		}
		mapView.getOverlays().add(quakeOverlay);

		return mapView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			geoPoint = savedInstanceState.getParcelable(KEY_GEOPOINT);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_CENTER, (GeoPoint) mapView.getMapCenter());
		outState.putParcelable(KEY_GEOPOINT, geoPoint);
		outState.putInt(KEY_ZOOMLEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	// =====================================================
	// middle layer, transform here
	// =====================================================
	
	public Location getLocation() {
		return geoPointToLocation(geoPoint);
	}

	public void setLocation(Location location) {
		this.geoPoint = locationToGeoPoint(location);
	}

	/**
	 * Convert Location to GeoPoint.
	 * 
	 * @param location
	 *            The Location instance to convert.
	 * @return The converted GeoPoint instance or NULL when the Location isn't
	 *         valid.
	 */
	private GeoPoint locationToGeoPoint(Location location) {
		if (location != null) {
			return new GeoPoint(location.getLatitude(), location.getLongitude());
		} else {
			return null;
		}
	}

	/**
	 * Convert GeoPoint to Location.
	 * 
	 * @param geoPoint
	 *            The GeoPoint instance to convert.
	 * @return The converted Location instance or NULL when the GeoPoint isn't
	 *         valid.
	 */
	private Location geoPointToLocation(GeoPoint geoPoint) {
		if (geoPoint != null) {
			Location location = new Location("GPS");
			location.setLatitude(geoPoint.getLatitude());
			location.setLongitude(geoPoint.getLongitude());
			return location;
		} else {
			return null;
		}
	}
}
