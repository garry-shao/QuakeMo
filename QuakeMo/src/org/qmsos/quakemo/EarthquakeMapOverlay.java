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
public class EarthquakeMapOverlay extends Overlay {
	
	private static final int RADIUS = 3;
	
	private Cursor earthquakes;
	private ArrayList<GeoPoint> earthquakeLocations;
	
	/**
	 * Constructor of earthquake overlay.
	 * @param context Context of this overlay associated to.
	 * @param cursor The cursor that iterates each earthquake.
	 */
	public EarthquakeMapOverlay(Context context, Cursor cursor) {
		super(context);

		earthquakes = cursor;
		
		earthquakeLocations = new ArrayList<GeoPoint>();
		
		refreshEarthquakeLocations();
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		
		Paint paint = new Paint();
		paint.setARGB(250, 255, 0, 0);
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);
		
		if (shadow == false) {
			for (GeoPoint geoPoint : earthquakeLocations) {
				Point point = new Point();
				projection.toPixels(geoPoint, point);
				
				RectF oval = new RectF(
						point.x - RADIUS, point.y - RADIUS,
						point.x + RADIUS, point.y + RADIUS);
				
				canvas.drawOval(oval, paint);
			}
		}
	}

	/**
	 * Passing new earthquake result cursor.
	 * @param cursor The new cursor. 
	 */
	public void swapCursor(Cursor cursor) {
		earthquakes = cursor;
		
		refreshEarthquakeLocations();
	}
	
	/**
	 * Iterating and extracting the location of each earthquake.
	 */
	private void refreshEarthquakeLocations() {
		earthquakeLocations.clear();
		
		if (earthquakes != null && earthquakes.moveToFirst()) {
			do {
				int LaIndex = 
						earthquakes.getColumnIndexOrThrow(EarthquakeProvider.KEY_LOCATION_LA);
				int LoIndex = 
						earthquakes.getColumnIndexOrThrow(EarthquakeProvider.KEY_LOCATION_LO);
				
				Double la = earthquakes.getFloat(LaIndex) * 1E6;
				Double lo = earthquakes.getFloat(LoIndex) * 1E6;
				
				GeoPoint geoPoint = new GeoPoint(la.intValue(), lo.intValue());
				
				earthquakeLocations.add(geoPoint);
			} while (earthquakes.moveToNext());
		}
	}
}
