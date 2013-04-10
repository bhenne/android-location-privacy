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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.locationprivacy.algorithm.Deactivate;
import android.locationprivacy.algorithm.FixedPosition;
import android.locationprivacy.algorithm.GeoReverseGeo;
import android.locationprivacy.algorithm.Radius;
import android.locationprivacy.algorithm.RadiusDistance;
import android.locationprivacy.algorithm.TestAlgorithm;
import android.locationprivacy.algorithm.Webservice;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The LocationPrivacyManager abstracts access to the location privacy framework
 * and configuration. It provides all methods for configuration and usage.
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyManager {
	/**
	 * Cached LocationPrivacyApplications. Caching data to minimize access to database.
	 */
	private ArrayList<LocationPrivacyApplication> applications;
	/**
	 * Is location privacy framework enabled?
	 */
	private boolean status;

	/**
	 * Creates new Instance of obfuscation algorithm with given name
	 */
	public static AbstractLocationPrivacyAlgorithm getAlgorithm(String name) {
		return algorithms.get(name).newInstance();
	}

	/**
	 * Returns a list of available obfuscation algorihms
	 */
	public static List<String> getAllAlgorithm() {
		ArrayList<String> sortedKeys = new ArrayList<String>(
				algorithms.keySet());
		Collections.sort(sortedKeys);
		sortedKeys.add(0, "default");
		return sortedKeys;
	}

	/** Context the location privacy framework is running in */
	private Context context;

	private CryptoDatabase database;

	/** All location obfuscation algorithms */
	private static HashMap<String, AbstractLocationPrivacyAlgorithm> algorithms;

	/**
	 * Creates new instance of LocationPrivacyManager
	 * 
	 * @param oContext
	 *            Context the location privacy framework is running in
	 */
	public LocationPrivacyManager(Context oContext) {
		try {
			this.context = oContext.createPackageContext(
					"com.android.settings", Context.CONTEXT_INCLUDE_CODE);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		SharedPreferences sharedPreference = PreferenceManager
				.getDefaultSharedPreferences(context);
		String password = sharedPreference.getString("password", "");
		if (password.equals("")) {
			password = generateRandomString();
			sharedPreference.edit().putString("password", password).commit();
		}
		String salt = sharedPreference.getString("salt", "");
		if (salt.equals("")) {
			salt = generateRandomString();
			sharedPreference.edit().putString("salt", salt).commit();
		}
		int iterationCount = sharedPreference.getInt("iterationCount", -1);
		if (iterationCount == -1) {
			iterationCount = (int) (Math.random() * 50.0) + 50;
			sharedPreference.edit().putInt("iterationCount", iterationCount)
					.commit();
		}
		database = new CryptoDatabase(password, salt, iterationCount, context);
		if (algorithms == null) {
			initialize();
		}
		status = getStatus();
		applications = new ArrayList<LocationPrivacyApplication>();
	}

	/**
	 * Adds new location obfuscation algorithm
	 */
	private void addAlgorithm(AbstractLocationPrivacyAlgorithm algorithm) {
		algorithms.put(algorithm.getName(), algorithm);
	}

	/**
	 * Adds new application to the location privacy framework
	 */
	public LocationPrivacyApplication addApplication(String uid, String name) {
		ContentValues values = new ContentValues();
		values.put("uid", uid);
		values.put("name", name);
		values.put("status", "true");
		values.put("algorithm", "defaultAlg");
		database.beginTransaction();
		try {
			database.insert("APPLICATION", null, values);
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		return new LocationPrivacyApplication(uid, name, true,
				getDefaultAlgorithm(), true);
	}

	public void close() {
		database.close();
	}

	/**
     * Obfuscates location. Based on the uid the corresponding algorithm is used.
     * If uid is new to framework, it is added with default values.
	 * 
	 * @param location
	 *            original location
	 * @param uid
	 *            uid app is running as
	 * @param name
	 *            app name
	 * @return obfuscated location
	 */
	public Location disguiseLocation(Location location, String uid, String name) {
		if (location != null) {
			Location locTemp = new Location(location);
			if (status) {
				LocationPrivacyApplication app = null;
				for (LocationPrivacyApplication application : applications) {
					if (application.getUid().equals(uid)) {
						app = application;
						break;
					}
				}
				if (app == null) {
					app = getApplication(uid);
					if (app == null) {
						app = addApplication(uid, name);
						Log.i("LPA", "added " + uid);
					}
					applications.add(app);
				}
				if (app.isEnabled()) {
					AbstractLocationPrivacyAlgorithm algorithm = app
							.getAlgorithm();
					algorithm.setContext(context);
					locTemp = algorithm.calculateLocation(location);
				}
			}
			return locTemp;
		}
		return null;
	}

	private String generateRandomString() {
		SecureRandom random = new SecureRandom();
		String randomString = new BigInteger(128, random).toString(32);
		return randomString;
	}

	/**
	 * Returns LocationPrivacyApplication corresponding to an app (uid)
	 * 
	 * @param uid
	 * @return LocationPrivacyApplication. null if uid not known to framework
	 */
	public LocationPrivacyApplication getApplication(String uid) {
		Cursor cursor = database.query("APPLICATION", null, "uid = ?",
				new String[] { uid }, null, null, null);
		LocationPrivacyApplication app = null;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String name = cursor.getString(1);
			String statusTemp = cursor.getString(2);
			boolean status = Boolean.parseBoolean(statusTemp);
			String algorithmTemp = cursor.getString(3);
			AbstractLocationPrivacyAlgorithm algorithm;
			if (algorithmTemp.equals("defaultAlg")) {
				algorithm = getDefaultAlgorithm();
				app = new LocationPrivacyApplication(uid, name, status,
						algorithm, true);
			} else {
				algorithm = getAlgorithm(algorithmTemp);
				algorithm.setConfiguration(getConfiguration(uid));
				app = new LocationPrivacyApplication(uid, name, status,
						algorithm, false);
			}
		}
		cursor.close();
		return app;
	}

	/**
	 * Returns a List of all apps known by the location privacy framework
	 */
	public List<LocationPrivacyApplication> getApplications() {
		ArrayList<LocationPrivacyApplication> list = new ArrayList<LocationPrivacyApplication>();
		Cursor cursor = database.query("APPLICATION", null, null, null, null,
				null, null);
		LocationPrivacyApplication app = null;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String uid = cursor.getString(0);
			if (!uid.equals("defaultApp")) {
				String name = cursor.getString(1);
				String statusTemp = cursor.getString(2);
				boolean status = Boolean.parseBoolean(statusTemp);
				String algorithmTemp = cursor.getString(3);
				AbstractLocationPrivacyAlgorithm algorithm;
				if (algorithmTemp.equals("defaultAlg")) {
					algorithm = getDefaultAlgorithm();
					app = new LocationPrivacyApplication(uid, name, status,
							algorithm, true);
				} else {
					algorithm = getAlgorithm(algorithmTemp);
					algorithm.setConfiguration(getConfiguration(uid));
					app = new LocationPrivacyApplication(uid, name, status,
							algorithm, false);
				}
				list.add(app);
			}
			cursor.move(1);
		}
		cursor.close();
		return list;
	}

	/**
	 * Returns LocationPrivacyConfiguration of an app (uid)
	 * 
	 * @param uid
	 *            uid app is running as
	 * @return LocationPrivacyConfiguration
	 */
	public LocationPrivacyConfiguration getConfiguration(String uid) {
		Cursor integers = database.query("INTEGERVALUES", null, "app = ?",
				new String[] { uid }, null, null, null);
		integers.moveToFirst();
		HashMap<String, Integer> integerValues = new HashMap<String, Integer>();
		while (!integers.isAfterLast()) {
			integerValues.put(integers.getString(0), integers.getInt(1));
			integers.move(1);
		}
		integers.close();
		Cursor doubles = database.query("DOUBLEVALUES", null, "app = ?",
				new String[] { uid }, null, null, null);
		doubles.moveToFirst();
		HashMap<String, Double> doubleValues = new HashMap<String, Double>();
		while (!doubles.isAfterLast()) {
			doubleValues.put(doubles.getString(0), doubles.getDouble(1));
			doubles.move(1);
		}
		doubles.close();
		Cursor strings = database.query("STRINGVALUES", null, "app = ?",
				new String[] { uid }, null, null, null);
		strings.moveToFirst();
		HashMap<String, String> stringValues = new HashMap<String, String>();
		while (!strings.isAfterLast()) {
			stringValues.put(strings.getString(0), strings.getString(1));
			strings.move(1);
		}
		strings.close();
		Cursor enums = database.query("ENUMVALUES", null, "app = ?",
				new String[] { uid }, null, null, null);
		enums.moveToFirst();
		HashMap<String, String> enumChoose = new HashMap<String, String>();
		HashMap<String, ArrayList<String>> enumValues = new HashMap<String, ArrayList<String>>();
		while (!enums.isAfterLast()) {
			String key = enums.getString(0);
			String choosen = enums.getString(1);
			enumChoose.put(key, choosen);
			Cursor listValues = database.query("ENUMENTRY",
					new String[] { "value" }, "app = ? and enumkey = ?",
					new String[] { uid, key }, null, null, null);
			ArrayList<String> enumList = new ArrayList<String>();
			listValues.moveToFirst();
			while (!listValues.isAfterLast()) {
				enumList.add(listValues.getString(0));
				listValues.move(1);
			}
			enumValues.put(key, enumList);
			enums.move(1);
			listValues.close();
		}
		enums.close();
		Cursor coordinates = database.query("COORDINATEVALUES", null,
				"app = ?", new String[] { uid }, null, null, null);
		coordinates.moveToFirst();
		HashMap<String, Coordinate> coordinateValues = new HashMap<String, Coordinate>();
		while (!coordinates.isAfterLast()) {
			String key = coordinates.getString(0);
			double longitude = coordinates.getDouble(1);
			double latitude = coordinates.getDouble(2);
			double altitude = coordinates.getDouble(3);
			coordinateValues.put(key, new Coordinate(longitude, latitude,
					altitude));
			coordinates.move(1);
		}
		coordinates.close();
		Cursor cBooleanValue = database.query("BOOLEANVALUES", null, "app = ?",
				new String[] { uid }, null, null, null);
		cBooleanValue.moveToFirst();
		HashMap<String, Boolean> booleanValues = new HashMap<String, Boolean>();
		while (!cBooleanValue.isAfterLast()) {
			String key = cBooleanValue.getString(0);
			boolean value = Boolean.parseBoolean(cBooleanValue.getString(1));
			booleanValues.put(key, value);
			cBooleanValue.move(1);
		}
		cBooleanValue.close();
		LocationPrivacyConfiguration config = new LocationPrivacyConfiguration(
				integerValues, doubleValues, stringValues, enumValues,
				enumChoose, coordinateValues, booleanValues);
		return config;
	}

	/**
	 * Returns the default obfuscation algorithm
	 * 
	 * @return AbstractLocationPrivacyAlgorithm select by user as default
	 */
	public AbstractLocationPrivacyAlgorithm getDefaultAlgorithm() {
		Cursor cDefaultAlgorithm = database.query("GENRALCONFIGURATION", null,
				"configkey = ?", new String[] { "defaultAlgorithm" }, null,
				null, null);
		cDefaultAlgorithm.moveToFirst();
		AbstractLocationPrivacyAlgorithm defaultAlgorithm = getAlgorithm(cDefaultAlgorithm
				.getString(1));
		defaultAlgorithm.setConfiguration(getConfiguration("defaultApp"));
		cDefaultAlgorithm.close();
		return defaultAlgorithm;
	}

	/**
	 * Returns location privacy framework state (enaled/disabled)
	 * 
	 * @return state of location privacy framework
	 */
	public boolean getStatus() {
		Cursor cStatus = database.query("GENRALCONFIGURATION", null,
				"configkey = ?", new String[] { "status" }, null, null, null);
		cStatus.moveToFirst();
		Boolean status = Boolean.parseBoolean(cStatus.getString(1));
		cStatus.close();
		return status;
	}

	/**
	 * Adds all obfuscation algorithms to the framework
	 */
	private void initialize() {
		algorithms = new HashMap<String, AbstractLocationPrivacyAlgorithm>();
		addAlgorithm(new Deactivate());
		addAlgorithm(new FixedPosition());
		addAlgorithm(new Radius());
		addAlgorithm(new RadiusDistance());
		addAlgorithm(new GeoReverseGeo());
		addAlgorithm(new Webservice());
	}

	/**
     * Removes apps/configuration that have been deinstalled
	 */
	public void removeOldApplications() {

		List<ApplicationInfo> packages = context.getPackageManager()
				.getInstalledApplications(0);
		List<LocationPrivacyApplication> apps = getApplications();
		List<LocationPrivacyApplication> removedApps = new ArrayList<LocationPrivacyApplication>();
		for (LocationPrivacyApplication app : apps) {
			boolean remove = true;
			for (ApplicationInfo packageInfo : packages) {
				if (("" + packageInfo.uid).equals(app.getUid())) {
					remove = false;
					break;
				}
			}
			if (remove) {
				removedApps.add(app);
			}
		}
		database.beginTransaction();
		try {
			for (LocationPrivacyApplication app : removedApps) {
				String uid = app.getUid();
				database.delete("INTEGERVALUES", "app = ?",
						new String[] { uid });
				database.delete("DOUBLEVALUES", "app = ?", new String[] { uid });
				database.delete("STRINGVALUES", "app = ?", new String[] { uid });
				database.delete("ENUMVALUES", "app = ?", new String[] { uid });
				database.delete("ENUMENTRY", "app = ?", new String[] { uid });
				database.delete("COORDINATEVALUES", "app = ?",
						new String[] { uid });
				database.delete("BOOLEANVALUES", "app = ?",
						new String[] { uid });
				database.delete("APPLICATION", "uid = ?", new String[] { uid });
			}
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		dataChanged();
	}

	/**
	 * Updates a LocationPrivacyApplication
	 * 
	 * @param app
	 *            LocationPrivacyApplication to be updated
	 */
	public void setApplication(LocationPrivacyApplication app) {
		ContentValues values = new ContentValues();
		values.put("name", app.getName());
		values.put("status", "" + app.isEnabled());
		if (app.isDefaultAlgorithm()) {
			values.put("algorithm", "defaultAlg");
		} else {
			values.put("algorithm", app.getAlgorithm().getName());
		}
		database.beginTransaction();
		try {
			database.update("APPLICATION", values, "uid = ?",
					new String[] { app.getUid() });
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		setConfiguration(app.getUid(), app.getAlgorithm().getConfiguration(),
				app.isDefaultAlgorithm());
	}

	/**
	 * Updates a LocationPrivacyConfiguration
	 * 
	 * @param uid
	 *            uid of app corresponding to LocationPrivacyConfiguration
	 * @param config
	 *            updated LocationPrivacyConfiguration
	 * @param defaultAlgorithm
	 *            is this configuration of default algorithm?
	 */
	public void setConfiguration(String uid,
			LocationPrivacyConfiguration config, boolean defaultAlgorithm) {

		database.delete("INTEGERVALUES", "app = ?", new String[] { uid });
		database.delete("DOUBLEVALUES", "app = ?", new String[] { uid });
		database.delete("STRINGVALUES", "app = ?", new String[] { uid });
		database.delete("ENUMVALUES", "app = ?", new String[] { uid });
		database.delete("ENUMENTRY", "app = ?", new String[] { uid });
		database.delete("COORDINATEVALUES", "app = ?", new String[] { uid });
		database.delete("BOOLEANVALUES", "app = ?", new String[] { uid });

		if (!defaultAlgorithm) {
			Map<String, Integer> intValues = config.getIntValues();
			Map<String, Double> doubleValues = config.getDoubleValues();
			Map<String, String> stringValues = config.getStringValues();
			Map<String, ArrayList<String>> enumValues = config.getEnumValues();
			Map<String, String> enumChoosen = config.getEnumChoosen();
			Map<String, Coordinate> coordinateValues = config
					.getCoordinateValues();
			Map<String, Boolean> booleanValues = config.getBooleanValues();

			database.beginTransaction();
			try {
				for (String key : intValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("intkey", key);
					values.put("value", intValues.get(key));
					database.insert("INTEGERVALUES", null, values);
				}

				for (String key : doubleValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("doublekey", key);
					values.put("value", doubleValues.get(key));
					database.insert("DOUBLEVALUES", null, values);
				}

				for (String key : stringValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("stringkey", key);
					values.put("value", stringValues.get(key));
					database.insert("STRINGVALUES", null, values);
				}
				for (String key : enumValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("enumkey", key);
					values.put("choosen", enumChoosen.get(key));
					database.insert("ENUMVALUES", null, values);
					ArrayList<String> valueList = enumValues.get(key);
					for (String string : valueList) {
						ContentValues values2 = new ContentValues();
						values2.put("app", uid);
						values2.put("enumkey", key);
						values2.put("value", string);
						database.insert("ENUMENTRY", null, values2);
					}
				}
				for (String key : coordinateValues.keySet()) {
					Coordinate coord = coordinateValues.get(key);
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("coordinatekey", key);
					values.put("longitude", coord.getLongitude());
					values.put("latitude", coord.getLatitude());
					values.put("altitude", coord.getAltitude());
					database.insert("COORDINATEVALUES", null, values);
				}
				for (String key : booleanValues.keySet()) {
					ContentValues values = new ContentValues();
					values.put("app", uid);
					values.put("booleankey", key);
					values.put("value", "" + booleanValues.get(key));
					database.insert("BOOLEANVALUES", null, values);
				}
				database.setTransactionSuccessful();

			} finally {
				database.endTransaction();
			}
		}
		dataChanged();
	}

	/**
	 * Updates default obfuscation algorithm
	 * 
	 * @param algorithmName
	 *            name of default algorithm
	 * @param config
	 *            LocationPrivacyConfiguration of default algorithm
	 */
	public void setDefaultAlgorithm(String algorithmName,
			LocationPrivacyConfiguration config) {
		ContentValues values = new ContentValues();
		values.put("value", "" + algorithmName);
		database.update("GENRALCONFIGURATION", values, "configkey = ?",
				new String[] { "defaultAlgorithm" });
		setConfiguration("defaultApp", config, false);
		Log.i("LPManager", "Framework defaultAlgorithm = " + algorithmName);
		dataChanged();
	}

	/**
	 * Updates state of location privacy framework
	 * 
	 * @param on
	 *            new framework state
	 */
	public void setStatus(boolean on) {
		ContentValues values = new ContentValues();
		values.put("value", "" + on);
		database.update("GENRALCONFIGURATION", values, "configkey = ?",
				new String[] { "status" });
		dataChanged();
		Log.i("LPManager", "Framework status = " + on);
	}

    /**
     * Send broadcast on data change for updating configurations
     */
	private void dataChanged() {
		context.sendBroadcast(new Intent(
				"com.android.server.LocationManagerService.locationprivacy"));
	}

	/**
	 * Cleans cached data of LocationPrivacyApplication re-reads it from database
	 */
	public void updateData() {
		applications = new ArrayList<LocationPrivacyApplication>();
		status = getStatus();
	}
}
