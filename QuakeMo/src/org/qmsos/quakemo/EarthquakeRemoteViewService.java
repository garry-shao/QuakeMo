package org.qmsos.quakemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class EarthquakeRemoteViewService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new EarthquakeRemoteViewsFactory(getApplicationContext());
	}

	public class EarthquakeRemoteViewsFactory implements RemoteViewsFactory {
		private Context context;
		private Cursor quakeCursor;
		
		public EarthquakeRemoteViewsFactory(Context context) {
			this.context = context;
		}

		@Override
		public void onCreate() {
			quakeCursor = executeQuery();
		}

		@Override
		public void onDataSetChanged() {
//			Working around: 
//			Change process identity so the manifest don't have to expose provider. 
			final long token = Binder.clearCallingIdentity();
			
			quakeCursor = executeQuery();
			
			Binder.restoreCallingIdentity(token);
		}

		@Override
		public void onDestroy() {
			quakeCursor.close();
		}

		@Override
		public int getCount() {
			if (quakeCursor != null) {
				return quakeCursor.getCount();
			} else {
				return 0;
			}
		}

		@Override
		public RemoteViews getViewAt(int position) {
			quakeCursor.moveToPosition(position);
			
			int idIndex = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_ID);
			int magnitudeIndex = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE);
			int detailsIndex = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_DETAILS);
			
			String id = quakeCursor.getString(idIndex);
			String magnitude = quakeCursor.getString(magnitudeIndex) + "M";
			String details = quakeCursor.getString(detailsIndex);
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.earthquake_widget);
			views.setTextViewText(R.id.widget_magnitude, magnitude);
			views.setTextViewText(R.id.widget_details, details);
			
			Intent fillInIntent = new Intent();
			fillInIntent.setData(Uri.withAppendedPath(EarthquakeProvider.CONTENT_URI, id));
			
			views.setOnClickFillInIntent(R.id.widget_magnitude, fillInIntent);
			views.setOnClickFillInIntent(R.id.widget_details, fillInIntent);
			
			return views;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int position) {
			if (quakeCursor != null) {
				int idIndex = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_ID);
				
				return quakeCursor.getLong(idIndex);
			} else {
				return position;
			}
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		/**
		 * Query the earthquake provider to get list of current earthquakes.
		 * @return Cursor to the list of current earthquakes.
		 */
		private Cursor executeQuery() {
			String[] projection = new String[] {
				EarthquakeProvider.KEY_ID,
				EarthquakeProvider.KEY_MAGNITUDE,
				EarthquakeProvider.KEY_DETAILS,
			};
			
			SharedPreferences prefs = PreferenceManager.
					getDefaultSharedPreferences(getApplicationContext());
			
			int minMagnitude = Integer.parseInt(
					prefs.getString(EarthquakePreferences.PREF_MIN_MAG, "3"));
			
			String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
			
			return context.getContentResolver().query(
					EarthquakeProvider.CONTENT_URI, projection, where, null, null);
		}
		
	}

}
