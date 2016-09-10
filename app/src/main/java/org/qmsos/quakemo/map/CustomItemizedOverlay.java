package org.qmsos.quakemo.map;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.qmsos.quakemo.contract.ProviderContract.Entity;

/**
 * Customized overlay that draws items of earthquake on the map,
 * the items data is in the cursor.
 */
public class CustomItemizedOverlay extends BaseCursorItemizedOverlay {

	private int mRadius;
	private Paint mPaint;

	// Field variables that will be used as buffer.
	private Point mBufferPoint;
	private GeoPoint mBufferGeoPoint;

	public CustomItemizedOverlay(Cursor cursor) {
		super(cursor);

		init();
	}

	public CustomItemizedOverlay() {
		super();

		init();
	}

    /**
     * Initialize field variables.
     */
    private void init() {
        mRadius = 3;
        mPaint = new Paint();
        mPaint.setARGB(255, 255, 0, 0);
        mPaint.setAntiAlias(true);
        mPaint.setFakeBoldText(true);
        mPaint.setStyle(Style.FILL);

        mBufferPoint = new Point();
        mBufferGeoPoint = new GeoPoint(0.0, 0.0);
    }

	@Override
	protected void draw(Canvas canvas, MapView mapView, Cursor data) {
		Projection projection = mapView.getProjection();
		
		if (data != null && data.moveToFirst()) {
			do {
				double latitude = data.getDouble(data.getColumnIndexOrThrow(Entity.LATITUDE));
				double longitude = data.getDouble(data.getColumnIndexOrThrow(Entity.LONGITUDE));
				
				mBufferGeoPoint.setCoords(latitude, longitude);
				mBufferPoint = projection.toPixels(mBufferGeoPoint, mBufferPoint);
				
				canvas.drawCircle(mBufferPoint.x, mBufferPoint.y, mRadius, mPaint);
			} while (data.moveToNext());
		}
	}

}
