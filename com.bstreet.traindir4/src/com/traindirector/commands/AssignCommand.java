package com.traindirector.commands;

import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.simulator.PathFinder;
import com.traindirector.simulator.SimulatorCommand;

public class AssignCommand extends SimulatorCommand {

	Train _from, _to;
	
	public AssignCommand(Train from, Train to) {
		_from = from;
		_to = to;
	}
	
	public void handle() {
		if (_from._stock != null && !_from._stock.isEmpty() && !_from._stock.equals(_to._name))
			_simulator._performanceCounters.wrong_assign++;
		
		//if(_from._status != TrainStatus.ARRIVED) {
		//	
		//}
		PathFinder finder = new PathFinder();
		if (_from.isSet(Train.STRANDED)) {
			_from._path = finder.find(_from._position._position, _from._direction);
			if(_from._path == null) {
				_simulator._performanceCounters.denied++;
				return;
			}
			if (!_from._path.isFree()) {
				// TODO: why should we check if path[1] == GREEN?
				_simulator.alert(String.format("Train %s: path is not free at %s",
						_from._name, _from._position._position.toString()));
				_simulator._performanceCounters.denied++;
				return;
			}
			if (_from._tail != null && _from._tail._path != null) {
				_from._tail._path.append(_from._path);
			}
			_from._path.setStatus(TrackStatus.BUSY, 0);
			// TODO: findStopPoint(_from);
			// TODO: findSlowPoint(_from);
		}
		
		if((_to.isArrived() || _to._arrived != 0) && _to._merging != null) { // we are shunting
			_to.arrived();
			_to._stopping = null;
			_to._speed = 0;
			_to._shunting = false;
			_to._outOf = null;
		} else
			_to.stopped();

		if(_from.isArrived() && _from._position == null) {
			return;
		}
		_to._path = _from._path;
		_to._position = _from._position;
		_to._direction = _from._direction;
		_to._curmaxspeed = _from._curmaxspeed;
		if(_to._maxspeed == 0)
			_to._maxspeed = _from._maxspeed;
		// the new train departs in the morning, but the old train arrived in the afternoon
		// this means the new train will start in the following day, so add 24 hours
		if(_to._timeIn < 12 * 60 * 60 && _from._timeExited >= 12 * 60 * 60)
			_to._timeDep = _to._timeIn + 24 * 60 * 60;
		else
			_to._timeDep = _to._timeIn;
		if (_to._waitFor != null && !_to._waitFor.isEmpty()) {
			if(_to._waitTime == 0)
				_to._waitTime = 60;	// default we wait 60 seconds
		}
		if(_from._tail != null) {
			if(_to._tail == null) {
				_to._length = _from._length;
				_to._tail = new Train("");
				_to._tail._path = new TrackPath();
				_to._eastCarIcon = _from._eastCarIcon;
				_to._westCarIcon = _from._westCarIcon;
			}
			if(_from.isSet(Train.STRANDED) && _to._tail._path != null) {
				// extend stranded train's tail path with incoming train's tail path
				_from._tail._path.insert(_to._tail._path);
				_from._tail._position = _to._tail.getHeadTrack();
				_from._tail._trackDistance = _to._tail._trackDistance;
			} else {
				
				// here we are assigning the material of a train
				// already in the territory (oldtrain) to a train
				// which is not on the territory (t).
				// We should check that the preset length of the
				// destination train is the same as that of the old train.
				// If it is, then we can simply copy the path to the
				// destination train.
				// If the destination train is longer, we should
				// notify the player that we don't have enough rolling
				// stock, and add to the penalty.
				// If the destination train is longer we should split
				// the source in two, and leave some cars in the path.
				//
				// For the time being we simply assign the source train
				// to the destination train.
				//
		
			}
			_to._tail._path = _from._tail._path;
			_to._tail._position = _from._tail._position;
			_to._tail._direction = _from._tail._direction;
			_to._tail._trackDistance = _from._tail._trackDistance;
			_to._fleet = _from._fleet;
			_from._fleet = null;
			_from._tail._path = null;
			_from._tail._trackDistance = 0;
			_from._tail._position = null;
		}
		// maybe the assignment was initiated by a trigger
		if(!_from.isArrived()) {
			_from.arrived();
		}
		if(_to._waitFor != null && !_to._waitFor.isEmpty()) {
			// TODO: handle _to._waitFor different from _from
			if(_from._timeExited + _to._waitTime > + _to._timeDep)
				_to._timeDep = _from._timeExited + _to._waitTime;
		}
		_from._path = null;
		_from._position = null;
		if(_from.isSet(Train.STRANDED)) {
			_simulator._schedule.removeStranded(_from);
			// TODO: force removal from schedule view?
		} else {
			// TODO: force removal from schedule view
		}
		_to.onAssign();
			/*
	if(oldtrain->tail) {
	    if(!t->tail) {
    		t->length = oldtrain->length;
			t->tail = (Train *)calloc(sizeof(Train), 1);
			t->ecarpix = oldtrain->ecarpix;
			t->wcarpix = oldtrain->wcarpix;
	    }
	    if((oldtrain->flags & TFLG_STRANDED) && t->tail->path) {
			// extend stranded train's tail path with incoming train's tail path
			oldtrain->tail->path->Insert(t->tail->path);
			oldtrain->tail->position = t->tail->path->TrackAt(0);
			oldtrain->tail->trackpos = t->tail->trackpos;
	    } else {
	    }
	    t->tail->path = oldtrain->tail->path;
//	    t->tail->pathpos = oldtrain->tail->pathpos;
	    t->tail->position = oldtrain->tail->position;
	    t->tail->trackpos = oldtrain->tail->trackpos;
	    t->fleet = oldtrain->fleet;
	    oldtrain->fleet = 0;
	    oldtrain->tail->path = 0;
//	    oldtrain->tail->pathpos = 0;
	    oldtrain->tail->trackpos = 0;
	    oldtrain->tail->position = 0;
	}
	// maybe the assignment was initiated by a trigger
	if(oldtrain->status != train_ARRIVED) {
	    train_arrived(oldtrain);
	    oldtrain->OnArrived();
//	    add_update_schedule(oldtrain);
	} 
        if(t->waitfor) {
            if(oldtrain->timeexited + t->waittime > t->timedep)
		t->timedep = oldtrain->timeexited + t->waittime;
        }
//	oldtrain->pathpos = 0;
	oldtrain->path = 0;
	oldtrain->position = 0;
	if(oldtrain->flags & TFLG_STRANDED) {
	    remove_from_stranded_list(oldtrain);
	    change_coord(t->position->x, t->position->y);
	    update_schedule(oldtrain);	// to remove from list
	    delete oldtrain;
	} else {
	    change_coord(t->position->x, t->position->y);
	    update_schedule(oldtrain);	// to remove from list
	}
	update_schedule(t);
	t->OnAssign();

			 */
 	}
}
