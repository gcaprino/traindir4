package com.traindirector.commands;

import com.traindirector.files.SavFile;
import com.traindirector.simulator.SimulatorCommand;

public class RestoreCommand extends SimulatorCommand {
	
	private String _fname;

	public RestoreCommand(String fname) {
		_fname = fname;
	}
	
	@Override
	public void handle() {
		SavFile savFile = new SavFile(_simulator, _simulator._territory, _fname);
		savFile.load();
	}
}
