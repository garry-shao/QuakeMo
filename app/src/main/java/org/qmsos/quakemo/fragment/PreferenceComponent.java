package org.qmsos.quakemo.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.qmsos.quakemo.R;

/**
 * The fragment of preference instance about component.
 */
public class PreferenceComponent extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference_component);
	}

}