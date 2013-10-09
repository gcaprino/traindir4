package com.traindirector.options;

public class FileOption extends Option {

	public FileOption(String name, String descr) {
		super(name, descr);
	}
	
	public FileOption(String name, String descr, String value) {
		super(name, descr);
		if (value != null) {
			_value = value;
		}
	}

}
