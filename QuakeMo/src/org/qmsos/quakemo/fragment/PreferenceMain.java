package org.qmsos.quakemo.fragment;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * The fragment of preferences.
 * 
 *
 */
public class PreferenceMain extends PreferenceFragmentCompat {

	private OnSubPreferenceClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnSubPreferenceClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnSubPreferenceClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.preference_main);
	}
	
	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		mListener.onSubPreferenceClicked(preference);
		
		return super.onPreferenceTreeClick(preference);
	}

	/**
	 * Interface that as callback to switch fragments of sub preference. 
	 */
	public interface OnSubPreferenceClickedListener {
		/**
		 * Called when some preference of the inflated preference tree is clicked.
		 * 
		 * @param preference
		 *            The preference that clicked.
		 */
		void onSubPreferenceClicked(Preference preference);
	}
	
	/**
	 * The fragment of sub preference instance.
	 */
	public static class PreferenceSub extends PreferenceFragmentCompat {

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			addPreferencesFromResource(R.xml.preference_sub);
		}
		
	}

}
