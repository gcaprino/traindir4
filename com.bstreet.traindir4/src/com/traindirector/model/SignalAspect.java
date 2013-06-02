package com.traindirector.model;

public class SignalAspect {

	public static final String STOP = "stop";
	public static final String PROCEED = "proceed";
	public static final String NONE = "none";
	public static final String SPEEDLIMIT = "speedLimit";
	
	public static final String RED = "red";
	public static final String GREEN = "green";

	public String _name;
	public String _action;
	public String[] _iconN, _iconE, _iconS, _iconW;	// names of xpm files for 4 directions
	int _nextFlashing;

	public String getXpmStrings(TrackDirection direction) {
		String[] icons;
		switch(direction) {
		case W_E:
			icons = _iconE;
			break;
		case E_W:
			icons = _iconW;
			break;
		case N_S:
			icons = _iconS;
			break;
		case S_N:
			icons = _iconN;
			break;
		default:
			return null;
		}
		if(icons.length == 1)
			return icons[0];
		return icons[_nextFlashing % icons.length];
	}
	
	public void nextFlashing() {
		++_nextFlashing;
	}

	public int getSpeedLimit() {
		// TODO: override
		return 0;
	}
}
