package org.qmsos.quakemo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * 
 * The fragment show the UI part of preferences.
 *
 */
public class PrefUIFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference_ui);
	}

}
