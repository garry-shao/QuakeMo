package org.qmsos.quakemo.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of customized FragmentPagerAdapter that backed by a list.
 */
public abstract class BaseFragmentListPagerAdapter extends FragmentPagerAdapter {

    // Store tags of fragments.
    private SparseArray<String> mFragmentTags = new SparseArray<>();

    private List<Fragment> mFragmentList;

    public BaseFragmentListPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);

        this.mFragmentList = fragmentList;
    }

    public BaseFragmentListPagerAdapter(FragmentManager fm) {
        super(fm);

        this.mFragmentList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        // Misleading method name, fragments returned
        // here aren't same with those in FragmentPagerAdapter.
        return (mFragmentList == null || mFragmentList.size() == 0)
				? null
                : mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    /**
     * Get the fragment of the specific position if any.
     *
     * @param position
     *            The position of the requesting fragment in the list.
     * @return The Fragment at the specific position, or NULL if invalid.
     */
    public Fragment getFragment(int position) {
        if ((mFragmentList == null)
                || (position < 0)
                || (position >= mFragmentList.size())) {
            return null;
        } else {
            return mFragmentList.get(position);
        }
    }

    /**
     * Add a fragment to the this adapter, notice that the same fragment will not
     * be added twice or more.
     *
     * @param fragment
     *            The fragment that will be added.
     */
    public void addPage(Fragment fragment) {
        if ((mFragmentList != null)
                && (fragment != null)
                && !(mFragmentList.contains(fragment))) {
            mFragmentList.add(fragment);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // The fragment on specific position, watch out here.
        Fragment fragment = (Fragment) super.instantiateItem(container, position);

        String fragmentTag = fragment.getTag();
        mFragmentTags.append(position, fragmentTag);

        return fragment;
    }

    /**
     * Get the tag of containing fragments, because the original
     * FragmentPagerAdapter does not expose tag to outside.
     * <br><br>
     * <b>the getItem() method is misleading, it does not return
     * the containing fragment, but rather creates and return a new one.</b>
     *
     * @param position
     *            the index of the fragment querying.
     * @return The tag of querying fragment or NULL if not found.
     */
    public String getFragmentTag(int position) {
        return mFragmentTags.get(position);
    }
}