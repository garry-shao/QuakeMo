package org.qmsos.quakemo;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Utility class.
 * 
 *
 */
public class UtilPagerAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragmentList;

	public UtilPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
		super(fm);
		this.fragmentList = fragmentList;
	}

	@Override
	public Fragment getItem(int arg0) {
		return (fragmentList == null || fragmentList.size() == 0) ? null : fragmentList.get(arg0);
	}

	@Override
	public int getCount() {
		return fragmentList == null ? 0 : fragmentList.size();
	}
	
}