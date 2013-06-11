package com.traindirector.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.Application;
import com.traindirector.events.AlertEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.files.BooleanOption;
import com.traindirector.files.FileManager;
import com.traindirector.files.IntegerOption;
import com.traindirector.model.Alert;
import com.traindirector.model.OptionsManager;
import com.traindirector.model.PerformanceCounters;
import com.traindirector.model.Schedule;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.scripts.ScriptFactory;

public class Simulator {

	public static Simulator INSTANCE;
	public static final String _version = "Train Director 4.0 ";
	
	List<SimulatorCommand> _commands;
	Thread _executor;

	public Territory _territory;
	public Schedule _schedule;
	public List<Alert> _alerts;

	public IconFactory _iconFactory;
	public SignalFactory _signalFactory;
	public ColorFactory _colorFactory;
	public ScriptFactory _scriptFactory;
	public FileManager _fileManager;

	public static int VGRID = 9;
	public static int HGRID = 9;
	public Timer _timer;
	private TimerTask _timedExecutor;
	public static Random _random = new Random(System.currentTimeMillis());
    private static boolean _currentlyEditing;

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
	public boolean _gMustBeClearPath;
	public int[] _lateData = new int[24 * 60]; // how many late minutes we accumulated for each minute in the day
	
	public PerformanceCounters _performanceCounters = new PerformanceCounters();

	// TODO: move to options
	public OptionsManager _optionsManager;
	public BooleanOption _terseStatus;
	public BooleanOption _statusOnTop;	// unused
	public BooleanOption _showSeconds;
	public BooleanOption _traditionalSignals;
	public BooleanOption _autoLink;
	public BooleanOption _showGrid;
	public BooleanOption _hardCounters;
	public boolean _showCanceled;
	public BooleanOption _showLinks;
	public BooleanOption _beepOnEnter;
	public boolean _showCoords;
	public BooleanOption _showIcons;
	public boolean _showTooltip;
	public BooleanOption _showScripts;
	public BooleanOption _randomDelays;
	public BooleanOption _linkToLeft;
	public BooleanOption _playSynchronously;
	public BooleanOption _showSpeeds;
	public BooleanOption _showBlocks;
	public BooleanOption _beepOnAlert;
	public BooleanOption _savePreferences;
	public BooleanOption _showTrackFirst;
	public BooleanOption _traceScript;
	public BooleanOption _useRealTime;// not implemented
	public IntegerOption _enableTraining;	// not implemented
	public IntegerOption _serverPort;
	public BooleanOption _drawTrainNames;
	public boolean _noTrainNamesColors;
	public String _locale;
	public int _currentTimeMultiplier;
	public int _runPoints;
	public int _totalDelay;
	public int _totalLate;
	public int _timeMult;
	public List<String> _oldSimulations;
	public int _runPointBase;
	public int _runDay;
    private int _editorTrackType;
    private int _editorTrackDirection;

