package com.traindirector.model;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.commands.ClickCommand;
import com.traindirector.scripts.ExprValue;
import com.traindirector.scripts.NodeOp;
import com.traindirector.scripts.TDSScript;
import com.traindirector.simulator.PathFinder;
import com.traindirector.simulator.Simulator;

public class Signal extends Track {

	public boolean _fleeted;
	public boolean _fixedred;
	public boolean _nopenalty;
	public boolean _signalx;
	public boolean _noClickPenalty;
	public SignalAspect _currentAspect;

	public List<SignalAspect> _aspects = new ArrayList<SignalAspect>();
	public Track _controls;
	public boolean _aspectChanged;
	private boolean _isApproach;
	private boolean _isShunting;
	private boolean _nowfleeted;
	public TDPosition _blockedBy;
	public boolean _intermediate;

	public Signal() {
	    
	}

	public Signal(TDPosition pos) {
	    super(pos);
    }

    public SignalAspect getAspect() {
		return _currentAspect;
	}

	public void addAspect(SignalAspect asp) {
		_aspects.add(asp);
	}

	public SignalAspect findAspect(String name) {
		for (SignalAspect asp : _aspects) {
			if (asp._name.compareTo(name) == 0)
				return asp;
		}
		return null;
	}

	public boolean isClear() {
		return _currentAspect._action.compareTo(SignalAspect.STOP) != 0;
	}

	public Direction getDirectionFrom(TrackDirection direction) {
		Direction dir = null;
		switch (direction) {
		case E_W:
			dir = Direction.W;
			break;
		case NE_S:
			break;
		case NW_E:
			break;
		case NW_S:
			break;
		case NW_SE:
			break;
		case N_NE_S_SW:
			break;
		case N_NW_S_SE:
			break;
		case N_S:
			dir = Direction.S;
			break;
		case N_S_E:
			break;
		case N_S_W:
			break;
		case SE_N:
			break;
		case SW_E:
			break;
		case SW_N:
			break;
		case SW_NE:
			break;
		case S_N:
			dir = Direction.N;
			break;
		case TRK_N_S:
			break;
		case W_E:
			dir = Direction.E;
			break;
		case W_NE:
			break;
		case W_SE:
			break;
		case XH_NW_SE:
			break;
		case XH_SW_NE:
			break;
		case X_PLUS:
			break;
		case X_X:
			break;
		case signal_NORTH_FLEETED:
			break;
		case signal_SOUTH_FLEETED:
			break;
		default:
			break;
		}
		return dir;
	}

	public boolean toggle() {
		if (_controls == null) {
			// TODO: penalty
			return false;
		}
		
		// TODO: always red
		
		SignalAspect aspect = getAspect(); // get current aspect
		if (aspect._action.equalsIgnoreCase(SignalAspect.NONE))
			return true;
		
		PathFinder finder = new PathFinder();
		Direction dir = getDirectionFrom(_direction);
		TrackPath path = finder.find(_controls._position, dir);

		if (isClear()) {
			// TODO: penalty if !fleeted
			setAspectFromName(SignalAspect.RED);
			onUnclear();
			_nowfleeted = false;
			if (path == null)
				return false;
			finder.setAs(path, TrackStatus.FREE);
			return true;
		}
		if (path == null) {
			return false;
		}

		// we want to clear the signal, check if the controlled block is free
		if (!finder.isFree(path)) {
			// TODO: penalty;
			return false;
		}

		if (this._script != null && _script instanceof TDSScript) {
			// try to set the signal's aspect using the associated script, if any
			if (!_script.handle("onCleared", this, null))
				setAspectFromName(SignalAspect.GREEN);
		} else
			setAspectFromName(SignalAspect.GREEN);
		finder.setAs(path, TrackStatus.BUSY);
		return true;
	}

	@Override
	public void onClick() {
		toggle();
	}
	
	@Override
	public void onRightClick() {
		if(!isClear())
			return;
		_fleeted = !_fleeted;
		_nowfleeted = _fleeted;
		if(_fleeted)
			setAspectFromName("greenFleeted");
		else
			setAspectFromName("green");
	}

	public void setAspectFromName(String action) {
		for (SignalAspect asp : _aspects) {
			if (asp._name.equalsIgnoreCase(action)) {
				_currentAspect = asp;
				return;
			}
		}
	}

