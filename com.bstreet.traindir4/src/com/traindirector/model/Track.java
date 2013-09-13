package com.traindirector.model;

import org.eclipse.ui.internal.SwitchToWindowMenu;

import com.traindirector.commands.ClickCommand;
import com.traindirector.scripts.ExprValue;
import com.traindirector.scripts.NodeOp;
import com.traindirector.scripts.Script;
import com.traindirector.scripts.ScriptFactory;
import com.traindirector.simulator.Simulator;

public class Track {

	public static final int NSPEEDS = 10;

	public static final int CHANGED = 1;		// needed to avoid accessing Simulator._updatedTime directly
	
	public	int _type;
	public	TrackDirection _direction;
	public TDPosition _position;
	public TDPosition _wlink;
	public TDPosition _elink;
	public Track _wlinkTrack, _elinkTrack;	// computed for internal use
	public boolean _isStation;
	public int _length;
	public int[] _speed;
	public int _km;
	public String _station;
	public TrackStatus _status = TrackStatus.FREE;
	public String _scriptFile;
	public Script _script;
	public boolean _invisible;
	public int _flags;
	public long _updateTime;
	public Signal _esignal, _wsignal;

	// performance flags
	public static final int TURNED = 1;
	public static final int THROWN = 2;

	public static final int DONTSTOPSHUNTERS = 1024; // don't stop here if train is shunting

	public Track() {
		_position = new TDPosition();
		_wlink = new TDPosition();
		_elink = new TDPosition();
		_speed = new int[NSPEEDS];
	}
	
	public Track(int x, int y) {
		_position = new TDPosition(x, y);
		_wlink = new TDPosition();
		_elink = new TDPosition();
		_speed = new int[NSPEEDS];
	}

	public Track(TDPosition pos) {
	    _position = pos;
        _wlink = new TDPosition();
        _elink = new TDPosition();
		_speed = new int[NSPEEDS];
    }

    public Direction walk(Direction dir) {
		if (_direction != null) {
			switch(_direction) {
	
			case NE_S:
				return dir == Direction.SW || dir == Direction.S ? Direction.S : Direction.NE;
	
			case NW_E:
				return dir == Direction.SE || dir == Direction.E ? Direction.E : Direction.NW;
	
			case NW_S:
				return dir == Direction.SE || dir == Direction.S ? Direction.S : Direction.NW;
	
			case SE_N:
				return dir == Direction.NW || dir == Direction.N ? Direction.N : Direction.SE;
	
			case SW_E:
				return dir == Direction.NE || dir == Direction.E ? Direction.E : Direction.SW;
	
			case SW_N:
				return dir == Direction.NE || dir == Direction.N ? Direction.N : Direction.SW;
	
			case W_NE:
				return dir == Direction.SW || dir == Direction.W ? Direction.W : Direction.NE;
	
			case W_SE:
				return dir == Direction.NW || dir == Direction.W ? Direction.W : Direction.SE;
	
			case E_W:
			case W_E:
			case N_S:
			case X_PLUS:
			case X_X:
			case NW_SE:
			case SW_NE:
			case XH_NW_SE:
			case XH_SW_NE:
			case N_NE_S_SW:
			case N_NW_S_SE:
			case S_N:
			case TRK_N_S:
			case N_S_E:
			case N_S_W:
			case signal_NORTH_FLEETED:
			case signal_SOUTH_FLEETED:
			default:
				break;
			}
		}
		return dir;
	}

    public boolean getNextTrack(Direction dir, TrackAndDirection out) {
    	Territory territory = Simulator.INSTANCE._territory;
		Track newTrack = null;
		if (!(this instanceof Switch)) {
			newTrack = territory.findTrackLinkedTo(this, dir);
			if (newTrack != null) {
				out._track = newTrack;
				out._direction = dir;
				// TODO: set direction based on old and new track positions
				return true;
			}
		}
		Direction newDir = walk(dir);
		TDPosition newPos = newDir.offset(_position);
		//System.out.println(">> " + track.toString() + " [" + oldDir.toString() + " -> " + newDir.toString() + "] -> " + newPos.toString());
		newTrack = territory.findTrack(newPos);
		if (newTrack == null || (newTrack instanceof TextTrack))
			return false;
		out._track = newTrack;
		out._direction = newDir;
		return true;
    }
    
	public void setStatus(TrackStatus status) {
		if(_status != status) {
			_flags |= CHANGED;
			_status = status;
		}
	}

	public void setUpdated(long timeOfUpdate) {
		_updateTime = timeOfUpdate;
	}
	
