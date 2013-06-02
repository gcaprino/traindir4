package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;

public class TimeSliceEvent extends CGEvent {

	long currentUpdateTime;
	
	public TimeSliceEvent(Object target, long time) {
		super(TimeSliceEvent.class, target);
		currentUpdateTime = time;
	}

}
