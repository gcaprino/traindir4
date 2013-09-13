package com.traindirector.model;

import com.traindirector.simulator.Simulator;

public class Switch extends Track {

	public boolean _switched;

	public Switch() {
		
	}
	
	public Switch(int x, int y) {
		super(x, y);
	}

	public Switch(TDPosition pos) {
	    super(pos._x, pos._y);
    }

    public void onClick() {
		
		// if _status != FREE return
		
		Track linkedTrack = Simulator.INSTANCE._territory.findTrack(_wlink);
		if (linkedTrack != null && linkedTrack instanceof Switch) {
			Switch linkedSwitch = (Switch) linkedTrack;
			linkedSwitch._switched = !linkedSwitch._switched;
			linkedSwitch.setThrown();
			linkedSwitch.setUpdated(Simulator.INSTANCE._updateCounter++);
		}
		Simulator.INSTANCE.clearCachedPaths();
		_switched = !_switched;
		
		// TODO: if was thrown, count penalty
		setThrown();
		setUpdated(Simulator.INSTANCE._updateCounter++);
		Simulator.INSTANCE.updateAllIcons();
	}

	public void setThrown() {
		_flags |= Track.THROWN;
	}
	
	public void resetThrown() {
		_flags &= ~Track.THROWN;
	}
	
	public boolean wasThrown() {
		return (_flags & Track.THROWN) != 0;
	}

	public boolean isThrown() {
		return _switched;
	}

	public Direction walk(Direction dir) {
		int tmp = _direction.ordinal();
		switch(tmp) {
		case 0:			// w-e or w-ne
			if(_switched) {
				if(dir == Direction.E)	return Direction.NE;
				if(dir == Direction.SW)	return Direction.W;
			}
			break;

		case 1:			// nw-e or w-e
			if(_switched) {
				if(dir == Direction.SE)	return Direction.E;
				if(dir == Direction.W)	return Direction.NW;
			}
			break;

		case 2:			// w-e or w-se
			if(_switched) {
				if(dir == Direction.E)	return Direction.SE;
				if(dir == Direction.NW)	return Direction.W;
			}
			break;

		case 3:			// w-e or sw-e
			if(_switched) {
				if(dir == Direction.NE)	return Direction.E;
				if(dir == Direction.W)	return Direction.SW; 
			}
			break;

		case 4:			// sw-e or sw-ne
			if(_switched) {
				if(dir == Direction.NE)	return Direction.E;
				if(dir == Direction.W)	return Direction.SW;
			}
			break;

		case 5:			// w-ne or sw-ne
			if(_switched) {
				if(dir == Direction.E)	return Direction.NE;
				if(dir == Direction.SW)	return Direction.W;
			}
			break;

		case 6:			// nw-e or nw-se
			if(_switched) {
				if(dir == Direction.SE)	return Direction.E;
				if(dir == Direction.W)	return Direction.NW;
			}
			break;

		case 7:			// w-se or nw-se
			if(_switched) {
				if(dir == Direction.E)	return Direction.SE;
				if(dir == Direction.NW)	return Direction.W;
			}
			break;

		case 8:			// horizontal English switch sw-ne or w-e
			if(_switched) {
				if(dir == Direction.E)	return Direction.NE;
				if(dir == Direction.W)	return Direction.SW;
				if(dir == Direction.NE)	return Direction.E;
				if(dir == Direction.SW)	return Direction.W;
			}
			break;

		case 9:			// horizontal english switch nw-se or w-e
			if(_switched) {
				if(dir == Direction.E)	return Direction.SE;
				if(dir == Direction.W)	return Direction.NW;
				if(dir == Direction.SE)	return Direction.E;
				if(dir == Direction.NW)	return Direction.W;
			}
			break;

		case 10:		// Y switch:  w-se or w-ne
			if (_switched)
				return dir == Direction.NW ? Direction.W : Direction.SE;
			return dir == Direction.E ? Direction.NE : Direction.W;

		case 11:		// Y switch:  sw-e or nw-e
			if (_switched)
				return dir == Direction.NE ? Direction.E : Direction.SW;
			return dir == Direction.SE ? Direction.E : Direction.NW;

		case 12:		// sw-n or s-n
			if(_switched) {
				if(dir == Direction.NE)	return Direction.N;
				if(dir == Direction.S)	return Direction.SW;
			}
			break;

		case 13:		// se-n or s-n
			if(_switched) {
				if(dir == Direction.NW)	return Direction.N;
				if(dir == Direction.S)	return Direction.SE;
			}
			break;

		case 14:		// nw-s or s-n
			if(_switched) {
				if(dir == Direction.SE)	return Direction.S;
				if(dir == Direction.N)	return Direction.NW;
			}
			break;

		case 15:		// ne-s or s-n
			if(_switched) {
				if(dir == Direction.SW)	return Direction.S;
				if(dir == Direction.N)	return Direction.NE;
			}
			break;

		case 16:		// vertical English switch  sw-ne or s-n
			if(_switched) {
				if(dir == Direction.N)	return Direction.NE;
				if(dir == Direction.S)	return Direction.SW;
				if(dir == Direction.NE)	return Direction.N;
				if(dir == Direction.SW)	return Direction.S;
			}
			break;

		case 17:		// vertical English switch	nw-se or s-n
			if(_switched) {
				if(dir == Direction.N)	return Direction.NW;
				if(dir == Direction.S)	return Direction.SE;
				if(dir == Direction.NW)	return Direction.N;
				if(dir == Direction.SE)	return Direction.S;
			}
			break;

		case 18:		// sw-n or sw-ne
			if(_switched) {
				if(dir == Direction.NE)	return Direction.N;
				if(dir == Direction.S)	return Direction.SW;
			}
			break;

		case 19:		// s-ne or sw-ne
			if(_switched) {
				if(dir == Direction.N)	return Direction.NE;
				if(dir == Direction.SW)	return Direction.S;
			}
			break;

		case 20:		// se-n or se-nw
			if(_switched) {
				if(dir == Direction.NW)	return Direction.N;
				if(dir == Direction.S)	return Direction.SE;
			}
			break;

		case 21:		// nw-s or nw-se
			if(_switched) {
				if(dir == Direction.SE)	return Direction.S;
				if(dir == Direction.N)	return Direction.NW;
			}
			break;

		case 22:		// Y switch:  nw-s or ne-s
			if(_switched) {
				if(dir == Direction.SE)	return Direction.S;
				if(dir == Direction.N)	return Direction.NW;
			} else {
				if(dir == Direction.SW)	return Direction.S;
				if(dir == Direction.N)	return Direction.NE;
			}
			break;

		case 23:		// Y switch:  sw-n or se-n
			if(_switched) {
				if(dir == Direction.NE)	return Direction.N;
				if(dir == Direction.S)	return Direction.SW;
			} else {
				if(dir == Direction.NW)	return Direction.N;
				if(dir == Direction.S)	return Direction.SE;
			}
		}
		return dir;
	}

