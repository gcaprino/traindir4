package com.traindirector.model;

import com.traindirector.simulator.Simulator;

public class ImageTrack extends Track {
	
	public void onClick() {
		super.onClick();
		Simulator.INSTANCE.updateAllIcons();
	}

	public void onIconUpdate() {
		if(_script != null)
			_script.handle("OnIconUpdate", this, null);
	}

}
