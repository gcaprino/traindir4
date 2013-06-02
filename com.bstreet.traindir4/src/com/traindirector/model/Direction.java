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

	public Direction opposite() {
		switch(this) {
		case E:		return W;
		case N:		return S;
		case NE:	return SW;
		case NW:	return SE;
		case S:		return N;
		case SE:	return NW;
		case SW:	return NE;
		case W:		return E;
		default:	return this;
		}
	}

	public TDPosition offset(TDPosition oldPos) {
		TDPosition pos = new TDPosition(oldPos._x, oldPos._y);
		switch(this) {
		case E:
			pos._x += 1;
			break;
			
		case N:
			pos._y -= 1;
			break;
			
		case NE:
			pos._x += 1;
			pos._y -= 1;
			break;
			
		case NW:
			pos._x -= 1;
			pos._y -= 1;
			break;
			
		case S:
			pos._y += 1;
			break;
			
		case SE:
			pos._x += 1;
			pos._y += 1;
			break;
			
		case SW:
			pos._x -= 1;
			pos._y += 1;
			break;
			
		case W:
			pos._x -= 1;
			break;

		default:
			break;
		}
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
}