	public void setUpdated() {
		setUpdated(Simulator.INSTANCE._updateCounter++);
	}

	public void onClick() {
		if(_script != null)
			_script.handle("OnClicked", this, null);
	}
	
	public void onRightClick() {
		
	}

	public void onShiftClick() {
		
	}
	
	public void onAltClick() {
		
	}
	
	public void onCtrlClick() {
		
	}


	public String speedsToString() {
    	StringBuilder value = new StringBuilder(); 
    	for(int i = 0; i < _speed.length; ++i) {
    		value.append("/" + _speed[i]); 
    	}
    	return value.toString().substring(1);
	}

	public void readSpeeds(String string) {
		int i;
		String[] s = string.split("/");

		if(_speed == null)
			_speed = new int[NSPEEDS];
		for(i = 0; i < s.length; ++i) {
			_speed[i] = Integer.parseInt(s[i]);
		}
		while(i < NSPEEDS)
			_speed[i++] = 0;
	}

	public void parseMilepost(String string) {
		String[] s = string.split("\\.");
		if(s.length < 1)
			return;
		_km = Integer.parseInt(s[0]) * 1000;
		if(s.length > 1)
			_km += Integer.parseInt(s[1]) % 1000;
	}
	

	public VLine[] getSegments() {
		if(_direction == null)
			return null;
		return getSegments(_direction);
	}
	
	public int getSpeedLimit(int trainType) {
		if(trainType < NSPEEDS && _speed != null) {
			return _speed[trainType];
		}
		return 0;
	}

	public String toString() {
		String s = _position.toString() + " length: " + _length;
		if (_station != null)
			s += "  " + _station;
		if (_scriptFile != null)
			s += "  script: " + _scriptFile;
		return s;
	}
	
	public static VLine[] getSegments(TrackDirection direction) {
		switch(direction) {
		case E_W:
		case W_E:
		{
			VLine	w_e_layout[] = {
					/*new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),*/
					new VLine(0, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine(0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
				};
			return w_e_layout;
		}
		case NW_SE:
		{
			
			VLine	nw_se_layout[] = {
					new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
				};
			return nw_se_layout;
		}

		case SW_NE:
		{			
			VLine	sw_ne_layout[] = {
					new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
					new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
					new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
			};
			return sw_ne_layout;
		}
		
		case W_NE:
		{
				VLine	w_ne_layout[] = {
					//new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID / 2, Simulator.VGRID / 2 - 1 ),
					new VLine( 0, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2, Simulator.VGRID / 2 - 0 ),
					new VLine( 0, Simulator.VGRID / 2 + 1, Simulator.HGRID / 2, Simulator.VGRID / 2 + 1 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 1, Simulator.HGRID - 2, 0 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, 0 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, 1 ),
				};
				return w_ne_layout;
		}

		case W_SE:
		{
			VLine	w_se_layout[] = {
					//new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 - 1 ),
					new VLine( 0, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2, Simulator.VGRID / 2 - 0 ),
					new VLine( 0, Simulator.VGRID / 2 + 1, Simulator.HGRID / 2, Simulator.VGRID / 2 + 1 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 , Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 + 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
				};
			return w_se_layout;
		}
		
		case NW_E:
		{
			VLine	nw_e_layout[] = {
					new VLine( 1, 0, Simulator.HGRID / 2, Simulator.VGRID / 2 - 1 ),
					new VLine( 0, 0, Simulator.HGRID / 2, Simulator.VGRID / 2 - 0 ),
					new VLine( 0, 1, Simulator.HGRID / 2, Simulator.VGRID / 2 + 1 ),
					//new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 )
				};
			return nw_e_layout;
		}
		case SW_E:
		{
			VLine	sw_e_layout[] = {
					new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 ),
					new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID / 2, Simulator.VGRID / 2 - 0 ),
					new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID / 2, Simulator.VGRID / 2 + 1 ),
					//new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
				};
			return sw_e_layout;
		}
		case TRK_N_S:
		{
			VLine	n_s_layout[] = {
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
				};
			return n_s_layout;
		}
		case SW_N:
		{
			VLine	sw_n_layout[] = {
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2, 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2, 0, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 - 1, 0, Simulator.VGRID - 2 ),
				};
			return sw_n_layout;
		}
		case NW_S:
		{
			VLine	nw_s_layout[] = {
					new VLine( 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 ),
					new VLine( 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 ),
					new VLine( 0, 1, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
				};

				return nw_s_layout;
		}
		case SE_N:
		{
			VLine	se_n_layout[] = {
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
				};
				return se_n_layout;
		}
		case NE_S:
		{
			VLine	ne_s_layout[] = {
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 1, Simulator.HGRID - 2, 0 ),
					new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2, Simulator.HGRID - 1, 0 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2, Simulator.HGRID - 1, 1 ),
					new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID / 2 - 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
				};
			return ne_s_layout;
		}

