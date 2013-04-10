package android.locationprivacy.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Parcel;
import android.util.Log;

/**
 * GeoReverseGeo führt Reverse-Geocoding durch, um die Adresse des Standorts zu
 * ermitteln. Die Adresse wird auf eine, vom Benutzer festgelegte, Weise
 * ungenauer. Zu dieser ungenaueren Adresse wird ein Geocoding durchgeführt. Der
 * dabei entstandene Standort wird zurückgegeben.
 */
public class GeoReverseGeo extends AbstractLocationPrivacyAlgorithm {

	/** The Constant NAME. */
	private static final String NAME = "georeversegeo";

	/**
	 * Instanziiert eine neues GeoReverseGeo-Objekt.
	 * 
	 */
	public GeoReverseGeo() {
		super(NAME);
	}

	/**
	 * Instanziiert eine neues GeoReverseGeo-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 */
	public GeoReverseGeo(Parcel in) {
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
		return new GeoReverseGeo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * getDefaultConfiguration()
	 */
	@Override
	public LocationPrivacyConfiguration getDefaultConfiguration() {
		HashMap<String, ArrayList<String>> enumValues = new HashMap<String, ArrayList<String>>();
		ArrayList<String> detail = new ArrayList<String>();
		detail.add("street");
		detail.add("postalcode");
		detail.add("city");
		detail.add("country");
		enumValues.put("detail", detail);
		HashMap<String, String> enumChoosen = new HashMap<String, String>();
		enumChoosen.put("detail", "city");
		return new LocationPrivacyConfiguration(new HashMap<String, Integer>(),
				new HashMap<String, Double>(), new HashMap<String, String>(),
				enumValues, enumChoosen, new HashMap<String, Coordinate>(),
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
		String detail = configuration.getEnumChoosen("detail");
		Geocoder geocoder = new Geocoder(context);
		Location newLocation = null;
		List<Address> nextAddressList = null;
		try {
			nextAddressList = geocoder.getFromLocation(location.getLatitude(),
					location.getLongitude(), 1);
		} catch (IOException e) {
			Log.d(TAG, "Error: Could not read from Geocoder");
			Log.d(TAG, e.getMessage());
			return null;
		}
		if (nextAddressList != null && nextAddressList.size() > 0) {
			Address nextAddress = nextAddressList.get(0);
			String addressString = formateAddress(nextAddress, detail);
			List<Address> nextStreetList = null;
			try {
				nextStreetList = geocoder.getFromLocationName(addressString, 1);
			} catch (IOException e) {
				Log.d(TAG, "Error: Could not read from Geocoder");
				Log.d(TAG, e.getMessage());
				return null;
			}
			if (nextAddressList != null && nextAddressList.size() > 0) {
				Address nextModifiedAddress = nextStreetList.get(0);
				newLocation = new Location(location);
				newLocation.setLatitude(nextModifiedAddress.getLatitude());
				newLocation.setLongitude(nextModifiedAddress.getLongitude());
			}
		}
		return newLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.locationprivacy.model.AbstractLocationPrivacyAlgorithm#
	 * instanceFromParcel(android.os.Parcel)
	 */
	@Override
	public AbstractLocationPrivacyAlgorithm instanceFromParcel(Parcel in) {
		return new GeoReverseGeo(in);
	}

	/**
	 * Formate address.
	 * 
	 * @param address
	 *            the address
	 * @param detail
	 *            the detail
	 * @return the string
	 */
	private String formateAddress(Address address, String detail) {
		String postal = address.getPostalCode();
		String street = address.getThoroughfare();
		String city = address.getLocality();
		String country = address.getCountryName();
		String addressString = "";

		if (street != null && detail.equals("street")) {
			addressString += street;
			addressString += ", ";
		}
		if (city != null
				&& (detail.equals("street") || detail.equals("postalcode") || detail
						.equals("city"))) {
			if (postal != null
					&& (detail.equals("street") || detail.equals("postalcode"))) {
				addressString += postal + " ";
			}
			addressString += city + ", ";
		}
		if (country != null) {
			addressString += country;
		} else if (addressString.endsWith(", ")) {
			addressString.substring(0, addressString.length() - 2);
		}
		return addressString;
	}

}
