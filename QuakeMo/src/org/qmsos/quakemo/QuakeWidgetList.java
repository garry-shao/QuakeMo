package org.qmsos.quakemo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * 
 * Widget of this application shown as a list of earthquakes.
 *
 */
public class QuakeWidgetList extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		for (int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];

			Intent intent = new Intent(context, QuakeViewsService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list);
			views.setRemoteAdapter(R.id.widget_list_view, intent);
			views.setEmptyView(R.id.widget_list_view, R.id.widget_list_empty_text);

			Intent templateIntent = new Intent(context, MainActivity.class);
			templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

			PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0, templateIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.widget_list_view, templatePendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

}
