package org.qmsos.quakemo;

import java.util.HashMap;

import android.app.SearchManager;
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
 * 
 * Content provider storing all earthquakes.
 *
 */
public class QuakeProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI = 
			Uri.parse("content://org.qmsos.quakeprovider/earthquakes");
	public static final String KEY_ID = "_id";
	public static final String KEY_DATE = "date";
	public static final String KEY_DETAILS = "datails";
	public static final String KEY_SUMMARY = "summary";
	public static final String KEY_LOCATION_LA = "latitude";
	public static final String KEY_LOCATION_LO = "longitude";
	public static final String KEY_MAGNITUDE = "magnitude";
	public static final String KEY_LINK = "link";
	
	private static final int QUAKES = 1;
	private static final int QUAKE_ID = 2;
	private static final int SEARCH = 3;
	
	private static final HashMap<String, String> SEARCH_PROJECTION_MAP;
	private static final UriMatcher uriMatcher;
	
	/**
	 * Initializing the complex object fields.
	 */
	static {
		SEARCH_PROJECTION_MAP = new HashMap<String, String>();
		SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, 
				KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		SEARCH_PROJECTION_MAP.put("_id", KEY_ID + " AS " + "_id");
		
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("org.qmsos.quakeprovider", "earthquakes", QUAKES);
		uriMatcher.addURI("org.qmsos.quakeprovider", "earthquakes/#", QUAKE_ID);
		uriMatcher.addURI("org.qmsos.quakeprovider", 
				SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		uriMatcher.addURI("org.qmsos.quakeprovider", 
				SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
		uriMatcher.addURI("org.qmsos.quakeprovider", 
				SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
		uriMatcher.addURI("org.qmsos.quakeprovider", 
				SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
	}
	
	/**
	 * 
	 * Helper class using for managing the earthquake database.
	 *
	 */
	private static class QuakeDatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = "EarthquakeProvider";
		
		private static final String DATABASE_NAME = "earthquake.db";
		private static final int DATABASE_VERSION = 1;
		private static final String EARTHQUAKE_TABLE = "earthquakes";
		
		private static final String DATABASE_CREATE = 
				"create table " + EARTHQUAKE_TABLE + " (" + 
				KEY_ID + " integer primary key autoincrement, " +
				KEY_DATE + " INTEGER, " + KEY_DETAILS + " TEXT, " +
				KEY_SUMMARY + " TEXT, " + KEY_LOCATION_LA + " FLOAT, " +
				KEY_LOCATION_LO + " FLOAT, " + KEY_MAGNITUDE + " FLOAT, " + 
				KEY_LINK + " TEXT);";
		
		public QuakeDatabaseHelper(Context context, String name, 
				CursorFactory factory, int version) {
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
	
	private QuakeDatabaseHelper dbHelper ;
	
	@Override
	public boolean onCreate() {
		dbHelper = new QuakeDatabaseHelper(getContext(), 
				QuakeDatabaseHelper.DATABASE_NAME, null, 
				QuakeDatabaseHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(QuakeDatabaseHelper.EARTHQUAKE_TABLE);
		
		switch (uriMatcher.match(uri)) {
		case QUAKE_ID:
			queryBuilder.appendWhere(
					KEY_ID + "=" + uri.getPathSegments().get(1));
			break;
		case SEARCH:
			queryBuilder.appendWhere(
					KEY_SUMMARY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
			queryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
			break;
		default:
			break;
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = KEY_DATE;
		} else {
			orderBy = sortOrder;
		}
		
		Cursor cursor = queryBuilder.query(database, projection, 
				selection, selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case QUAKES:
			return "vnd.android.cursor.dir/vnd.qmsos.quakemo";
		case QUAKE_ID:
			return "vnd.android.cursor.item/vnd.qmsos.quakemo";
		case SEARCH:
			return SearchManager.SUGGEST_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		long rowID = database.insert(
				QuakeDatabaseHelper.EARTHQUAKE_TABLE, "quake", values);
		if (rowID > 0) {
			Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(resultUri, null);
			
			return resultUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)) {
		case QUAKES:
			count = database.delete(
					QuakeDatabaseHelper.EARTHQUAKE_TABLE, selection, selectionArgs);
			break;
		case QUAKE_ID:
			String where = KEY_ID + "=" + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(
					QuakeDatabaseHelper.EARTHQUAKE_TABLE, where, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)) {
		case QUAKES:
			count = database.update(
					QuakeDatabaseHelper.EARTHQUAKE_TABLE, values, selection, selectionArgs);
			break;
		case QUAKE_ID:
			String where = KEY_ID + "=" + uri.getPathSegments().get(1) + 
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

}
