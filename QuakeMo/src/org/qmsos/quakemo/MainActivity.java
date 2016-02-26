package org.qmsos.quakemo;

import org.qmsos.quakemo.fragment.BaseLoaderFragment;
import org.qmsos.quakemo.fragment.CompatPurgeDialog;
import org.qmsos.quakemo.fragment.CompatPurgeDialog.OnPurgeSelectedListener;
import org.qmsos.quakemo.fragment.EarthquakeDetails;
import org.qmsos.quakemo.fragment.EarthquakeDetails.OnLinkSelectedListener;
import org.qmsos.quakemo.fragment.EarthquakeList;
import org.qmsos.quakemo.fragment.EarthquakeMap;
import org.qmsos.quakemo.fragment.EarthquakePagerAdapter;
import org.qmsos.quakemo.util.IntentConstants;
import org.qmsos.quakemo.widget.CursorRecyclerViewAdapter.OnViewHolderClickedListener;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.MenuItem;
import android.view.View;

/**
 * Main activity of this application.
 *
 *
 */
public class MainActivity extends AppCompatActivity 
implements OnSharedPreferenceChangeListener, OnMenuItemClickListener, 
	OnPurgeSelectedListener, OnLinkSelectedListener, OnViewHolderClickedListener {

	// flags used to show different layout of Snackbar.
	private static final int SNACKBAR_REFRESH = 1;
	private static final int SNACKBAR_PURGE = 2;
	private static final int SNACKBAR_NORMAL = 3;
	
	private MessageReceiver mMessageReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.app_name);
		toolbar.inflateMenu(R.menu.menu_main_options);
		toolbar.setOnMenuItemClickListener(this);

		EarthquakePagerAdapter adapter = new EarthquakePagerAdapter(getSupportFragmentManager(), this);
		adapter.addPage(new EarthquakeList());
		adapter.addPage(new EarthquakeMap());
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(adapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		mMessageReceiver = new MessageReceiver();

		Intent intent = new Intent(this, EarthquakeService.class);
		intent.setAction(IntentConstants.ACTION_REFRESH_AUTO);
		startService(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(IntentConstants.ACTION_PURGE_EXECUTED);
		filter.addAction(IntentConstants.ACTION_REFRESH_EXECUTED);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);

		super.onDestroy();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.menu_preferences):
			Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
	
			return true;
		case (R.id.menu_purge):
			CompatPurgeDialog dialog = new CompatPurgeDialog();
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
		if (key.equals(getString(R.string.PREF_DISPLAY_RANGE)) || 
				key.equals(getString(R.string.PREF_DISPLAY_ALL))) {

			reload(null);
		}

		if (key.equals(getString(R.string.PREF_APP_WIDGET_TOGGLE))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			int newState = prefs.getBoolean(key, false) ? 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED : 
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

			ComponentName componentName = new ComponentName(this, EarthquakeAppWidget.class);
			
			getPackageManager().setComponentEnabledSetting(
					componentName, newState, PackageManager.DONT_KILL_APP);
		}
		
		if (key.equals(getString(R.string.PREF_REFRESH_AUTO_TOGGLE)) || 
				key.equals(getString(R.string.PREF_REFRESH_AUTO_FREQUENCY))) {
			
			Intent intent = new Intent(this, EarthquakeService.class);
			intent.setAction(IntentConstants.ACTION_REFRESH_AUTO);
			
			startService(intent);
		}
	}

	@Override
	public void onPurgeSelected() {
		showSnackbar(SNACKBAR_PURGE, null);
	}

	@Override
	public void onLinkSelected(String link) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(link));
		
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		}		
	}

	@Override
	public void onLinkInvalid() {
		showSnackbar(SNACKBAR_NORMAL, getString(R.string.snackbar_invalid));
	}

	@Override
	public void onViewHolderClicked(long earthquakeId) {
		EarthquakeDetails dialog = EarthquakeDetails.newInstance(this, earthquakeId);
		dialog.show(getSupportFragmentManager(), "dialog");
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
		if (view == null) {
			return;
		}
		
		final Intent intent = new Intent(this, EarthquakeService.class);
		
		Snackbar snackbar = null;
		switch (flag) {
		case SNACKBAR_REFRESH:
			intent.setAction(IntentConstants.ACTION_REFRESH_MANUAL);
			
			snackbar = Snackbar.make(view, R.string.snackbar_refreshing, Snackbar.LENGTH_SHORT);
			snackbar.setCallback(new Callback() {

				@Override
				public void onDismissed(Snackbar snackbar, int event) {
					startService(intent);
				}
			});
			break;
		case SNACKBAR_PURGE:
			intent.setAction(IntentConstants.ACTION_PURGE_DATABASE);
			
			snackbar = Snackbar.make(view, R.string.snackbar_purging, Snackbar.LENGTH_LONG);
			snackbar.setAction(R.string.snackbar_undo, new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					intent.putExtra(IntentConstants.EXTRA_PURGE_DATABASE,	false);
				}
			});
			snackbar.setCallback(new Callback() {

				@Override
				public void onDismissed(Snackbar snackbar, int event) {
					if (event != Callback.DISMISS_EVENT_ACTION) {
						intent.putExtra(IntentConstants.EXTRA_PURGE_DATABASE, true);
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
			int snackbarActionTextColor = 
					ContextCompat.getColor(this, R.color.snackbar_action_text_color);
			int snakbarBackgroundColor = 
					ContextCompat.getColor(this, R.color.snackbar_background_color);
			
			snackbar.setActionTextColor(snackbarActionTextColor);
			snackbar.getView().setBackgroundColor(snakbarBackgroundColor);
			snackbar.show();
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
		EarthquakePagerAdapter adapter = (EarthquakePagerAdapter) viewPager.getAdapter();

		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			String tag = adapter.getTag(i);
			if (tag == null) {
				return;
			}
			
			Fragment rawFragment = manager.findFragmentByTag(tag);
			if ((rawFragment != null) && rawFragment.isAdded() && 
					(rawFragment instanceof BaseLoaderFragment)) {
				
				BaseLoaderFragment castedFragment = (BaseLoaderFragment) rawFragment;
				
				castedFragment.getLoaderManager().restartLoader(0, bundle, castedFragment);
			}
		}
	}

	/**
	 * Private Receiver used when receiving local broadcast from Service thread.
	 */
	private class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) {
				return;
			}
			
			String result = null;
			if (action.equals(IntentConstants.ACTION_REFRESH_EXECUTED)) {
				boolean flag = intent.getBooleanExtra(IntentConstants.EXTRA_REFRESH_EXECUTED, false);
				if (flag) {
					result = intent.getIntExtra(IntentConstants.EXTRA_ADDED_COUNT, 0) + 
							" "	+ getString(R.string.snackbar_refreshed);
				} else {
					result = getString(R.string.snackbar_disconnected);
				}
			} else if (action.equals(IntentConstants.ACTION_PURGE_EXECUTED)) {
				boolean flag = intent.getBooleanExtra(IntentConstants.EXTRA_PURGE_EXECUTED, false);
				if (flag) {
					result = getString(R.string.snackbar_purged);
				} else {
					result = getString(R.string.snackbar_canceled);
				}
			}
			
			showSnackbar(SNACKBAR_NORMAL, result);
		}

	}

}
