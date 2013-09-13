package com.traindirector.model;

public enum TrainStatus {

	READY,
	RUNNING,
	STOPPED,
	DELAYED,		// ready but cannot enter the territory
	WAITING,
	DERAILED,		// couldn't place on territory!
	ARRIVED,		// reached some destination
	STARTING,		// delayed starting
	CANCELLED;		// does not run some days of the week
	
	public String toString() {
		switch(this) {
		case ARRIVED:	return "arrived";
		case DELAYED:	return "delayed";
		case DERAILED:	return "derailed";
		case READY:		return "ready";
		case RUNNING:	return "running";
		case STOPPED:	return "stopped";
		case WAITING:	return "waiting";
		case STARTING:	return "starting";
		case CANCELLED:	return "cancelled";
		default:		return "?";
		
		}
	}
	
	public static TrainStatus fromInteger(int i) {
		switch(i) {
		case 0: return READY;
		case 1: return RUNNING;
		case 2: return STOPPED;
		case 3: return DELAYED;
		case 4: return WAITING;
		case 5: return DERAILED;
		case 6: return ARRIVED;
		case 7: return STARTING;
		case 8: return CANCELLED;
		}
		return DERAILED;
	}
}
