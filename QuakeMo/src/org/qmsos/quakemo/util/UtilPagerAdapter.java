package org.qmsos.quakemo.util;

import java.util.List;

import org.qmsos.quakemo.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Customized PagerAdapter for easier fragment switch.
 * 
 *
 */
public class UtilPagerAdapter extends FragmentPagerAdapter {

	private SparseArray<String> tags = new SparseArray<String>();
	private List<Fragment> fragmentList;
	private Context context;

	public UtilPagerAdapter(FragmentManager fm, List<Fragment> fragmentList, Context context) {
		super(fm);

		this.fragmentList = fragmentList;
		this.context = context;
	}

	@Override
	public Fragment getItem(int arg0) {
		// Misleading method name, fragments returned
		// here aren't same with those in FragmentPagerAdaper.
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

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		switch (position) {
		case 0:
			String firstTag = fragment.getTag();
			tags.append(0, firstTag);
			break;
		case 1:
			String secondTag = fragment.getTag();
			tags.append(1, secondTag);
			break;
		}

		return fragment;
	}

	/**
	 * Get the tag of containing fragments, because the original
	 * FragmentPagerAdapter does not expose tag to outside. the getItem() method
	 * is misleading, it does not return the containing fragment, bu rather
	 * create a new one.
	 * 
	 * @param position
	 *            the index of the fragment querying.
	 * @return The tag of querying fragment or NULL if not found.
	 */
	public String getTag(int position) {
		return tags.get(position);
	}

}