package com.traindirector.commands;

import com.traindirector.simulator.SimulatorCommand;

public class SpeedCommand extends SimulatorCommand {

	public static int[] speedMultipliers = {
		1, 2, 3, 5, 7, 10, 15, 20, 30, 60, 120, 240, 300
	};
	
	int _dir;
	
	public SpeedCommand(int dir) {
		_dir = dir;
	}
	
	public void handle() {
		int curSpeed = _simulator.getSimulatedSpeed();
		int j;
		for(j = 0; j < speedMultipliers.length; ++j)
			if(speedMultipliers[j] == curSpeed) {
				if(_dir < 0 && j > 0)
					--j;
				else if(j + 1 < speedMultipliers.length)
					++j;
				_simulator.setSimulationSpeed(j, speedMultipliers[j]);
				break;
			}
	}
}
