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

package android.locationprivacy.algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Build;
import android.os.Parcel;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

/**
 * The algorithm Webservice makes a call to an obfuscation service on the Web.
 * Service is requested via HTTPS connection and authenticated via HTTP Basic Auth.
 * The service returns the obfuscated location as JSON data, which is parsed and
 * finally returned by the algorithm.
 *
 * @author Christian Kater
 *
 */
public class Webservice extends AbstractLocationPrivacyAlgorithm {

	/** The Constant NAME. */
	private static final String NAME = "webservice";

	/**
	 * Creates new instance of Webservice
	 */
	public Webservice() {
		super(NAME);
	}

	/**
	 * Creates new instance of Webservice
	 * 
	 * @param in
	 *            Parcel object containing the configuration of the algorithm
	 */
	private Webservice(Parcel in) {
		super(in, NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#newInstance
	 * ()
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new Webservice();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * getDefaultConfiguration()
	 */
	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		Map<String, String> stringValues = new HashMap<String, String>();
		stringValues.put("host",
				"https://studserv.dcsec.uni-hannover.de:8443/authed/json");
		stringValues.put("username", "Test");
		stringValues.put("secret_password", "geheim");
		return new LocationPrivacyConfiguration(new HashMap<String, Integer>(),
				new HashMap<String, Double>(), stringValues,
				new HashMap<String, ArrayList<String>>(),
				new HashMap<String, String>(),
				new HashMap<String, Coordinate>(),
				new HashMap<String, Boolean>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * obfuscate(android.location.Location)
	 */
	@Override
	public Location obfuscate(Location location) {
        // We do it this way to run network connection in main thread. This
        // way is not the normal one and does not comply to best practices,
        // but the main thread must wait for the obfuscation service reply anyway.
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		final String HOST_ADDRESS = configuration.getString("host");
		String username = configuration.getString("username");
		String password = configuration.getString("secret_password");

		Location newLoc = new Location(location);
		double lat = location.getLatitude();
		double lon = location.getLongitude();

		String urlString = HOST_ADDRESS;
		urlString += "?lat=" + lat;
		urlString += "&lon=" + lon;
		URL url;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Error: could not build URL");
			Log.e(TAG, e.getMessage());
			return null;
		}
		HttpsURLConnection connection = null;
		JSONObject json = null;
		InputStream is = null;
		try {
			connection = (HttpsURLConnection) url.openConnection();
			connection
					.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			connection.setRequestProperty(
					"Authorization",
					"Basic "
							+ Base64.encodeToString(
									(username + ":" + password).getBytes(),
									Base64.NO_WRAP));
			is = connection.getInputStream();

		} catch (IOException e) {
			Log.e(TAG, "Error while connectiong to " + url.toString());
			Log.e(TAG, e.getMessage());
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			String line = reader.readLine();
			System.out.println("Line " + line);
			json = new JSONObject(line);
			newLoc.setLatitude(json.getDouble("lat"));
			newLoc.setLongitude(json.getDouble("lon"));
		} catch (IOException e) {
			Log.e(TAG, "Error: could not read from BufferedReader");
			Log.e(TAG, e.getMessage());
			return null;
		} catch (JSONException e) {
			Log.e(TAG, "Error: could not read from JSON");
			Log.e(TAG, e.getMessage());
			return null;
		}
		connection.disconnect();
		return newLoc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * instanceFromParcel(android.os.Parcel)
	 */
	@Override
	protected AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new Webservice(in);
	}

}
