package com.traindirector.commands;

import com.traindirector.simulator.SimulatorCommand;

public class RunStopCommand extends SimulatorCommand {

	public void handle() {
		if(_simulator.isRunning())
			_simulator.stopRunning();
		else
			_simulator.startRunning();
	}
}
