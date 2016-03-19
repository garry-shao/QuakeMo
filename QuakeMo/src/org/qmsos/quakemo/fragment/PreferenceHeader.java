package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The fragment that mocking preference headers.
 *
 */
public class PreferenceHeader extends PreferenceFragmentCompat {

	private OnPreferenceHeaderClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnPreferenceHeaderClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnPreferenceHeaderClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference_header);
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		mListener.onPreferenceHeaderClicked(preference);
		
		return super.onPreferenceTreeClick(preference);
	}

	/**
	 * Interface that as callback when the mock up of preference header is clicked. 
	 */
	public interface OnPreferenceHeaderClickedListener {
		
		/**
		 * Called when any mock up of preference header is clicked.
		 * 
		 * @param preferenceHeader
		 *            The preference header that clicked.
		 */
		void onPreferenceHeaderClicked(Preference preferenceHeader);
	
	}

	/**
	 * The fragment of preference instance about various information.
	 */
	public static class PreferenceAbout extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_preference_about, container, false);
			
			return view;
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
	
	/**
	 * The fragment of preference instance about component.
	 */
	public static class PreferenceComponent extends PreferenceFragmentCompat {

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.preference_component);
		}
		
	}

	/**
	 * The fragment of preference instance about display.
	 */
	public static class PreferenceDisplay extends PreferenceFragmentCompat {
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.preference_display);
		}
		
	}

	/**
	 * The fragment of preference instance about refreshing.
	 */
	public static class PreferenceRefresh extends PreferenceFragmentCompat {
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.preference_refresh);
		}
		
	}

}
