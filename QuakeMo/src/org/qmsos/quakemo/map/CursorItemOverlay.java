package org.qmsos.quakemo.map;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.qmsos.quakemo.provider.EarthquakeContract.Entity;

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
public class CursorItemOverlay extends Overlay {

	// Radius of each dot.
	private static final int RADIUS = 3;

	private boolean mDataValid;
	private Cursor mCursor;
	private Paint mPaint;
	
	// Each GeoPoint of earthquake stored.
	private List<GeoPoint> mGeoPoints;

	/**
	 * Constructor of overlay that shows geo points of earthquake on map.
	 * 
	 * @param context
	 *            Context of this overlay associated to.
	 * @param cursor
	 *            The cursor contained of longitude&latitude of each earthquake.
	 */
	public CursorItemOverlay(Context context, Cursor cursor) {
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
		
		mGeoPoints = new LinkedList<GeoPoint>();
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		
		Projection projection = mapView.getProjection();
		for (GeoPoint geoPoint : mGeoPoints) {
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
		if (!mDataValid) {
			return;
		}
		
		mGeoPoints.clear();
		if (mCursor.moveToFirst()) {
			do {
				double latitude = mCursor.getDouble(mCursor.getColumnIndexOrThrow(Entity.LATITUDE));
				double longitude = mCursor.getDouble(mCursor.getColumnIndexOrThrow(Entity.LONGITUDE));
				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				
				mGeoPoints.add(geoPoint);
			} while (mCursor.moveToNext());
		}
	}

}