		case X_X:
		{
			VLine	x_x_layout[] = {
					new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
					new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
					new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
					new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
			};
			return x_x_layout;
		
		}
		case X_PLUS:
		{
			VLine	x_plus_layout[] = {
					/*new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),*/
					new VLine(0, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine(0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
			};
			return x_plus_layout;
		}
		
		case XH_SW_NE:
		{
			VLine	xh_sw_ne_layout[] = {
					/*new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),*/
					new VLine(0, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine(0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
					new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
					new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
					new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
			};
			return xh_sw_ne_layout;
		}
		
		case XH_NW_SE:
		{
			VLine	xh_nw_se_layout[] = {
					/*new VLine( 0, Simulator.VGRID / 2 - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 - 1 ),*/
					new VLine(0, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
					new VLine(0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
					new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
			};
			return xh_nw_se_layout;
		}

		case N_NE_S_SW:
		{
			VLine	n_s_layout[] = {
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
					new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
					new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
					new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
			};
			return n_s_layout;
		}

		case N_NW_S_SE:
		{
			VLine	n_nw_s_se_layout[] = {
					new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
					new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),
					new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
					new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
					new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
			};
			return n_nw_s_se_layout;
			
		}
		/*
		case N_S:
		case N_S_W:
		case N_S_E:
		case S_N:
		case signal_SOUTH_FLEETED:
		case signal_NORTH_FLEETED:



VLine	block_layout[] = {
	new VLine( Simulator.HGRID / 2, Simulator.VGRID / 2 - 1, Simulator.HGRID / 2, Simulator.VGRID / 2 + 2 ),
	new VLine( -1 }
};

VLine	block_layout_ns[] = {
	new VLine( Simulator.HGRID / 2 - 1, Simulator.VGRID / 2, Simulator.HGRID / 2 + 2, Simulator.VGRID / 2 ),
	new VLine( -1 }
};

VLine	sw_ne_layout[] = {
	new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
	new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
	new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
	new VLine( -1 }
};


VLine	w_e_platform_out[] = {
	new VLine( 0, Simulator.VGRID / 2 - 3, Simulator.HGRID - 1, Simulator.VGRID / 2 - 3 ),
	new VLine( 0, Simulator.VGRID / 2 + 3, Simulator.HGRID - 1, Simulator.VGRID / 2 + 3 ),
	new VLine( 0, Simulator.VGRID / 2 - 3, 0, Simulator.VGRID / 2 + 3 ),
	new VLine( Simulator.HGRID - 1, Simulator.VGRID / 2 - 3, Simulator.HGRID - 1, Simulator.VGRID / 2 + 3 ),
	new VLine( -1 }
};

VLine	w_e_platform_in[] = {
	new VLine( 1, Simulator.VGRID / 2 - 2, Simulator.HGRID - 2, Simulator.VGRID / 2 - 2 ),
	new VLine( 1, Simulator.VGRID / 2 - 1, Simulator.HGRID - 2, Simulator.VGRID / 2 - 1 ),
	new VLine( 1, Simulator.VGRID / 2 - 0, Simulator.HGRID - 2, Simulator.VGRID / 2 - 0 ),
	new VLine( 1, Simulator.VGRID / 2 + 1, Simulator.HGRID - 2, Simulator.VGRID / 2 + 1 ),
	new VLine( 1, Simulator.VGRID / 2 + 2, Simulator.HGRID - 2, Simulator.VGRID / 2 + 2 ),
	new VLine( -1 }
};

VLine	n_s_platform_out[] = {
	new VLine( Simulator.HGRID / 2 - 3, 0, Simulator.HGRID / 2 - 3, Simulator.VGRID - 1 ),
	new VLine( Simulator.HGRID / 2 + 3, 0, Simulator.HGRID / 2 + 3, Simulator.VGRID - 1 ),
	new VLine( Simulator.HGRID / 2 - 3, 0, Simulator.HGRID / 2 + 3, 0 ),
	new VLine( Simulator.HGRID / 2 - 3, Simulator.VGRID - 1, Simulator.HGRID / 2 + 3, Simulator.VGRID - 1 ),
	new VLine( -1 }
};

VLine	n_s_platform_in[] = {
	new VLine( Simulator.HGRID / 2 - 2, 1, Simulator.HGRID / 2 - 2, Simulator.VGRID - 2 ),
	new VLine( Simulator.HGRID / 2 - 1, 1, Simulator.HGRID / 2 - 1, Simulator.VGRID - 2 ),
	new VLine( Simulator.HGRID / 2 - 0, 1, Simulator.HGRID / 2 - 0, Simulator.VGRID - 2 ),
	new VLine( Simulator.HGRID / 2 + 1, 1, Simulator.HGRID / 2 + 1, Simulator.VGRID - 2 ),
	new VLine( Simulator.HGRID / 2 + 2, 1, Simulator.HGRID / 2 + 2, Simulator.VGRID - 2 ),
	new VLine( -1 }
};

VLine	itin_layout[] = {
	new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
	new VLine( 0, Simulator.VGRID / 2, Simulator.HGRID - 1, Simulator.VGRID / 2 ),
	new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
	new VLine( -1 }
};




			*/
		}
		return null;
	}

	public void SetPropertyValue(String prop, ExprValue val) {
		if(prop.equalsIgnoreCase("click")) {
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("=%d"), val._val);
			ClickCommand ccmd = new ClickCommand(_position);
			Simulator.INSTANCE.addCommand(ccmd);
		    return ;
		}
		/* TODO:
		if(prop.equalsIgnoreCase("color")) {
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("=%d"), val._val);
		    if(fgcolor != conf.fgcolor && fgcolor != color_blue)
		    	return ;
		    grcolor newcolor = wxStrcmp(val._txt, wxT("blue")) == 0 ? color_blue : conf.fgcolor;
		    SetColor(newcolor);
		    return ;
		}
		*/
	}

	public boolean getPropertyValue(String prop, ExprValue result) {
		Track	t = this;

		// move to Track::GetPropertyValue()
		if(prop.equalsIgnoreCase("length")) {
		    result._op = NodeOp.Number;
		    result._val = _length;
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%d}"), result._val);
		    return true;
		}
		if(prop.equalsIgnoreCase("station")) {
		    result._op = NodeOp.String;
		    result._txt = _station == null ? "" : _station;
		    //wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%s}"), result._txt);
		    return true;
		}
		if(prop.equalsIgnoreCase("busy")) {
		    result._op = NodeOp.Number;
		    result._val = _status == TrackStatus.BUSY ? 1 : 0;
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%d}"), result._val);
		    return true;
		}
		if(prop.equalsIgnoreCase("free")) {
		    result._op = NodeOp.Number;
		    result._val = t._status == TrackStatus.FREE ? 1 : 0;
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%d}"), result._val);
		    return true;
		}
		if(prop.equalsIgnoreCase("thrown")) {
		    result._op = NodeOp.Number;
		    if(t instanceof Switch) {
		/*		switch(t.direction) {
				case 10:	// Y switches could be considered always set to a siding
				case 11:	// but it conflicts with the option of reading the status
				case 22:	// then throwing the switch, so this is not enabled.
				case 23:
				    result._val = 1;
				    break;
	
				default: */
				    result._val = ((Switch)t)._switched ? 1 : 0;
				//}
		    } else
		    	result._val = 0;
//		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%d}"), result._val);
		    return true;
		}
		/* TODO:
		if(prop.equalsIgnoreCase("color")) {
		    result._op = String;
		    result._txt = GetColorName(t.fgcolor);
		    wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff), wxT("{%d}"), result._val);
		    return true;
		}
		*/

		result._op = NodeOp.Number;
		result._val = 0;
		return false;
	}

	public void onSetFree() {
		if (_script != null)
			_script.handle("OnSetFree", this, null);
	}

	public void onSetBusy() {
		if (_script != null)
			_script.handle("OnSetBusy", this, null);
	}

	public void onClicked() {
		if (_script != null)
			_script.handle("OnClicked", this, null);
	}

	public void onInit() {
		if (_script != null)
			_script.handle("OnInit", this, null);
	}

	public void DoEnter(Train train) {
		if (_script != null) {
			_script.handle("OnEnter", this, train);
		}
	}

	public void doStopped(Train train) {
		if (_script != null) {
			_script.handle("OnStopped", this, train);
		}
	}


	public void doScript(String cmd, Train train) {
		if (_script == null)
			return;
		ScriptFactory factory = Simulator.INSTANCE._scriptFactory;
		Script script = factory.createInstance(cmd);
		script.handle("OnEnter", this, train);
	}
	
}
