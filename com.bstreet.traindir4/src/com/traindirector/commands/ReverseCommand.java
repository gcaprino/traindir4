package com.traindirector.commands;

import com.traindirector.model.Direction;
import com.traindirector.model.Track;
import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.simulator.PathFinder;
import com.traindirector.simulator.SimulatorCommand;

public class ReverseCommand extends SimulatorCommand {
	Train _train;
	
	public ReverseCommand(Train train) {
		_train = train;
	}
	
	public void handle() {
		Track headPos = _train._position;
		if (headPos == null)
			return;
		Direction newdir = _train._direction.opposite();
		Track tailpos = null;
		TrackPath path = null;
		PathFinder finder = new PathFinder();
		if (_train._tail != null && (tailpos = _train._tail.getHeadTrack()) != null && tailpos != headPos) {
			path = finder.find(tailpos._position, newdir);
		} else {
			path = finder.find(headPos._position, newdir);
		}
		if(path == null) {
			_simulator._performanceCounters.denied++;
			return;
		}
		if(tailpos != null) {
			tailpos.setStatus(TrackStatus.FREE); // TODO if this is the new head it should remain OCCUPIED!
			_train._tail._position = null;
		} else {
			_train._position.setStatus(TrackStatus.FREE); // so that path.isFree() will succeed
			_train._position = null;
		}
		if(!path.isFree()) {
			if(tailpos != null) {
				_train._tail._position = tailpos;
				tailpos.setStatus(TrackStatus.OCCUPIED);
			} else {
				_train._position = headPos;
				headPos.setStatus(TrackStatus.OCCUPIED);
			}
			_simulator.alert(String.format("Train %s: cannot reverse direction. Path is busy.", _train._name));
			path = null;
			++_simulator._performanceCounters.denied;
			return;
		}
		
		// We verified that all conditions are good for reversing
		// the direction, so now do the actual reverse.

		int f;
		if(_train._path != null) {
			if(_train._tail != null && _train._tail._path != null) {
				// remove tracks ahead of old head from tail's path
				// leaving head's position (the loco) in the path
				for(f = 1; f < _train._path.getTrackCount(0); ++f) {
					_train._tail._path.remove(_train._path.getTrackAt(f));
				}
			}
			_train._path.setStatus(TrackStatus.FREE, 0);
			_train._path = null;
		}
		if(_train._merging != null) {
			_train._merging.clearFlag(Train.WAITINGMERGE);
			_train.clearFlag(Train.MERGING);
			_train._merging = null;
		}
		if(tailpos != null && _train._tail._path != null) {
			_train._tail._path.reverse();
			f = _train._tail._trackDistance;
			_train._tail._trackDistance = headPos._length - _train._trackDistance;
			_train._trackDistance = tailpos._length - f;
			if(_train._trackDistance < 0)
				_train._trackDistance = 0;
		}

		_train._direction = newdir;
		if(!_train.isSet(Train.STRANDED)) {
			path.setStatus(TrackStatus.BUSY, 0);
		}
		if(_train._tail != null) {
			_train._position = tailpos != null ? tailpos : headPos;
			_train._tail._position = tailpos != null ? headPos : null;
			_train._tail._path.append(path);
		} else {
			_train._position = headPos;
		}
		Track pos = _train._position;
		pos.setStatus(TrackStatus.OCCUPIED);
		if(_train.isSet(Train.TURNED)) {
			++_simulator._performanceCounters.turned_train;
		}
		_train.setFlag(Train.TURNED);
		if(_train.isSet(Train.SWAPHEADTAIL)) // swap head and tail icons
			_train.clearFlag(Train.SWAPHEADTAIL);
		else
			_train.setFlag(Train.SWAPHEADTAIL);
		_train._path = path;
		_train._pathtravelled = 0;
		if(_train._status != TrainStatus.STOPPED) { // waiting at a signal?
			if(!_train.isArrived()) {
				if(_train.isWaiting()) {
					_train._status = TrainStatus.RUNNING;
					// TODO: findStopPoint(train)
					// TODO: findSlowPoint(train)
				} else
					_train._status = TrainStatus.STOPPED;
				_train._timeDep = _simulator._simulatedTime;
				_train._outOf = null;
			}
		}
		// TODO: _simulator._trainRunner.tailAdvance(_train, 0); // compute tail path?
		_simulator._trainRunner.findSlowPoint(_train);
		_simulator._trainRunner.findStopPoint(_train);
		_train.onReverse();
	}
}
