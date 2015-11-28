package org.qmsos.quakemo;

import java.util.List;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * 
 * The main activity managing preferences.
 *
 */
public class PrefActivity extends PreferenceActivity {
	
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
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		toggleWidgets();
	}

	/**
	 * Toggle if widgets available by user preferences.
	 */
	private void toggleWidgets() {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		boolean useWidgetChecked = 
				prefs.getBoolean(PrefActivity.PREF_USE_WIDGETS, false);
		
		getPackageManager().setComponentEnabledSetting(
				new ComponentName(this, QuakeWidgetSingle.class), 
				(useWidgetChecked ? 
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED : 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED), 
				PackageManager.DONT_KILL_APP);
		
		getPackageManager().setComponentEnabledSetting(
				new ComponentName(this, QuakeWidgetList.class), 
				(useWidgetChecked ? 
						PackageManager.COMPONENT_ENABLED_STATE_ENABLED : 
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED), 
				PackageManager.DONT_KILL_APP);
	}

}
