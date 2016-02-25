package org.qmsos.quakemo.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Customized ViewPager that makes MapView answering the scroll gestures first.
 * 
 *
 */
public class CustomViewPager extends ViewPager {

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof CustomMapView) {
			return true;
		} else {
			return super.canScroll(v, checkV, dx, x, y);
		}
	}

}
