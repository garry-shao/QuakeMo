package org.qmsos.quakemo;

import org.qmsos.quakemo.contract.IntentContract;
import org.qmsos.quakemo.contract.ProviderContract.Entity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Widget of this application show a single earthquake.
 *
 *
 */
public class EarthquakeAppWidget extends AppWidgetProvider {

	private static final String TAG = EarthquakeAppWidget.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_earthquake);
		views.setOnClickPendingIntent(R.id.appwidget_magnitude, pendingIntent);
		views.setOnClickPendingIntent(R.id.appwidget_details, pendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);

		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String action = intent.getAction();
		if (action != null && action.equals(IntentContract.ACTION_REFRESH_APPWIDGET)) {
			updateEarthquake(context);
		}
	}

	/**
	 * Update AppWidget using the following parameters.
	 * 
	 * @param context
	 *            The context this AppWidget provider is in.
	 * @param appWidgetManager
	 *            The AppWidget Manager.
	 * @param appWidgetIds
	 *            The AppWidge IDs.
	 */
	private void updateEarthquake(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		double magnitude = 0.0f;
		String details = context.getString(R.string.appwidget_empty);

		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Entity.CONTENT_URI, null, null, null, null);
			if (cursor != null && cursor.moveToLast()) {
				magnitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Entity.MAGNITUDE));
				details = cursor.getString(cursor.getColumnIndexOrThrow(Entity.DETAILS));
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "error parsing number");
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Columns do not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		for (int i = 0; i < appWidgetIds.length; i++) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_earthquake);
			views.setTextViewText(R.id.appwidget_magnitude, "M " + magnitude);
			views.setTextViewText(R.id.appwidget_details, details);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}

	/**
	 * Update AppWidget using Context only.
	 * 
	 * @param context
	 *            The context this AppWidget provider is in.
	 */
	private void updateEarthquake(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
				new ComponentName(context, EarthquakeAppWidget.class));

		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}
}
