package com.traindirector.commands;

import com.traindirector.model.Train;
import com.traindirector.simulator.SimulatorCommand;

public class SkipAheadCommand  extends SimulatorCommand {

	@Override
	public void handle() {
		int	next_time = 0x7fffffff;

		for(Train t : _simulator._schedule._trains) {
            if(t._isExternal)
                continue;
		    switch(t._status) {
		    case READY:
	
				if(t._entrance == null)
				    continue;
				if(t._days != 0 && _simulator._runDay != 0 && (t._days & _simulator._runDay) == 0)
				    continue;
				if(t._timeIn < _simulator._schedule._startTime)	/* will always ignore it */
				    continue;
				if(t._timeIn >= _simulator._simulatedTime && t._timeIn < next_time)
				    next_time = t._timeIn;
				break;

		    case STOPPED:
		    	
				if(t._timeDep > _simulator._simulatedTime && t._timeDep < next_time)
				    next_time = t._timeDep;
				break;

		    case DELAYED:
		    case RUNNING:
		    case WAITING:
		    	
				_simulator.alert("Not all trains are stopped.");
				return;	    // failed

		    case DERAILED:
		    case ARRIVED:

				break;	    // don't care
		    }
		}
		if(_simulator._simulatedTime + 180 > next_time) {
		    _simulator.alert("Next event is within 3 minutes.");
		    return;
		}
		_simulator._simulatedTime = next_time - 180;
	}
}
