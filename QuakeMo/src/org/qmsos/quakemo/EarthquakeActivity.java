package org.qmsos.quakemo;

import org.osmdroid.views.MapView;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnSuggestionListener;

@SuppressWarnings("deprecation")
public class EarthquakeActivity extends Activity {
	private static final int SHOW_PREFERENCES = 1;
	private static final String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";
	
	public boolean autoUpdateChecked = false;
	public int minMagnitude = 0;
	public int updateFreq = 0;
	MapView mapView;
	
	TabListener<EarthquakeListFragment> listTabListener;
	TabListener<EarthquakeMapFragment> mapTabListener;
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private Fragment fragment;
		private Activity activity;
		private Class<T> fragmentClass;
		private int fragmentContainer;
		
		public TabListener(Activity activity, Class<T> fragmentClass, int fragmentContainer) {
			this.activity = activity;
			this.fragmentClass = fragmentClass;
			this.fragmentContainer = fragmentContainer;
		}
	
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (fragment == null) {
				String fragmentName = fragmentClass.getName();
				fragment = Fragment.instantiate(activity, fragmentName);
				
				ft.add(fragmentContainer, fragment, fragmentName);
			} else {
				ft.attach(fragment);
			}
		}
	
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (fragment != null) {
				ft.detach(fragment);
			}
		}
	
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			if (fragment != null) {
				ft.attach(fragment);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.earthquake_main);
		
		mapView = new MapView(getApplicationContext(), null);
		mapView.setBuiltInZoomControls(true);
		mapView.setTilesScaledToDpi(true);
		mapView.getController().setZoom(1);
		
		updateFromPreferences();
		
		ActionBar actionBar = getActionBar();
		
		View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
		if (fragmentContainer != null) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.setDisplayShowTitleEnabled(false);
			
			listTabListener = new TabListener<EarthquakeListFragment>(
					this, EarthquakeListFragment.class, R.id.EarthquakeFragmentContainer);
			Tab listTab = actionBar.newTab();
			listTab.setText("List")
					.setContentDescription("List of earthquakes")
					.setTabListener(listTabListener);
			actionBar.addTab(listTab);

			mapTabListener = new TabListener<EarthquakeMapFragment>(
					this, EarthquakeMapFragment.class, R.id.EarthquakeFragmentContainer);
			Tab mapTab = actionBar.newTab();
			mapTab.setText("Map")
			.setContentDescription("Map of earthquakes")
			.setTabListener(mapTabListener);
			actionBar.addTab(mapTab);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
		
		if (fragmentContainer != null) {
			SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
			int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
			getActionBar().setSelectedNavigationItem(actionBarIndex);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
		if (fragmentContainer != null) {
			listTabListener.fragment = getFragmentManager().
					findFragmentByTag(EarthquakeListFragment.class.getName());
			mapTabListener.fragment = getFragmentManager().
					findFragmentByTag(EarthquakeMapFragment.class.getName());
			
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			int actionBarIndex = prefs.getInt(ACTION_BAR_INDEX, 0);
			getActionBar().setSelectedNavigationItem(actionBarIndex);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
		
		if (fragmentContainer != null) {
			int actionBarIndex = getActionBar().getSelectedTab().getPosition();
			
			SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
			editor.putInt(ACTION_BAR_INDEX, actionBarIndex);
			editor.apply();
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (listTabListener.fragment != null) {
				ft.detach(listTabListener.fragment);
			}
			if (mapTabListener.fragment != null) {
				ft.detach(mapTabListener.fragment);
			}
			ft.commit();
		}
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_option_menu, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnSuggestionListener(new OnSuggestionListener() {

			@Override
			public boolean onSuggestionSelect(int position) {
				return false;
			}
			
			@Override
			public boolean onSuggestionClick(int position) {
				Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
				String suggestion = cursor.getString(
						cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
				
				searchView.setQuery(suggestion, true);
				
				return true;
			}
		});
	
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case (R.id.menu_preferences): {
			Intent i = new Intent(this, EarthquakePreferences.class);
			startActivityForResult(i, SHOW_PREFERENCES);
		
			return true;
		}
		case (R.id.menu_refresh): {
			Intent i = new Intent(this, EarthquakeUpdateService.class);
			i.putExtra(EarthquakeUpdateService.MANUAL_REFRESH, true);
			startService(i);
			
			return true;
		}
		case (R.id.menu_purge): {
			Intent i = new Intent(this, EarthquakeUpdateService.class);
			i.putExtra(EarthquakeUpdateService.PURGE_DATABASE, true);
			startService(i);
			
			return true;
		}
		default:
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == SHOW_PREFERENCES) {
			updateFromPreferences();
			
			startService(new Intent(this, EarthquakeUpdateService.class));
		}
	}

	/**
	 * Get new values from shared preferences.
	 */
	private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(context);

		autoUpdateChecked = 
				prefs.getBoolean(EarthquakePreferences.PREF_AUTO_UPDATE, false);
		minMagnitude = Integer.parseInt(
				prefs.getString(EarthquakePreferences.PREF_MIN_MAG, "3"));
		updateFreq = Integer.parseInt(
				prefs.getString(EarthquakePreferences.PREF_UPDATE_FREQ, "60"));
	}
}
