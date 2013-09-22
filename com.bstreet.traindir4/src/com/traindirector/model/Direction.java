package com.traindirector.model;


public enum Direction {

	N,
	NE,
	E,
	SE,
	S,
	SW,
	W,
	NW;

	static Direction[] _opposites;
	static int[] _xDeltas;
	static int[] _yDeltas;
	
	static {

		// using static arrays for constant values is faster than a switch on the direction

		_opposites = new Direction[8];
		_opposites[E.ordinal()]  = W;
		_opposites[NE.ordinal()] = SW;
		_opposites[SE.ordinal()] = NW;
		_opposites[W.ordinal()]  = E;
		_opposites[NW.ordinal()] = SE;
		_opposites[SW.ordinal()] = NE;
		_opposites[N.ordinal()]  = S;
		_opposites[S.ordinal()]  = N;
		_xDeltas = new int[8];
		_yDeltas = new int[8];
		_xDeltas[E.ordinal()] = 1;
		_xDeltas[W.ordinal()] = -1;
		_yDeltas[N.ordinal()] = -1;
		_yDeltas[S.ordinal()] = 1;
		_xDeltas[NE.ordinal()] = 1;
		_yDeltas[NE.ordinal()] = -1;
		_xDeltas[NW.ordinal()] = -1;
		_yDeltas[NW.ordinal()] = -1;
		_xDeltas[SE.ordinal()] = 1;
		_yDeltas[SE.ordinal()] = 1;
		_xDeltas[SW.ordinal()] = -1;
		_yDeltas[SW.ordinal()] = 1;
	};
	
	public Direction opposite() {
		return _opposites[this.ordinal()];
	}

	public TDPosition offset(TDPosition oldPos) {
		TDPosition pos = new TDPosition(oldPos._x, oldPos._y);
		pos._x += _xDeltas[this.ordinal()];
		pos._y += _yDeltas[this.ordinal()];
		return pos;
	}
	
	public static Direction fromInteger(int i) {
		switch(i) {
		case 0: return N;
		case 1: return NE;
		case 2: return E;
		case 3: return SE;
		case 4: return S;
		case 5: return SW;
		case 6: return W;
		case 7: return NW;
		default: return E;
		}
	}

	public String toString() {
		switch(this) {
		case N:	 return "N";
		case NE: return "NE";
		case E:  return "E";
		case SE: return "SE";
		case S:  return "S";
		case SW: return "SW";
		case W:  return "W";
		case NW: return "NW";
		default: return "?";
		}
	}
}
