package org.qmsos.quakemo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.qmsos.quakemo.R;

/**
 * The fragment of preference instance about various information.
 */
public class PreferenceAbout extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_preference_about, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String htmlInfo = getString(R.string.about_info);
		String htmlRegards = getString(R.string.about_regards);
		
		TextView info = (TextView) getView().findViewById(R.id.preference_about_info);
		info.setText(Html.fromHtml(htmlInfo));
		TextView regards = (TextView) getView().findViewById(R.id.preference_about_regards);
		regards.setText(Html.fromHtml(htmlRegards));
	}

}