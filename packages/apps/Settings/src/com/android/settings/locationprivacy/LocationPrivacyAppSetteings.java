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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyApplication;
import android.locationprivacy.model.LocationPrivacyConfiguration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * LocationPrivacyAppSettings implements the GUI for the configuration
 * of an app's obfuscation algorithm and configuration
 * 
 * @author Christian Kater
 * 
 */
public class LocationPrivacyAppSetteings extends SettingsPreferenceFragment
		implements OnPreferenceChangeListener, OnClickListener,
		OnPreferenceClickListener {
	private Resources r;

	private LocationPrivacyApplication app;
	private LocationPrivacyConfiguration config;
	private LocationPrivacyManager lpManager;
	private List<String> lpAlgorithm;
	/*
	 * Assignment of Preference and name and data type
	 */
	private HashMap<Preference, String[]> prefToKey;
	private ListPreference algorithm;

	/**
	 * Provides the Preference objects for selecting the
	 * AbstractLocationPrivacyAlgorithm and parameter values of
	 * LocationPrivacyConfiguration
	 */
	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceScreen();
		if (root != null) {
			root.removeAll();
		}
		addPreferencesFromResource(R.xml.lp_app);
		root = getPreferenceScreen();

		CharSequence[] entries = new CharSequence[lpAlgorithm.size()];
		CharSequence[] values = new CharSequence[lpAlgorithm.size()];
		r = getActivity().getResources();
		for (int i = 0; i < lpAlgorithm.size(); i++) {
			String text = "lp_" + lpAlgorithm.get(i);
			int resIdText = r.getIdentifier(text, "string",
					"com.android.settings");
			entries[i] = r.getText(resIdText);
			values[i] = lpAlgorithm.get(i);
		}
		algorithm = new ListPreference(getActivity());
		algorithm.setEntries(entries);
		algorithm.setEntryValues(values);
		algorithm.setTitle(R.string.lp_algo);
		algorithm.setSummary(R.string.lp_algo_summary);
		algorithm.setOnPreferenceChangeListener(this);
		algorithm.setEnabled(app.isEnabled());
		if (app.isDefaultAlgorithm()) {
			algorithm.setDefaultValue("default");
		} else {
			algorithm.setDefaultValue(app.getAlgorithm().getName());
		}

		root.addPreference(algorithm);

		if (!app.isDefaultAlgorithm()) {
			PreferenceCategory configurationCategory = new PreferenceCategory(
					getActivity());
			configurationCategory.setTitle(R.string.lp_configuration);
			root.addPreference(configurationCategory);

			Map<String, Integer> intValues = config.getIntValues();
			for (String key : intValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getIntegerPreference(key,
							intValues.get(key)));
				}
			}

			Map<String, Double> doubleValues = config.getDoubleValues();
			for (String key : doubleValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getDoublePreference(key,
							doubleValues.get(key)));
				}
			}

			Map<String, String> stringValues = config.getStringValues();
			for (String key : stringValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getStringPreference(key,
							stringValues.get(key)));
				}
			}

			Map<String, ArrayList<String>> enumValues = config.getEnumValues();
			Map<String, String> enumChoosen = config.getEnumChoosen();
			for (String key : enumValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getEnumPreference(key,
							enumValues.get(key), enumChoosen.get(key)));
				}
			}

			Map<String, Boolean> booleanValues = config.getBooleanValues();
			for (String key : booleanValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getBooleanPreference(key,
							booleanValues.get(key)));
				}
			}
			Map<String, Coordinate> coordinateValues = config
					.getCoordinateValues();
			for (String key : coordinateValues.keySet()) {
				if (!key.startsWith("private_")) {
					root.addPreference(getCoordinatePreference(key,
							coordinateValues.get(key)));
				}
			}

		}
		return root;

	}

	/**
	 * @see SettingsPreferenceFragment#onStart()
	 * 
	 *      Creates the settings UI and the switch at the top
	 */
	public void onStart() {
		super.onStart();
		lpAlgorithm = LocationPrivacyManager.getAllAlgorithm();
		Bundle extras = getArguments();
		app = (LocationPrivacyApplication) extras.getParcelable("app");
		config = app.getAlgorithm().getConfiguration();
		prefToKey = new HashMap<Preference, String[]>();
		Activity activity = getActivity();
		lpManager = new LocationPrivacyManager(getActivity());
		Switch actionBarSwitch = new Switch(activity);
		actionBarSwitch.setOnClickListener(this);
		actionBarSwitch.setChecked(app.isEnabled());

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
		activity.getActionBar().setTitle(app.getName());
		createPreferenceHierarchy();
	}

	@Override
	public void onStop() {
		lpManager.close();
		lpManager = null;
		super.onStop();
	}

	@Override
	public void onResume() {
		lpManager = new LocationPrivacyManager(getActivity());
		super.onResume();
	}

	/**
     * If a parameter value changes, this method is called. Using the
     * LocationPrivacyManager, the new values are stored in the database
	 * 
	 * @param preference
	 *            changed preference object
	 * @param newValue
	 *            new value of that preference
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == algorithm) {
			String algorithmName = (String) newValue;
			System.out.println("preference == algorithm and newValue = "
					+ algorithmName);
			if (algorithmName.equals("default")) {
				app.setDefaultAlgorithm(true);
				System.out.println("app.setDefault(true)");
				app.setAlgorithm(lpManager.getDefaultAlgorithm());
			} else {
				app.setDefaultAlgorithm(false);
				app.setAlgorithm(LocationPrivacyManager
						.getAlgorithm(algorithmName));
			}
			lpManager.setApplication(app);
			System.out.println(app);
			config = app.getAlgorithm().getConfiguration();
			prefToKey = new HashMap<Preference, String[]>();
			createPreferenceHierarchy();
		} else {
			String[] extras = prefToKey.get(preference);
			String type = extras[1];
			String key = extras[0];
			System.out.println("Type " + type);
			System.out.println("Key " + key);
			System.out.println("new Value" + newValue);

			if (type.equals("int")) {
				config.setInt(key, Integer.parseInt((String) newValue));
			} else if (type.equals("double")) {
				config.setDouble(key, Double.parseDouble((String) newValue));
			} else if (type.equals("string")) {
				config.setString(key, (String) newValue);
			} else if (type.equals("enum")) {
				config.setEnumChoosen(key, (String) newValue);
			} else if (type.equals("boolean")) {
				config.setBoolean(key, (Boolean) newValue);
			} else {
				return false;
			}
			AbstractLocationPrivacyAlgorithm algorithm = app.getAlgorithm();
			algorithm.setConfiguration(config);
			app.setAlgorithm(algorithm);
			lpManager.setApplication(app);
		}
		return true;
	}

	/**
	 * Changes the state of the app's obfuscation if clicked the switch
	 */
	public void onClick(View v) {
		app.setEnabled(!app.isEnabled());
		lpManager.setApplication(app);
		createPreferenceHierarchy();
	}

	/**
	 * Starts the Activity for Coordinate selection on map
	 */
	public boolean onPreferenceClick(Preference preference) {
		String[] extras = prefToKey.get(preference);
		String key = extras[0];
		Coordinate coord = config.getCoordinate(key);
		Intent i = new Intent(getActivity(), LocationPrivacyMap.class);
		i.putExtra("key", key);
		i.putExtra("coordinate", coord);
		startActivityForResult(i, 0);
		return true;
	}

	/**
     * Catches return value of MapActivity and stores it in the database
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == 1) {
				Coordinate newCoordinate = data
						.getParcelableExtra("coordinate");
				String key = data.getStringExtra("key");
				config.setCoordinate(key, newCoordinate);
				AbstractLocationPrivacyAlgorithm algorithm = app.getAlgorithm();
				algorithm.setConfiguration(config);
				app.setAlgorithm(algorithm);
				lpManager.setApplication(app);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Returns resource id for preference title by key
	 * 
	 * @param key
	 *            key of configuration parameter
	 * @return resource id
	 */
	private int getTitleId(String key) {
		String title = "lp_" + app.getAlgorithm().getName() + "_" + key;
		return r.getIdentifier(title, "string", "com.android.settings");
	}

	/**
	 * Returns resource id for preference description by key
	 * 
	 * @param key
	 *            key of configuration parameter
	 * @return the resource id
	 */
	private int getSummaryId(String key) {
		String summary = "lp_" + app.getAlgorithm().getName() + "_" + key
				+ "_summary";
		return r.getIdentifier(summary, "string", "com.android.settings");
	}


	/**
	 * Stores assignment of Preference to parameter key and datatype
	 * 
	 * @param pref
	 *            Preference
	 * @param key
	 *            Preference key
	 * @param type
	 *            datatype
	 */
	private void setPreferenceExtras(Preference pref, String key, String type) {
		String[] prefExtras = new String[2];
		prefExtras[0] = key;
		prefExtras[1] = type;
		prefToKey.put(pref, prefExtras);
	}

	/**
	 * Creates a EditTextPreference for Integer parameters.
     * Its title and description are shown.
     * Keyboard layout is limited to integer inputs.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its value
	 * @return EditTextPreference for Integers
	 */
	private EditTextPreference getIntegerPreference(String key, int value) {
		EditTextPreference intETP = new EditTextPreference(getActivity());

		intETP.setOnPreferenceChangeListener(this);
		intETP.setTitle(getTitleId(key));
		intETP.setSummary(getSummaryId(key));
		intETP.setDefaultValue("" + value);
		intETP.setEnabled(app.isEnabled());

		EditText editText = intETP.getEditText();
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
		editText.setRawInputType(Configuration.KEYBOARD_12KEY);
		editText.setKeyListener(DigitsKeyListener.getInstance(true, false));
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(intETP, key, "int");

		return intETP;
	}

	/**
	 * Creates a EditTextPreference for Double parameters.
     * Its title and description are shown.
     * Keyboard layout is limited to double inputs.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its value
	 * @return EditTextPreference for double
	 */
	private EditTextPreference getDoublePreference(String key, double value) {
		EditTextPreference doubleETP = new EditTextPreference(getActivity());

		doubleETP.setOnPreferenceChangeListener(this);
		doubleETP.setTitle(getTitleId(key));
		doubleETP.setSummary(getSummaryId(key));
		doubleETP.setDefaultValue("" + value);
		doubleETP.setEnabled(app.isEnabled());

		EditText editText = doubleETP.getEditText();
		editText.setRawInputType(Configuration.KEYBOARD_12KEY);
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editText.setKeyListener(DigitsKeyListener.getInstance(false, true));
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(doubleETP, key, "double");

		return doubleETP;
	}

	/**
	 * Creates a EditTextPreference for Strings parameters.
     * Its title and description are shown.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its value
	 * @return EditTextPreference for String
	 */
	private EditTextPreference getStringPreference(String key, String value) {
		EditTextPreference stringETP = new EditTextPreference(getActivity());

		stringETP.setOnPreferenceChangeListener(this);
		stringETP.setTitle(getTitleId(key));
		stringETP.setSummary(getSummaryId(key));
		stringETP.setDefaultValue("" + value);
		stringETP.setEnabled(app.isEnabled());

		EditText editText = stringETP.getEditText();
		if (key.startsWith("secret_")) {
			editText.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
		}

		setPreferenceExtras(stringETP, key, "string");

		return stringETP;
	}

	/**
	 * Creates a ListPreference for Enum parameters.
     * Its title and description are shown.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its values
	 * @param choosen
	 *            selected value
	 * @return ListPreference for Enum
	 */
	private ListPreference getEnumPreference(String key, List<String> values,
			String choosen) {
		ListPreference enumLP = new ListPreference(getActivity());

		CharSequence[] eEntries = new CharSequence[values.size()];
		CharSequence[] eValues = new CharSequence[values.size()];
		for (int i = 0; i < values.size(); i++) {
			String enumValueText = "lp_" + app.getAlgorithm().getName() + "_"
					+ key + "_" + values.get(i);
			int resIDEnumValue = r.getIdentifier(enumValueText, "string",
					"com.android.settings");
			eEntries[i] = r.getText(resIDEnumValue);
			eValues[i] = values.get(i);
		}

		enumLP.setOnPreferenceChangeListener(this);
		enumLP.setEntries(eEntries);
		enumLP.setEntryValues(eValues);
		enumLP.setTitle(getTitleId(key));
		enumLP.setSummary(getSummaryId(key));
		enumLP.setPersistent(false);
		enumLP.getEntries();
		enumLP.setDefaultValue(choosen);
		enumLP.setEnabled(app.isEnabled());

		setPreferenceExtras(enumLP, key, "enum");

		return enumLP;
	}

	/**
	 * Creates a CheckboxPreference.
     * Its title and description are shown.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its value
	 * @return CheckBoxPreference for Boolean
	 */
	private CheckBoxPreference getBooleanPreference(String key, boolean value) {
		CheckBoxPreference booleanCBP = new CheckBoxPreference(getActivity());

		booleanCBP.setOnPreferenceChangeListener(this);
		booleanCBP.setChecked(value);
		booleanCBP.setTitle(getTitleId(key));
		booleanCBP.setSummary(getSummaryId(key));
		booleanCBP.setPersistent(false);
		booleanCBP.setEnabled(app.isEnabled());

		setPreferenceExtras(booleanCBP, key, "boolean");

		return booleanCBP;
	}
	
	/**
	 * Creates a Preference for Coordinates.
     * Its title and description are shown.
     * MapActivity is started.
	 *
	 * @param key
	 *            parameter key
	 * @param value
	 *            its value
	 * @return Preference for Coordinates
	 */
	private Preference getCoordinatePreference(String key, Coordinate value) {
		Preference coordPref = new Preference(getActivity());
		coordPref.setEnabled(app.isEnabled());
		coordPref.setTitle(getTitleId(key));
		coordPref.setSummary(getSummaryId(key));
		coordPref.setOnPreferenceClickListener(this);

		setPreferenceExtras(coordPref, key, "coordinate");
		return coordPref;
	}
}
