package org.qmsos.quakemo;

import java.util.ArrayList;
import java.util.List;

import org.qmsos.quakemo.fragment.DetailsDialogFragment;
import org.qmsos.quakemo.fragment.PurgeDialogFragment;
import org.qmsos.quakemo.fragment.PurgeDialogFragment.ShowSnackbarListener;
import org.qmsos.quakemo.fragment.QuakeListFragment;
import org.qmsos.quakemo.fragment.QuakeMapFragment;
import org.qmsos.quakemo.util.UtilCursorAdapter.ShowDialogListener;
import org.qmsos.quakemo.util.UtilPagerAdapter;
import org.qmsos.quakemo.util.UtilResultReceiver;
import org.qmsos.quakemo.util.UtilResultReceiver.Receiver;

import android.app.SearchManager;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Main activity of this application.
 *
 *
 */
public class MainActivity extends AppCompatActivity 
implements OnSharedPreferenceChangeListener, Receiver, ShowSnackbarListener, ShowDialogListener {

	private static final String KEY_RECEIVER = "KEY_RECEIVER";
	
	/**
	 * Callback from update service.
	 */
	private UtilResultReceiver receiver;
	
	private QuakeListFragment quakeList;
	private QuakeMapFragment quakeMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			receiver = savedInstanceState.getParcelable(KEY_RECEIVER);
		} else {
			receiver = new UtilResultReceiver(new Handler());
		}
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		quakeList = new QuakeListFragment();
		quakeMap = new QuakeMapFragment();
		
		List<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(quakeList);
		fragmentList.add(quakeMap);
		
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(new UtilPagerAdapter(getSupportFragmentManager(), fragmentList, this));
		
		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);
		
		// start service for the first time.
		Intent startIntent = new Intent(this, QuakeUpdateService.class);
		startIntent.setAction(QuakeUpdateService.ACTION_REFRESH_AUTO);
		startService(startIntent);
	}

	@Override
	protected void onPause() {
		receiver.setReceiver(null);

		super.onPause();
	}

	@Override
	protected void onResume() {
		receiver.setReceiver(this);

		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_RECEIVER, receiver);

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	
		getMenuInflater().inflate(R.menu.menu_main_options, menu);
		
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
				
				cursor.close();
				
				return true;
			}
		});
	
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case (R.id.menu_preferences): 
			Intent i = new Intent(this, PrefActivity.class);
			startActivity(i);
		
			return true;
		case (R.id.menu_purge):
			PurgeDialogFragment dialog = new PurgeDialogFragment();
			dialog.show(getSupportFragmentManager(), "dialog");
		
			return true;
		case (R.id.menu_refresh):
			View coordinatorLayout = findViewById(R.id.coordinator_layout);
			if (coordinatorLayout != null) {
				Snackbar.make(coordinatorLayout, 
						getString(R.string.snackbar_refreshing), Snackbar.LENGTH_INDEFINITE).show();
			}
			
			i = new Intent(this, QuakeUpdateService.class);
			i.setAction(QuakeUpdateService.ACTION_REFRESH_MANUAL);
			i.putExtra(UtilResultReceiver.RECEIVER, receiver);
			
			startService(i);

			return true;
		default:
			return false;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.PREF_SHOW_MINIMUM))
				|| key.equals(getString(R.string.PREF_SHOW_RANGE))) {
			//getLoaderManager() will throw if this fragment not attached to activity.
			if (quakeList.isAdded()) {
				quakeList.getLoaderManager().restartLoader(0, null, quakeList);
			}
			if (quakeMap.isAdded()) {
				quakeMap.getLoaderManager().restartLoader(0, null, quakeMap);
			}
		}
		
		if (key.equals(getString(R.string.PREF_WIDGET))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			int newState = prefs.getBoolean(key, false) ? 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
			
			PackageManager manager = getPackageManager();
			manager.setComponentEnabledSetting(new ComponentName(this, QuakeWidget.class), 
					newState, PackageManager.DONT_KILL_APP);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		String result;
		switch (resultCode) {
		case QuakeUpdateService.RESULT_CODE_REFRESHED:
			result = resultData.getInt(QuakeUpdateService.BUNDLE_KEY_COUNT) + 
					" " + getString(R.string.snackbar_refreshed);
			break;
		case QuakeUpdateService.RESULT_CODE_PURGED:
			result = getString(R.string.snackbar_purged);
			break;
		case QuakeUpdateService.RESULT_CODE_CANCELED:
			result = getString(R.string.snackbar_canceled);
			break;
		default:
			result = null;
		}
		
		View coordinatorLayout = findViewById(R.id.coordinator_layout);
		if (coordinatorLayout != null && result != null) {
			Snackbar.make(coordinatorLayout, result, Snackbar.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onShowSnackbar() {
		final Intent intent = new Intent(this, QuakeUpdateService.class);
		intent.setAction(QuakeUpdateService.ACTION_PURGE_DATABASE);
		intent.putExtra(UtilResultReceiver.RECEIVER, receiver);

		View coordinatorLayout = findViewById(R.id.coordinator_layout);
		if (coordinatorLayout != null) {
			Snackbar snackbar = Snackbar.make(
					coordinatorLayout, R.string.snackbar_purging, Snackbar.LENGTH_LONG);
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
			snackbar.show();
		}
	}

	@Override
	public void onShowDialog(long id) {
		DetailsDialogFragment dialog = DetailsDialogFragment.newInstance(this, id);
		dialog.show(getSupportFragmentManager(), "dialog");
	}	

}
