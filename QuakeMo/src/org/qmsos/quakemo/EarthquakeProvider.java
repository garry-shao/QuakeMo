package org.qmsos.quakemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider storing all earthquakes.
 *
 *
 */
public class EarthquakeProvider extends ContentProvider {
	
	private static final String AUTHORITY = "org.qmsos.quakemo.earthquakeprovider";
	
	public static final Uri CONTENT_URI = 
			Uri.parse("content://" + AUTHORITY + "/earthquakes");
	
	//base columns of database
	public static final String KEY_ID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_MAGNITUDE = "magnitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_DEPTH = "depth";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_LINK = "link";

	// return code of UriMatcher.
	private static final int QUAKES = 1;
	private static final int QUAKE_ID = 2;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "earthquakes", QUAKES);
		URI_MATCHER.addURI(AUTHORITY, "earthquakes/#", QUAKE_ID);
	}
	
	private QuakeDatabaseHelper mDatabaseHelper ;
	
	@Override
	public boolean onCreate() {
		mDatabaseHelper = new QuakeDatabaseHelper(getContext(), 
				QuakeDatabaseHelper.DATABASE_NAME, null, QuakeDatabaseHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(QuakeDatabaseHelper.EARTHQUAKE_TABLE);
		
		switch (URI_MATCHER.match(uri)) {
		case QUAKE_ID:
			if (uri.getPathSegments().size() > 1) {
				queryBuilder.appendWhere(KEY_ID + " = " + uri.getPathSegments().get(1));
			}
			break;
		default:
			break;
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_TIME;
		} else {
			orderBy = sortOrder;
		}
		
		Cursor cursor = queryBuilder.query(
				database, projection, selection, selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case QUAKES:
			return "vnd.android.cursor.dir/vnd.org.qmsos.quakemo";
		case QUAKE_ID:
			return "vnd.android.cursor.item/vnd.org.qmsos.quakemo";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		long rowID = database.insert(QuakeDatabaseHelper.EARTHQUAKE_TABLE, "quake", values);
		if (rowID > 0) {
			Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			
			getContext().getContentResolver().notifyChange(resultUri, null);
			
			return resultUri;
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		database.beginTransaction();
		try {
			for (ContentValues value : values) {
				long rowID = database.insert(QuakeDatabaseHelper.EARTHQUAKE_TABLE, "quake", value);
				if (rowID < 0) {
					return 0;
				}
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return values.length;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		int count;
		switch (URI_MATCHER.match(uri)) {
		case QUAKES:
			count = database.delete(QuakeDatabaseHelper.EARTHQUAKE_TABLE, selection, selectionArgs);
			
			break;
		case QUAKE_ID:
			String where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(QuakeDatabaseHelper.EARTHQUAKE_TABLE, where, selectionArgs);
			
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		int count;
		switch (URI_MATCHER.match(uri)) {
		case QUAKES:
			count = database.update(
					QuakeDatabaseHelper.EARTHQUAKE_TABLE, values, selection, selectionArgs);
			break;
		case QUAKE_ID:
			String where = KEY_ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.update(
					QuakeDatabaseHelper.EARTHQUAKE_TABLE, values, where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	/**
	 * 
	 * Helper class using for managing the earthquake database.
	 *
	 */
	private static class QuakeDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = EarthquakeProvider.class.getSimpleName();
		
		private static final String DATABASE_NAME = "earthquake.db";
		private static final int DATABASE_VERSION = 1;
		private static final String EARTHQUAKE_TABLE = "earthquakes";
		
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + EARTHQUAKE_TABLE + " (" + 
						KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						KEY_TIME + " INTEGER, " + 
						KEY_MAGNITUDE + " REAL, " + 
						KEY_LONGITUDE + " REAL, " +
						KEY_LATITUDE + " REAL, " +
						KEY_DEPTH + " REAL, " +
						KEY_DETAILS + " TEXT, " +
						KEY_LINK + " TEXT);";
	
		public QuakeDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + 
					newVersion + ", which will destroy all old data");
			
			db.execSQL("DROP TABLE IF EXISTS " + EARTHQUAKE_TABLE);
			onCreate(db);
		}
	}

}
