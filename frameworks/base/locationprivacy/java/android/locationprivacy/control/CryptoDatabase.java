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
 * Die Klasse CryptoDatabase speichert alle Daten verschlüsselt als Strings in
 * einer SQLitedatenbank.
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
	 * Instanziert eine neues CryptoDatabase-Objekt.
	 * 
	 * @param password
	 *            Passwort mit dem die Daten verschlüsselt werden
	 * @param salt
	 *            Salt der Verschlüsselung
	 * @param iterationCount
	 *            Anzahl der Hashvorgänge
	 * @param context
	 *            Context, in dem die Datenbank gespeichert werden soll.
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
	 *      ContentValues) Die Einträge von ContentValues werden verschlüsselt.
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
	 *      String, String[]) Die Einträge von ContentValues werden
	 *      verschlüsselt.
	 */
	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		return database.update(table, encryptContentValues(values),
				whereClause, encryptArgs(whereArgs));
	}

	/**
	 * @see android.database.sqlite.SQLiteDatabase#query(String, String[],
	 *      String, String[], String, String, String) Der Parameter groupBy wird
	 *      ignoriert.
	 */
	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		// groupBy, having und orderBy sind nicht sinnvoll auf verschlüsselten
		// Daten
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
	 * Verschlüsselt den Inhalt des ContValues-Objekt
	 * 
	 * @param values
	 *            zu verschlüsselndes ContentValues-Objekt
	 * @return verschlüsselndes ContentValues-Objekt
	 */
	private ContentValues encryptContentValues(ContentValues values) {
		ContentValues encryptedValues = new ContentValues();
		for (String key : values.keySet()) {
			encryptedValues.put(key, encrypt("" + values.get(key)));
		}
		return encryptedValues;
	}

	/**
	 * Verschlüsselt ein Array von Strings.
	 * 
	 * @param args
	 *            zu verschlüsselnde Strings
	 * @return verschlüsseltes String-Array
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
	 * Erzeugt aus Passwort, Salt und Anzahl der Hashvorgänge ein Secret Key mit
	 * dem die Daten verschlüsselt und entschlüsselt werden.
	 * 
	 * @param password
	 *            Passwort
	 * @param salt
	 *            Salt
	 * @param iterationCount
	 *            Anzahl der Hashvorgänge
	 * @return SecretKey-Objekt
	 */
	private SecretKey generateKey(String password, String salt,
			int iterationCount) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWITHSHAAND128BITAES-CBC-BC");
			return keyFactory.generateSecret(new PBEKeySpec(password
					.toCharArray(), salt.getBytes(), iterationCount, 128));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Verschlüsselt einen String.
	 * 
	 * @param value
	 *            zu verschlüsselnder String
	 * @return the verschlüsselter String
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
	 * Entschlüsselt einen String.
	 * 
	 * @param value
	 *            zu entschlüsselnder String
	 * @return the entschlüsselter String
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
	 * Die Klasse CryptoCursor beinhaltet die verschlüsselten Daten eine
	 * Select-Anfrage auf die Datenbank. Bei Zugriff auf die Daten werden diese
	 * entschlüsselt zurückgegeben.
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
			// ToDO evt. Type übersicht
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
	 * Die Klasse CryptoOpenHelper erzeugt alle für das
	 * Location-Privacy-Framework notwendigen Tabellen und initialisiert die den
	 * Standardalgorithmus und den Status.
	 */
	private class CryptoOpenHelper extends SQLiteOpenHelper {

		/**
		 * Erzeugt ein neues DatabaseOpenHelper-Objekt.
		 * 
		 * @param context
		 *            Context in dem die Datenbank existiert.
		 */
		public CryptoOpenHelper(Context context) {
			super(context, "privacy.db", null, 1);
		}

		/**
		 * Erzeugt alle Tabellen und fügt die Standarddaten ein. Wird
		 * automatisch aufgerufen, wenn die Datenbank erzeugt wird.
		 * 
		 * @param db
		 *            Datenbank in der die Tabellen und Daten erzeugt werden.
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
		 * Löscht alle Tabellen der Datenbnak und ruft onCreate auf.
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
