package org.qmsos.quakemo;

import java.util.List;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

/**
 * 
 * The main activity managing preferences.
 *
 */
public class PrefActivity extends UtilPreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
	public static final String PREF_USE_WIDGETS = "PREF_USE_WIDGETS";
	public static final String PREF_NOTIFICATION_ENABLE = "PREF_NOTIFICATION_ENABLE";
	public static final String PREF_NOTIFICATION_SOUND = "PREF_NOTIFICATION_SOUND";
	public static final String PREF_NOTIFICATION_VIBRATE = "PREF_NOTIFICATION_VIBRATE";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		
		setContentView(R.layout.activity_pref);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.menu_preferences);
		setSupportActionBar(toolbar);
		
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return PrefGraphicFragment.class.getName().equals(fragmentName) || 
				PrefUpdateFragment.class.getName().equals(fragmentName);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PrefActivity.PREF_USE_WIDGETS)) {
			SharedPreferences prefs = 
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			int newState = 
					prefs.getBoolean(key, false) ? 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
			
			PackageManager manager = getPackageManager();
			manager.setComponentEnabledSetting(
					new ComponentName(getApplicationContext(), QuakeWidgetSingleton.class), 
					newState, PackageManager.DONT_KILL_APP);
			manager.setComponentEnabledSetting(
					new ComponentName(getApplicationContext(), QuakeWidgetList.class), 
					newState, PackageManager.DONT_KILL_APP);
		}
	}

}
