package com.traindirector.model;

import com.traindirector.simulator.TDTime;

public class Alert {

	public int _time;
	public String _text;
	
	public Alert(int time, String text) {
		_time = time;
		_text = text;
	}

	public String toString() {
		String s = TDTime.toString(_time) + ": " + _text;
		return s;
	}

	public String[] getStrings() {
		String[] s = new String[2];
		s[0] = TDTime.toString(_time);
		s[1] = _text;
		return s;
	}
}
