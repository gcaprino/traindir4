package com.traindirector.files;

public class Option {

	public String _name;
	public String _description;
	public String _value;
	public int _intValue;
	
	public Option(String name) {
		_name = name;
		_description = name;
	}
	
	public Option(String name, String descr) {
		_name = name;
		_description = descr;
	}
	
}
