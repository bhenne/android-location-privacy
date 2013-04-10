/*
 * Copyright (C) 2013 Distributed Computing & Security Group,
 *                    Leibniz Universitaet Hannover, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.locationprivacy.control;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.locationprivacy.algorithm.Radius;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;

/**
 * The CryptoDatabase stores all data as encrypted strings using SQLite
 * 
 * @author Christian Kater
 * 
 */
public class CryptoDatabase {

	/** The Constant UTF8. */
	protected static final String UTF8 = "utf-8";

	/** The database. */
	private SQLiteDatabase database;

	/** The key. */
	private SecretKey key;

	/** The context. */
	private Context context;

	/**
	 * Creates new instance of CryptoDatabase
	 * 
	 * @param password
	 *            Encryption password
	 * @param salt
	 *            Encryption salt
	 * @param iterationCount
	 *            Encryption iteration count
	 * @param context
	 *            Context the database is stored in
	 */
	public CryptoDatabase(String password, String salt, int iterationCount,
			Context context) {
		super();
		this.key = generateKey(password, salt, iterationCount);
		this.context = context;
		CryptoOpenHelper helper = new CryptoOpenHelper(context);
		database = helper.getWritableDatabase();
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#insert(String, String,
	 *      ContentValues) Entries of ContentValues are encrypted
	 */
	public long insert(String table, String nullColumnHack, ContentValues values) {
		return database.insert(table, nullColumnHack,
				encryptContentValues(values));
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#delete(String, String,
	 *      String[])
	 */
	public int delete(String table, String whereClause, String[] whereArgs) {
		return database.delete(table, whereClause, encryptArgs(whereArgs));
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#update(String, ContentValues,
	 *      String, String[]) Entries of ContentValues are encrypted
	 */
	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		return database.update(table, encryptContentValues(values),
				whereClause, encryptArgs(whereArgs));
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#query(String, String[],
	 *      String, String[], String, String, String) Parameter groupBy is ignored
	 */
	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		// groupBy, having and orderBy are not meaningful on encrypted values
		Cursor cursor = database.query(table, columns, selection,
				encryptArgs(selectionArgs), null, null, null);
		return new CryptoCursor(cursor);
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#close()
	 */
	public void close() {
		database.close();
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#beginTransaction()
	 */
	public void beginTransaction() {
		database.beginTransaction();
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#setTransactionSuccessful()
	 */
	public void setTransactionSuccessful() {
		database.setTransactionSuccessful();
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#endTransaction()
	 */
	public void endTransaction() {
		database.endTransaction();
	}

	/**
	 * Encrypts content of ContValues
	 * 
	 * @param values
	 *            ContentValues to be encrypted
	 * @return encrypted ContentValues object
	 */
	private ContentValues encryptContentValues(ContentValues values) {
		ContentValues encryptedValues = new ContentValues();
		for (String key : values.keySet()) {
			encryptedValues.put(key, encrypt("" + values.get(key)));
		}
		return encryptedValues;
	}

	/**
	 * Encrypts an Array of Strings
	 * 
	 * @param args
	 *            Strings to be encrypted
	 * @return encrypted Array of Strings
	 */
	private String[] encryptArgs(String[] args) {
		String[] encryptedArgs = null;
		if (args != null) {
			encryptedArgs = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				encryptedArgs[i] = encrypt(args[i]);
			}
		}
		return encryptedArgs;
	}

	/**
     * Generates secret key from password, salt and iteration count
	 * 
	 * @param password
	 *            Passwort
	 * @param salt
	 *            Salt
	 * @param iterationCount
	 *            Interation count
	 * @return SecretKey object
	 */
	private SecretKey generateKey(String password, String salt,
			int iterationCount) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWITHSHAAND128BITAES-CBC-BC");
			return keyFactory.generateSecret(new PBEKeySpec(password
					.toCharArray(), salt.getBytes(), iterationCount, 128));
		} catch (NoSuchAlgorithmException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Encrypts a String
	 * 
	 * @param value
	 *            String to be encrypted
	 * @return encrypted String
	 */
	private String encrypt(String value) {

		try {
			final byte[] bytes = value != null ? value.getBytes(UTF8)
					: new byte[0];
			Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key);
			return new String(Base64.encode(pbeCipher.doFinal(bytes),
					Base64.NO_WRAP), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Decrypts a String
	 * 
	 * @param value
	 *            String to be decrypted
	 * @return decrypted String
	 */
	private String decrypt(String value) {
		try {
			final byte[] bytes = value != null ? Base64.decode(value,
					Base64.DEFAULT) : new byte[0];
			Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			pbeCipher.init(Cipher.DECRYPT_MODE, key);
			return new String(pbeCipher.doFinal(bytes), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The CryptoCursor contains the encrypted result of a SELECT query.
     * Data is decrypted on access.
     *
     * @author Christian Kater
     *
	 */
	private class CryptoCursor implements Cursor {

		private Cursor c;

		public CryptoCursor(Cursor cursor) {
			super();
			this.c = cursor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#close()
		 */
		@Override
		public void close() {
			c.close();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#copyStringToBuffer(int,
		 * android.database.CharArrayBuffer)
		 */
		@Override
		public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
			c.copyStringToBuffer(columnIndex, buffer);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#deactivate()
		 */
		@Override
		@Deprecated
		public void deactivate() {
			c.deactivate();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getBlob(int)
		 */
		@Override
		public byte[] getBlob(int columnIndex) {
			return decrypt(c.getString(columnIndex)).getBytes();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return c.getColumnCount();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getColumnIndex(java.lang.String)
		 */
		@Override
		public int getColumnIndex(String columnName) {
			return c.getColumnIndex(columnName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getColumnIndexOrThrow(java.lang.String)
		 */
		@Override
		public int getColumnIndexOrThrow(String columnName)
				throws IllegalArgumentException {
			return c.getColumnIndexOrThrow(columnName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getColumnName(int)
		 */
		@Override
		public String getColumnName(int columnIndex) {
			return c.getColumnName(columnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getColumnNames()
		 */
		@Override
		public String[] getColumnNames() {
			return c.getColumnNames();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getCount()
		 */
		@Override
		public int getCount() {
			return c.getCount();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getDouble(int)
		 */
		@Override
		public double getDouble(int columnIndex) {
			return Double.parseDouble(decrypt(c.getString(columnIndex)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getExtras()
		 */
		@Override
		public Bundle getExtras() {
			return c.getExtras();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getFloat(int)
		 */
		@Override
		public float getFloat(int columnIndex) {
			return Float.parseFloat(decrypt(c.getString(columnIndex)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getInt(int)
		 */
		@Override
		public int getInt(int columnIndex) {
			return Integer.parseInt(decrypt(c.getString(columnIndex)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getLong(int)
		 */
		@Override
		public long getLong(int columnIndex) {
			return Long.parseLong(decrypt(c.getString(columnIndex)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getPosition()
		 */
		@Override
		public int getPosition() {
			return c.getPosition();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getShort(int)
		 */
		@Override
		public short getShort(int columnIndex) {
			return Short.parseShort(decrypt(c.getString(columnIndex)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getString(int)
		 */
		@Override
		public String getString(int columnIndex) {
			return decrypt(c.getString(columnIndex));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getType(int)
		 */
		@Override
		public int getType(int columnIndex) {
			// ToDO evt. Type Ÿbersicht
			return c.getType(columnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#getWantsAllOnMoveCalls()
		 */
		@Override
		public boolean getWantsAllOnMoveCalls() {
			return c.getWantsAllOnMoveCalls();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isAfterLast()
		 */
		@Override
		public boolean isAfterLast() {
			return c.isAfterLast();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isBeforeFirst()
		 */
		@Override
		public boolean isBeforeFirst() {
			return c.isBeforeFirst();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isClosed()
		 */
		@Override
		public boolean isClosed() {
			return c.isClosed();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isFirst()
		 */
		@Override
		public boolean isFirst() {
			return c.isFirst();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isLast()
		 */
		@Override
		public boolean isLast() {
			return c.isLast();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#isNull(int)
		 */
		@Override
		public boolean isNull(int columnIndex) {
			// ToDo anpassen
			return c.isNull(columnIndex);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#move(int)
		 */
		public boolean move(int offset) {
			return c.move(offset);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#moveToFirst()
		 */
		@Override
		public boolean moveToFirst() {
			return c.moveToFirst();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#moveToLast()
		 */
		@Override
		public boolean moveToLast() {
			return c.moveToLast();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#moveToNext()
		 */
		@Override
		public boolean moveToNext() {
			return c.moveToNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#moveToPosition(int)
		 */
		@Override
		public boolean moveToPosition(int position) {
			return c.moveToPosition(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#moveToPrevious()
		 */
		@Override
		public boolean moveToPrevious() {
			return c.moveToPrevious();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.Cursor#registerContentObserver(android.database.
		 * ContentObserver)
		 */
		@Override
		public void registerContentObserver(ContentObserver observer) {
			c.registerContentObserver(observer);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.Cursor#registerDataSetObserver(android.database.
		 * DataSetObserver)
		 */
		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			c.registerDataSetObserver(observer);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#requery()
		 */
		@Override
		@Deprecated
		public boolean requery() {
			return c.requery();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#respond(android.os.Bundle)
		 */
		@Override
		public Bundle respond(Bundle extras) {
			return c.respond(extras);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.Cursor#setNotificationUri(android.content.
		 * ContentResolver, android.net.Uri)
		 */
		@Override
		public void setNotificationUri(ContentResolver cr, Uri uri) {
			c.setNotificationUri(cr, uri);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.Cursor#unregisterContentObserver(android.database
		 * .ContentObserver)
		 */
		@Override
		public void unregisterContentObserver(ContentObserver observer) {
			c.unregisterContentObserver(observer);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.Cursor#unregisterDataSetObserver(android.database
		 * .DataSetObserver)
		 */
		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			c.unregisterDataSetObserver(observer);

		}

	}

	/**
	 * CryptoOpenHelper creates erzeugt necessary tables for the location privacy
     * framework and initilizes the default algorithm and state.
     *
     * @author Christian Kater
     *
	 */
	private class CryptoOpenHelper extends SQLiteOpenHelper {

		/**
		 * Creates new instance of DatabaseOpenHelper
		 * 
		 * @param context
		 *            Context the database is stored in
		 */
		public CryptoOpenHelper(Context context) {
			super(context, "privacy.db", null, 1);
		}

		/**
		 * Creates all tables and inserts default values. 
         * Is executed on database creation automatically.
		 * 
		 * @param db
		 *            the Database
		 */
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("Create Table APPLICATION(uid text PRIMARY KEY, name text, status text, algorithm text)");
			db.execSQL("CREATE UNIQUE INDEX idx_APPLICATION ON APPLICATION (uid)");
			db.execSQL("Create Table GENRALCONFIGURATION(configkey text PRIMARY KEY, value text)");
			db.execSQL("CREATE UNIQUE INDEX idx_GENRALCONFIGURATION ON GENRALCONFIGURATION (configkey)");
			db.execSQL("Create Table INTEGERVALUES(intkey text not null, value text, app text not null, PRIMARY KEY(intkey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_INTEGERVALUES ON INTEGERVALUES (intkey, app)");
			db.execSQL("Create Table DOUBLEVALUES(doublekey text not null, value text, app text not null, PRIMARY KEY(doublekey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_DOUBLEVALUES ON DOUBLEVALUES (doublekey, app)");
			db.execSQL("Create Table STRINGVALUES(stringkey text not null,  value text, app text not null, PRIMARY KEY(stringkey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_STRINGVALUES ON STRINGVALUES (stringkey, app)");
			db.execSQL("Create Table ENUMVALUES(enumkey text not null, choosen text, app text not null, PRIMARY KEY(enumkey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_ENUMVALUES ON ENUMVALUES (enumkey, app)");
			db.execSQL("Create Table ENUMENTRY(value text not null,  enumkey text not null, app text not null, PRIMARY KEY(value, enumkey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_ENUMENTRY ON ENUMENTRY (value, enumkey, app)");
			db.execSQL("Create Table COORDINATEVALUES(coordinatekey text not null,  longitude text, latitude text, altitude text, app text not null, PRIMARY KEY(coordinatekey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_COORDINATEVALUES ON COORDINATEVALUES (coordinatekey, app)");
			db.execSQL("Create Table BOOLEANVALUES(booleankey text not null, value text, app text not null,  PRIMARY KEY(booleankey, app))");
			db.execSQL("CREATE UNIQUE INDEX idx_BOOLEANVALUES ON BOOLEANVALUES (booleankey, app)");
			ContentValues status = new ContentValues();
			status.put("configkey", "status");
			status.put("value", "true");
			db.insert("GENRALCONFIGURATION", null, encryptContentValues(status));

			AbstractLocationPrivacyAlgorithm algorithm = new Radius();

			ContentValues defaultAlgorithm = new ContentValues();
			defaultAlgorithm.put("configkey", "defaultAlgorithm");
			defaultAlgorithm.put("value", algorithm.getName());
			db.insert("GENRALCONFIGURATION", null,
					encryptContentValues(defaultAlgorithm));

			ContentValues application = new ContentValues();
			application.put("uid", "defaultApp");
			application.put("name", "defaultName");
			application.put("status", "defaultStatus");
			application.put("algorithm", "defaultAlgorithm");
			db.insert("APPLICATION", null, encryptContentValues(application));

			LocationPrivacyConfiguration config = algorithm.getConfiguration();
			Map<String, Integer> intValues = config.getIntValues();
			Map<String, Double> doubleValues = config.getDoubleValues();
			Map<String, String> stringValues = config.getStringValues();
			Map<String, ArrayList<String>> enumValues = config.getEnumValues();
			Map<String, String> enumChoosen = config.getEnumChoosen();
			Map<String, Coordinate> coordinateValues = config
					.getCoordinateValues();
			Map<String, Boolean> booleanValues = config.getBooleanValues();
			db.beginTransaction();
			try {
				for (String key : intValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("intkey", key);
					values.put("value", intValues.get(key));
					db.insert("INTEGERVALUES", null,
							encryptContentValues(values));
				}

				for (String key : doubleValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("doublekey", key);
					values.put("value", doubleValues.get(key));
					db.insert("DOUBLEVALUES", null,
							encryptContentValues(values));
				}

				for (String key : stringValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("stringkey", key);
					values.put("value", stringValues.get(key));
					db.insert("STRINGVALUES", null,
							encryptContentValues(values));
				}

				for (String key : enumValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("enumkey", key);
					values.put("choosen", enumChoosen.get(key));
					db.insert("ENUMVALUES", null, encryptContentValues(values));
					ArrayList<String> valueList = enumValues.get(key);
					for (String string : valueList) {
						ContentValues values2 = new ContentValues();
						values2.put("app", "defaultApp");
						values2.put("enumkey", key);
						values2.put("value", string);
						db.insert("ENUMENTRY", null,
								encryptContentValues(values2));
					}
				}
				for (String key : coordinateValues.keySet()) {
					Coordinate coord = coordinateValues.get(key);
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("coordinatekey", key);
					values.put("longitude", coord.getLongitude());
					values.put("latitude", coord.getLatitude());
					values.put("altitude", coord.getAltitude());
					db.insert("COORDINATEVALUES", null,
							encryptContentValues(values));
				}
				for (String key : booleanValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", "defaultApp");
					values.put("booleankey", key);
					values.put("value", booleanValues.get(key));
					db.insert("BOOLEANVALUES", null,
							encryptContentValues(values));
				}
				db.setTransactionSuccessful();

			} finally {
				db.endTransaction();
			}
		}

		/**
		 * Drops all tables of database and calls OnCreate
		 * 
		 * @param db
		 *            the db
		 * @param oldVersion
		 *            the old version
		 * @param newVersion
		 *            the new version
		 */
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS APPLICATION");
			db.execSQL("DROP TABLE IF EXISTS GENRALCONFIGURATION");
			db.execSQL("DROP TABLE IF EXISTS INTEGERVALUES");
			db.execSQL("DROP TABLE IF EXISTS DOUBLEVALUES");
			db.execSQL("DROP TABLE IF EXISTS STRINGVALUES");
			db.execSQL("DROP TABLE IF EXISTS ENUMVALUES");
			db.execSQL("DROP TABLE IF EXISTS ENUMENTRY");
			db.execSQL("DROP TABLE IF EXISTS COORDINATEVALUES");
			db.execSQL("DROP TABLE IF EXISTS BOOLEANVALUES");
			onCreate(db);

		}

	}

}
