package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;

public class LoadEndEvent extends CGEvent {

	public LoadEndEvent(Object target) {
		super(LoadEndEvent.class, target);
	}

}
