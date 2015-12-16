package org.qmsos.quakemo.util;

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
 * An simple overlay that show only one earthquake.
 *
 *
 */
public class UtilSimpleOverlay extends Overlay {

	private static final int RADIUS = 3;

	private GeoPoint geoPoint;

	/**
	 * Constructor of simple overlay that show particular earthquake.
	 * 
	 * @param context
	 *            Context of this overlay associated to.
	 * @param geoPoint
	 *            The GeoPoint of earthquake.
	 */
	public UtilSimpleOverlay(Context context, GeoPoint geoPoint) {
		super(context);
		this.geoPoint = geoPoint;
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
		Point point = new Point();
		point = projection.toPixels(geoPoint, point);

		canvas.drawCircle(point.x, point.y, RADIUS, paint);
	}

}
