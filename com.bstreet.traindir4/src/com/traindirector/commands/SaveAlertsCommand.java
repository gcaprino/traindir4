package com.traindirector.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.traindirector.files.SavFile;
import com.traindirector.files.TextFile;
import com.traindirector.model.Alert;
import com.traindirector.model.Schedule;
import com.traindirector.model.Territory;
import com.traindirector.simulator.SimulatorCommand;

public class SaveAlertsCommand extends SimulatorCommand {

	String _fname;
	Territory _territory;
	Schedule _schedule;

	public SaveAlertsCommand(String fname) {
		_fname = fname;
	}

	public void handle() {
		boolean wasRunning = _simulator.isRunning();
		_simulator.stopRunning();
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(new File(_fname)));
			List<Alert> alerts = _simulator._alerts;
			for(Alert a : alerts) {
				String txt = a.toString() + "\n";
				out.write(txt);
			}
		} catch(Exception e) {
			
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		if (wasRunning) {
			_simulator.startRunning();
		}
	}

}
