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

/**
 * 
 * Providing remote views service of earthquakes for updating widgets.
 *
 */
public class QuakeViewsService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new QuakeViewsFactory(getApplicationContext());
	}

	/**
	 * 
	 * Factory class used for managing remote views.
	 *
	 */
	public class QuakeViewsFactory implements RemoteViewsFactory {
		private Context context;
		private Cursor quakeCursor;

		public QuakeViewsFactory(Context context) {
			this.context = context;
		}

		@Override
		public void onCreate() {
			quakeCursor = executeQuery();
		}

		@Override
		public void onDataSetChanged() {
			// Working around:
			// Change process identity so the manifest don't have to expose
			// provider.
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

			int idIndex = quakeCursor.getColumnIndex(QuakeProvider.KEY_ID);
			int magnitudeIndex = quakeCursor.getColumnIndex(QuakeProvider.KEY_MAGNITUDE);
			int detailsIndex = quakeCursor.getColumnIndex(QuakeProvider.KEY_DETAILS);

			String id = quakeCursor.getString(idIndex);
			String magnitude = "M " + quakeCursor.getString(magnitudeIndex);
			String details = quakeCursor.getString(detailsIndex);

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_singleton);
			views.setTextViewText(R.id.widget_singleton_magnitude, magnitude);
			views.setTextViewText(R.id.widget_singleton_details, details);

			Intent fillInIntent = new Intent();
			fillInIntent.setData(Uri.withAppendedPath(QuakeProvider.CONTENT_URI, id));

			views.setOnClickFillInIntent(R.id.widget_singleton_magnitude, fillInIntent);
			views.setOnClickFillInIntent(R.id.widget_singleton_details, fillInIntent);

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
				int idIndex = quakeCursor.getColumnIndex(QuakeProvider.KEY_ID);

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
		 * 
		 * @return Cursor to the list of current earthquakes.
		 */
		private Cursor executeQuery() {
			String[] projection = new String[] { 
					QuakeProvider.KEY_ID, QuakeProvider.KEY_MAGNITUDE, QuakeProvider.KEY_DETAILS };

			SharedPreferences prefs = 
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			int minMagnitude = Integer.parseInt(prefs.getString(PrefActivity.PREF_MIN_MAG, "3"));
			String where = QuakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;

			return context.getContentResolver().query(
					QuakeProvider.CONTENT_URI, projection, where, null, null);
		}

	}

}
