package com.traindirector.simulator;

public class TDDelay {
	
	public static final int MAX_DELAY = 10;
	
	public int _nDelays;
	public int _nSeconds;
	public int  _prob[];
	public int _seconds[];
			
	public TDDelay() {
		_prob = new int[MAX_DELAY];
		_seconds = new int[MAX_DELAY];
	}
}
