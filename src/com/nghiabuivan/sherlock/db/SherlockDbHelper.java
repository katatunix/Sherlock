package com.nghiabuivan.sherlock.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SherlockDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "Sherlock.db";
	private static final int DATABASE_VERSION = 1;
	
	//=================================================================================
	// Table: Person
	private static abstract class Person implements BaseColumns {
		public static final String TABLE_NAME = "Person";
		public static final String COL_NAME_FULL_NAME = "fullName";
		public static final String COL_NAME_GENDER = "gender";
		public static final String COL_NAME_HEIGHT = "height";
		public static final String COL_NAME_AGE_MIN = "ageMin";
		public static final String COL_NAME_AGE_MAX = "ageMax";
		public static final String COL_NAME_HAIR_COLOR = "hairColor";
		public static final String COL_NAME_COMMENT = "comment";
	}
	
	private static final String SQL_CREATE_PERSON =
			"CREATE TABLE " + Person.TABLE_NAME + "(" +
			Person._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			Person.COL_NAME_FULL_NAME		+ " TEXT NOT NULL, " +
			Person.COL_NAME_GENDER			+ " INTEGER NOT NULL, " +
			Person.COL_NAME_HEIGHT			+ " REAL NOT NULL, " +
			Person.COL_NAME_AGE_MIN			+ " INTEGER NOT NULL, " +
			Person.COL_NAME_AGE_MAX			+ " INTEGER NOT NULL, " +
			Person.COL_NAME_HAIR_COLOR		+ " TEXT, " +
			Person.COL_NAME_COMMENT			+ " TEXT" +
			")";
	
	private static final String SQL_DELETE_PERSON =
			"DROP TABLE IF EXIST " + Person.TABLE_NAME;
	
	//=================================================================================
	// Table: Location
	private static abstract class Location implements BaseColumns {
		public static final String TABLE_NAME = "Location";
		public static final String COL_NAME_PERSON_ID = "personId";
		public static final String COL_NAME_POSITION = "position";
		public static final String COL_NAME_DESCRIPTION = "description";
		public static final String COL_NAME_TIME = "time";
		public static final String COL_NAME_NOTE = "note";
		public static final String COL_NAME_PHOTO = "photo";
	}
	
	private static final String SQL_CREATE_LOCATION =
			"CREATE TABLE " + Location.TABLE_NAME + "(" +
			Location._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			Location.COL_NAME_PERSON_ID + " INTEGER NOT NULL, " +
			Location.COL_NAME_POSITION + " TEXT, " +
			Location.COL_NAME_DESCRIPTION + " TEXT NOT NULL, " +
			Location.COL_NAME_TIME + " INTEGER NOT NULL, " +
			Location.COL_NAME_NOTE + " TEXT NOT NULL, " +
			Location.COL_NAME_PHOTO + " BLOB" +
			")";
	
	private static final String SQL_DELETE_LOCATION =
			"DROP TABLE IF EXIST " + Location.TABLE_NAME;
	
	//=================================================================================
	private static Context s_context;
	private static SherlockDbHelper s_instance = null;
	
	private SQLiteDatabase m_db = null;
	
	/**
	 * NOTE: remember to call this method first
	 * @param ctx
	 */
	public static void setContext(Context ctx) {
		s_context = ctx;
	}
	
	/**
	 * Singleton pattern.
	 * Remember to call setContext() before.
	 * @return
	 */
	public static SherlockDbHelper getInstance() {
		if (s_instance == null) {
			s_instance = new SherlockDbHelper();
		}
		return s_instance;
	}

	private SherlockDbHelper() {
		super(s_context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_PERSON);
		db.execSQL(SQL_CREATE_LOCATION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_PERSON);
		db.execSQL(SQL_DELETE_LOCATION);
		onCreate(db);
	}
	
	public void openReadDb() {
		if (m_db != null) {
			closeDb();
		}
		m_db = getReadableDatabase();
	}
	
	public void openWriteDb() {
		if (m_db != null) {
			closeDb();
		}
		m_db = getWritableDatabase();
	}
	
	public void closeDb() {
		if (m_db != null) {
			m_db.close();
			m_db = null;
		}
	}
	
	//====================================================================================
	//====================================================================================
	
	public List<DaoBriefPerson> getDaoBriefPersonList() {
		List<DaoBriefPerson> list = new ArrayList<DaoBriefPerson>();
		
		String sql = "SELECT " +
						Person._ID + ", " +
						Person.COL_NAME_FULL_NAME +
						" FROM " + Person.TABLE_NAME +
						" ORDER BY " + Person.COL_NAME_FULL_NAME;
		
		Cursor cursor = m_db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(Person._ID));
				String fullName = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_FULL_NAME));
				
				DaoBriefPerson person = new DaoBriefPerson(id, fullName);
				
				list.add(person);
			} while (cursor.moveToNext());
		}
		
		cursor.close();
		
		return list;
	}
	
	public long addPerson(String fullName, int gender, float height, int ageMin, int ageMax,
			String hairColor, String comment) {
		
		ContentValues values = new ContentValues();
		
		values.put(Person.COL_NAME_FULL_NAME, fullName);
		values.put(Person.COL_NAME_GENDER, gender);
		values.put(Person.COL_NAME_HEIGHT, height);
		values.put(Person.COL_NAME_AGE_MIN, ageMin);
		values.put(Person.COL_NAME_AGE_MAX, ageMax);
		if (hairColor != null) {
			values.put(Person.COL_NAME_HAIR_COLOR, hairColor);
		}
		if (comment != null) {
			values.put(Person.COL_NAME_COMMENT, comment);
		}
		
		return m_db.insert(Person.TABLE_NAME, null, values);
	}
	
	public void editPerson(int personId, String fullName, int gender, float height,
			int ageMin, int ageMax, String hairColor, String comment) {
		
		ContentValues values = new ContentValues();
		
		values.put(Person.COL_NAME_FULL_NAME, fullName);
		values.put(Person.COL_NAME_GENDER, gender);
		values.put(Person.COL_NAME_HEIGHT, height);
		values.put(Person.COL_NAME_AGE_MIN, ageMin);
		values.put(Person.COL_NAME_AGE_MAX, ageMax);
		values.put(Person.COL_NAME_HAIR_COLOR, hairColor); // can be null
		values.put(Person.COL_NAME_COMMENT, comment); // can be null
		
		m_db.update(Person.TABLE_NAME, values, Person._ID + " = " + personId, null);
	}
	
	public List<DaoPerson> getListPersonAll() {
		String sql = "SELECT *" +
				" FROM " + Person.TABLE_NAME +
				" ORDER BY " + Person.COL_NAME_FULL_NAME;
		
		List<DaoPerson> list = new ArrayList<DaoPerson>();
		
		Cursor cursor = m_db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex(Person._ID));
				String fullName = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_FULL_NAME));
				int gender = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_GENDER));
				float height = cursor.getFloat(cursor.getColumnIndex(Person.COL_NAME_HEIGHT));
				int ageMin = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MIN));
				int ageMax = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MAX));
				String hairColor = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_HAIR_COLOR));
				String comment = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_COMMENT));
				
				DaoPerson person = new DaoPerson(id, fullName, gender, height, ageMin, ageMax, hairColor, comment);
				fetchLocationListForPerson(person);
				
				list.add(person);
				
			} while (cursor.moveToNext());
		}
		cursor.close();
		
		return list;
	}
	
	public DaoPerson getPersonById(int id) {
		String sql = "SELECT *" +
				" FROM " + Person.TABLE_NAME +
				" WHERE " + Person._ID + " = " + id;
		
		DaoPerson person = null;
		Cursor cursor = m_db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			String fullName = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_FULL_NAME));
			int gender = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_GENDER));
			float height = cursor.getFloat(cursor.getColumnIndex(Person.COL_NAME_HEIGHT));
			int ageMin = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MIN));
			int ageMax = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MAX));
			String hairColor = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_HAIR_COLOR));
			String comment = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_COMMENT));
			
			person = new DaoPerson(id, fullName, gender, height, ageMin, ageMax, hairColor, comment);
		}
		cursor.close();
		
		if (person != null) {
			fetchLocationListForPerson(person);
		}
		
		return person;
	}
	
	public void deletePersonById(int id) {
		deleteLocationByPersonId(id); // Delete all locations belong to this person first
		m_db.delete(Person.TABLE_NAME, Person._ID + " = " + id, null);
	}
	
	public void fetchBasicInfoForPerson(DaoPerson person) {
		String sql = "SELECT *" +
				" FROM " + Person.TABLE_NAME +
				" WHERE " + Person._ID + " = " + person.getId();
		
		Cursor cursor = m_db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			String fullName = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_FULL_NAME));
			int gender = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_GENDER));
			float height = cursor.getFloat(cursor.getColumnIndex(Person.COL_NAME_HEIGHT));
			int ageMin = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MIN));
			int ageMax = cursor.getInt(cursor.getColumnIndex(Person.COL_NAME_AGE_MAX));
			String hairColor = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_HAIR_COLOR));
			String comment = cursor.getString(cursor.getColumnIndex(Person.COL_NAME_COMMENT));
			
			person.update(fullName, gender, height, ageMin, ageMax, hairColor, comment);
		}
		cursor.close();
	}
	
	public void fetchLocationListForPerson(DaoPerson person) {
		int personId = person.getId();
		
		String sql = "SELECT *" +
				" FROM " + Location.TABLE_NAME +
				" WHERE " + Location.COL_NAME_PERSON_ID + " = " + personId +
				" ORDER BY " + Location.COL_NAME_TIME;
		
		List<DaoLocation> locationList = person.getLocationList();
		locationList.clear();
		
		Cursor cursor = m_db.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			do {
				int locationId = cursor.getInt(cursor.getColumnIndex(Location._ID));
				String position = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_POSITION));
				String description = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_DESCRIPTION));
				long time = cursor.getLong(cursor.getColumnIndex(Location.COL_NAME_TIME));
				String note = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_NOTE));
				byte[] photoBytes = cursor.getBlob(cursor.getColumnIndex(Location.COL_NAME_PHOTO));
				
				DaoLocation loc = new DaoLocation(locationId, personId, position, description, time, note, photoBytes);
				locationList.add(loc);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}
	
	public void addLocation(int personId, String position, String description, long time, String note, byte[] photoBytes) {
		ContentValues values = new ContentValues();
		values.put(Location.COL_NAME_PERSON_ID, personId);
		if (position != null) {
			values.put(Location.COL_NAME_POSITION, position);
		}
		values.put(Location.COL_NAME_DESCRIPTION, description);
		values.put(Location.COL_NAME_TIME, time);
		values.put(Location.COL_NAME_NOTE, note);
		if (photoBytes != null) {
			values.put(Location.COL_NAME_PHOTO, photoBytes);
		}
		
		m_db.insert(Location.TABLE_NAME, null, values);
	}
	
	public void editLocation(int locationId, String position, String description, long time, String note, byte[] photoBytes) {
		ContentValues values = new ContentValues();
		if (position != null) {
			values.put(Location.COL_NAME_POSITION, position);
		}
		values.put(Location.COL_NAME_DESCRIPTION, description);
		values.put(Location.COL_NAME_TIME, time);
		values.put(Location.COL_NAME_NOTE, note);
		values.put(Location.COL_NAME_PHOTO, photoBytes); // photoBytes can be null
		
		m_db.update(Location.TABLE_NAME, values, Location._ID + " = " + locationId, null);
	}
	
	public DaoLocation getLocationById(int id) {
		String sql = "SELECT *" +
				" FROM " + Location.TABLE_NAME +
				" WHERE " + Location._ID + " = " + id;
		
		DaoLocation location = null;
		Cursor cursor = m_db.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			int personId = cursor.getInt(cursor.getColumnIndex(Location.COL_NAME_PERSON_ID));
			String position = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_POSITION));
			String description = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_DESCRIPTION));
			long time = cursor.getLong(cursor.getColumnIndex(Location.COL_NAME_TIME));
			String note = cursor.getString(cursor.getColumnIndex(Location.COL_NAME_NOTE));
			byte[] photoBytes = cursor.getBlob(cursor.getColumnIndex(Location.COL_NAME_PHOTO));
			
			location = new DaoLocation(id, personId, position, description, time, note, photoBytes);
		}
		cursor.close();
		
		return location;
	}

	public void deleteLocationById(int id) {
		m_db.delete(Location.TABLE_NAME, Location._ID + " = " + id, null);
	}
	
	public void deleteLocationByPersonId(int personId) {
		m_db.delete(Location.TABLE_NAME, Location.COL_NAME_PERSON_ID + " = " + personId, null);
	}

}
