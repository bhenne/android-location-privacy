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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.locationprivacy.control.LocationPrivacyManager;
import android.locationprivacy.model.AbstractLocationPrivacyAlgorithm;
import android.locationprivacy.model.Coordinate;
import android.locationprivacy.model.LocationPrivacyConfiguration;
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
import android.widget.EditText;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/**
 * LocationPrivacyDefaultSettings provides the GUI for selecting the 
 * default algorithm and its configuration.
 *
 * @author Christian Kater
 * 
 */
public class LocationPrivacyDefaultSettings extends SettingsPreferenceFragment
		implements OnPreferenceChangeListener, OnPreferenceClickListener {

	private AbstractLocationPrivacyAlgorithm algorithm;
	private LocationPrivacyConfiguration config;
	private LocationPrivacyManager lpManager;
	private List<String> lpAlgorithms;
	private HashMap<Preference, String[]> prefToKey;
	private ListPreference chooseAlgorithm;
	private Resources res;

	/**
	 * Creates the Preference objects
	 *
	 * @return viewed PreferenceScreen
	 */
	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceScreen();
		if (root != null) {
			root.removeAll();
		}
		addPreferencesFromResource(R.xml.lp_app);
		root = getPreferenceScreen();
		CharSequence[] entries = new CharSequence[lpAlgorithms.size()];
		CharSequence[] values = new CharSequence[lpAlgorithms.size()];
		for (int i = 0; i < lpAlgorithms.size(); i++) {
			String text = "lp_" + lpAlgorithms.get(i);
			int resIdText = res.getIdentifier(text, "string",
					"com.android.settings");
			entries[i] = res.getText(resIdText);
			values[i] = lpAlgorithms.get(i);
		}
		chooseAlgorithm = new ListPreference(getActivity());
		chooseAlgorithm.setEntries(entries);
		chooseAlgorithm.setEntryValues(values);
		chooseAlgorithm.setTitle(R.string.lp_algo);
		chooseAlgorithm.setSummary(R.string.lp_algo_summary);
		chooseAlgorithm.setDefaultValue(algorithm.getName());
		chooseAlgorithm.setOnPreferenceChangeListener(this);
		root.addPreference(chooseAlgorithm);

		PreferenceCategory configurationCategory = new PreferenceCategory(
				getActivity());
		// toDO R.string
		configurationCategory.setTitle(R.string.lp_configuration);
		root.addPreference(configurationCategory);
		Map<String, Integer> intValues = config.getIntValues();
		for (String key : intValues.keySet()) {
			if (!key.startsWith("private_")) {
				root.addPreference(getIntegerPreference(key, intValues.get(key)));
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
				root.addPreference(getEnumPreference(key, enumValues.get(key),
						enumChoosen.get(key)));
			}
		}

		Map<String, Boolean> booleanValues = config.getBooleanValues();
		for (String key : booleanValues.keySet()) {
			if (!key.startsWith("private_")) {
				root.addPreference(getBooleanPreference(key,
						booleanValues.get(key)));
			}
		}
		Map<String, Coordinate> coordinateValues = config.getCoordinateValues();
		for (String key : coordinateValues.keySet()) {
			if (!key.startsWith("private_")) {
				root.addPreference(getCoordinatePreference(key,
						coordinateValues.get(key)));
			}
		}

		return root;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onStart()
	 */
	public void onStart() {
		super.onStart();
		res = getActivity().getResources();
		lpManager = new LocationPrivacyManager(getActivity());
		lpAlgorithms = LocationPrivacyManager.getAllAlgorithm();
		lpAlgorithms.remove("default");
		algorithm = lpManager.getDefaultAlgorithm();
		config = algorithm.getConfiguration();
		prefToKey = new HashMap<Preference, String[]>();
		getActivity().getActionBar().setTitle(R.string.lp_defaultalgo);
		createPreferenceHierarchy();
	}


	@Override
	public void onDestroy() {
		lpManager = null;
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onResume()
	 */
	public void onResume() {
		super.onResume();
		lpAlgorithms = LocationPrivacyManager.getAllAlgorithm();
		lpAlgorithms.remove("default");
		prefToKey = new HashMap<Preference, String[]>();
		createPreferenceHierarchy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange
	 * (android.preference.Preference, java.lang.Object)
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == chooseAlgorithm) {
			String algorithmName = (String) newValue;
			AbstractLocationPrivacyAlgorithm newAlgorithm = LocationPrivacyManager
					.getAlgorithm(algorithmName);
			config = newAlgorithm.getConfiguration();
			algorithm = newAlgorithm;
			lpManager.setDefaultAlgorithm(algorithmName, config);
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
			algorithm.setConfiguration(config);
			lpManager.setDefaultAlgorithm(algorithm.getName(), config);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.preference.Preference.OnPreferenceClickListener#onPreferenceClick
	 * (android.preference.Preference)
	 */
	@Override
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == 1) {
				Coordinate newCoordinate = data
						.getParcelableExtra("coordinate");
				String key = data.getStringExtra("key");
				config.setCoordinate(key, newCoordinate);
				algorithm.setConfiguration(config);
				lpManager.setDefaultAlgorithm(algorithm.getName(), config);
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
		String title = "lp_" + algorithm.getName() + "_" + key;
		return res.getIdentifier(title, "string", "com.android.settings");
	}

	/**
	 * Returns resource id for preference description by key
	 * 
	 * @param key
	 *            key of configuration parameter
	 * @return resource id
	 */
	private int getSummaryId(String key) {
		String summary = "lp_" + algorithm.getName() + "_" + key + "_summary";
		return res.getIdentifier(summary, "string", "com.android.settings");
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
			String enumValueText = "lp_" + algorithm.getName() + "_" + key
					+ "_" + values.get(i);
			int resIDEnumValue = res.getIdentifier(enumValueText, "string",
					"com.android.settings");
			eEntries[i] = res.getText(resIDEnumValue);
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
		coordPref.setTitle(getTitleId(key));
		coordPref.setSummary(getSummaryId(key));
		coordPref.setOnPreferenceClickListener(this);

		setPreferenceExtras(coordPref, key, "coordinate");
		return coordPref;
	}
}
