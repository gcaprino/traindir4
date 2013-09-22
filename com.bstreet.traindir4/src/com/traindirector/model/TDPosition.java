package com.traindirector.model;

public class TDPosition {

	public int _x, _y; // position in grid coordinates (not screen coordinates)

	public TDPosition() {

	}

	public TDPosition(int x, int y) {
		_x = x;
		_y = y;
	}

	public TDPosition(String s) {
		fromString(s, 0);
	}

	public boolean sameAs(TDPosition pos) {
		return pos._x == _x && pos._y == _y;
	}

	public boolean sameAs(int x, int y) {
		return _x == x && _y == y;
	}

	public boolean isNull() {
		return _x == 0 && _y == 0;
	}
	
	public int fromString(String s, int offset) {
		while (offset < s.length() && s.charAt(offset) == ' ')
			++offset;
		boolean hasParens = false;
		if (s.charAt(offset) == '(') {
			hasParens = true;
			++offset;
		}
		char ch = 'z';
		int val = 0;
		while (offset < s.length() && ((ch = s.charAt(offset)) >= '0' && s.charAt(offset) <= '9')) {
			val = val * 10 + ch - '0';
			++offset;
		}
		_x = val;
		if (offset >= s.length())
			return offset;
		if (ch == ',')
			++offset;
		val = 0;
		while (offset < s.length() && ((ch = s.charAt(offset)) >= '0' && s.charAt(offset) <= '9')) {
			val = val * 10 + ch - '0';
			++offset;
		}
		_y = val;
		if (hasParens && offset < s.length() && s.charAt(offset) == ')')
			++offset;
		return offset;
	}

	public String toString() {
		return "(" + _x + "," + _y + ")";
	}

	public boolean equals(TDPosition other) {
		if(other == null)
			return false;
		return _x == other._x && _y == other._y;
	}
}
