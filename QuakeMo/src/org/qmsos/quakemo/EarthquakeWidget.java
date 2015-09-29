package org.qmsos.quakemo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

public class EarthquakeWidget extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		Intent intent = new Intent(context, EarthquakeActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.earthquake_widget);
		views.setOnClickPendingIntent(R.id.widget_magnitude, pendingIntent);
		views.setOnClickPendingIntent(R.id.widget_details, pendingIntent);
		
		appWidgetManager.updateAppWidget(appWidgetIds, views);
		
		updateQuake(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if (EarthquakeUpdateService.QUAKES_REFRESHED.equals(intent.getAction())) {
			updateQuake(context);
		}
	}

	/**
	 * Update AppWidget using the following parameters.
	 * @param context The context this AppWidget provider is in.
	 * @param appWidgetManager The AppWidget Manager.
	 * @param appWidgetIds The AppWidge IDs.
	 */
	public void updateQuake(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		Cursor lastEarthquake;
		ContentResolver resolver = context.getContentResolver();
		lastEarthquake = resolver.query(EarthquakeProvider.CONTENT_URI, null, null, null, null);
		
		String magnitude = "--M";
		String details = "-- None --";
		
		if (lastEarthquake != null) {
			try {
				if (lastEarthquake.moveToFirst()) {
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
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.earthquake_widget);
			views.setTextViewText(R.id.widget_magnitude, magnitude);
			views.setTextViewText(R.id.widget_details, details);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	/**
	 * Update AppWidget using Context only.
	 * @param context The context this AppWidget provider is in. 
	 */
	public void updateQuake(Context context) {
		ComponentName appWidget = new ComponentName(context, EarthquakeWidget.class);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
		
		updateQuake(context, appWidgetManager, appWidgetIds);
	}
}
