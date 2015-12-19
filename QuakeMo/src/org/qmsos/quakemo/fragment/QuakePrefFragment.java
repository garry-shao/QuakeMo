package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * The fragment of preferences.
 * 
 *
 */
public class QuakePrefFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle arg0, String arg1) {
		addPreferencesFromResource(R.xml.preference);
	}

}
