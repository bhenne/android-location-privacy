package android.locationprivacy.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Die Klasse LocationPrivacyApplication modeliert eine Anwendung innerhalb des
 * Location-Privacy-Frameworks.
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyApplication implements Parcelable {

	/** User-Id der Anwendung */
	private String uid;
	/** Name der Anwendung */
	private String name;
	/**Information, ob das Location-Privacy-Framework für diese Anwendung aktiviert ist */
	private boolean enabled;
	/** Das verwendete AbstractLocationPrivacyAlgorithm-Objekt für die Anwendung */
	private AbstractLocationPrivacyAlgorithm algorithm;
	/**Information, ob das AbstractLocationPrivacyAlgorithm-Objekt der Standardalgorithmus ist */
	private boolean defaultAlgorithm;

	public static final Parcelable.Creator<LocationPrivacyApplication> CREATOR = new Creator<LocationPrivacyApplication>() {

		@Override
		public LocationPrivacyApplication createFromParcel(Parcel source) {
			return new LocationPrivacyApplication(source);
		}

		@Override
		public LocationPrivacyApplication[] newArray(int size) {
			return new LocationPrivacyApplication[size];
		}
	};

	/**
	 * Instanziert eine neues LocationPrivacyApplication-Objekt.
	 * 
	 * @param in
	 *            Parcel-Objekt mit Attributen des
	 *            LocationPrivacyApplication-Objekt
	 */
	public LocationPrivacyApplication(Parcel in) {
		uid = in.readString();
		name = in.readString();
		enabled = Boolean.parseBoolean(in.readString());
		algorithm = AbstractLocationPrivacyAlgorithm.CREATOR
				.createFromParcel(in);
		defaultAlgorithm = Boolean.parseBoolean(in.readString());
	}

	/**
	 * Instanziert eine neues LocationPrivacyApplication-Objekt.
	 * 
	 * @param uid
	 *            User-Id der Anwednung
	 * @param name
	 *            the Name der Anwendung
	 * @param status
	 *            the Ist das Location-Privacy-Framework für diese Anwendung
	 *            aktiviert
	 * @param algorithm
	 *            Algorithmus zur Verschleierung des Standortes
	 * @param defaultAlgorithm
	 *            Verwendet die Anwendung den Standardalgorithmus
	 */
	public LocationPrivacyApplication(String uid, String name, boolean status,
			AbstractLocationPrivacyAlgorithm algorithm, boolean defaultAlgorithm) {
		super();
		this.setUid(uid);
		this.name = name;
		this.enabled = status;
		this.algorithm = algorithm;
		this.setDefaultAlgorithm(defaultAlgorithm);
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
		LocationPrivacyApplication other = (LocationPrivacyApplication) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (defaultAlgorithm != other.defaultAlgorithm)
			return false;
		if (enabled != other.enabled)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	public AbstractLocationPrivacyAlgorithm getAlgorithm() {
		return algorithm;
	}

	public String getName() {
		return name;
	}

	public String getUid() {
		return uid;
	}

	public boolean isDefaultAlgorithm() {
		return defaultAlgorithm;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setAlgorithm(AbstractLocationPrivacyAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void setDefaultAlgorithm(boolean defaultAlgorithm) {
		this.defaultAlgorithm = defaultAlgorithm;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return uid + " ; " + name + " ; " + enabled + " ; "
				+ algorithm.getName() + " ; " + defaultAlgorithm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uid);
		dest.writeString(name);
		dest.writeString("" + enabled);
		algorithm.writeToParcel(dest, flags);
		dest.writeString("" + defaultAlgorithm);

	}

}
