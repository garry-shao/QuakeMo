package org.qmsos.quakemo;

import java.util.ArrayList;
import java.util.List;

import org.qmsos.quakemo.fragment.DetailsDialogFragment;
import org.qmsos.quakemo.fragment.PurgeDialogFragment;
import org.qmsos.quakemo.fragment.PurgeDialogFragment.OnPurgeSelectedListener;
import org.qmsos.quakemo.fragment.QuakeListFragment;
import org.qmsos.quakemo.fragment.QuakeMapFragment;
import org.qmsos.quakemo.util.UtilCursorAdapter.ShowDialogCallback;
import org.qmsos.quakemo.util.UtilPagerAdapter;
import org.qmsos.quakemo.util.UtilResultReceiver;
import org.qmsos.quakemo.util.UtilResultReceiver.OnReceiveListener;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

/**
 * Main activity of this application.
 *
 *
 */
public class MainActivity extends AppCompatActivity 
implements OnSharedPreferenceChangeListener, OnMenuItemClickListener, 
	OnReceiveListener, OnPurgeSelectedListener, ShowDialogCallback {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final String STATE_KEY_RECEIVER = "STATE_KEY_RECEIVER";

	// flags used to show different layout of Snackbar.
	private static final int SNACKBAR_REFRESH = 1;
	private static final int SNACKBAR_PURGE = 2;
	private static final int SNACKBAR_NORMAL = 3;
	
	/**
	 * Callback from update service.
	 */
	private UtilResultReceiver receiver;

	/**
	 * Key to the query string passed in bundle.
	 */
	public static final String BUNDLE_KEY_QUERY = "BUNDLE_KEY_QUERY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.app_name);
		toolbar.inflateMenu(R.menu.menu_main_options);
		toolbar.setOnMenuItemClickListener(this);
		
		MenuItem searchItem = toolbar.getMenu().findItem(R.id.menu_search);
		initialSearch(searchItem);

		List<Fragment> fragmentList = new ArrayList<Fragment>();
		QuakeListFragment quakeList = new QuakeListFragment();
		QuakeMapFragment quakeMap = new QuakeMapFragment();
		fragmentList.add(quakeList);
		fragmentList.add(quakeMap);

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(new UtilPagerAdapter(getSupportFragmentManager(), fragmentList, this));

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		if (savedInstanceState != null) {
			receiver = savedInstanceState.getParcelable(STATE_KEY_RECEIVER);
		} else {
			receiver = new UtilResultReceiver(new Handler());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	
		receiver.setListener(this);
	}

	@Override
	protected void onPause() {
		receiver.setListener(null);

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(STATE_KEY_RECEIVER, receiver);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			Bundle args = new Bundle();
			args.putString(BUNDLE_KEY_QUERY, query);

			reload(args);
		}
	}
