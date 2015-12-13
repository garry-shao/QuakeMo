package org.qmsos.quakemo;

import java.util.List;

import org.qmsos.quakemo.fragment.PrefGraphicFragment;
import org.qmsos.quakemo.fragment.PrefRefreshFragment;
import org.qmsos.quakemo.util.UtilPreferenceActivity;

import android.support.v7.widget.Toolbar;

/**
 * 
 * The main activity managing preferences.
 *
 */
public class PrefActivity extends UtilPreferenceActivity {

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		
		setContentView(R.layout.activity_pref);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return PrefGraphicFragment.class.getName().equals(fragmentName) || 
				PrefRefreshFragment.class.getName().equals(fragmentName);
	}

}
