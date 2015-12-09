package org.qmsos.quakemo.util;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Customized ViewPager to make MapView answer map-related gesture only.
 * 
 *
 */
public class UtilViewPager extends ViewPager {

	public UtilViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UtilViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View arg0, boolean arg1, int arg2, int arg3, int arg4) {
		if (arg0 instanceof MapView) {
			return true;
		} else {
			return super.canScroll(arg0, arg1, arg2, arg3, arg4);
		}
	}

}
