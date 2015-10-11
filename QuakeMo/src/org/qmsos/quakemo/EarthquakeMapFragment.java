package org.qmsos.quakemo;

import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class EarthquakeMapFragment extends Fragment implements LoaderCallbacks<Cursor> {
	
	private EarthquakeMapOverlay mapOverlay;
	protected MapView mapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		mapView = new MapView(getActivity().getApplicationContext(), null);
		mapView.setBuiltInZoomControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.getController().setZoom(1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapOverlay = new EarthquakeMapOverlay(getActivity(), null);
		mapView.getOverlays().add(mapOverlay);
		
		return mapView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.map_options_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case (R.id.menu_download_view_area): {
			CacheManager cacheManager = new CacheManager(mapView);
			int zoomMin = mapView.getZoomLevel();
			int zoomMax = mapView.getZoomLevel() + 4;
			cacheManager.downloadAreaAsync(getActivity(), mapView.getBoundingBox(), zoomMin, zoomMax);
			
			return true;
		}
		case (R.id.menu_clear_view_area): {
			CacheManager cacheManager = new CacheManager(mapView);
			int zoomMin = mapView.getZoomLevel();
			int zoomMax = mapView.getZoomLevel() + 7;
			cacheManager.cleanAreaAsync(getActivity(), mapView.getBoundingBox(), zoomMin, zoomMax);
			
			return true;
		}
		case (R.id.menu_cache_usage): {
			CacheManager cacheManager = new CacheManager(mapView);
			long cacheUsage = cacheManager.currentCacheUsage() / (1024 * 1024);
			long cacheCapacity = cacheManager.cacheCapacity() / (1024 * 1024);
			float percent = 100.0f * cacheUsage / cacheCapacity;
			String message = "Cache usage:\n" + 
					cacheUsage + " MB / " + cacheCapacity + " MB = " + (int) percent + "%";
			Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
			
			return true;
		}
		default:
			return false;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] {
				EarthquakeProvider.KEY_ID, 
				EarthquakeProvider.KEY_LOCATION_LA, 
				EarthquakeProvider.KEY_LOCATION_LO };
		
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		int minMagnitude = Integer.parseInt(
				prefs.getString(MainPreferenceActivity.PREF_MIN_MAG, "3"));
		String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
				
		CursorLoader loader = new CursorLoader(getActivity(), 
				EarthquakeProvider.CONTENT_URI, projection, where, null, null);
				
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mapOverlay.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mapOverlay.swapCursor(null);
	}

}
