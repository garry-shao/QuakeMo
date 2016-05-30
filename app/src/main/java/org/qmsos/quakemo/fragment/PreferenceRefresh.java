package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * The fragment of preference instance about refreshing.
 */
public class PreferenceRefresh extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference_refresh);
	}

}