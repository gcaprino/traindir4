package com.traindirector.model;

import com.traindirector.simulator.Simulator;

public class PlatformTrack extends Track {
	VLine	w_e_platform[] = {
			new VLine( 0, Simulator.VGRID / 2 - 3, Simulator.HGRID - 1, Simulator.VGRID / 2 - 3 ),
			new VLine( 0, Simulator.VGRID / 2 + 3, Simulator.HGRID - 1, Simulator.VGRID / 2 + 3 ),
			new VLine( 0, Simulator.VGRID / 2 - 3, 0, Simulator.VGRID / 2 + 3 ),
			new VLine( Simulator.HGRID - 1, Simulator.VGRID / 2 - 3, Simulator.HGRID - 1, Simulator.VGRID / 2 + 3 ),
//		};

	//	VLine	w_e_platform_in[] = {
			new VLine( 1, Simulator.VGRID / 2 - 2, Simulator.HGRID - 2, Simulator.VGRID / 2 - 2 ),
			new VLine( 1, Simulator.VGRID / 2 - 1, Simulator.HGRID - 2, Simulator.VGRID / 2 - 1 ),
			new VLine( 1, Simulator.VGRID / 2 - 0, Simulator.HGRID - 2, Simulator.VGRID / 2 - 0 ),
			new VLine( 1, Simulator.VGRID / 2 + 1, Simulator.HGRID - 2, Simulator.VGRID / 2 + 1 ),
			new VLine( 1, Simulator.VGRID / 2 + 2, Simulator.HGRID - 2, Simulator.VGRID / 2 + 2 )
		};

		VLine	n_s_platform[] = {
			new VLine( Simulator.HGRID / 2 - 3, 0, Simulator.HGRID / 2 - 3, Simulator.VGRID - 1 ),
			new VLine( Simulator.HGRID / 2 + 3, 0, Simulator.HGRID / 2 + 3, Simulator.VGRID - 1 ),
			new VLine( Simulator.HGRID / 2 - 3, 0, Simulator.HGRID / 2 + 3, 0 ),
			new VLine( Simulator.HGRID / 2 - 3, Simulator.VGRID - 1, Simulator.HGRID / 2 + 3, Simulator.VGRID - 1 ),
//		};

//		VLine	n_s_platform_in[] = {
			new VLine( Simulator.HGRID / 2 - 2, 1, Simulator.HGRID / 2 - 2, Simulator.VGRID - 2 ),
			new VLine( Simulator.HGRID / 2 - 1, 1, Simulator.HGRID / 2 - 1, Simulator.VGRID - 2 ),
			new VLine( Simulator.HGRID / 2 - 0, 1, Simulator.HGRID / 2 - 0, Simulator.VGRID - 2 ),
			new VLine( Simulator.HGRID / 2 + 1, 1, Simulator.HGRID / 2 + 1, Simulator.VGRID - 2 ),
			new VLine( Simulator.HGRID / 2 + 2, 1, Simulator.HGRID / 2 + 2, Simulator.VGRID - 2 )
		};


		@Override
		public VLine[] getSegments() {
			switch(_direction) {
			case W_E:
			case E_W:
				return w_e_platform;
			}
			return n_s_platform;
		}
}
