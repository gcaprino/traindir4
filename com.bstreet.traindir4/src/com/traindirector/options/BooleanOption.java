package com.traindirector.options;

public class BooleanOption extends Option {

	public BooleanOption(String name, String descr) {
		super(name, descr);
	}
	
	public boolean isSet() {
		return _intValue != 0;
	}

	public boolean get() {
		return _intValue != 0;
	}
	
	public void set(boolean value) {
		_intValue = value ? 1 : 0;
	}
}
