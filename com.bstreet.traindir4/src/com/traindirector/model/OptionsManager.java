package com.traindirector.model;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.files.Option;

public class OptionsManager {

	public List<Option> _options;
	
	public OptionsManager() {
		_options = new ArrayList<Option>();
	}
	
	public void add(Option o) {
		_options.add(o);
	}

	public Option get(String name) {
		for (Option o : _options)
			if (o._name.equals(name))
				return o;
		return null;
	}
	
	public String getString(String name) {
		Option o = get(name);
		if (o == null)
			return "";
		return o._value;
	}
	
	public int getInteger(String name) {
		Option o = get(name);
		if (o == null)
			return 0;
		return o._intValue;
	}
}
