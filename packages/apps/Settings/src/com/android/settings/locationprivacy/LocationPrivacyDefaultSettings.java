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

// TODO: Auto-generated Javadoc
/**
 * Die Klasse LocationPrivacyDefaultSettings realisiert die GUI für die
 * Zuweisung von des Standardalgorithmus zu AbstractLocationPrivacyAlgorithm und
 * LocationPrivacyConfiguration.
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
	 * Erzeugt die einzelnen Preference-Objekte für die Auswahl des
	 * AbstractLocationPrivacyAlgorithm und der Parameter der
	 * LocationPrivacyConfiguration. Der Bildschirm wird gelöscht und ihm werden
	 * diese Objekte zugeordnet.
	 * 
	 * @return auf Bildschirm angezeigter PreferenceScreen
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onStop()
	 */
	public void onStop() {
		super.onStop();


	}

	@Override
	public void onDestroy() {
		lpManager.close();
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
	 * Gibt die Ressourcen-Id zum Titels der Preference-Objekte zurück
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @return ID Ressourcen-Id
	 */
	private int getTitleId(String key) {
		String title = "lp_" + algorithm.getName() + "_" + key;
		return res.getIdentifier(title, "string", "com.android.settings");
	}

	/**
	 * Gibt die Ressourcen-Id zur Beschreibung der Preference-Objekte zurück
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @return the Ressourcen-Id
	 */
	private int getSummaryId(String key) {
		String summary = "lp_" + algorithm.getName() + "_" + key + "_summary";
		return res.getIdentifier(summary, "string", "com.android.settings");
	}

	/**
	 * Speichert Zuordnung von Preference zu Parameterschlüssel und Datentyp.
	 * 
	 * @param pref
	 *            Preference
	 * @param key
	 *            Schlüssel des zur Preference gehörenden Parameter der
	 *            Konfiguration
	 * @param type
	 *            Datentyp des Parameters
	 */
	private void setPreferenceExtras(Preference pref, String key, String type) {
		String[] prefExtras = new String[2];
		prefExtras[0] = key;
		prefExtras[1] = type;
		prefToKey.put(pref, prefExtras);
	}

	/**
	 * Erzeugt ein EditTextPreference für Integer-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Das Tastaturlayout
	 * und die aktzeptierten Eingaben sind an Integer angepasst.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für Integer Parameter
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
	 * Erzeugt ein EditTextPreference für Double-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Das Tastaturlayout
	 * und die aktzeptierten Eingaben sind an Double angepasst.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für Double Parameter
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
	 * Erzeugt ein EditTextPreference für String-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return EditTextPreference für String Parameter
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
	 * Erzeugt ein ListPreference für Enum-Parameter. Der zum Schlüssel passende
	 * Titel, sowie die Beschreibung wird angezeigt. Zu den Einträgen werden die
	 * entsprechenden Strings ermittelt und angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörende Werte
	 * 
	 * @param choosen
	 *            Als ausgewählt angezeigter Wert
	 * @return ListPreference für Enum-Parameter
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
	 * Erzeugt eine CheckBoxPreference für Boolean-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return CheckBoxPreference für Boolean Parameter
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
	 * Erzeugt eine Preference für Coordinate-Parameter. Der zum Schlüssel
	 * passende Titel, sowie die Beschreibung wird angezeigt. Wird auf das
	 * Preference Objekt geklickt wird eine MapActivity zum Einstellen der
	 * Koordinate gestartet.
	 * 
	 * @param key
	 *            Schlüssel des Parameters
	 * @param value
	 *            Zum Parameter zugehörender Wert
	 * @return CheckBoxPreference für Boolean Parameter
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
