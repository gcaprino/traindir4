package com.traindirector.commands;

import com.traindirector.model.Direction;
import com.traindirector.model.Track;
import com.traindirector.model.TrackPath;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class SplitCommand extends SimulatorCommand {

	private Train _train;
	private int _length;

	public SplitCommand(Train train, int length) {
		_train = train;
		_length = length;
	}
	
	public void handle() {
		Train	stranded;

		if(_train.isSet(Train.STRANDED))	    /* can't split multiple times */
		    return;
		stranded = new Train("Stock from " + _train._name);
		_simulator._schedule._stranded.add(stranded);
		stranded._type = _train._type;
		stranded.setFlag(Train.STRANDED);
		stranded._position = _train._position;
		stranded._direction = _train._direction;
		stranded._curmaxspeed = _train._curmaxspeed;  /* in case of re-assignment later */
		stranded._maxspeed = _train._maxspeed;
		stranded._westCarIcon = _train._westCarIcon;
		stranded._eastCarIcon = _train._eastCarIcon;
		stranded._status = TrainStatus.ARRIVED;
		if(_train._length > 0) {
		    stranded._length = _train._length - _length;
		    if(_train._tail != null) {
				int	l = 0;
	
				stranded._tail = new Train("");
				stranded._tail._path = new TrackPath();
				for(l = 0; l < stranded._length; ) {
				    Track trk = _train._tail._path.getTrackAt(0);
				    Direction f = _train._tail._path.getDirectionAt(0);
				    stranded._tail._path.add(trk, f);
				    l += trk._length;
				    if(l >= stranded._length) {
						_train._tail._position = trk;
						stranded._position = trk;
						stranded._tail._position = stranded._tail._path.getTrackAt(0);
						break;
				    }
				    _train._tail._path.remove(trk);
				    if(_train._tail._path.getTrackCount(0) < 1)
				    	break;
				}
		    }
		    if(_length > 0)
		    	_train._length = _length;
		}
		ShuntCommand shunt = new ShuntCommand(_train);
		_simulator.addCommand(shunt);
	}
}
