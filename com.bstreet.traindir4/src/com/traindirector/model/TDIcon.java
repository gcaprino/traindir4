package com.traindirector.model;

public class TDIcon {

	public String _name;
	public String _xpmFile;
	public String[] _xpmBytes;
	public Object _bytes;		// rendered bytes
	
	public TDIcon(String xpmFile) {
		_xpmFile = xpmFile;
	}
	
	public TDIcon(String xpmFile, String[] xpmBytes) {
		_xpmFile = xpmFile;
		_xpmBytes = xpmBytes;
	}
	
	public String toStrign() {
		return _name + "(" + _xpmFile + ")";
	}
}
