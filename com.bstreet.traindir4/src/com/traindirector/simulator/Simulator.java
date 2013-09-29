package com.traindirector.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.Application;
import com.traindirector.dialogs.ColorOption;
import com.traindirector.editors.InfoContent;
import com.traindirector.events.AlertEvent;
import com.traindirector.events.ResetEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.files.FileManager;
import com.traindirector.model.Alert;
import com.traindirector.model.ImageTrack;
import com.traindirector.model.PerformanceCounters;
import com.traindirector.model.Schedule;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Switchboard;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.options.Option;
import com.traindirector.options.OptionsManager;
import com.traindirector.scripts.ScriptFactory;
import com.traindirector.uicomponents.SoundPlayer;
import com.traindirector.uicomponents.SwitchboardContent;
import com.traindirector.web.pages.PerformanceContent;
import com.traindirector.web.pages.StationInfoContent;
import com.traindirector.web.pages.StationsListContent;
import com.traindirector.web.pages.TrainInfoContent;
import com.traindirector.web.server.WebServer;

public class Simulator {

	public static Simulator INSTANCE;
	public static final String _version = "Train Director 4.0 ";
	
	List<SimulatorCommand> _commands;
	Thread _executor;

	public Territory _territory;
	public Schedule _schedule;
	public List<Alert> _alerts;
	public List<Switchboard> _switchboards;

	public IconFactory _iconFactory;
	public SignalFactory _signalFactory;
	public ColorFactory _colorFactory;
	public ScriptFactory _scriptFactory;
	public FileManager _fileManager;
	public SoundPlayer _soundPlayer;
	public OptionsManager _options;

	public static int VGRID = 9;
	public static int HGRID = 9;
	public Timer _timer;
	private TimerTask _timedExecutor;
	public static Random _random = new Random(System.currentTimeMillis());
    private static boolean _currentlyEditing;
    private static boolean _currentlyEditingItineraries;

	public int _timeSliceCount;
	public int _timeSlice;
	public boolean _ignoreTimer = true;

	public long _updateCounter;
	
	public int _simulatedTime;
	public int _simulatedSpeed = 10;
	public TrainRunner _trainRunner;
	public boolean _signalsChanged;

	private boolean _running;
	public String _baseDir = "";
	public String _baseFileName;
	public boolean _gMustBeClearPath;
	public int[] _lateData = new int[24 * 60]; // how many late minutes we accumulated for each minute in the day

	public int[] _startDelay = new int[Track.NSPEEDS];
	
	public PerformanceCounters _performanceCounters = new PerformanceCounters();
	public int _runPoints;
	public int _totalDelay;
	public int _totalLate;
	public int _runPointBase;
	public int _runDay;

	// TODO: move to options
	public int _currentTimeMultiplier;
	public int _timeMult;
	public List<String> _oldSimulations;
    private int _editorTrackType;
    private int _editorTrackDirection;
    public WebServer _webServer;
	
	public Simulator() {

		INSTANCE = this;

		_iconFactory = new IconFactory();
		_signalFactory = new SignalFactory();
		_colorFactory = new ColorFactory(Application._display);
		_scriptFactory = new ScriptFactory();
		_options = new OptionsManager();
		_soundPlayer = new SoundPlayer();

		_commands = new LinkedList<SimulatorCommand>();
		_alerts = new LinkedList<Alert>();
		_territory = new Territory();
		_schedule = new Schedule();
		_switchboards = new ArrayList<Switchboard>();

		_oldSimulations = new ArrayList<String>();

		_timedExecutor = new MyScheduler();

		_trainRunner = new TrainRunner(this);

		_timeSlice = 10;			// user-visible update every second
		_timer = new Timer();
		_timer.schedule(_timedExecutor, 500, 100);
		_options._locale = "en";
		
		_options.initOptions();
		for(Option o : _options._colorOptions) {
			ColorOption col = (ColorOption)o;
			col._color = _colorFactory.get(o._name);
			if (col._color == null)
				col._color = _colorFactory.set(o._name, o._intValue >> 16, (o._intValue >> 8) & 0xff, o._intValue & 0xff);
		}

		/*
		Runnable runner = new Runnable() {

			@Override
			public void run() {
				performCommands();
			}
		};
		_executor = new Thread(runner);
		_executor.start();
		*/
	}

