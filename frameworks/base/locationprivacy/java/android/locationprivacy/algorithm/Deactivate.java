package android.locationprivacy.algorithm;

import java.util.ArrayList;
import java.util.HashMap;

import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;

/**
 * Das Verfahren Deactivate deaktiviert den Standortzugriff. 
 */
public class Deactivate extends AbstractLocationPrivacyAlgorithm {
	
	private static final String NAME = "deactivate";

	/**
	 * Instanziiert eine neues Deactivate-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 */
	public Deactivate(Parcel in) {
		super(in, NAME);
	}
	
	/**
	 * Instanziiert eine neues Deactivate-Objekt.
	 */
	public Deactivate() {
		super(NAME);
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#newInstance()
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm newInstance() {
		return new Deactivate();
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#getDefaultConfiguration()
	 */
	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		return new LocationPrivacyConfiguration(new HashMap<String, Integer>(),
				new HashMap<String, Double>(), new HashMap<String, String>(),
				new HashMap<String, ArrayList<String>>(),
				new HashMap<String, String>(), new HashMap<String, Coordinate>(),
				new HashMap<String, Boolean>());
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#calculateLocation(android.location.Location)
	 */
	@Override
	public Location calculateLocation(Location location) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#instanceFromParcel(android.os.Parcel)
	 */
	@Override
	protected AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new Deactivate(in);
	}

}