	public void setAspectFromAction(String action) {
		for (SignalAspect asp : _aspects) {
			if (asp._name.equalsIgnoreCase(action)) {
				_currentAspect = asp;
				return;
			}
		}
	}

	public boolean linkedTo(Track track) {
		if (_controls == track)
			return true;
		return false;
	}

	public void SetPropertyValue(String prop, ExprValue val) {
		Signal s = this;
		ClickCommand ccmd = null;

		if (prop.equalsIgnoreCase("thrown")) {
			// call t->Throw(val._val);
		} else if (prop.equalsIgnoreCase("aspect")) {
			s.setAspectFromName(val._txt);
		} else if (prop.equalsIgnoreCase("auto")) {
			s._fleeted = val._val != 0;
		} else if (prop.equalsIgnoreCase("enabled")) {
			s._nowfleeted = val._val != 0;
		} else if (prop.equalsIgnoreCase("fleeted")) {
			s._fleeted = val._val != 0;
			s._nowfleeted = val._val != 0;
		} else if (prop.equalsIgnoreCase("shunting")) {
			s._isShunting = val._val != 0;
		} else if (prop.equalsIgnoreCase("click")) {
			ccmd = new ClickCommand(_position);
		} else if (prop.equalsIgnoreCase("rclick")) {
			ccmd = new ClickCommand(_position);
			ccmd.setLeftClick(false);
		} else if (prop.equalsIgnoreCase("ctrlclick")) {
			ccmd = new ClickCommand(_position);
			ccmd.setCtrlKey(true);
		}
		if (ccmd != null) {
			Simulator.INSTANCE.addCommand(ccmd);
		}
	}

