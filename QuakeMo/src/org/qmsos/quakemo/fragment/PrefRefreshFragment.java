package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.PrefActivity;
import org.qmsos.quakemo.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * The fragment show Update part of preferences.
 *
 */
public class PrefRefreshFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preference_refresh);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_pref, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		PrefActivity activity = (PrefActivity) getActivity();
		
		Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
		activity.setSupportActionBar(toolbar);
	}
	
}
