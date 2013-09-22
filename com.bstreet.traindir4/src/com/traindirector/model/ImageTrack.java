package com.traindirector.model;

import com.traindirector.scripts.ExprValue;
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

	public void setPropertyValue(String property, ExprValue result) {
		if(property.compareTo("icon") == 0) {
			_station = result._txt;
			setUpdated(Simulator.INSTANCE._updateCounter++);
			return;
		}
	}

}
