package com.traindirector.commands;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.events.ShowTrainStopsEvent;
import com.traindirector.model.Train;
import com.traindirector.simulator.SimulatorCommand;

public class ShowTrainStopsCommand extends SimulatorCommand {

	String _trainName;
	
	public ShowTrainStopsCommand(String trainName) {
		super();
		_trainName = trainName;
	}
	
	public void handle() {
		Train train = _simulator._schedule.findTrainNamed(_trainName);
		if (train == null)
			return;
		ShowTrainStopsEvent event = new ShowTrainStopsEvent(_simulator, train);
		CGEventDispatcher.INSTANCE.postEvent(event);
	}
}
