package org.qmsos.quakemo;

import org.qmsos.quakemo.fragment.PreferenceHeader;
import org.qmsos.quakemo.fragment.PreferenceHeader.OnPreferenceHeaderClickedListener;
import org.qmsos.quakemo.fragment.PreferenceHeader.PreferenceAbout;
import org.qmsos.quakemo.fragment.PreferenceHeader.PreferenceComponent;
import org.qmsos.quakemo.fragment.PreferenceHeader.PreferenceDisplay;
import org.qmsos.quakemo.fragment.PreferenceHeader.PreferenceRefresh;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;

/**
 * The main activity managing preferences.
 *
 *
 */
public class PreferenceActivity extends AppCompatActivity
implements OnBackStackChangedListener, OnPreferenceHeaderClickedListener {

	private static final String FRAGMENT_TAG_HEADER = "FRAGMENT_TAG_HEADER";

	private static final String FRAGMENT_TAG_ABOUT = "FRAGMENT_TAG_ABOUT";
	private static final String FRAGMENT_TAG_COMPONENT = "FRAGMENT_TAG_COMPONENT";
	private static final String FRAGMENT_TAG_DISPLAY = "FRAGMENT_TAG_DISPLAY";
	private static final String FRAGMENT_TAG_REFRESH = "FRAGMENT_TAG_REFRESH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);
		
		FragmentManager manager = getSupportFragmentManager();
		manager.addOnBackStackChangedListener(this);
		
		Fragment fragmentPreferenceHeader = manager.findFragmentByTag(FRAGMENT_TAG_HEADER);
		if (fragmentPreferenceHeader == null) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.fragment_container, new PreferenceHeader(), FRAGMENT_TAG_HEADER);
			transaction.commit();
			
			// Toolbar's title will be changed based on currently loaded Fragment.
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			toolbar.setTitle(R.string.activity_name_preference);
		}
	}

	@Override
	protected void onDestroy() {
		FragmentManager manager = getSupportFragmentManager();
		manager.removeOnBackStackChangedListener(this);
		
		super.onDestroy();
	}

	@Override
	public void onBackStackChanged() {
		String toolbarTitle;
		
		FragmentManager manager = getSupportFragmentManager();
		int backStackCount = manager.getBackStackEntryCount();
		switch (backStackCount) {
		case 0:
			toolbarTitle = getString(R.string.activity_name_preference);
			
			break;
		case 1:
			BackStackEntry entry = manager.getBackStackEntryAt(0);
			
			toolbarTitle = entry.getBreadCrumbTitle().toString();
			
			break;
		default:
			toolbarTitle = null;
		}
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(toolbarTitle);
	}

	@Override
	public void onPreferenceHeaderClicked(Preference preferenceHeader) {
		String key = preferenceHeader.getKey();
		if (key.equals(getString(R.string.PREF_HEADER_ABOUT))) {
			PreferenceAbout preferenceAbout = new PreferenceAbout();
			
			loadFragment(preferenceAbout, FRAGMENT_TAG_ABOUT);
		} else if (key.equals(getString(R.string.PREF_HEADER_COMPONENT))) {
			PreferenceComponent preferenceComponent = new PreferenceComponent();
			
			loadFragment(preferenceComponent, FRAGMENT_TAG_COMPONENT);
		} else if (key.equals(getString(R.string.PREF_HEADER_DISPLAY))) {
			PreferenceDisplay preferenceDisplay = new PreferenceDisplay();
			
			loadFragment(preferenceDisplay, FRAGMENT_TAG_DISPLAY);
		} else if (key.equals(getString(R.string.PREF_HEADER_REFRESH))){
			PreferenceRefresh preferenceRefresh = new PreferenceRefresh();
			
			loadFragment(preferenceRefresh, FRAGMENT_TAG_REFRESH);
		}
	}

	/**
	 * Load fragment to backstack.
	 * 
	 * @param fragment
	 *            The fragment to be loaded.
	 * @param fragmentTag
	 *            The tag of the fragment to be loaded.
	 */
	private void loadFragment(Fragment fragment, String fragmentTag) {
		int breadCrumbTitleResId = projectBreadCrumbTitle(fragmentTag);
		
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.fragment_container, fragment, fragmentTag);
		transaction.addToBackStack(null);
		transaction.setBreadCrumbTitle(breadCrumbTitleResId);
		transaction.commit();
	}

	/**
	 * Get the resource id that would be the bread crumb title of the transaction's BackStack.
	 * <br>
	 * <br>
	 * Since each transaction only contains a single fragment, this id can be regarded as this 
	 * Fragment's "bread crumb title".
	 * 
	 * @param fragmentTag
	 *            The tag of the fragment in this transaction.
	 * @return The resource id of the "bread crumb title" of this fragment.
	 */
	private int projectBreadCrumbTitle(String fragmentTag) {
		if (fragmentTag.equals(FRAGMENT_TAG_ABOUT)) {
			return R.string.pref_header_about_title;
		} else if (fragmentTag.equals(FRAGMENT_TAG_COMPONENT)) {
			return R.string.pref_header_component_title;
		} else if (fragmentTag.equals(FRAGMENT_TAG_DISPLAY)) {
			return R.string.pref_header_display_title;
		} else if (fragmentTag.equals(FRAGMENT_TAG_REFRESH)) {
			return R.string.pref_header_refresh_title;
		} else {
			return 0;
		}
	}

}
