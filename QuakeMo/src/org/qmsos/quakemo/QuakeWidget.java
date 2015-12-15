package org.qmsos.quakemo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Widget of this application show a single earthquake.
 *
 *
 */
public class QuakeWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quake);
		views.setOnClickPendingIntent(R.id.widget_magnitude, pendingIntent);
		views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, views);

		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (intent.getAction().equals(QuakeUpdateService.ACTION_REFRESH_WIDGET)) {
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		int minMagnitude = Integer.parseInt(
				prefs.getString(context.getString(R.string.PREF_SHOW_MINIMUM), "3"));

		String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;

		Cursor lastEarthquake = 
				context.getContentResolver().query(QuakeProvider.CONTENT_URI, null, where, null, null);

		String magnitude = "--";
		String details = context.getString(R.string.widget_empty);

		if (lastEarthquake != null) {
			try {
				if (lastEarthquake.moveToLast()) {
					int magnitudeIndex = lastEarthquake.getColumnIndexOrThrow(QuakeProvider.KEY_MAGNITUDE);
					int detailsIndex = lastEarthquake.getColumnIndexOrThrow(QuakeProvider.KEY_DETAILS);

					magnitude = lastEarthquake.getString(magnitudeIndex);
					details = lastEarthquake.getString(detailsIndex);
				}
			} finally {
				lastEarthquake.close();
			}
		}

		for (int i = 0; i < appWidgetIds.length; i++) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quake);
			views.setTextViewText(R.id.widget_magnitude, "M " + magnitude);
			views.setTextViewText(R.id.widget_details, details);

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
				new ComponentName(context, QuakeWidget.class));

		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}
}
