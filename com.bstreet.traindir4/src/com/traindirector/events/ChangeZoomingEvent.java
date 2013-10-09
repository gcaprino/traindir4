package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;

public class ChangeZoomingEvent extends CGEvent {

	int _factor;
	
	public ChangeZoomingEvent(int factor) {
		super(ChangeZoomingEvent.class, null);
		_factor = factor;
	}

	public int getFactor() {
		return _factor;
	}

}
