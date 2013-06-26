package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;
import com.traindirector.simulator.Simulator;

public class ResetEvent extends CGEvent {

	public ResetEvent(Simulator simulator) {
		super(ResetEvent.class, simulator);
	}

}
