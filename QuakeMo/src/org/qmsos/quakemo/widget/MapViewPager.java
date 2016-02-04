package org.qmsos.quakemo.widget;

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
public class MapViewPager extends ViewPager {

	public MapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof MapView) {
			return true;
		} else {
			return super.canScroll(v, checkV, dx, x, y);
		}
	}

}
