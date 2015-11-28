package org.qmsos.quakemo;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

/**
 * 
 * Overlay on the map of earthquakes.
 *
 */
public class QuakeMapOverlay extends Overlay {

	/**
	 * Radius of earthquake dot symbol.
	 */
	private static final int RADIUS = 3;

	/**
	 * Cursor used to iterate location stored.
	 */
	private Cursor locationsCursor;
	
	/**
	 * Earthquake locations retrieved.
	 */
	private ArrayList<GeoPoint> quakeLocations;

	/**
	 * Constructor of earthquake overlay.
	 * 
	 * @param context
	 *            Context of this overlay associated to.
	 * @param cursor
	 *            The cursor that iterates each earthquake.
	 */
	public QuakeMapOverlay(Context context, Cursor cursor) {
		super(context);

		locationsCursor = cursor;

		quakeLocations = new ArrayList<GeoPoint>();

		refreshQuakeLocations();
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();

		Paint paint = new Paint();
		paint.setARGB(250, 255, 0, 0);
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);

		if (shadow == false) {
			for (GeoPoint geoPoint : quakeLocations) {
				Point point = new Point();
				projection.toPixels(geoPoint, point);

				RectF oval = new RectF(
						point.x - RADIUS, point.y - RADIUS, point.x + RADIUS, point.y + RADIUS);

				canvas.drawOval(oval, paint);
			}
		}
	}

	/**
	 * Passing new earthquake result cursor.
	 * 
	 * @param cursor
	 *            The new cursor.
	 */
	public void swapCursor(Cursor cursor) {
		locationsCursor = cursor;

		refreshQuakeLocations();
	}

	/**
	 * Iterating and extracting the location of each earthquake.
	 */
	private void refreshQuakeLocations() {
		quakeLocations.clear();

		if (locationsCursor != null && locationsCursor.moveToFirst()) {
			do {
				int LaIndex = locationsCursor.getColumnIndexOrThrow(QuakeProvider.KEY_LOCATION_LA);
				int LoIndex = locationsCursor.getColumnIndexOrThrow(QuakeProvider.KEY_LOCATION_LO);

				Double la = locationsCursor.getFloat(LaIndex) * 1E6;
				Double lo = locationsCursor.getFloat(LoIndex) * 1E6;

				GeoPoint geoPoint = new GeoPoint(la.intValue(), lo.intValue());

				quakeLocations.add(geoPoint);
			} while (locationsCursor.moveToNext());
		}
	}
}
