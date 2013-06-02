package com.traindirector.events;

import com.bstreet.cg.events.CGEvent;
import com.traindirector.model.Train;

public class ShowTrainStopsEvent extends CGEvent {

	public Train _train;
	
	public ShowTrainStopsEvent(Object target, Train train) {
		super(ShowTrainStopsEvent.class, target);
		_train = train;
	}
}
