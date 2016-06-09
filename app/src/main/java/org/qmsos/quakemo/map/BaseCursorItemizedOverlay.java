package org.qmsos.quakemo.map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

/**
 * Base class that use cursor to get data of items. 
 */
public abstract class BaseCursorItemizedOverlay extends Overlay {

	private boolean mDataValid;
	private Cursor mCursor;
	
	public BaseCursorItemizedOverlay(Context context, Cursor cursor) {
		super(context);
		
		init(context, cursor);
	}

	// Initialize the field variables.
	private void init(Context context, Cursor cursor) {
		mDataValid = (cursor != null);
		mCursor = cursor;
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		
		draw(canvas, mapView, mCursor);
	}

	/**
	 * Implement to draw overlay on the map, assuming this is a proxy method with 
	 * shadow=false on {@link #draw(Canvas, MapView, boolean)}. 
	 * 
	 * @param canvas
	 *            The canvas to draw on.
	 * @param mapView
	 *            The map view of this overlay.
	 * @param data
	 *            The cursor containing data.
	 */
	protected abstract void draw(Canvas canvas, MapView mapView, Cursor data);

	/**
	 * Swap the data cursor.
	 * 
	 * @param newCursor
	 *            The new cursor that swapped in.
	 * @return The old cursor that swapped out.
	 */
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}

		Cursor oldCursor = mCursor;
		mCursor = newCursor;
		mDataValid = (newCursor != null);

		changeDataSet();

		return oldCursor;
	}

	/**
	 * Change the data set in this overlay, by default does nothing; can be used
	 * to extract data from the data cursor.
	 */
	protected void changeDataSet() {
	}

}
