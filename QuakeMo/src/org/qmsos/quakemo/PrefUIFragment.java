package org.qmsos.quakemo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefUIFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.pref_body_ui);
	}

}
