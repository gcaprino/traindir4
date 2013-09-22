package com.traindirector.model;

import com.traindirector.simulator.TDDelay;

public class TrainStop {

	public int _minstop;
	public String _station;
	public int _arrival, _departure;
	public TDDelay _depDelay;
	public boolean _stopped;		// did we ever stop at this station?
	public int _delay;			// how much we were late arriving at this stop
	public boolean _late;	// we were late arriving at this station - TODO: can't we just use _delay != 0?

	public TrainStop() {
		_depDelay = null;
	}

	public String toString() {
		return _station;
	}

	public void reset() {
		_depDelay = null;
		_stopped = false;
		_delay = 0;
	}
}