	public VLine[] getSegments() {
	    int dir = _direction.ordinal();
	    return getSegments(dir);
	}
	
	public VLine[] getSegments(int direction) {
		VLine[] arr = new VLine[0];
		VLine[] t1 = null;
		VLine[] t2 = null;
		boolean editing = Simulator.getEditing();
		switch(direction) {
		case 0:
			if(editing) {
				t1 = getSegments(TrackDirection.W_NE);
				t2 = getSegments(TrackDirection.W_E);
			} else if(_switched) {
				return getSegments(TrackDirection.W_NE);
			} else
				return getSegments(TrackDirection.W_E);
			break;

		case 1:
			if(editing) {
				t1 = getSegments(TrackDirection.NW_E);
				t2 = getSegments(TrackDirection.W_E);
			} else if(_switched) {
				return getSegments(TrackDirection.NW_E);
			} else
				return getSegments(TrackDirection.W_E);
			break;

		case 2:
			if(editing) {
				t1 = getSegments(TrackDirection.W_SE);
				t2 = getSegments(TrackDirection.W_E);
			} else if(_switched) {
				return getSegments(TrackDirection.W_SE);
			} else
				return getSegments(TrackDirection.W_E);
			break;

		case 3:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_E);
				t2 = getSegments(TrackDirection.W_E);
			} else if(_switched) {
				return getSegments(TrackDirection.SW_E);
			} else
				return getSegments(TrackDirection.W_E);
			break;

