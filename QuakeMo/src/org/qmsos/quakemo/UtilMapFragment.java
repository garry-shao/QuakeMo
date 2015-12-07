package org.qmsos.quakemo;

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
 * 
 * A utility map fragment shows only ONE earthquake.
 *
 */
public class UtilMapFragment extends Fragment {

	private static final String KEY_LOCATION = "KEY_LOCATION";
	private static final String KEY_ZOOMLEVEL = "KEY_ZOOMLEVEL";

	private QuakeMapOverlay mapOverlay;
	private MapView mapView;

	/**
	 * The ONLY location to display.
	 */
	private Location location;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapView = new MapView(getActivity());

		mapView.setMultiTouchControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.setMinZoomLevel(1);

		mapOverlay = new QuakeMapOverlay(getContext());
		List<GeoPoint> locations = new LinkedList<GeoPoint>();
		if (location != null) {
			locations.add(locationToGeoPoint(location));
		}
		mapOverlay.setLocations(locations);

		mapView.getOverlays().add(mapOverlay);
		
		if (savedInstanceState != null) {
			mapView.getController().setZoom(savedInstanceState.getInt(KEY_ZOOMLEVEL));
		} else {
			mapView.getController().setZoom(3);
		}

		List<GeoPoint> geoLocations = mapOverlay.getLocations();
		if (!geoLocations.isEmpty()) {
			mapView.getController().setCenter(geoLocations.get(0));
		}

		return mapView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			location = savedInstanceState.getParcelable(KEY_LOCATION);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_LOCATION, location);
		outState.putInt(KEY_ZOOMLEVEL, mapView.getZoomLevel());

		super.onSaveInstanceState(outState);
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Convert Location to GeoPoint.
	 * 
	 * @param location
	 *            The Location instance to convert.
	 * @return GeoPoint The converted GeoPoint instance or NULL when the
	 *         location isn't valid.
	 */
	private GeoPoint locationToGeoPoint(Location location) {
		if (location != null) {
			return new GeoPoint(location.getLatitude(), location.getLongitude());
		} else {
			return null;
		}
	}

}
