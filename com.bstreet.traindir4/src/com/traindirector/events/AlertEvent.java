package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;

public class AlertEvent extends CGEvent {

	public AlertEvent(Object target) {
		super(AlertEvent.class, target);
	}

}
