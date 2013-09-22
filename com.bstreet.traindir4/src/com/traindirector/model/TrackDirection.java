package com.traindirector.model;

public enum TrackDirection {

	E_W,		// NONE
	W_E,
	NW_SE,
	SW_NE,
	W_NE,
	W_SE,
	NW_E,
	SW_E,
	TRK_N_S,	// 8
	
	N_S_W,
	N_S_E,
	SW_N,
	NW_S,
	SE_N,
	NE_S,
	_FILLER,	// N_S must be 16, so we need a filler here
	N_S,
	S_N,
	signal_SOUTH_FLEETED,
	signal_NORTH_FLEETED,
	XH_NW_SE,
	XH_SW_NE,
	X_X,		// 22
	X_PLUS,		// 23
	N_NE_S_SW,
	N_NW_S_SE;

	public static TrackDirection parse(String string) {
		int val = Integer.parseInt(string);
		return parse(val);
	}
	
	public static TrackDirection parse(int val) {
		switch(val) {
		case 0:	return E_W;
		case 1:	return W_E;
		case 2:	return NW_SE;
		case 3:	return SW_NE;
		case 4:	return W_NE;
		case 5:	return W_SE;
		case 6:	return NW_E;
		case 7:	return SW_E;
		case 8:	return TRK_N_S;
		case 9:	return N_S_W;
		case 10:	return N_S_E;
		case 11:	return SW_N;
		case 12:	return NW_S;
		case 13:	return SE_N;
		case 14:	return NE_S;
		case 15:	return _FILLER;
		case 16:	return N_S;
		case 17:	return S_N;
		case 18:	return signal_SOUTH_FLEETED;
		case 19:	return signal_NORTH_FLEETED;
		case 20:	return XH_NW_SE;
		case 21:	return XH_SW_NE;
		case 22:	return X_X;
		case 23:	return X_PLUS;
		case 24:	return N_NE_S_SW;
		case 25:	return N_NW_S_SE;
		}
		return null;
	}

}