		case 4:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_E);
				t2 = getSegments(TrackDirection.SW_NE);
			} else if(_switched)
				return getSegments(TrackDirection.SW_E);
			else
				return getSegments(TrackDirection.SW_NE);
			break;

		case 5:
			if(editing) {
				t1 = getSegments(TrackDirection.W_NE);
				t2 = getSegments(TrackDirection.SW_NE);
			} else if(_switched)
				return getSegments(TrackDirection.W_NE);
			else
				return getSegments(TrackDirection.SW_NE);
			break;

		case 6:
			if(editing) {
				t1 = getSegments(TrackDirection.NW_E);
				t2 = getSegments(TrackDirection.NW_SE);
			} else if(_switched) {
				return getSegments(TrackDirection.NW_E);
			} else
				return getSegments(TrackDirection.NW_SE);
			break;

		case 7:
			if(editing) {
				t1 = getSegments(TrackDirection.W_SE);
				t2 = getSegments(TrackDirection.NW_SE);
			} else if(_switched)
				return getSegments(TrackDirection.W_SE);
			else
				return getSegments(TrackDirection.NW_SE);
			break;

		case 8:				/* horizontal english switch */
			if(_switched && !editing)
				return sweng_sw_ne_switched;
			else
				return sweng_sw_ne_straight;

		case 9:				/* horizontal english switch */
			if(_switched && !editing)
			    return sweng_nw_se_switched;
			else
			    return sweng_nw_se_straight;

		case 10:
			if(editing) {
				t1 = getSegments(TrackDirection.W_SE);
				t2 = getSegments(TrackDirection.W_NE);
			} else if (_switched)
				return getSegments(TrackDirection.W_SE);
			else
				return getSegments(TrackDirection.W_NE);
			break;

		case 11:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_E);
				t2 = getSegments(TrackDirection.NW_E);
			} else if (_switched)
				return getSegments(TrackDirection.SW_E);
			else
				return getSegments(TrackDirection.NW_E);
			break;

		case 12:
			if(editing) {
				t1 = getSegments(TrackDirection.TRK_N_S);
				t2 = getSegments(TrackDirection.SW_N);
			} else if(_switched)
				return getSegments(TrackDirection.SW_N);
			else
				return getSegments(TrackDirection.TRK_N_S);
			break;

		case 13:
			if(editing) {
				t1 = getSegments(TrackDirection.TRK_N_S);
				t2 = getSegments(TrackDirection.SE_N);
			} else if(_switched)
			    return getSegments(TrackDirection.SE_N);
			else
			    return getSegments(TrackDirection.TRK_N_S);
			break;

		case 14:
			if(editing) {
				t1 = getSegments(TrackDirection.TRK_N_S);
				t2 = getSegments(TrackDirection.NW_S);
			} else if(_switched)
				return getSegments(TrackDirection.NW_S);
			else
				return getSegments(TrackDirection.TRK_N_S);
			break;

		case 15:
			if(editing) {
				t1 = getSegments(TrackDirection.TRK_N_S);
				t2 = getSegments(TrackDirection.NE_S);
			} else if(_switched)
				return getSegments(TrackDirection.NE_S);
			else
				return getSegments(TrackDirection.TRK_N_S);
			break;

		case 16:			/* vertical english switch */
			if(_switched && !editing)
			    return swengv_sw_ne_switched;
			else
			    return swengv_sw_ne_straight;

		case 17:			/* vertical english switch */
			if(_switched && !editing)
			    return swengv_nw_se_switched;
			else
			    return swengv_nw_se_straight;

		case 18:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_NE);
				t2 = getSegments(TrackDirection.SW_N);
			} else if(_switched)
				t2 = getSegments(TrackDirection.SW_N);
			else
				t1 = getSegments(TrackDirection.SW_NE);
			break;

		case 19:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_NE);
				t2 = getSegments(TrackDirection.NE_S);
			} else if(_switched)
				return getSegments(TrackDirection.NE_S);
			else
				return getSegments(TrackDirection.SW_NE);
			break;

		case 20:
			if(editing) {
				t1 = getSegments(TrackDirection.NW_SE);
				t2 = getSegments(TrackDirection.SE_N);
			} else if(_switched)
				return getSegments(TrackDirection.SE_N);
			else
				return getSegments(TrackDirection.NW_SE);
			break;

		case 21:
			if(editing) {
				t1 = getSegments(TrackDirection.NW_SE);
				t2 = getSegments(TrackDirection.NW_S);
			} else if(_switched)
				return getSegments(TrackDirection.NW_S);
			else
				return getSegments(TrackDirection.NW_SE);
			break;

		case 22:
			if(editing) {
				t1 = getSegments(TrackDirection.NW_S);
				t2 = getSegments(TrackDirection.NE_S);
			} else if(_switched)
				return getSegments(TrackDirection.NW_S);
			else
				return getSegments(TrackDirection.NE_S);
			break;

		case 23:
			if(editing) {
				t1 = getSegments(TrackDirection.SW_N);
				t2 = getSegments(TrackDirection.SE_N);
			} else if(_switched)
				return getSegments(TrackDirection.SW_N);
			else
				return getSegments(TrackDirection.SE_N);
			break;
		}
