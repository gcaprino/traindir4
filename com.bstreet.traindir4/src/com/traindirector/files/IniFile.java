package com.traindirector.files;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.options.BooleanOption;
import com.traindirector.options.FileOption;
import com.traindirector.options.IntegerOption;
import com.traindirector.options.Option;
import com.traindirector.simulator.Simulator;

public class IniFile extends TextFile {

	Simulator _simulator;

	public IniFile(Simulator simulator) {
		
		_simulator = simulator;
		String userDir = getUserDir();
		setFileName(userDir + "/tdir3.ini", "ini");
	}

	public String getUserDir() {
		String path = System.getenv("TDHOME");
		if (path == null)
			path = System.getenv("HOME");
		if (path != null) {
			return path;
		}
		if (System.getProperty("os.name").contains("Windows"))
			path = "C:/Temp";
		else
			path = "/tmp";
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}
		return path;
	}
	
	Map<String, String> _skinOptions;
	Map<String, String> _preferences;
	Map<String, String> _mainView;

	public void load() {
		List<String> lines = loadFileContent();
		Map<String, String> curOptions = null;
		for (String s : lines) {
			if (s.equals("[Skin1]")) {
				_skinOptions = new HashMap<String, String>();
				curOptions = _skinOptions;
			} else if (s.equals("[Preferences]")) {
				_preferences = new HashMap<String, String>();
				curOptions = _preferences;
			} else if (s.equals("[MainView]")) {
				_mainView = new HashMap<String, String>();
				curOptions = _mainView;
			} else if (curOptions != null) {
				String[] keyVal = s.split("=");
				if (keyVal.length != 2)
					continue;
				curOptions.put(keyVal[0].trim(), keyVal[1].trim());
			}
		}

		_simulator._oldSimulations.clear();
		int nOldSimulations = getInteger(_mainView.get("OldSimulations"));
		if (nOldSimulations > 0) {
			for (int i = 0; i < nOldSimulations; ++i) {
				String key = "simulation" + (i + 1);
				String value = _mainView.get(key);
				if (value != null && !value.isEmpty())
					_simulator._oldSimulations.add(value);
			}
		}
		
		_simulator._colorFactory.set("schedule.colorCanceled", getColor(_mainView.get("colorCanceled")));
		_simulator._colorFactory.set("schedule.colorReady", getColor(_mainView.get("colorReady")));
		_simulator._colorFactory.set("schedule.colorArrived", getColor(_mainView.get("colorArrived")));
		_simulator._colorFactory.set("schedule.colorDerailed", getColor(_mainView.get("colorDerailed")));
		_simulator._colorFactory.set("schedule.colorRunning", getColor(_mainView.get("colorRunning")));

		for (Option option : _simulator._options._options) {
			if (option instanceof BooleanOption) {
				((BooleanOption) option).set(getBooleanPref(option._name));
			} else if (option instanceof IntegerOption) {
				((IntegerOption) option)._intValue = Integer.parseInt(_preferences.get(option._name));
			} else {
				option.set(_preferences.get(option._name));
			}
		}
		_simulator._options._noTrainNamesColors.set(getBooleanPref("NoTrainNamesColors"));
		_simulator._options._locale = _preferences.get("locale");
		if (_simulator._options._locale == null)
			_simulator._options._locale = "en";
	}

	public String getPreference(String option) {
		if (_preferences == null)
			return null;
		return _preferences.get(option);
	}

	public void save() {
		
	}

	public boolean getBooleanPref(String key) {
		return getBoolean(_preferences.get(key));
	}

	public int getInteger(String value) {
		if (value == null)
			return 0;
		return Integer.parseInt(value);
	}
	
	public boolean getBoolean(String value) {
		if (value == null)
			return false;
		return Integer.parseInt(value) != 0;
	}
	
	public int getColor(String str) {
		if (str == null || str.isEmpty())
			return 0;
		if (str.equals("red")) return 0xFF0000;
		if (str.equals("blue")) return 0x0000FF;
		if (str.equals("green")) return 0x00FF00;
		if (str.equals("black")) return 0x000000;
		if (str.equals("white")) return 0xFFFFFF;
		if (str.equals("cyan")) return 0x00FFFF;
		if (str.equals("lightgray")) return 0xc0c0c0;

		String[] rgb = str.split(" ");
		if (rgb.length != 3)
			return 0;
		int val = (Integer.parseInt(rgb[0]) & 0xff) << 16;
		val |= (Integer.parseInt(rgb[0]) & 0xFF) << 8;
		val |= (Integer.parseInt(rgb[0]) & 0xFF);
		return val;
	}
}
