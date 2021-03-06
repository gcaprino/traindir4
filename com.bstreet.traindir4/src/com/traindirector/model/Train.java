package com.traindirector.model;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.scripts.ExprValue;
import com.traindirector.scripts.NodeOp;
import com.traindirector.scripts.Script;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDDelay;
import com.traindirector.simulator.TDTime;

public class Train {

	public static final int MAX_SHUNTING_SPEED = 30;

	public static final int NEEDFINDSTOP = 1;

	public static final int TURNED = 1;	// train changed direction
	public static final int WAITED = 4;	// train waited at signal
	public static final int  MERGING = 8;// train is shunting to merge with another train
	public static final int  STRANDED = 16; // material left on track without engine
	public static final int  WAITINGMERGE = 32; // another train is approaching us to be merged
	public static final int  ENTEREDLATE = 64; // don't penalize for late arrivals
	public static final int  GOTDELAYATSTOP = 128; // only select delay (or none) once
	public static final int  SETLATEARRIVAL = 256; // only compute late arrival once
	public static final int  SWAPHEADTAIL = 512; // swap loco and caboose icons
	public static final int  DONTSTOPSHUNTERS = 1024; // don't stop here if train is shunting // TODO: move to Track

	public TrainStatus _status;
	public TrainStatus _oldStatus; // status of train before shunting	
	public String _name;
	public int  _timeIn;
	public int  _timeOut;
	public int  _type;
	public String _entrance;
	public String _exit;
	public String _waitFor;
	public String _stock;
	public List<TrainStop> _stops;
	public List<String> _notes;
	public Track _position;
	public Track _stopping; // station we are stopping at (head may be beyond
							// this track if length > 0)
	public double  _distanceToStop;
	public double _distanceToSlow;
	public Track _stopPoint;
	public Track _slowPoint;
	public long _etd;
	public int  _speed; // in km/h
	public int  _curmaxspeed; // such as when shunting, i.e. less than _maxspeed
	public int  _maxspeed; // as specified by the schedule (i.e. locomotive
							// limit)
	public int  _speedlimit; // last speed limit seen while traveling
	public int  _minDel;
	public int  _minLate;
	public int  _inDelay;
    public int  _startDelay;     // seconds to wait until starting after stop (runtime - set from train schedule or train type)
    public int  _myStartDelay;   // seconds this train should wait (from schedule file)
    public boolean _gotDelay;	// we computed a delay upon entry in the territory
	public boolean _shunting;
	public TrackPath _path;
	public TDDelay _entryDelay;
	public Train _tail;
	public int  _trackDistance; // distance still to travel in path[0] - formerly trackpos
	public int  _entryDistanceToGo; // distance of a tail from the territory entry track - formerly tailEntry
	public int  _flags;
	public int  _days;
	public Direction _direction;

	public int	_entryLength;	// length when entering layout - 
	public int  _length;		// current length (may have been changed by split/merge)

	public String _exited;		// the station where the train exited, if not the correct one
	public int  _timeExited;

	public boolean  _wrongDest;

	public int  _trackpos;		// should be unused, here for backward compatibility with .sav files

	// statistics about train's performance
	public int  _timeLate;
	public int  _timeDelay;
	public int  _timeRed;
	public int  _waitTime;
	public int  _arrived;

	// state of running trains
	public int  _pathtravelled;
	public int  _timeDep;
	public boolean _needFindStop;
	public Track _outOf;		// when shunting, we were stopped at this station.
	public List<Track> _fleet;
	public int  _tailEntry;	// distance of tail from entry point
	public int  _tailExit;	// distance of tail from exit point
	public Train _merging;	// we are bound to merge with this train
	public Script _script;

	public TDIcon _westIcon;
	public TDIcon _eastIcon;
	public TDIcon _westCarIcon;
	public TDIcon _eastCarIcon;
	public TrainStatus _oldstatus;
	public boolean _isExternal;

