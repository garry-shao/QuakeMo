package org.qmsos.quakemo;

import java.util.List;

import org.qmsos.quakemo.fragment.PrefGraphicFragment;
import org.qmsos.quakemo.fragment.PrefRefreshFragment;
import org.qmsos.quakemo.util.UtilPreferenceActivity;

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
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		
		setContentView(R.layout.activity_pref);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return PrefGraphicFragment.class.getName().equals(fragmentName) || 
				PrefRefreshFragment.class.getName().equals(fragmentName);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.PREF_WIDGET))) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

			int newState = prefs.getBoolean(key, false) ? 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
			
			PackageManager manager = getPackageManager();
			manager.setComponentEnabledSetting(
					new ComponentName(this, QuakeWidgetSingleton.class), 
					newState, PackageManager.DONT_KILL_APP);
			manager.setComponentEnabledSetting(
					new ComponentName(this, QuakeWidgetList.class), 
					newState, PackageManager.DONT_KILL_APP);
		}
	}

}
