package org.qmsos.quakemo.util;

import java.util.List;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Customized PagerAdapter for easier fragment switch.
 * 
 *
 */
public class UtilPagerAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragmentList;
	private Context context;

	public UtilPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, Context context) {
		super(fm);
		
		this.fragmentList = fragmentList;
		this.context = context;
	}

	@Override
	public Fragment getItem(int arg0) {
		return (fragmentList == null || fragmentList.size() == 0) ? null : fragmentList.get(arg0);
	}

	@Override
	public int getCount() {
		return fragmentList == null ? 0 : fragmentList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return context.getString(R.string.tab_list);
		case 1:
			return context.getString(R.string.tab_map);
		default:
			return context.getString(R.string.tab_null);
		}
	}
	
}