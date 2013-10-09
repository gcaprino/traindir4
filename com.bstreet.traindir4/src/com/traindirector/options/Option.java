package com.traindirector.options;

public class Option {

	public String _name;
	public String _description;
	public String _value;
	public int _intValue;
	
	public Option(String name) {
		_name = name;
		_description = name;
		_value = "";
	}
	
	public Option(String name, String descr) {
		_name = name;
		_description = descr;
	}

	public void set(String value) {
		_value = value;
	}
	
	public void set(int value) {
		_intValue = value;
	}
}
