package org.qmsos.quakemo;

import org.qmsos.quakemo.contract.ProviderContract;
import org.qmsos.quakemo.contract.ProviderContract.Entity;

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

	// return code of UriMatcher.
	private static final int QUAKES = 1;
	private static final int QUAKE_ID = 2;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(ProviderContract.AUTHORITY, "earthquakes", QUAKES);
		URI_MATCHER.addURI(ProviderContract.AUTHORITY, "earthquakes/#", QUAKE_ID);
	}
	
	private DatabaseHelper mDatabaseHelper ;
	
	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext(), 
				DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.DATABASE_VERSION);
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DatabaseHelper.TABLE_EARTHQUAKE);
		
		switch (URI_MATCHER.match(uri)) {
		case QUAKE_ID:
			queryBuilder.appendWhere(Entity.ID + " = " + uri.getPathSegments().get(1));
			
			break;
		default:
			break;
		}
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Entity.TIME;
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
		
		long rowId = database.insert(DatabaseHelper.TABLE_EARTHQUAKE, "earthquake", values);
		if (rowId > 0) {
			Uri resultUri = ContentUris.withAppendedId(Entity.CONTENT_URI, rowId);
			
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
				long rowId = database.insert(DatabaseHelper.TABLE_EARTHQUAKE, "earthquake", value);
				if (rowId < 0) {
					// some insert in transaction failed, abort and roll back.
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
			count = database.delete(DatabaseHelper.TABLE_EARTHQUAKE, selection, selectionArgs);
			
			break;
		case QUAKE_ID:
			String where = Entity.ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.delete(DatabaseHelper.TABLE_EARTHQUAKE, where, selectionArgs);
			
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
					DatabaseHelper.TABLE_EARTHQUAKE, values, selection, selectionArgs);
			break;
		case QUAKE_ID:
			String where = Entity.ID + " = " + uri.getPathSegments().get(1) + 
					(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			count = database.update(
					DatabaseHelper.TABLE_EARTHQUAKE, values, where, selectionArgs);
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
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = DatabaseHelper.class.getSimpleName();
		
		private static final String DATABASE_NAME = "earthquake.db";
		private static final int DATABASE_VERSION = 1;
		private static final String TABLE_EARTHQUAKE = "earthquakes";
		
		private static final String DATABASE_CREATE = 
				"CREATE TABLE " + TABLE_EARTHQUAKE + " (" + 
						Entity.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
						Entity.TIME + " INTEGER, " + 
						Entity.MAGNITUDE + " REAL, " + 
						Entity.LONGITUDE + " REAL, " +
						Entity.LATITUDE + " REAL, " +
						Entity.DEPTH + " REAL, " +
						Entity.DETAILS + " TEXT, " +
						Entity.LINK + " TEXT);";
	
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
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
			
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_EARTHQUAKE);
			onCreate(db);
		}
	}

}
