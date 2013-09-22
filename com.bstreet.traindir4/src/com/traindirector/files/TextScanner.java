package com.traindirector.files;

public class TextScanner {

	public int _intValue;
	public String _stringValue;
	public int _offset;
	public String _input;

	public TextScanner(String input) {
		_input = input;
		_offset = 0;
	}

	public boolean scanInteger() {
		_intValue = 0;
		StringBuilder val = new StringBuilder();
		char ch;
		while(_offset < _input.length()) {
			ch = _input.charAt(_offset);
			if(ch < '0' || ch > '9')
				break;
			val.append(ch);
			++_offset;
		}
		if(val.length() < 1) {
			return false;
		}
		_intValue = Integer.parseInt(val.toString());
		return skipBlanks();
	}

	public boolean scanString() {
		StringBuilder val = new StringBuilder();
		char ch;
		while(_offset < _input.length()) {
			ch = _input.charAt(_offset);
			if(ch == ' ' || ch == '\t')
				break;
			val.append(ch);
			++_offset;
		}
		_stringValue = val.toString();
		if(val.length() < 1) {
			return true;
		}
		return skipBlanks();
	}

	public boolean skipBlanks() {
		while(_offset < _input.length()) {
			char ch = _input.charAt(_offset);
			if(ch != ' ' && ch != '\t')
				return true;
			++_offset;
		}
		if(_offset >= _input.length())
			return false;
		return true;
	}

}