//		if(!t->norect)
	//	    draw_layout(t->x, t->y, switch_rect, curSkin->outline); //fieldcolors[TRACK]);
		//t->direction = (trkdir)tmp;
		if (t1 != null && t2 != null) {
			arr = new VLine[t1.length + t2.length];
			int i, j;
			for(i = 0; i < t1.length; ++i) {
				arr[i] = t1[i];
			}
			for(j = 0; j < t2.length; ++j, ++i) {
				arr[i] = t2[j];
			}
			return arr;
		}
		return null;
	}

	public VLine[] getBlockSegments() {
		return switch_rect;
	}

	VLine	switch_rect[] = {
			new VLine( 0, 0, Simulator.HGRID - 1, 0 ),
			new VLine( Simulator.HGRID - 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
			new VLine( 0, 0, 0, Simulator.VGRID - 1 ),
			new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
	};

	VLine	sweng_sw_ne_straight[] = {
		new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
		new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
		new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),

		new VLine( 0, Simulator.VGRID / 2, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 ),
		new VLine( 0, Simulator.VGRID / 2 + 1, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 + 1 ),

		new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
		new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
	};

	VLine	sweng_sw_ne_switched[] = {

		new VLine( 0, Simulator.VGRID / 2, Simulator.HGRID - 2, 0 ),
		new VLine( 0, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, 0 ),

		new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 ),
		new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
	};

	VLine	sweng_nw_se_straight[] = {
		new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
		new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
		new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),

		new VLine( 0, Simulator.VGRID / 2, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 ),
		new VLine( 0, Simulator.VGRID / 2 + 1, Simulator.HGRID / 2 - 1, Simulator.VGRID / 2 + 1 ),

		new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),
		new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID / 2 - 0, Simulator.HGRID - 1, Simulator.VGRID / 2 - 0 ),
	};

	VLine	sweng_nw_se_switched[] = {

		new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID / 2 ),
		new VLine( 0, 1, Simulator.HGRID - 1, Simulator.VGRID / 2 + 1 ),

		new VLine( 0, Simulator.VGRID / 2, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
		new VLine( 1, Simulator.VGRID / 2 + 1, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
	};

	VLine	swengv_sw_ne_straight[] = {
		new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
		new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),

		new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID - 2, 0 ),
		new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
		new VLine( 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
	};

	VLine	swengv_sw_ne_switched[] = {

		new VLine( 0, Simulator.VGRID - 2, Simulator.HGRID / 2 - 0, 0 ),
		new VLine( 0, Simulator.VGRID - 1, Simulator.HGRID / 2 + 1, 0 ),

		new VLine( Simulator.HGRID / 2 - 0, Simulator.VGRID - 1, Simulator.HGRID - 1, 0 ),
		new VLine( Simulator.HGRID / 2 + 1, Simulator.VGRID - 1, Simulator.HGRID - 1, 1 ),
	};

	VLine	swengv_nw_se_straight[] = {
		new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID / 2 + 1, Simulator.VGRID - 1 ),
		new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),

		new VLine( 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
		new VLine( 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
		new VLine( 0, 1, Simulator.HGRID - 2, Simulator.VGRID - 1 ),
	};

	VLine	swengv_nw_se_switched[] = {

		new VLine( 0, 0, Simulator.HGRID / 2 - 1, Simulator.VGRID - 1 ),
		new VLine( 0, 1, Simulator.HGRID / 2 - 0, Simulator.VGRID - 1 ),

		new VLine( Simulator.HGRID / 2 - 0, 0, Simulator.HGRID - 1, Simulator.VGRID - 2 ),
		new VLine( Simulator.HGRID / 2 + 1, 0, Simulator.HGRID - 1, Simulator.VGRID - 1 ),
	};


	public String toString() {
		return "(" + _position._x + "," + _position._y + ") thrown: " + _switched;
	}
}
