package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;

public class LoadStartEvent extends CGEvent {

	public LoadStartEvent(Object target) {
		super(LoadStartEvent.class, target);
	}

}
