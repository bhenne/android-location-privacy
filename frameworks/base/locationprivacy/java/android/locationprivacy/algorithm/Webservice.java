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

// TODO: Auto-generated Javadoc
/**
 * Die Klasse Webservice baut eine Verbindung zu einem Webservice auf, der über
 * HTTPS zu erreichen ist, als Authentifizierung Hypertext Transfer Protocol
 * (HTTP) Basic benutzt, und die Rückgabe im JavaScript Object Notation (JSON)
 * liefert.
 */
public class Webservice extends AbstractLocationPrivacyAlgorithm {

	/** The Constant NAME. */
	private static final String NAME = "webservice";

	/**
	 * Instanziert eine neues Webservice-Objekt.
	 */
	public Webservice() {
		super(NAME);
	}

	/**
	 * Instanziert eine neues Webservice-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
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
	 * calculateLocation(android.location.Location)
	 */
	@Override
	public Location calculateLocation(Location location) {
		// Notwendig um die Netzwerkverbindung auf den Haupt-Thread laufen zu
		// lassen. Das ist so nicht vorgesehen und eigentlich auch nicht Best
		// Practise, aber der Haupt-Thread müsste sowieso auf die Standortdaten
		// warten
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