	public Simulator() {

		INSTANCE = this;

		_iconFactory = new IconFactory();
		_signalFactory = new SignalFactory();
		_colorFactory = new ColorFactory(Application._display);
		_scriptFactory = new ScriptFactory();

		_commands = new LinkedList<SimulatorCommand>();
		_alerts = new LinkedList<Alert>();
		_territory = new Territory();
		_schedule = new Schedule();
		
		_oldSimulations = new ArrayList<String>();

		_timedExecutor = new MyScheduler();

		_trainRunner = new TrainRunner(this);

		_timeSlice = 10;			// user-visible update every second
		_timer = new Timer();
		_timer.schedule(_timedExecutor, 500, 100);
		_locale = "en";
		
		initOptions();
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

	public void setSimulationSpeed(int speed) {
		_simulatedSpeed = speed;		// 1 clock second = speed simulated seconds
	}

	public int getSimulatedSpeed() {
		return _simulatedSpeed;
	}
	
	public void alert(String text) {
		Alert a = new Alert(_simulatedTime, text);
		_alerts.add(a);
		AlertEvent e = new AlertEvent(this);
		CGEventDispatcher.getInstance().postEvent(e);
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
		for(Track track : _territory._tracks) {
			if (!(track instanceof Signal))
				continue;
			Signal signal = (Signal) track;
			SignalAspect aspect = signal.getAspect();
			if (aspect == null)		// impossible
				continue;
			// TODO: optimize, since we don't need to have a local counter for each aspect.
			// We could have a global flashing counter, since each aspect uses count % aspects.length
			aspect.nextFlashing();
		}
	}

	public void doTimeSlice() {
		synchronized (this) {
			int simulatedSpeed = getSimulatedSpeed();
			
			for(int i = 0; i < simulatedSpeed; ++i) {
				++_simulatedTime;
				_trainRunner.timeStep();
			}
		}
	}

	public void updateSignals(Signal ignore) {
		openAllFleeted();
		if(_signalsChanged) {
			if (ignore != null)
				ignore._aspectChanged = true;
			for(Track track : _territory._tracks) {
				if(!(track instanceof Signal))
					continue;
				Signal signal = (Signal)track;
				if(signal != ignore)
					signal._aspectChanged = false;
			}
			do {
				_signalsChanged = false;
				for(Track track : _territory._tracks) {
					if(!(track instanceof Signal))
						continue;
					Signal signal = (Signal)track;
					if (signal == ignore)
						continue;
					signal.onUpdate();
				}
			} while(_signalsChanged);
			
		}
	}

	private void openAllFleeted() {
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

	public void initOptions() {
		_terseStatus = new BooleanOption("fullstatus", "Show full status");
		_statusOnTop = new BooleanOption("statusontop", "Show status line at the top of the screen");
		_showSeconds = new BooleanOption("showsecs", "Show seconds");
		_beepOnAlert = new BooleanOption("alertsound", "Play a sound when an alert is issued");
		_beepOnEnter = new BooleanOption("entersound", "Play a sound when a train enters the territory");
		_showSpeeds  = new BooleanOption("viewspped", "Show speed limit markers in the layout");
		_autoLink    = new BooleanOption("autolink", "Automatically link signals to the adjacent track when editing the scenario");
		_linkToLeft  = new BooleanOption("linktoleft", "Link signals to the adjacent track to the left of the signal");
		_showGrid	 = new BooleanOption("showgrid", "Show layout grid");
		_showBlocks	 = new BooleanOption("showblocks", "Show long blocks");
		_traditionalSignals = new BooleanOption("standardsigs", "Show traditional icons for automatic signals");
		_hardCounters = new BooleanOption("hardcounters", "Strict tracking of simulation performance");
		_showLinks	 = new BooleanOption("showlinks", "Show links between tracks when editing");
		_showScripts = new BooleanOption("showscripts", "Show whether a script is associated to track elements");
		_savePreferences = new BooleanOption("saveprefs", "Automatically save preferences when the program exits");
		_showTrackFirst = new BooleanOption("ShowTrkFirst", "ShowTrackFirst");
		_traceScript = new BooleanOption("traceScript", "Show execution of script instructions");
		_showIcons   = new BooleanOption("ShowIcons", "Show train icons on the layout");
		_useRealTime = new BooleanOption("RealTimeData", "Get real time train data");
		_enableTraining = new IntegerOption("EnableTraining", "Training mode");
		_randomDelays = new BooleanOption("RandomDelays", "Add random delays to trains arrival times");
		_playSynchronously = new BooleanOption("PlaySynchronously", "Stop simulation while playing sounds");
		_serverPort  = new IntegerOption("ServerPort", "Port to use for the web server");
		_drawTrainNames = new BooleanOption("TrainNames", "Show train names instead of train icons");
		_optionsManager = new OptionsManager();
		_optionsManager.add(_terseStatus);
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
	
}
