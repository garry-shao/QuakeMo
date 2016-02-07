package org.qmsos.quakemo.fragment;

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
public class EarthquakePagerAdapter extends FragmentPagerAdapter {

	private SparseArray<String> mTags = new SparseArray<String>();
	private List<Fragment> mFragmentList;
	private Context mContext;

	public EarthquakePagerAdapter(FragmentManager fm, List<Fragment> fragmentList, Context context) {
		super(fm);

		this.mFragmentList = fragmentList;
		this.mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		// Misleading method name, fragments returned
		// here aren't same with those in FragmentPagerAdaper.
		return (mFragmentList == null || mFragmentList.size() == 0) ? null : mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList == null ? 0 : mFragmentList.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Fragment fragment = mFragmentList.get(position);
		if (fragment instanceof EarthquakeList) {
			return mContext.getString(R.string.tab_list);
		} else if (fragment instanceof EarthquakeMap) {
			return mContext.getString(R.string.tab_map);
		} else {
			return mContext.getString(R.string.tab_null);
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		switch (position) {
		case 0:
			String firstTag = fragment.getTag();
			mTags.append(0, firstTag);
			break;
		case 1:
			String secondTag = fragment.getTag();
			mTags.append(1, secondTag);
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
		return mTags.get(position);
	}

}