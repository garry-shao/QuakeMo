package org.qmsos.quakemo;

import java.util.LinkedList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

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
	 * Earthquake geoPoints stored.
	 */
	private List<GeoPoint> geoPoints = new LinkedList<GeoPoint>();

	/**
	 * Constructor of earthquake overlay.
	 * 
	 * @param context
	 *            Context of this overlay associated to.
	 */
	public QuakeMapOverlay(Context context) {
		super(context);
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}

		Paint paint = new Paint();
		paint.setARGB(250, 255, 0, 0);
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);
		paint.setStyle(Style.FILL);
		
		Projection projection = mapView.getProjection();
		for (GeoPoint geoPoint : geoPoints) {
			Point point = new Point();
			point = projection.toPixels(geoPoint, point);
			
			canvas.drawCircle(point.x, point.y, RADIUS, paint);
		}
		
	}

	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}

	public void setGeoPoints(List<GeoPoint> geoPoints) {
		this.geoPoints = geoPoints;
	}

}
