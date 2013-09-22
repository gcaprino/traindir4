package com.traindirector.commands;

import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.simulator.SimulatorCommand;

public class ShuntCommand extends SimulatorCommand {

	Train _train;
	
	public ShuntCommand(Train train) {
		_train = train;
	}
	
	public void handle() {
		if(_train.isSet(Train.STRANDED | Train.WAITINGMERGE))
		    return;
		_train._oldstatus = _train._status;
		_train._status = TrainStatus.RUNNING;
		_train._shunting = true;
		_train._stopPoint = null;
		_train._slowPoint = null;
		_train._outOf = _train._position;
		_train.onShunt();
	}
}