	public boolean getPropertyValue(String prop, ExprValue result) {
		boolean res;
		TrackPath path;
		int i;

		// wxStrncat(expr_buff, prop, sizeof(expr_buff)-1);

		if (prop.equalsIgnoreCase("aspect")) {
			result._op = NodeOp.String;
			result._txt = getAspect()._name;
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%s}"), result._txt);
			return true;
		}
		if (prop.equalsIgnoreCase("auto")) {
			result._op = NodeOp.Number;
			result._val = _fleeted ? 1 : 0;
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%d}"), result._val);
			return true;
		}
		if (prop.equalsIgnoreCase("enabled")) {
			result._op = NodeOp.Number;
			result._val = (_fleeted && _nowfleeted) ? 1 : 0;
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%d}"), result._val);
			return true;
		}

		result._op = NodeOp.Number;
		result._val = 0;
		if (prop.equalsIgnoreCase("switchThrown")) {
			path = getNextPath();
			if (path == null)
				return false;

			for (i = 0; i < path.getTrackCount(0); ++i) {
				Track trk = path.getTrackAt(i);

				if (!(trk instanceof Switch))
					continue;
				Switch sw = (Switch) trk;
				switch (sw._direction.ordinal()) { // TODO: use real directions
				case 10: // these are Y switches, which are always
				case 11: // considered as going to the main line,
				case 22: // thus ignored as far as signals are concerned.
				case 23:
					continue;
				}
				if (sw._switched) {
					result._val = 1;
					break;
				}
			}
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%s}"), result._val ? wxT("switchThrown") :
			// wxT("switchNotThrown"));
			return true;
		}
		if (prop.equalsIgnoreCase("nextLimit")) {
			path = getNextPath();
			if (path == null)
				return false;

			int j;
			int lowSpeed = 1000;

			for (i = 0; i < path.getTrackCount(0); ++i) {
				Track trk = path.getTrackAt(i);

				for (j = 0; j < trk._length; ++j)
					if (trk._speed[j] != 0 && trk._speed[j] < lowSpeed)
						lowSpeed = trk._speed[j];
			}
			result._val = lowSpeed;
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%d}"), lowSpeed);
			return true;
		}
		if (prop.equalsIgnoreCase("nextLength")) {
			path = getNextPath();
			if (path == null)
				return false;

			result._val = path.getDistance(0);
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%d}"), length);
			return true;
		}
		if (prop.equalsIgnoreCase("nextApproach")) {
			return getApproach(result);
		}
		if (prop.equalsIgnoreCase("nextIsApproach")) {
			res = getApproach(result);
			result._op = NodeOp.Number;
			result._val = (res == true) ? 1 : 0;
			return true;
		}
		if (prop.equalsIgnoreCase("nextStation")) {
			result._op = NodeOp.String;
			result._txt = "";

			path = getNextPath();
			if (path == null)
				return false;

			for (i = 0; i < path.getTrackCount(0); ++i) {
				Track trk = path.getTrackAt(i);

				if (!trk._isStation)
					continue;
				result._txt = trk._station;
				break;
			}
			// wxSnprintf(expr_buff + wxStrlen(expr_buff),
			// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
			// wxT("{%s}"), result._txt);
			return true;
		}
		/*
		 * TODO if(prop.equalsIgnoreCase("color")) { result._op = NodeOp.String;
		 * result._txt = ""; if(s._controls != null) result._txt =
		 * GetColorName(s._controls._fgcolor); return true; }
		 */
		return false;
	}

	public boolean isApproach() {
		// is signal an approach signal?
		return _isApproach;
	}

	public boolean isShuntingSignal() {
		// is a shunting signal?
		return _isShunting;
	}

	public void onClear() {
		// set signal to green
	}

	public void onUnclear() {
		// set signal to red
	}

	public void onShunt() {
		// set signal to clear for shunting
	}

	public void onCross() {
		// set signal to red when a train enters the controlled section
	}

	public void onUnlock() {
		// set signal to green after path has become clear
	}

	public void onUnfleet() {
		// set fleeted signal to green after path has become clear
	}

	public void onUpdate() {
		// some other signal changed, see if we need to change, too
		if (_aspectChanged)
			return;
		if (_script == null)
			return;
		_script.handle("OnUpdate", this, null);
	}

	public void onInit() {
		// initial setting (when load or restart)
	}

	public void onFlash() {
		// display next flashing aspect
	}

	public void onAuto() {
		// automatic signal has been enabled/disabled
	}

	public void onClicked() {
		// for shunting signals
	}

	private TrackPath getNextPath() {
		Signal s = this;
		if (s._fixedred) {
			// wxStrncat(expr_buff, wxT("signal is always red"),
			// sizeof(expr_buff)-1);
			return null;
		}
		PathFinder finder = new PathFinder();
		TDPosition position = _controls._position;
		TrackPath path = finder.find(position, getDirectionFrom(_direction));
		if (path == null)
			return null;
		if (Simulator.INSTANCE._gMustBeClearPath) {
			// if(!s->IsClear()) { // t->status == ST_GREEN) {
			// Vector_delete(path);
			// return true;
			// }
			if (!path.isFree())
				// wxStrncat(expr_buff, wxT("path is busy"),
				// sizeof(expr_buff)-1);
				return null;
		}
		return path;
	}

	public boolean getApproach(ExprValue result) {
		boolean res = false;
		TrackPath path;
		int i;

		path = getNextPath();
		if (path == null)
			return res;

		for (i = 0; i < path._tracks.size(); ++i) {
			Track trk = path.getTrackAt(i);
			Signal sig;
			sig = Simulator.INSTANCE._territory.findSignalLinkedTo(trk,
					path.getDirectionAt(i));
			if (sig != this && !sig.isShuntingSignal() && sig.isApproach()) {
				result._op = NodeOp.Addr;
				result._signal = sig;
				result._track = sig;
				break;
			}
		}
		res = (i < path._tracks.size());
		// wxSnprintf(expr_buff + wxStrlen(expr_buff),
		// sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
		// wxT(" found"));
		return res;
	}

	public String toString() {
		if (_position == null)
			return "Null position.";
		if (_controls == null)
			return _position.toString() + " -> ? aspect: " + _currentAspect._name;
		return _position.toString() + " -> " + _controls._position.toString() + " aspect: " + _currentAspect._name;
	}

	public boolean isFleeted() {
		return _nowfleeted;
	}
	
	public void setFleeted(boolean v) {
		_nowfleeted = v;
	}

	public void unlock() {
		// TODO Auto-generated method stub
	}

	public void doUnclear() {
		if (_script == null)
			return;
		_script.handle("OnClear", this, null);
	}

	public void doCross(Train train) {
		if (_script != null) {
			_script.handle("OnCross", this, null);
		}
		_aspectChanged = true;
		if(!_currentAspect._action.equals(SignalAspect.STOP)) {
			for(SignalAspect aspect : _aspects) {
				if (aspect._action.equals(SignalAspect.STOP)) {
					_currentAspect = aspect;
					break;
				}
			}
		}
	}
}
