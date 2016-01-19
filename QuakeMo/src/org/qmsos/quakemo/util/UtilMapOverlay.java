package org.qmsos.quakemo.util;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.qmsos.quakemo.QuakeProvider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

/**
 * Overlay on the MapView to show earthquakes.
 *
 *
 */
public class UtilMapOverlay extends Overlay {

	/**
	 * Radius of earthquake dot symbol.
	 */
	private static final int RADIUS = 3;

	private boolean mDataValid;
	private Cursor mCursor;
	private Paint mPaint;
	
	/**
	 * Earthquake geoPoints stored.
	 */
	private List<GeoPoint> geoPoints;

	/**
	 * Constructor of earthquake overlay.
	 * 
	 * @param context
	 *            Context of this overlay associated to.
	 */
	public UtilMapOverlay(Context context, Cursor cursor) {
		super(context);
		
		init(context, cursor);
	}

	private void init(Context context, Cursor cursor) {
		boolean cursorPresent = cursor != null;
		mDataValid = cursorPresent;
		mCursor = cursor;

		mPaint = new Paint();
		mPaint.setARGB(255, 255, 0, 0);
		mPaint.setAntiAlias(true);
		mPaint.setFakeBoldText(true);
		mPaint.setStyle(Style.FILL);
		
		geoPoints = new LinkedList<GeoPoint>();
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		
		Projection projection = mapView.getProjection();
		for (GeoPoint geoPoint : geoPoints) {
			Point point = new Point();
			point = projection.toPixels(geoPoint, point);
			
			canvas.drawCircle(point.x, point.y, RADIUS, mPaint);
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}

		Cursor oldCursor = mCursor;
		mCursor = newCursor;
		if (newCursor != null) {
			mDataValid = true;
		} else {
			mDataValid = false;
		}

		changeDataSet();

		return oldCursor;
	}

	private void changeDataSet() {
		if (mDataValid) {
			geoPoints.clear();
			
			if (mCursor.moveToFirst()) {
				do {
					double latitude = mCursor.getDouble(
							mCursor.getColumnIndexOrThrow(QuakeProvider.KEY_LATITUDE));
					double longitude = mCursor.getDouble(
							mCursor.getColumnIndexOrThrow(QuakeProvider.KEY_LONGITUDE));
					GeoPoint geoPoint = new GeoPoint(latitude, longitude);
					
					geoPoints.add(geoPoint);
				} while (mCursor.moveToNext());
			}
		}
	}

}