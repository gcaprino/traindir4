
package com.traindirector.model;

import com.traindirector.simulator.Simulator;

public class TriggerTrack extends Track {

	VLine  etrigger_layout[] = {
		new VLine( 1, 2, Simulator.HGRID - 2, 2 ),
		new VLine( 1, 2, Simulator.HGRID / 2, Simulator.VGRID - 2 ),
		new VLine( Simulator.HGRID / 2, Simulator.VGRID - 2, Simulator.HGRID - 2, 2 ),
	};

	VLine  wtrigger_layout[] = {
		new VLine( 1, Simulator.VGRID - 2, Simulator.HGRID - 2, Simulator.VGRID - 2 ),
		new VLine( 1, Simulator.VGRID - 2, Simulator.HGRID / 2, 2 ),
		new VLine( Simulator.HGRID / 2, 2, Simulator.HGRID - 2, Simulator.VGRID - 2 ),
	};

	VLine  ntrigger_layout[] = {
		new VLine( 2, 1, 2, Simulator.VGRID - 2 ),
		new VLine( 2, 1, Simulator.HGRID - 2, Simulator.VGRID / 2 ),
		new VLine( 2, Simulator.VGRID - 2, Simulator.HGRID - 2, Simulator.VGRID / 2 ),
	};

	VLine  strigger_layout[] = {
		new VLine( Simulator.HGRID - 2, 1, Simulator.HGRID - 2, Simulator.VGRID - 2 ),
		new VLine( 2, Simulator.VGRID / 2, Simulator.HGRID - 2, 1 ),
		new VLine( 2, Simulator.VGRID / 2, Simulator.HGRID - 2, Simulator.VGRID - 2 ),
	};

	@SuppressWarnings("incomplete-switch")
	@Override
	public VLine[] getSegments() {
		switch(_direction) {
		case W_E:
			return etrigger_layout;
		case E_W:
			return wtrigger_layout;
		case N_S:
			return strigger_layout;
		case S_N:
			return ntrigger_layout;
		}
		return null;
	}

	public void doCrossed(Train train) {
		// TODO Auto-generated method stub
		
	}
	
}
