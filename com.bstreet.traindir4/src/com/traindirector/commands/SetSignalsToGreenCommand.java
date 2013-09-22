package com.traindirector.commands;

import java.util.List;

import com.traindirector.model.Signal;
import com.traindirector.simulator.SimulatorCommand;

public class SetSignalsToGreenCommand extends SimulatorCommand {

	@Override
	public void handle() {
		
		synchronized(_simulator._territory) {
			List<Signal> signals = _simulator._territory.getAllSignals();
	
			for(Signal sig : signals) {
				if(sig._fleeted) {
				    if(sig.isFleeted() || sig.isClear() || sig._signalx)
				    	continue;
				    if(sig.isApproach())
				    	continue;
				    sig.setFleeted(true);
				    sig.toggle();
				}
			}
		}
	}

}
