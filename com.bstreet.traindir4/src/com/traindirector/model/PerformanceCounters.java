package com.traindirector.model;

public class PerformanceCounters {

	public int wrong_dest;
	public int denied;
	public int late_trains;
	public int thrown_switch;
	public int turned_train;
	public int ntrains_wrong;	// unused?
	public int wrong_platform;
	public int waiting_train;
	public int cleared_signal;
	public int nmissed_stops;
	public int wrong_assign;
	public int ntrains_late; // unused?

	public void clear() {
		setAll(0);
	}
	
	public void setAll(int value) {
		wrong_dest = value;
		denied = value;
		late_trains = value;
		thrown_switch = value;
		turned_train = value;
		ntrains_wrong = value;
		wrong_platform = value;
		waiting_train = value;
		cleared_signal = value;
		nmissed_stops = value;
		wrong_assign = value;
		ntrains_late = value;
	}
}
