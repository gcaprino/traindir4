package com.traindirector.commands;

import org.eclipse.swt.widgets.Display;

import com.traindirector.editors.InfoPage;
import com.traindirector.simulator.SimulatorCommand;

public class ShowInfoCommand extends SimulatorCommand {

	String _page;
	
	public ShowInfoCommand(String page) {
		_page = page;
	}

	public void handle() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				InfoPage.openEditor(null, _page);
			}
		});

	}

}
