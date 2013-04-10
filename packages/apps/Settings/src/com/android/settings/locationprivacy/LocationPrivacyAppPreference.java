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

package com.android.settings.locationprivacy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.settings.R;

/**
 * LocationPrivacyAppPreference is a modified SwitchPreference. 
 * Its backgroud is overridden with an OnClickListener.
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyAppPreference extends SwitchPreference implements
		OnClickListener, OnPreferenceChangeListener {

	private LocationPrivacyApplication app;
	private LocationPrivacySettings settings;
	private LocationPrivacyManager lpManager;

	/**
	 * Instantiates a new LocationPrivacyAppPreference
	 * 
	 * @param context
	 *            Context the object is running in
	 * @param app
	 *            app that will be displayed
	 * @param settings
	 *            LocationPrivacySettings object the app is displayed in
	 */
	public LocationPrivacyAppPreference(Context context,
			LocationPrivacyApplication app, LocationPrivacySettings settings) {
		super(context);
		this.app = app;
		lpManager = new LocationPrivacyManager(context);
		this.settings = settings;
		this.setOnPreferenceChangeListener(this);
		this.setTitle(app.getName());
		Resources r = context.getResources();
		if (app.isDefaultAlgorithm()) {
			// ToDo R.string
			this.setSummary(R.string.lp_defaultalgo);
		} else {
			String text = "lp_" + app.getAlgorithm().getName();
			int resIdText = r.getIdentifier(text, "string",
					"com.android.settings");
			this.setSummary(resIdText);
		}

		this.setChecked(app.isEnabled());
		this.setEnabled(lpManager.getStatus());
	}

	protected void onBindView(View view) {
		super.onBindView(view);
		view.setOnClickListener(this);
	}

	/**
	 * Acts when background or name of app is clicked.
	 * 
	 * @param v
	 *            View element that have been clicked
	 */
	public void onClick(View v) {
		Bundle extras = new Bundle();
		extras.putParcelable("app", app);
		v.setSelected(true);
		// v.setBackgroundColor(android.R.color.holo_blue_light);
		v.setBackgroundColor(Color.rgb(6, 128, 170));
		settings.startFragment(settings,
				LocationPrivacyAppSetteings.class.getName(), 0, extras);

	}

	/**
	 * Stores change of the switch in the database
     *
	 * @param preference LocationPrivacyAppPreference object
	 * @param newValue new state of switch
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Boolean on = (Boolean) newValue;
		app.setEnabled(on);
		lpManager.setApplication(app);
		return true;
	}

}
