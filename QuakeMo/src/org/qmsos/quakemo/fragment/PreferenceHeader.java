package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

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
	public static class PreferenceAbout extends PreferenceFragmentCompat {
		
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.preference_about);
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