	public void performCommands() {
		SimulatorCommand cmd;
		while (true) {
			synchronized (_commands) {
				try {
					_commands.wait();
				} catch (InterruptedException e) {
					break;
				}
				cmd = _commands.get(0);
				cmd._simulator = this;
				_commands.remove(0);
			}
			try {
				cmd.handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void addCommand(SimulatorCommand cmd) {
		synchronized (_commands) {
			_commands.add(cmd);
			_commands.notifyAll();
		}
	}

	public boolean isRunning() {
		return _running;
	}
	
	public void startRunning() {
		_running = true;
		startTimer();
	}
	
	public void stopRunning() {
		_running = false;
		stopTimer();
	}

	public void restartSimulation() {
		stopRunning();
		clearDelays();
		_territory.removeTrains();
		_schedule.reset();
		_schedule.computeCounters();
		_simulatedTime = _schedule._startTime;
	}

	public String getHeader() {
		StringBuilder sb = new StringBuilder(_version);
		sb.append("   ");
		sb.append(TDTime.toString(_simulatedTime));
		sb.append(" x");
		sb.append(_simulatedSpeed);
		return sb.toString();
	}

	public void setSimulationSpeed(int index, int speed) {
		_currentTimeMultiplier = index;
		_simulatedSpeed = speed;		// 1 clock second = speed simulated seconds
	}

	public int getSimulatedSpeed() {
		return _simulatedSpeed;
	}
	
	public void alert(String text) {
		Alert a = new Alert(_simulatedTime, text);
		_alerts.add(a);
		if (_options._beepOnAlert.isSet())
			_soundPlayer.play(_options._alertSoundPath._value);
		AlertEvent e = new AlertEvent(this);
		CGEventDispatcher.getInstance().postEvent(e);
	}
	
	public void removeAllAlerts() {
		_alerts.clear();
	}

	public void stopTimer() {
		_ignoreTimer = true;
	}

	public void startTimer() {
		_ignoreTimer = false;
	}

	public void doRepaint() {
		TimeSliceEvent e = new TimeSliceEvent(this, _updateCounter);
		CGEventDispatcher.getInstance().postEvent(e);
	}


	public class MyScheduler extends TimerTask {

		public MyScheduler() {
			super();
		}

		@Override
		public void run() {
			// we run every 100 ms
			
			// first drain the command list
			
			SimulatorCommand[] cmdList = null;
			do {
				cmdList = null;
				synchronized (_commands) {
					if(!_commands.isEmpty()) {
						cmdList = new SimulatorCommand[_commands.size()];
						_commands.toArray(cmdList);
						_commands.clear();
					}
				}
				if(cmdList != null) {
					for(SimulatorCommand c : cmdList) {
						c._simulator = Simulator.this;
						try {
							c.handle();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			} while(cmdList != null);
			++_timeSliceCount;
			if(_timeSliceCount >= _timeSlice) {
				_timeSliceCount = 0;
				flashSignals();
				if(!_ignoreTimer)
					doTimeSlice();
				doRepaint();
			}
		}
	}

	public void flashSignals() {
		synchronized (_territory) {
			List<Signal> signals = _territory.getAllSignals();
			if(signals == null)	// not set up, yet
				return;
			for (Signal signal : signals) {
				SignalAspect aspect = signal.getAspect();
				if (aspect == null)		// impossible
					continue;
				// TODO: optimize, since we don't need to have a local counter for each aspect.
				// We could have a global flashing counter, since each aspect uses count % aspects.length
				aspect.nextFlashing();
			}
		}
	}

	public void doTimeSlice() {
		synchronized (this) {
			int simulatedSpeed = getSimulatedSpeed();
			
			for(int i = 0; i < simulatedSpeed; ++i) {
				++_simulatedTime;
				_trainRunner.timeStep();
				updateSignals(null); // to update fleeted signals
			}
		}
	}

	public void updateSignals(Signal ignore) {
		openAllFleeted();
		if(_signalsChanged) {
			if (ignore != null)
				ignore._aspectChanged = true;
			List<Signal> signals = _territory.getAllSignals();
			for(Signal signal : signals) {
				if(signal != ignore)
					signal._aspectChanged = false;
			}
			do {
				_signalsChanged = false;
				for(Signal signal : signals) {
					if (signal == ignore)
						continue;
					signal.onUpdate();
				}
			} while(_signalsChanged);
			updateAllIcons();
		}
	}

	private void openAllFleeted() {
		for(Signal signal : _territory.getAllSignals()) {
		    if(!signal._fleeted) // it's not an automatic signal
		    	continue;
		    if(!signal.isFleeted() || signal.isClear()) // not in automatic mode or already green
		    	continue;
		    if(signal._controls == null || signal._controls._status == TrackStatus.BUSY)
		    	continue;
		    signal.toggle();
		}

	}

	public void setBaseDirectory(String name) {
		_baseDir = name;
	}

	public String getFilePath(String fname) {
		File f = new File(fname);
		if (!f.exists()) {
			f = new File(_baseDir + File.separatorChar + fname);
			if(!f.exists())
				return null;
		}
		return f.getAbsolutePath();
	}

	public void clearDelays() {
		for (Train train : _schedule._trains) {
			train._startDelay = 0;
			if (train._myStartDelay == 0 && _startDelay[train._type] != 0) {
				train._myStartDelay = _startDelay[train._type];
			}
			if (train._entryDelay != null)
				train._entryDelay._nSeconds = 0;
			for (TrainStop stop : train._stops) {
				if (stop._depDelay != null)
					stop._depDelay._nSeconds = 0;
			}
			train.clearFlag(Train.ENTEREDLATE | Train.GOTDELAYATSTOP);
		}
	}

	public String getVersion() {
		return _version;
	}

	public void setNewProject(String fname) {
		if (_fileManager != null) {
			_fileManager.close();
			_fileManager = null;
		}
		_fileManager = new FileManager(this, fname);
	}

	public BufferedReader getReaderFor(String extension) {
		return _fileManager.getReaderFor(extension);
	}

	public BufferedReader getReaderForFile(String fname) {
		return _fileManager.getReaderForFile(fname);
	}

	public void setEditing(boolean b) {
	    _currentlyEditing = b;
	}

    public static boolean getEditing() {
        return _currentlyEditing;
    }

	public void setEditingItineraries(boolean b) {
	    _currentlyEditingItineraries = b;
	}

    public static boolean getEditingItineraries() {
        return _currentlyEditingItineraries;
    }

    public void setEditorTool(int trackType, int direction) {
        _editorTrackType = trackType;
        _editorTrackDirection = direction;
    }
    
    public int getEditorTrackType() {
        return _editorTrackType;
    }
    
    public int getEditorTrackDirection() {
        return _editorTrackDirection;
    }

    //
    //	Switchboards
    //
    
    public Switchboard findSwitchboard(String name) {
        for (Switchboard swb : _switchboards) {
            if (swb._filename.equals(name)) {
                return swb;
            }
        }
        return null;
    }

    public Switchboard addSwitchboard(String name) {
        Switchboard swb = new Switchboard();
        swb._name = name;       // TODO: remove full path
        swb._filename = name;   // TODO:
        return swb;
    }
    
    public Switchboard createSwitchboard(String name) {
    	Switchboard sw = findSwitchboard(name);
    	if(sw != null)
    		_switchboards.remove(sw);
    	sw = addSwitchboard(name);
    	_switchboards.add(sw);
    	return sw;
    }
    
    public void removeAllSwitchboards() {
    	_switchboards.clear();
    }

	public boolean ask(final String question) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		Application._display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = MessageDialog.openQuestion(Application._display.getActiveShell(), "Question", question);
			}
		});
		return result[0];
	}

	public void newSimulation() {
		_territory.clear();
		_schedule.clear();
		_scriptFactory.clear();
		if(_switchboards != null)
			_switchboards.clear();
		_iconFactory.clear();
		if(_fileManager != null)
			_fileManager.close();
		_fileManager = null;
		_alerts.clear();
		_simulatedTime = 0;
		_running = false;
		_startDelay = new int[Track.NSPEEDS];
		clearPoints();
		CGEventDispatcher.getInstance().postEvent(new ResetEvent(this));
	}
	
	public void clearPoints() {
		for(int i = 0; i < _lateData.length; ++i)
			_lateData[i] = 0;
		_runPoints = 0;
		_totalDelay = 0;
		_totalLate = 0;
		_runPointBase = 0;
		_runDay = 0;
	}

	// Format of cmd:
	//	 (/switchboard/)name/x/y   select cell x,y in switchboard name

	public String getSwitchboardPage(String cmd, String urlBase) {
		String result = "";
		SwitchboardContent content = new SwitchboardContent();
		content.setUrlBase(urlBase);
		String[] elems = cmd.split("/");
		if(elems.length > 0) {
			Switchboard swb = Application.getSimulator().findSwitchboard(elems[0]);
			if(swb != null) {
				content.setCurrentSwitchboard(swb);
				if (elems.length > 2) {
					int x = Integer.parseInt(elems[1]);
					int y = Integer.parseInt(elems[2]);
					swb.select(x, y);
				}
			}
		}
		result = content.getHTML();
		return result;
	}

	public void clearCachedPaths() {
		List<Signal> signals = _territory.getAllSignals();
		for (Signal signal : signals)
			signal.clearCache();
	}

	public void updateAllIcons() {
		for (Track track : _territory.getTracks()) {
			if (!(track instanceof ImageTrack))
				continue;
			ImageTrack image = (ImageTrack) track;
			image.onIconUpdate();
		}
	}

	public void initWebServer() {
    	// This must be here to use the options from the .ini file
		_webServer = new WebServer();
		try {
			// Register with the server all the services that can handle web requests
			// TODO: pass class instead of instance, so that each request can get its own instance
			// 		 and operate in its own context
			_webServer.addContent(new SwitchboardContent());
			_webServer.addContent(new PerformanceContent());
			_webServer.addContent(new StationInfoContent());
			_webServer.addContent(new StationsListContent());
			_webServer.addContent(new TrainInfoContent());
			_webServer.addContent(new InfoContent());
			_webServer.startServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
