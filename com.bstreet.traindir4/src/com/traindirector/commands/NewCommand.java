package com.traindirector.commands;

import com.traindirector.simulator.SimulatorCommand;

public class NewCommand extends SimulatorCommand {

	public void handle() {
		_simulator.newSimulation();
	}
}
