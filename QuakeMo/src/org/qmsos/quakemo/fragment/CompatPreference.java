package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * The fragment of preferences.
 * 
 *
 */
public class CompatPreference extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference);
	}

}