	public Train(String name) {
		_name = name;
		_stops = new ArrayList<TrainStop>();
		_path = new TrackPath();
		_waitFor = null;
		_fleet = new ArrayList<Track>();
		_isExternal = false;
		_startDelay = 0;
		_myStartDelay = 0;
	}

	public String getStatusAsString() {
		int i, j;
		boolean finalStop;
		StringBuilder sb = new StringBuilder(_status.toString());
		switch (_status) {
		
		case ARRIVED:
			if (_wrongDest) {
				sb.append("  Arrived at ");
				sb.append(_exited);
				sb.append(" instead of ");
				sb.append(_exit);
			} else if (_timeExited / 60 > _timeOut / 60) {
				sb.append("  Arrived " + (_timeExited / 60 - _timeOut / 60));
				sb.append(" min. late at ");
				sb.append(_exit);
			} else {
				sb.append("  Arrived on time");
			}
			if (_stock != null && !_stock.isEmpty()) {
				sb.append(" - stock for ");
				sb.append(_stock);
			}
			break;
		
		case CANCELLED:
			sb.append("  Canceled - runs on ");
			for(i = 1, j = 1; i < 0x80; i <<= 1, ++j)
			    if((_days & i) != 0)
			    	sb.append("0" + j);
			break;
		
		case DELAYED:
			return "Delayed entry at " + _entrance;

		case DERAILED:
			break;

		case READY:
		    if(_entryDelay == null || _entryDelay._nSeconds == 0)
		    	break;
		    sb.append(" ETA ");
		    sb.append(TDTime.toString(_timeIn + _entryDelay._nSeconds));
			break;

		case RUNNING:
			finalStop = nextStop(sb);
			if (_shunting) {
				sb.append(" (shunting)");
			}
			sb.append(" Speed: " + _speed);
			sb.append(" km/h");
			if (!finalStop) {
				sb.append(" to ");
				sb.append(_exit);
			}
			break;

		case STOPPED:
		    int timedep = _timeDep;
		    TrainStop stp = findStop(_position);
		    if(stp != null && stp._depDelay != null && stp._depDelay._nSeconds != 0)
		    	timedep += stp._depDelay._nSeconds;
		    else if(_position._isStation && Territory.sameStation(_entrance, _position._station) && _entryDelay != null)
		    	timedep += _entryDelay._nSeconds;
		    sb.append("  ETD ");
		    sb.append(TDTime.toString(timedep));
		    boolean isFinalStop = nextStop(sb);
		    if(!isFinalStop) {
		    	sb.append(" Dest. ");
		    	sb.append(_exit);
		    }
			break;

		case WAITING:
			finalStop = nextStop(sb);
			if (!finalStop) {
				sb.append(" Dest. ");
				sb.append(_exit);
			}
			break;
			
		case STARTING:
			sb.append(" (-" + _startDelay);
			sb.append(")");
		}
		return sb.toString();
	}

	public void addNote(String s) {
		if (_notes == null)
			_notes = new ArrayList<String>();
		_notes.add(s);
	}

