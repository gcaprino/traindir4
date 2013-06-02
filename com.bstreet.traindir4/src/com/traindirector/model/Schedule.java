package com.traindirector.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Schedule {

	public List<Train> _trains;
	public int _startTime;
	public List<Train> _stranded;
	public int _runDay;

	public int _nRunning;
	public int _nWaiting;
	public int _nDelayed;
	public int _nReady;
	public int _nArrived;
	public int _nDerailed;

	public Schedule() {
		_trains = new LinkedList<Train>();
		_stranded = new LinkedList<Train>();
	}
	
	public void add(Train train) {
		_trains.add(train);
	}
	
	public Train get(int index) {
		return _trains.get(index);
	}

	public void clear() {
		_trains.clear();
	}

	public Train findTrainNamed(String name) {
		for (Train train : _trains) {
			if (train._name.equals(name))
				return train;
		}
		return null;
	}

	public Train findTrainAt(TDPosition pos) {
		for (Train train : _trains) {
			if (train._position != null && train._position._position.sameAs(pos))
				return train;
		}
		return null;
	}

	public Train findStrandedAt(TDPosition pos) {
		for (Train train : _stranded) {
			if (train._position != null && train._position._position.sameAs(pos))
				return train;
		}
		return null;
	}

	public Train findTailAt(TDPosition pos) {
		for (Train train : _trains) {
			if (train._tail != null && train._tail._position  != null && train._tail._position._position.sameAs(pos))
				return train;
		}
		return null;
	}

	public Train findStrandedTailAt(TDPosition pos) {
		for (Train train : _stranded) {
			if (train._tail != null && train._tail._position  != null && train._tail._position._position.sameAs(pos))
				return train;
		}
		return null;
	}

	public Train findAnyTrainAt(TDPosition pos) {
		Train t = findTrainAt(pos);
		if (t != null)
			return t;
		t = findTailAt(pos);
		if (t != null)
			return t;
		t = findStrandedAt(pos);
		if (t != null)
			return t;
		t = findStrandedTailAt(pos);
		return t;
	}

	public void reset() {
		for (Train train : _trains) {
			train._status = TrainStatus.READY;
		}
	}

	public void clearCounters() {
		_nReady = 0;
		_nWaiting = 0;
		_nDelayed = 0;
		_nArrived = 0;
		_nDerailed = 0;
		_nRunning = 0;
		computeCounters();
	}
	
	public void computeCounters() {
		for (Train train : _trains) {
			switch (train._status) {
			case READY:    ++_nReady;	 break;
			case WAITING:  ++_nWaiting;  break;
			case DELAYED:  ++_nDelayed;  break;
			case DERAILED: ++_nDerailed; break;
			case ARRIVED:  ++_nArrived;  break;
			case RUNNING:  ++_nRunning;  break;
			}
		}
	}

	public void removeStranded(Train train) {
		_stranded.remove(train);
	}

	public List<Train> getTrainsDepartingFrom(String station) {
		List<Train> departingTrains = new ArrayList<Train>();
		for(Train train : _trains) {
			switch(train._status) {
			case READY:
				if(train._entrance.equals(station))
					departingTrains.add(train);
			}
		}
		// TODO: stranded trains
		return departingTrains;
	}
}