@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.menu_preferences):
			Intent intent = new Intent(this, PrefActivity.class);
			startActivity(intent);
	
			return true;
		case (R.id.menu_purge):
			PurgeDialogFragment dialog = new PurgeDialogFragment();
			dialog.show(getSupportFragmentManager(), "dialog");
	
			return true;
		case (R.id.menu_refresh):
			showSnackbar(SNACKBAR_REFRESH, null);
	
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.PREF_SHOW_MINIMUM)) 
				|| key.equals(getString(R.string.PREF_SHOW_RANGE))
				|| key.equals(getString(R.string.PREF_SHOW_ALL))) {

			reload(null);
		}

		if (key.equals(getString(R.string.PREF_WIDGET))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			int newState = prefs.getBoolean(key, false) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
					: PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

			PackageManager manager = getPackageManager();
			manager.setComponentEnabledSetting(new ComponentName(this, QuakeWidget.class), newState,
					PackageManager.DONT_KILL_APP);
		}
		
		if (key.equals(getString(R.string.PREF_AUTO_REFRESH)) 
				|| key.equals(getString(R.string.PREF_AUTO_FREQUENCY))) {
			
			Intent intent = new Intent(this, QuakeUpdateService.class);
			intent.setAction(QuakeUpdateService.ACTION_REFRESH_AUTO);
			
			startService(intent);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		String result;
		switch (resultCode) {
		case QuakeUpdateService.RESULT_CODE_REFRESHED:
			result = resultData.getInt(QuakeUpdateService.BUNDLE_KEY_COUNT) + " "
					+ getString(R.string.snackbar_refreshed);
			break;
		case QuakeUpdateService.RESULT_CODE_PURGED:
			result = getString(R.string.snackbar_purged);
			break;
		case QuakeUpdateService.RESULT_CODE_CANCELED:
			result = getString(R.string.snackbar_canceled);
			break;
		case QuakeUpdateService.RESULT_CODE_DISCONNECTED:
			result = getString(R.string.snackbar_disconnected);
			break;
		default:
			result = null;
		}

		showSnackbar(SNACKBAR_NORMAL, result);
	}

	@Override
	public void onPurgeSelected() {
		showSnackbar(SNACKBAR_PURGE, null);
	}

	@Override
	public void onShowDialog(long id) {
		DetailsDialogFragment dialog = DetailsDialogFragment.newInstance(this, id);
		dialog.show(getSupportFragmentManager(), "dialog");
	}

	/**
	 * Initialize SearchView, should be used only once.
	 * 
	 * @param menuItem
	 *            The MenuItem of SearchView.
	 */
	private void initialSearch(MenuItem menuItem) {
		if (menuItem == null) {
			return;
		}
		
		MenuItemCompat.setOnActionExpandListener(menuItem, new OnActionExpandListener() {
	
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (item.getItemId() == R.id.menu_search) {
					reload(null);
					
					return true;
				} else {
					return false;
				}
			}
	
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				if (item.getItemId() == R.id.menu_search) {
					return true;
				} else {
					return false;
				}
			}
		});
		
		if (menuItem.getActionView() instanceof SearchView) {
			final SearchView searchView = (SearchView) menuItem.getActionView();
			
			SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchableInfo searchableInfo = manager.getSearchableInfo(getComponentName());
			if (searchableInfo != null) {
				searchView.setSearchableInfo(searchableInfo);
			}
			
			searchView.setOnSuggestionListener(new OnSuggestionListener() {
				
				@Override
				public boolean onSuggestionClick(int position) {
					Cursor cursor = null;
					try {
						Object item = searchView.getSuggestionsAdapter().getItem(position);
						if (item != null) {
							cursor = (Cursor) item;
							
							String suggestion = cursor.getString(
									cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1));
							
							searchView.setQuery(suggestion, true);
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "Columns do not exist");
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}
					
					return true;
				}
				
				@Override
				public boolean onSuggestionSelect(int position) {
					return false;
				}
			});
			
			searchView.setOnQueryTextListener(new OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextChange(String newText) {
					return false;
				}
				
				@Override
				public boolean onQueryTextSubmit(String query) {
					searchView.clearFocus();
					
					return false;
				}
			});
		}
	}

	/**
	 * Implementation of showing snackbar, should use {@link #showSnackbar(int)} or 
	 * {@link #showSnackbar(String)}.
	 * 
	 * @param flag
	 *            layout of the snackbar, either {@link SNACKBAR_REFRESH},  
	 *            {@link SNACKBAR_PURGE} or {@link SNACKBAR_NORMAL}.
	 * @param text
	 *            Text shown on snackbar, used when flag is {@link SNACKBAR_NORMAL}.
	 */
	private void showSnackbar(int flag, String text) {
		View view = findViewById(R.id.coordinator_layout);
		if (view != null) {
			
			Snackbar snackbar = null;
			
			final Intent intent = new Intent(this, QuakeUpdateService.class);
			
			switch (flag) {
			case SNACKBAR_REFRESH:
				intent.setAction(QuakeUpdateService.ACTION_REFRESH_MANUAL);
				intent.putExtra(UtilResultReceiver.RECEIVER, receiver);
				
				snackbar = Snackbar.make(view, R.string.snackbar_refreshing, Snackbar.LENGTH_SHORT);
				snackbar.setCallback(new Callback() {
	
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						
						startService(intent);
					}
				});
				break;
			case SNACKBAR_PURGE:
				intent.setAction(QuakeUpdateService.ACTION_PURGE_DATABASE);
				intent.putExtra(UtilResultReceiver.RECEIVER, receiver);
				
				snackbar = Snackbar.make(view, R.string.snackbar_purging, Snackbar.LENGTH_LONG);
				snackbar.setAction(R.string.snackbar_undo, new View.OnClickListener() {
	
					@Override
					public void onClick(View v) {
						intent.putExtra(QuakeUpdateService.EXTRA_PURGE_BURNDOWN,
								QuakeUpdateService.EXTRA_PURGE_BURNDOWN_NO);
					}
				});
				snackbar.setCallback(new Callback() {
	
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						if (event != Callback.DISMISS_EVENT_ACTION) {
							intent.putExtra(QuakeUpdateService.EXTRA_PURGE_BURNDOWN,
									QuakeUpdateService.EXTRA_PURGE_BURNDOWN_YES);
						}
						startService(intent);
					}
				});
				break;
			case SNACKBAR_NORMAL:
				if (text != null) {
					snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
				}
				break;
			}
			
			if (snackbar != null) {
				snackbar.show();
			}
		}
	}

	/**
	 * Reload all the cursors for new data.
	 * 
	 * @param bundle
	 *            May used to pass extra arguments to create new cursor.
	 */
	private void reload(Bundle bundle) {
		FragmentManager manager = getSupportFragmentManager();

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		UtilPagerAdapter adapter = (UtilPagerAdapter) viewPager.getAdapter();

		String listTag = adapter.getTag(0);
		if (listTag != null && manager.findFragmentByTag(listTag) != null) {
			QuakeListFragment quakeList = (QuakeListFragment) manager.findFragmentByTag(listTag);
			if (quakeList.isAdded()) {
				quakeList.getLoaderManager().restartLoader(0, bundle, quakeList);
			}
		}
		String mapTag = adapter.getTag(1);
		if (mapTag != null && manager.findFragmentByTag(mapTag) != null) {
			QuakeMapFragment quakeMap = (QuakeMapFragment) manager.findFragmentByTag(mapTag);
			if (quakeMap.isAdded()) {
				quakeMap.getLoaderManager().restartLoader(0, bundle, quakeMap);
			}
		}
	}

}
