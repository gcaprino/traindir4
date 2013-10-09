package com.traindirector.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.dialogs.DaysDialog;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.LoadStartEvent;
import com.traindirector.files.PthFile;
import com.traindirector.files.SchFile;
import com.traindirector.files.TrkFile;
import com.traindirector.model.EntryExitPath;
import com.traindirector.model.Schedule;
import com.traindirector.model.Territory;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class LoadCommand extends SimulatorCommand {

	String _fname;
	Territory _territory;
	Schedule _schedule;

	public LoadCommand(String fname) {
		_fname = fname;
	}
	
	public LoadCommand(Simulator sim, String fname) {
		_simulator = sim;
		_fname = fname;
	}

	public void handle() {
		_simulator.stopRunning();
		LoadStartEvent loadEvent = new LoadStartEvent(_simulator);
		CGEventDispatcher.getInstance().postEvent(loadEvent);
		_territory = _simulator._territory;
		_territory.removeAllElements();

		_simulator.setNewProject(_fname);

		File dir = new File(_fname);
		_simulator.setBaseDirectory(dir.getParent());	// make relative file names from the location of the .trk file
		_simulator._baseFileName = dir.getName().toLowerCase().replace(".trk", "").replace(".zip", "");

		BufferedReader rdr = _simulator.getReaderFor(".trk");
		if (rdr == null) {
			return;
		}
		TrkFile trkFile = new TrkFile(_simulator, _territory, _fname, rdr);
		trkFile.loadTracks(rdr);
		_territory.updateReferences();
		_territory.loadSignalAspects(_simulator._scriptFactory);
		_schedule = _simulator._schedule;
		_simulator._runDay = 0;
		loadSchedule();
		loadPaths();
		_schedule.sortByEntryTime();
		// parse programs from schedule
		LoadEndEvent endLoadEvent = new LoadEndEvent(_simulator);
		if(_schedule.runDaySpecified()) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					DaysDialog dialog = new DaysDialog(null);
					dialog.open();
					_simulator._runDay = 1 << dialog.getSelectedDay();
				}
			});
		}
		CGEventDispatcher.getInstance().postEvent(endLoadEvent);
		_simulator.restartSimulation();
	}
	
	private void loadPaths() {
		_territory._paths.clear();
		try {
			BufferedReader input = _simulator._fileManager.getReaderFor(".pth");
			if (input == null)
				return;
			PthFile pthFile = new PthFile(_simulator, _territory, _fname);
			pthFile.readFile(input);
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		resolvePaths();
	}

	public String asExtension(String ext) {
		if(ext.charAt(0) == '.')
			ext = ext.substring(1);
		String fname = _fname;
		int indx = fname.lastIndexOf('.');
		if (indx >= 0) {
			fname = fname.substring(0, indx + 1) + ext;
		}
		return fname;
	}

	public void loadSchedule() {
		_simulator._schedule.clear();
		try {
			BufferedReader input = _simulator._fileManager.getReaderFor(".sch");
			if (input != null) {
				SchFile schFile = new SchFile(_simulator);
				schFile.readFile(input);
				input.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int count = 0;
		for (Train train : _simulator._schedule._trains) {
			if (train._entrance == null || train._entrance.isEmpty()) {
				++count;
				train._entrance = "?";
			}
			if (train._exit == null || train._exit.isEmpty()) {
				++count;
				train._exit = "?";
			}
			if (train._script != null)
				train._script.parse();
		}
		if (count > 0) {
			_simulator.alert("Some train has unknown entry/exit point!");
		}
	}
	
	
	public void resolvePaths() {
		for (Train train : _simulator._schedule._trains) {
			resolvePaths(train);
		}
	}
	
	public void resolvePaths(Train t) {
		EntryExitPath	pt, pth;
		int t0 = 0;
		int	f1;
		boolean found = false;

		if (t._entrance == null) {
			_simulator.alert("No entrance for " + t._name);
			return;
		}
		if (t._exit == null) {
			_simulator.alert("No exit for " + t._name);
			return;
		}
		if(_territory.findStationNamed(t._entrance) != null) {
		    f1 = 1;
		} else {
			f1 = 0;
			for(TrainStop ts : t._stops) {
				pt = _territory.findPath(t._entrance, ts._station);
				if (pt == null)
					continue;
				t._entrance = pt._enter;
				t._timeIn = ts._arrival - pt._times[t._type];
				f1 = 1;
				found = true;
				break;
			}
			if (!found) {
				int tt, ts; 
				for(tt = 0; tt < t._stops.size() && !found; ++tt) {
				    for(ts = tt + 1; ts < t._stops.size() && !found; ++ts) {
						if((pt = _territory.findPath(t._stops.get(tt)._station, t._stops.get(ts)._station)) != null) {
						    t._entrance = pt._enter;
						    t._timeIn = t._stops.get(ts)._arrival - pt._times[t._type];
						    f1 = 1;
						    found = true;
						    break;
						}
				    }
			    }
			}
		}
	xit:
		if(_territory.findStationNamed(t._exit) != null) {
		    if(f1 != 0)			/* both entrance and exit in layout */
		    	return;
		    pth = null;
		    for(TrainStop tt : t._stops)
				if((pt = _territory.findPath(tt._station, t._exit)) != null) {
				    pth = pt;
				    break;
				}
		    if(pth == null)
		    	pth = _territory.findPath(t._entrance, t._exit);
		    if(pth == null)
		    	return;
		    t._entrance = pth._enter;
		    t._timeIn = t._timeOut - pth._times[t._type];
		    return;
		}
		pth = null;
		int tt, ts; 
		found = false;
		for(tt = 0; tt < t._stops.size(); ++tt) {
		    for(ts = tt + 1; ts < t._stops.size(); ++ts) {
				if((pt = _territory.findPath(t._stops.get(tt)._station, t._stops.get(ts)._station)) != null) {
				    t0 = t._stops.get(tt)._departure;
				    pth = pt;
				}
		    }
		}
		for(TrainStop stop : t._stops)
		    if((pt = _territory.findPath(stop._station, t._exit)) != null) {
				t0 = stop._departure;
				pth = pt;
				break;
		    }
		if(pth != null) {
		    t._exit = pth._enter;
		    t._timeOut = (int) (t0 + pth._times[t._type]);
		    return;
		}
		if(f1 == 0)
		    return;
		for(TrainStop stop : t._stops)
		    if((pt = _territory.findPath(t._entrance, stop._station)) != null) {
				t._exit = pt._enter;
				t._timeOut = t._timeIn + pt._times[t._type];
				return;
		    }
		if((pt = _territory.findPath(t._entrance, t._exit)) != null) {
		    t._exit = pt._enter;
		    t._timeOut = t._timeIn + pt._times[t._type];
		}

	}
}
