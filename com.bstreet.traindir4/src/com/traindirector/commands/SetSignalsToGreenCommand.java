package com.traindirector.commands;

import com.traindirector.model.Signal;
import com.traindirector.model.Track;
import com.traindirector.simulator.SimulatorCommand;

public class SetSignalsToGreenCommand extends SimulatorCommand {

	@Override
	public void handle() {

		for(Track t : _simulator._territory._tracks) {
		    if(!(t instanceof Signal))
		    	continue;
			Signal sig = (Signal )t;
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
