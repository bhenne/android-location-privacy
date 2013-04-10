package android.locationprivacy.model;

import android.content.Context;
import android.location.Location;
import android.locationprivacy.control.LocationPrivacyManager;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Christian Kater
 *
 * Die abstrakte Klasse AbstractLocationPrivacyAlgorithm dient als Vorlage für
 * die Implementierung von Algorithmen zur Verschleierung des Standortes
 */
public abstract class AbstractLocationPrivacyAlgorithm implements Parcelable {

	/** The Constant CREATOR. */
	public static final Parcelable.Creator<AbstractLocationPrivacyAlgorithm> CREATOR = new Creator<AbstractLocationPrivacyAlgorithm>() {

		@Override
		public AbstractLocationPrivacyAlgorithm[] newArray(int size) {
			return new AbstractLocationPrivacyAlgorithm[size];
		}

		@Override
		public AbstractLocationPrivacyAlgorithm createFromParcel(Parcel source) {
			String name = source.readString();
			return LocationPrivacyManager.getAlgorithm(name)
					.instanceFromParcel(source);
		}
	};

	/** Konfiguration des AbstractLocationPrivacyAlgorithm-Objekt. */
	protected LocationPrivacyConfiguration configuration;

	/** Name der Klasse. */
	protected String name;

	/** Context, in dem das Objekt ausgeführt wird. */
	protected Context context;

	/** Tag für die Verwendung des Logs. */
	protected final String TAG;

	/**
	 * 
	 * Instanziiert eine neues AbstractLocationPrivacyAlgorithm-Objekt.
	 * 
	 * @param name
	 *            Name der Klasse innerhalb des Location-Privacy-Framework
	 */
	protected AbstractLocationPrivacyAlgorithm(String name) {
		this.name = name;
		this.TAG = "LP_" + name;
		setConfiguration(getDefaultConfiguration());
	}

	/**
	 * Instanziiert eine neues AbstractLocationPrivacyAlgorithm-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 * @param name
	 *            Name der Klasse innerhalb des Location-Privacy-Framework
	 */
	public AbstractLocationPrivacyAlgorithm(Parcel in, String name) {
		this.name = name;
		this.TAG = "LP_" + name;
		configuration = new LocationPrivacyConfiguration(in);
	}

	public String getName() {
		return name;
	}

	public LocationPrivacyConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(LocationPrivacyConfiguration configuration) {
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractLocationPrivacyAlgorithm other = (AbstractLocationPrivacyAlgorithm) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		configuration.writeToParcel(dest, flags);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Sets the context.
	 * 
	 * @param context
	 *            the new context
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Gibt eine neue Instanz der Klasse zurück
	 * 
	 * @return Instanz der AbstractLocationPrivacyAlgorithm-Klasse
	 */
	public abstract AbstractLocationPrivacyAlgorithm newInstance();

	/**
	 * Gibt die Standardkonfiguration zurück. Diese wird dem
	 * AbstractLocationPrivacyAlgorithm-Objekt zugeordnet, wenn es neu
	 * Instanziiert wird. Die in dieser Konfiguration definierten Parameter wird
	 * dem Benutzer in der Oberfläche angezeigt.
	 * 
	 * @return Standardkonfiguration
	 */
	public abstract LocationPrivacyConfiguration getDefaultConfiguration();

	/**
	 * Verschleiert die übergebenen Standortdaten.
	 * 
	 * @param location
	 *            Standort des Endgerätes
	 * @return Verschleierter Standort
	 */
	public abstract Location calculateLocation(Location location);

	/**
	 * Gibt eine neue Instanz der Klasse zurück.
	 * 
	 * @param in
	 *            Parcel-Objekt, dass die Konfiguration des Algorithmus enthält.
	 * @return Neue Instanz der Klasse.
	 */
	protected abstract AbstractLocationPrivacyAlgorithm instanceFromParcel(
			Parcel in);
}
