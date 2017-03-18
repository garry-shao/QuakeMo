package org.qmsos.quakemo.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.qmsos.quakemo.R;

import java.util.List;

/**
 * Customized FragmentPagerAdapter that provides page titles. 
 */
public class CustomFragmentPagerAdapter extends BaseFragmentListPagerAdapter {

    private Context mContext;

    public CustomFragmentPagerAdapter(FragmentManager fm,
                                      List<Fragment> fragmentList,
                                      Context context) {
        super(fm, fragmentList);

        this.mContext = context;
    }

    public CustomFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        this.mContext = context;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = getFragment(position);

        if (fragment instanceof EarthquakeList) {
            return mContext.getString(R.string.tab_list);
        } else if (fragment instanceof EarthquakeMap) {
            return mContext.getString(R.string.tab_map);
        } else {
            return mContext.getString(R.string.tab_null);
        }
    }
}