	public String getNotesAsString() {
		if (_notes == null || _notes.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		for (String s : _notes) {
			sb.append(s);
			sb.append(' ');
		}
		return sb.toString();
	}

	public Track getHeadTrack() {
		return _path.getTrackAt(0);
	}

	public boolean stopsAt(Track track) {
		for (TrainStop stop : _stops) {
			if (stop._minstop != 0 && stop._station.equals(track._station)) {
				return true;
			}
		}
		return track._station.equals(_exit);
	}

	// TODO: what if our tail overlays multiple stations?
	public Track getStoppedAt() {
		if (_status != TrainStatus.STOPPED && _status != TrainStatus.ARRIVED)
			return null;
		if (_position == null)
			return null;
		if (_position._isStation)
			return _position;
		if (_tail == null || _tail._path == null)
			return null;
		int i = _tail._path.lastIndexOf(_position);
		while (i >= 0) {
			Track track = _tail._path.getTrackAt(i);
			if (track._isStation)
				return track;
			--i;
		}
		return null;
	}

	public boolean isSet(int flag) {
		return (_flags & flag) != 0;
	}

	public void setFlag(int  flag) {
		_flags |= flag;
	}

	public void clearFlag(int  flag) {
		_flags &= ~flag;
	}

	public String getRunInfo() {
		StringBuilder sb = new StringBuilder();
		if (_position != null) {
			sb.append(_position._position.toString());
		}
		sb.append(" ");
		sb.append(_length);
		sb.append(" m, slow: ");
		sb.append(_distanceToSlow);
		sb.append(" m, stop: ");
		sb.append(_distanceToStop);
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (_position != null) {
			sb.append(_position._position.toString());
		}
		sb.append(" ");
		sb.append(_name);
		sb.append(" ");
		sb.append(this.getStatusAsString());
		sb.append(" ");
		sb.append(_speed);
		sb.append(" km/h, slow: ");
		sb.append(_distanceToSlow);
		sb.append(" m, stop: ");
		sb.append(_distanceToStop);
		return sb.toString();
	}

	/**
	 * nextStop
	 *
	 * @param sb buffer to fill with stop information
	 * @return true if next is final stop
	 */

	public boolean nextStop(StringBuilder sb) {
		if(_status != TrainStatus.RUNNING && _status != TrainStatus.WAITING && _status != TrainStatus.STOPPED)
		    return false;
		Track tr;
		TrainStop last = null;
		for(TrainStop ts : _stops) {
		    if(ts._minstop == 0)
		    	continue;
			tr = Simulator.INSTANCE._territory.findStationNamed(ts._station);
		    if(tr == null || (tr instanceof TextTrack))
		    	continue;
		    if(ts._stopped)
		    	continue;
			last = ts;
			break;
		}
		if(last == null) {
			tr = Simulator.INSTANCE._territory.findStationNamed(_exit);
		    if(tr == null || (tr instanceof TextTrack))
	    		return false;
		    sb.append(" Final stop ");
		    sb.append(_exit);
		    sb.append(" at ");
		    sb.append(TDTime.toString(_timeOut));
		    return true;
		}
		sb.append(" Next stop ");
		sb.append(last._station);
		sb.append(" at ");
		sb.append(TDTime.toString(last._arrival));
		return false;
	}

	public void setPropertyValue(String prop, ExprValue value) {
		if (prop.equalsIgnoreCase("shunt")) {
			if (_status != TrainStatus.STOPPED
					&& _status != TrainStatus.WAITING
					&& _status != TrainStatus.ARRIVED) {
				Simulator.INSTANCE.alert(String.format("Train %s: cannot set property %s. Train is not stopped.",
						_name, prop));
				return;
			}
			// TODO: shunt_train(this);
			return;
		}
		if (prop.equalsIgnoreCase("wait")) {
			if (_status == TrainStatus.RUNNING || _speed != 0) {
				Simulator.INSTANCE.alert(String.format("Train %s: cannot set property %s. Train is not stopped.",
						_name, prop));
				return;
			}
			// TODO: _timedep += value._val;
			return;
		}
	}

	public boolean getPropertyValue(String prop, ExprValue result) {
		if (prop.equalsIgnoreCase("name")) {
			result._op = NodeOp.String;
			result._txt = _name;
			return true;
		}
		if (prop.equalsIgnoreCase("type")) {
			result._op = NodeOp.Number;
			result._val = _type;
			return true;
		}
		if (prop.equalsIgnoreCase("speed")) {
			result._op = NodeOp.Number;
			result._val = _speed;
			return true;
		}
		if (prop.equalsIgnoreCase("length")) {
			result._op = NodeOp.Number;
			result._val = _length;
			return true;
		}
		if (prop.equalsIgnoreCase("arrived")) {
			result._op = NodeOp.Number;
			result._val = _status == TrainStatus.ARRIVED ? 1 : 0;
			return true;
		}
		if (prop.equalsIgnoreCase("stopped")) {
			result._op = NodeOp.Number;
			result._val = _status == TrainStatus.STOPPED ? 1 : 0;
			return true;
		}
		if (prop.equalsIgnoreCase("direction")) {
			result._op = NodeOp.Number;
			result._val = _direction.ordinal();
			return true;
		}
		if (prop.equalsIgnoreCase("days")) {
			result._op = NodeOp.Number;
			result._val = _days;
			return true;
		}
		if (prop.equalsIgnoreCase("entry")) {
			result._op = NodeOp.String;
			result._txt = _entrance;
			return true;
		}
		if (prop.equalsIgnoreCase("exit")) {
			result._op = NodeOp.String;
			result._txt = _exit;
			return true;
		}
		if (prop.equalsIgnoreCase("nextStation")) {
			TrainStop stop = null;
			int i = 0;
			for (i = 0; i < _stops.size(); ++i) {
				stop = _stops.get(i);
				if (!stop._stopped)
					break;
			}
			if (i >= _stops.size())
				return false;
			result._op = NodeOp.String;
			result._txt = stop._station;
			return true;
		}
		if (prop.equalsIgnoreCase("stock")) {
			result._op = NodeOp.String;
			result._txt = _stock;
			return true;
		}
		if (prop.equalsIgnoreCase("waitfor")) {
			result._op = NodeOp.String;
			result._txt = _waitFor;
			return true;
		}
		if (prop.equalsIgnoreCase("status")) {
			result._op = NodeOp.String;
			switch (_status) {
			case READY:
				result._txt = "ready";
				break;
			case RUNNING:
				if (_shunting)
					result._txt = "shunting";
				else
					result._txt = "running";
				break;
			case STOPPED:
				result._txt = "stopped";
				break;
			case DELAYED:
				result._txt = "delayed";
				break;
			case WAITING:
				result._txt = "waiting";
				break;
			case DERAILED:
				result._txt = "derailed";
				break;
			case ARRIVED:
				result._txt = "arrived";
				break;
			}
			return true;
		}
		return false;
	}

	public boolean isMerging() {
		return (_flags & Train.MERGING) != 0;
	}

	public boolean isArrived() {
		return _status == TrainStatus.ARRIVED;
	}

	public boolean isWaiting() {
		return _status == TrainStatus.WAITING;
	}

	public boolean isCancelled() {
		return _status == TrainStatus.CANCELLED;
	}

	public void reset() {
		if (_path != null) {
			_path.setStatus(TrackStatus.FREE, 0);
			_path = null;
		}
		if (_tail != null && _tail._path != null) {
			_tail._path.setStatus(TrackStatus.FREE, 0);
			_tail._path = null;
		}
		
		_status = TrainStatus.READY;
		
		for (TrainStop stop : _stops) {
			stop.reset();
		}
		_position = null;
		_stopping = null;
		_distanceToStop = 0;
		_distanceToSlow = 0;
		_stopPoint = null;
		_slowPoint = null;
		_etd = 0; // TODO: remove
		_timeDep = 0;
		_speed = 0;
		_curmaxspeed = _maxspeed;
		_speedlimit = 0; 
		_minDel = 0;
		_minLate = 0;
		_inDelay = 0;
		_gotDelay = false;
		_shunting = false;
		_entryDelay = null;
		_trackDistance = 0; // distance still to travel in path[0]
		_entryDistanceToGo = 0; // distance of a tail from the territory
										// entry track
		_flags = 0;

		_exited = null;		// the station where the train exited, if not the correct one
		_timeExited = 0;
		_wrongDest = false;

		_trackpos = 0;		// should be unused, here for backward compatibility with .sav files

		// statistics about train's performance
		_timeLate = 0;
		_timeDelay = 0;
		_timeRed = 0;
		_waitTime = 0;
		_arrived = 0;

		// state of running trains
		_pathtravelled = 0;
		_timeDep = 0;
		_needFindStop = false;
		_outOf = null;
		_tailEntry = 0;
		_tailExit = 0;
		_merging = null;	// we are bound to merge with this train
		 _fleet = null;		// TODO: clear all fleeted signals

	}

	public void arrived() {
		Simulator sim = Simulator.INSTANCE;
		PerformanceCounters perf_tot = sim._performanceCounters;
		
		_status = TrainStatus.ARRIVED;
		_speed = 0;
		
		// penalty: check late arrival
		
		int minlate;
		int arrTime = _timeOut;
		
		if (arrTime < _timeIn)
			arrTime += 24 * 60 * 60;
		minlate = (sim._simulatedTime - arrTime) / 60;
		if ((_flags & Train.SETLATEARRIVAL) != 0)	/* we stopped here before! */
		    minlate = 0;
		else
		    _timeExited = sim._simulatedTime;
		_flags |= SETLATEARRIVAL;
		_arrived = 1;
		if (minlate > 0) {
		    _timeLate += minlate;
		    // total_late += minlate;
		    if(sim._options._hardCounters.isSet() || (_flags & ENTEREDLATE) == 0)
		    	++perf_tot.late_trains;
		} else for(TrainStop ts : _stops) {
		    if(ts._late && (sim._options._hardCounters.isSet() || (_flags & ENTEREDLATE) == 0)) {
		    	++perf_tot.late_trains;
		    	break;
		    }
		}

		// get entry delay
		// penalty: check correct destination
		
		for (TrainStop ts : _stops) {
		    if(sim._territory.findStation(ts._station) != null && !ts._stopped)
		    	++perf_tot.nmissed_stops;
		}

		if (_position != null && _position instanceof TextTrack)
			_position = null;	// remove it from the territory
		onArrived();
	}

	public void derailed() {
		_status = TrainStatus.DERAILED;
		_speed = 0;
		_position = null;
		_tail = null;
	}

	public void shunt() {
		_shunting = true;
	}

	public void merge() {
	}

	public boolean isStranded() {
		// TODO stranded
		return false;
	}

	public void doWaiting(Signal signal) {
		onWaiting(signal);
		_status = TrainStatus.WAITING;
	}

	public void stopped() {
		_status = TrainStatus.STOPPED;
		_speed = 0;
		onStopped();
	}

	public void start() {
		onStart();
	}

	public TrainStop findStop(Track station) {
		for (TrainStop stop : _stops) {
			if (Territory.sameStation(stop._station, station._station))
				return stop;
		}
		return null;
	}

	public TrainStop findStop(String station) {
		for (TrainStop stop : _stops) {
			if (Territory.sameStation(stop._station, station))
				return stop;
		}
		return null;
	}

	public boolean stoppingAtStation(Track position) {
		if (_shunting && !isSet(DONTSTOPSHUNTERS)) {
			return false;
		}
		for (TrainStop stop : _stops) {
			if (stop._minstop != 0 && Territory.sameStation(stop._station, position._station)) {
				return true;
			}
		}
		if (Territory.sameStation(position._station, _exit))
			return true;
		if (_shunting && _outOf != position)
			return true;
		return false;
	}

	private void onArrived() {
		if (_script != null)
			_script.handle("OnArrived", null, this);
	}

	private void onStopped() {
		if (_script != null)
			_script.handle("OnStop", null, this);
	}

	private void onStart() {
		if(_script != null)
			_script.handle("OnStart", null, this);
	}

	public void onWaiting(Signal signal) {
		if(_script != null)
			_script.handle("OnWaiting", signal, this);
	}

	public void onEntry() {
		if(_script != null)
			_script.handle("OnEntry", null, this);
	}

	public void onExit() {
		if(_script != null)
			_script.handle("OnExit", null, this);
	}

	public void onAssign() {
		if(_script != null)
			_script.handle("OnAssign", null, this);
	}

	public void onReverse() {
		if(_script != null)
			_script.handle("OnReverse", null, this);
	}

	public void onShunt() {
		if(_script != null)
			_script.handle("OnShunt", null, this);
	}

	public boolean runsToday() {
		if(_days == 0)
			return true;
		return (_days & Simulator.INSTANCE._runDay) != 0;
	}

	public boolean isRunning() {
		switch (_status) {
		case RUNNING:
		case STARTING:
			return true;
		}
		return false;
	}

}
