package org.qmsos.quakemo.util;

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
 * Overlay on the map of earthquakes.
 *
 *
 */
public class UtilQuakeOverlay extends Overlay {

	/**
	 * Radius of earthquake dot symbol.
	 */
	private static final int RADIUS = 3;

	/**
	 * Paint instance used to mark earthquake.
	 */
	private Paint quakePaint;
	
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
	public UtilQuakeOverlay(Context context) {
		super(context);
		
		quakePaint = new Paint();
		quakePaint.setARGB(250, 255, 0, 0);
		quakePaint.setAntiAlias(true);
		quakePaint.setFakeBoldText(true);
		quakePaint.setStyle(Style.FILL);
		
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
			
			canvas.drawCircle(point.x, point.y, RADIUS, quakePaint);
		}
		
	}

	public List<GeoPoint> getGeoPoints() {
		return geoPoints;
	}

	public void setGeoPoints(List<GeoPoint> geoPoints) {
		this.geoPoints = geoPoints;
	}

}
