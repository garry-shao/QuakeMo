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
 * 
 * Widget of this application show a single earthquake.
 *
 */
public class EarthquakeWidgetSingle extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single);
		views.setOnClickPendingIntent(R.id.widget_single_magnitude, pendingIntent);
		views.setOnClickPendingIntent(R.id.widget_single_details, pendingIntent);
		
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		
		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if (EarthquakeUpdateService.QUAKES_REFRESHED.equals(intent.getAction())) {
			updateEarthquake(context);
		}
		if (EarthquakeUpdateService.PURGE_DATABASE.equals(intent.getAction())) {
			updateEarthquake(context);
		}
	}

	/**
	 * Update AppWidget using the following parameters.
	 * @param context The context this AppWidget provider is in.
	 * @param appWidgetManager The AppWidget Manager.
	 * @param appWidgetIds The AppWidge IDs.
	 */
	public void updateEarthquake(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int minMagnitude = Integer.parseInt(
				prefs.getString(MainPreferenceActivity.PREF_MIN_MAG, "3"));
		
		String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
		
		Cursor lastEarthquake = context.getContentResolver().query(
				EarthquakeProvider.CONTENT_URI, null, where, null, null);
		
		String magnitude = "--M";
		String details = "-- None --";
		
		if (lastEarthquake != null) {
			try {
				if (lastEarthquake.moveToLast()) {
					int magnitudeIndex = 
							lastEarthquake.getColumnIndexOrThrow(EarthquakeProvider.KEY_MAGNITUDE);
					int detailsIndex = 
							lastEarthquake.getColumnIndexOrThrow(EarthquakeProvider.KEY_DETAILS);
					
					magnitude = lastEarthquake.getString(magnitudeIndex) + "M";
					details = lastEarthquake.getString(detailsIndex);
				}
			} finally {
				lastEarthquake.close();
			}
		}
		
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_single);
			views.setTextViewText(R.id.widget_single_magnitude, magnitude);
			views.setTextViewText(R.id.widget_single_details, details);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	/**
	 * Update AppWidget using Context only.
	 * @param context The context this AppWidget provider is in. 
	 */
	public void updateEarthquake(Context context) {
		ComponentName appWidget = new ComponentName(context, EarthquakeWidgetSingle.class);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
		
		updateEarthquake(context, appWidgetManager, appWidgetIds);
	}
}
