package org.qmsos.quakemo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.qmsos.quakemo.R;

/**
 * The fragment that mocking preference headers.
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

            throw new ClassCastException(context.toString()
                    + " must implements "
                    + listenerName);
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
}