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

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * LocationPrivacySettings provides GUI of main configuration dialog
 * of location privacy framework
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacySettings extends SettingsPreferenceFragment
		implements OnPreferenceClickListener {

	private Preference defaultAlgorithm;
	private LocationPrivacyManager lpManager;
	private List<LocationPrivacyApplication> applications;
	private Switch actionBarSwitch;
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			lpManager.setStatus(actionBarSwitch.isChecked());
			createPreferenceHierarchy();
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onStart()
	 */
	public void onStart() {
		super.onStart();
		System.out.println("Context Settings : "
				+ getActivity().getDatabasePath("privacy.db"));
		lpManager = new LocationPrivacyManager(getActivity());
		lpManager.removeOldApplications();
		applications = lpManager.getApplications();
		Activity activity = getActivity();
		actionBarSwitch = new Switch(activity);
		actionBarSwitch.setOnClickListener(listener);
		actionBarSwitch.setChecked(lpManager.getStatus());

		final int padding = activity.getResources().getDimensionPixelSize(
				R.dimen.action_bar_switch_padding);
		actionBarSwitch.setPadding(0, 0, padding, 0);
		activity.getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		activity.getActionBar().setCustomView(
				actionBarSwitch,
				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
						ActionBar.LayoutParams.WRAP_CONTENT,
						Gravity.CENTER_VERTICAL | Gravity.RIGHT));
		createPreferenceHierarchy();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onStop()
	 */
	public void onStop() {
		lpManager = null;
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onResume()
	 */
	public void onResume() {
		lpManager = new LocationPrivacyManager(getActivity());
		super.onResume();

	}

	/**
	 * Creates the Preference objects for default algorithm and apps configuration.
	 */
	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceScreen();
		if (root != null) {
			root.removeAll();
		}
		root = getPreferenceScreen();
		boolean status = false;
		status = lpManager.getStatus();
		Resources r = getResources();
		addPreferencesFromResource(R.xml.lp_settings);
		root = getPreferenceScreen();
		root.setTitle(getString(R.string.lp_title));
		defaultAlgorithm = new Preference(getActivity());
		defaultAlgorithm.setTitle(getString(R.string.lp_defaultalgo));
		String text = "lp_" + lpManager.getDefaultAlgorithm().getName();
		int resIdText = r.getIdentifier(text, "string", "com.android.settings");
		defaultAlgorithm.setSummary(resIdText);
		defaultAlgorithm.setOnPreferenceClickListener(this);
		defaultAlgorithm.setEnabled(status);

		root.addPreference(defaultAlgorithm);
		PreferenceCategory applicationHeadings = new PreferenceCategory(
				getActivity());
		applicationHeadings
				.setTitle(getString(R.string.lp_application_heading));
		root.addPreference(applicationHeadings);
		for (LocationPrivacyApplication app : applications) {
			LocationPrivacyAppPreference sp = new LocationPrivacyAppPreference(
					getActivity(), app, this);
			root.addPreference(sp);
		}

		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceClickListener#onPreferenceClick
	 * (android.preference.Preference)
	 */
	public boolean onPreferenceClick(Preference preference) {
		startFragment(this, LocationPrivacyDefaultSettings.class.getName(), 0,
				new Bundle());
		return true;
	}

}
