package com.traindirector.commands;

import java.io.File;

import com.traindirector.files.SavFile;
import com.traindirector.model.Schedule;
import com.traindirector.model.Territory;
import com.traindirector.simulator.SimulatorCommand;

public class SaveGameCommand extends SimulatorCommand {

	String _fname;
	Territory _territory;
	Schedule _schedule;

	public SaveGameCommand(String fname) {
		_fname = fname;
	}

	public void handle() {
		boolean wasRunning = _simulator.isRunning();
		_simulator.stopRunning();
		_territory = _simulator._territory;
		File dir = new File(_fname);
		_simulator.setBaseDirectory(dir.getParent()); // make relative file names from the
													  // location of the .trk file
		_simulator.setLastSaved(_fname);
		SavFile savFile = new SavFile(_simulator, _territory, _fname);
		savFile.save();
		if (wasRunning) {
			_simulator.startRunning();
		}
	}
}
