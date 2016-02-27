package org.qmsos.quakemo;

import org.qmsos.quakemo.fragment.PreferenceMain;
import org.qmsos.quakemo.fragment.PreferenceMain.OnSubPreferenceClickedListener;
import org.qmsos.quakemo.fragment.PreferenceMain.PreferenceSub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;

/**
 * The main activity managing preferences.
 *
 *
 */
public class PreferenceActivity extends AppCompatActivity implements OnSubPreferenceClickedListener {

	private static final String FRAGMENT_TAG_PREFERENCE_MAIN = "FRAGMENT_TAG_PREFERENCE_MAIN";
	private static final String FRAGMENT_TAG_PREFERENCE_SUB = "FRAGMENT_TAG_PREFERENCE_SUB";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		FragmentManager manager = getSupportFragmentManager();
		
		PreferenceMain preferenceMain = null;
		Fragment fragmentPreferenceMain = manager.findFragmentByTag(FRAGMENT_TAG_PREFERENCE_MAIN);
		if (fragmentPreferenceMain != null) {
			preferenceMain = (PreferenceMain) fragmentPreferenceMain;
		} else {
			preferenceMain = new PreferenceMain();
			
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.fragment_container, preferenceMain, FRAGMENT_TAG_PREFERENCE_MAIN);
			transaction.commit();
		}
		
		PreferenceSub preferenceSub = null;
		Fragment fragmentPreferenceSub = manager.findFragmentByTag(FRAGMENT_TAG_PREFERENCE_SUB);
		if (fragmentPreferenceSub != null) {
			preferenceSub = (PreferenceSub) fragmentPreferenceSub;
		}
		
		if (preferenceMain != null && preferenceSub != null) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.hide(preferenceMain);
			transaction.commit();
		}
	}

	@Override
	public void onSubPreferenceClicked(Preference preference) {
		if (preference.getKey().equals(getString(R.string.PREF_REFRESH_PARAMETERS))) {
			FragmentManager manager = getSupportFragmentManager();
			
			Fragment fragmentPreferenceMain = manager.findFragmentByTag(FRAGMENT_TAG_PREFERENCE_MAIN);
			Fragment fragmentPreferenceSub = manager.findFragmentByTag(FRAGMENT_TAG_PREFERENCE_SUB);
			if (fragmentPreferenceMain != null && fragmentPreferenceSub == null) {
				PreferenceMain preferenceMain = (PreferenceMain) fragmentPreferenceMain;
				PreferenceSub preferenceSub = new PreferenceSub();
				
				FragmentTransaction transaction = manager.beginTransaction();
				transaction.hide(preferenceMain);
				transaction.add(R.id.fragment_container, preferenceSub, FRAGMENT_TAG_PREFERENCE_SUB);
				transaction.addToBackStack(null);
				transaction.commit();
			}
		}
	}

}
