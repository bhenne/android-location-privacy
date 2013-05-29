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

package android.locationprivacy.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * LocationPrivacyApplication models an app in the location privacy framework
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyApplication implements Parcelable {

	/** uid an app is running as */
	private String uid;
	/** app name */
	private String name;
	/** Is location obfuscation enabled for this app? */
	private boolean enabled;
	/** Used AbstractLocationPrivacyAlgorithm object for this app */
	private AbstractLocationPrivacyAlgorithm algorithm;
	/** Is used AbstractLocationPrivacyAlgorithm the default one? */
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
	 * Creates new instance of LocationPrivacyApplication
	 * 
	 * @param in
	 *            Parcel object with attributes for LocationPrivacyApplication object
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
	 * Creates new instance of LocationPrivacyApplication
	 * 
	 * @param uid
	 *            uid an app is running as
	 * @param name
	 *            the app name
	 * @param status
	 *            obfuscation state of this app (enabled or not)
	 * @param algorithm
	 *            selected obfuscation algorithm
	 * @param defaultAlgorithm
	 *            does app use default algorithm?
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
