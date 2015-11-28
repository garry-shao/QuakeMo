package org.qmsos.quakemo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * 
 * The fragment show Update part of preferences.
 *
 */
public class PrefUpdateFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference_update);
	}

}
