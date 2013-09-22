package com.traindirector.model;

public enum TrackStatus {

	FREE,			// black track - not locked nor occupied
	BUSY,			// green track - locked by a signal
	BUSYSHUNTING,	// white track - reserved for shunting
	OCCUPIED		// red track   - occupied by train's cars

}
