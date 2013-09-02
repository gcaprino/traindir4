package com.traindirector.commands;

import org.eclipse.swt.widgets.Display;

import com.traindirector.editors.MapPage;
import com.traindirector.simulator.SimulatorCommand;

public class MapCommand extends SimulatorCommand {

	String _page;
	
	public MapCommand(String page) {
		_page = page;
	}

	public void handle() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MapPage.openEditor(null, _page);
			}
		});

	}


}
