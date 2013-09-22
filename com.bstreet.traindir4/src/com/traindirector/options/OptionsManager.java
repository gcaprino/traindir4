package com.traindirector.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.traindirector.dialogs.ColorOption;
import com.traindirector.dialogs.TextOption;


public class OptionsManager {


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
	public BooleanOption _showTooltip;
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
	public BooleanOption _enableHttpServer;
	public TextOption _userName;
	public BooleanOption _drawTrainNames;
	public BooleanOption _noTrainNamesColors;
	public BooleanOption _swapHeadTail;
	public String _locale;

	public ColorOption _readyColorFg, _readyColorBg;
	public ColorOption _runningColorFg, _runningColorBg;
	public ColorOption _stoppedColorFg, _stoppedColorBg;
	public ColorOption _waitingColorFg, _waitingColorBg;
	public ColorOption _delayedColorFg, _delayedColorBg;
	public ColorOption _arrivedColorFg, _arrivedColorBg;
	public ColorOption _derailedColorFg, _derailedColorBg;
	public ColorOption _cancelledColorFg, _cancelledColorBg;
	
	public List<Option> _options;
	public List<Option> _colorOptions;
	public ColorOption _background;
	public ColorOption _freeTrackColor;
	public ColorOption _reservedTrackColor;
	public ColorOption _shuntingTrackColor;
	public ColorOption _occupiedTrackColor;
	public ColorOption _workingTrackColor;
	public ColorOption _textTrackColor;

	public FileOption _alertSoundPath;
	public FileOption _enterSoundPath;
	public TextOption _scriptsPaths;

	public OptionsManager() {
		_options = new ArrayList<Option>();
	}

	public List<Option> getOptionList() {
		return _options;
	}

	public void add(Option o) {
		_options.add(o);
	}

	public Option get(String name) {
		for (Option o : _options)
			if (o._name.equals(name))
				return o;
		return null;
	}
	
	public String getString(String name) {
		Option o = get(name);
		if (o == null)
			return "";
		return o._value;
	}
	
	public int getInteger(String name) {
		Option o = get(name);
		if (o == null)
			return 0;
		return o._intValue;
	}
	
	public void initOptions() {
		
		if (_colorOptions == null) 
			_colorOptions = new ArrayList<Option>();
		else
			_colorOptions.clear();

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
		_enableHttpServer = new BooleanOption("httpServerEnabled", "Enable HTTP server");
		_userName = new TextOption("userName", "Name of the player (leave empty to disable multiplayer)");
		_drawTrainNames = new BooleanOption("TrainNames", "Show train names instead of icons");
		_noTrainNamesColors = new BooleanOption("NoTrainNamesColors", "Don't show train names colors");
		_swapHeadTail = new BooleanOption("SwapHeadTail", "Swap head and tail icons");
		_showTooltip = new BooleanOption("ShowTooltip", "Show trains tooltip");
		_options.add(_terseStatus);
		_options.add(_statusOnTop);
		_options.add(_showSeconds);
		_options.add(_beepOnAlert);
		_options.add(_beepOnEnter);
		_options.add(_showSpeeds);
		_options.add(_autoLink);
		_options.add(_linkToLeft);
		_options.add(_showGrid);
		_options.add(_showBlocks);
		_options.add(_traditionalSignals);
		_options.add(_hardCounters);
		_options.add(_showLinks);
		_options.add(_showScripts);
		_options.add(_savePreferences);
		_options.add(_showTrackFirst);
		_options.add(_traceScript);
		_options.add(_showIcons);
		_options.add(_useRealTime);
		_options.add(_enableTraining);
		_options.add(_randomDelays);
		_options.add(_playSynchronously);
		_options.add(_serverPort);
		_options.add(_drawTrainNames);
		_options.add(_noTrainNamesColors);
		_options.add(_swapHeadTail);
		_options.add(_showTooltip);

		_alertSoundPath = new FileOption("path.sound.alert", "Path to sound file for alert notifications");
		_enterSoundPath = new FileOption("path.sound.entry", "Path to sound file for train entry");
		_scriptsPaths = new TextOption("SearchPath", "Directories with signal scripts");
		
		_options.add(_alertSoundPath);
		_options.add(_enterSoundPath);
		_options.add(_scriptsPaths);

		_readyColorFg = new ColorOption("colors.ready.fg", "Color to use for ready trains (foreground)", 0, 0, 255);
		_readyColorBg = new ColorOption("colors.ready.bg", "Color to use for ready trains (background)", 255, 255, 255);
		_runningColorFg = new ColorOption("colors.running.fg", "Color to use for running trains (foreground)", 0, 0, 0);
		_runningColorBg = new ColorOption("colors.running.bg", "Color to use for running trains (background)", 255, 255, 255);
		_stoppedColorFg = new ColorOption("colors.stopped.fg", "Color to use for stopped trains (foreground)", 0, 0, 0);
		_stoppedColorBg = new ColorOption("colors.stopped.bg", "Color to use for stopped trains (background)", 255, 255, 255);
		_waitingColorFg = new ColorOption("colors.waiting.fg", "Color to use for waiting trains (foreground)", 0, 0, 0);
		_waitingColorBg = new ColorOption("colors.waiting.bg", "Color to use for waiting trains (background)", 255, 192, 0);
		_delayedColorFg = new ColorOption("colors.delayed.fg", "Color to use for delayed trains (foreground)", 0, 0, 0);
		_delayedColorBg = new ColorOption("colors.delayed.bg", "Color to use for delayed ftrains (background)", 255, 255, 0);
		_arrivedColorFg = new ColorOption("colors.arrived.fg", "Color to use for arrived trains (foreground)", 0, 192, 0);
		_arrivedColorBg = new ColorOption("colors.arrived.bg", "Color to use for arrived trains (background)", 255, 255, 255);
		_derailedColorFg = new ColorOption("colors.derailed.fg", "Color to use for derailed trains (foreground)", 255, 0, 0);
		_derailedColorBg = new ColorOption("colors.derailed.bg", "Color to use for derailed trains (background)", 255, 240, 240);
		_cancelledColorFg = new ColorOption("colors.cancelled.fg", "Color to use for cancelled trains (foreground)", 192, 192, 192);
		_cancelledColorBg = new ColorOption("colors.cancelled.bg", "Color to use for cancelled trains (background)", 255, 255, 255);

		_colorOptions.add(_readyColorFg);
		_colorOptions.add(_readyColorBg);
		_colorOptions.add(_runningColorFg);
		_colorOptions.add(_runningColorBg);
		_colorOptions.add(_stoppedColorFg);
		_colorOptions.add(_stoppedColorBg);
		_colorOptions.add(_waitingColorFg);
		_colorOptions.add(_waitingColorBg);
		_colorOptions.add(_delayedColorFg);
		_colorOptions.add(_delayedColorBg);
		_colorOptions.add(_arrivedColorFg);
		_colorOptions.add(_arrivedColorBg);
		_colorOptions.add(_derailedColorFg);
		_colorOptions.add(_derailedColorBg);
		_colorOptions.add(_cancelledColorFg);
		_colorOptions.add(_cancelledColorBg);
		
		_background = new ColorOption("colors.canvas.bg", "Color of layout background", 168, 168, 168);
		_freeTrackColor = new ColorOption("colors.canvas.free", "Color of free track", 0, 0, 0);
		_reservedTrackColor = new ColorOption("colors.canvas.reserved", "Color of reserved track", 0, 255, 0);
		_shuntingTrackColor = new ColorOption("colors.canvas.shunting", "Color of reserved for shunting track", 255, 255, 255);
		_occupiedTrackColor = new ColorOption("colors.canvas.occupied", "Color of occupied track", 255, 128, 0);
		_workingTrackColor = new ColorOption("colors.canvas.working", "Color of track reserved for working", 0, 0, 255);
		_textTrackColor = new ColorOption("colors.canvas.text", "Color of layout text", 0, 0, 0);
		
		_colorOptions.add(_background);
		_colorOptions.add(_freeTrackColor);
		_colorOptions.add(_reservedTrackColor);
		_colorOptions.add(_shuntingTrackColor);
		_colorOptions.add(_occupiedTrackColor);
		_colorOptions.add(_workingTrackColor);
		_colorOptions.add(_textTrackColor);
		
		_serverPort._intValue = 8081;
		_enableHttpServer._intValue = 1;
	}

	public Map<Option, Widget> createOptionsWidgets(Composite parent, List<Option> fields) {
		GridData ld;
		Label lbl;
		Text txt;
		Button checkBox;

		HashMap<Option, Widget> widgets = new HashMap<Option, Widget>();
		for(Option o : fields) {
			if(o instanceof TextOption || o instanceof IntegerOption) {
				lbl = new Label(parent, SWT.NONE);
				lbl.setText(o._description);
				txt = new Text(parent, SWT.BORDER);
				if(o instanceof IntegerOption)
					txt.setText("" + o._intValue);
				else if(o._value != null)
					txt.setText(o._value);
				ld = new GridData();
				ld.horizontalSpan = 2;
				ld.grabExcessHorizontalSpace = true;
				ld.horizontalAlignment = GridData.FILL;
				txt.setLayoutData(ld);
				widgets.put(o, txt);
			} else if(o instanceof BooleanOption) {
				checkBox = new Button(parent, SWT.CHECK);
				checkBox.setText(o._description);
				checkBox.setSelection(o._intValue != 0);
				ld = new GridData();
				ld.grabExcessHorizontalSpace = true;
				ld.horizontalAlignment = GridData.FILL;
				ld.horizontalSpan = 3;
				checkBox.setLayoutData(ld);
				widgets.put(o, checkBox);
			} else if(o instanceof FileOption) {
				lbl = new Label(parent, SWT.NONE);
				lbl.setText(o._description);
				txt = new Text(parent, SWT.BORDER);
				if(o._value != null)
					txt.setText(o._value);
				ld = new GridData();
				ld.grabExcessHorizontalSpace = true;
				ld.horizontalAlignment = GridData.FILL;
				txt.setLayoutData(ld);
				widgets.put(o, txt);
				Button browseButton = new Button(parent, SWT.PUSH);
				browseButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			} else if(o instanceof ColorOption) {
				lbl = new Label(parent, SWT.NONE);
				lbl.setText(o._description);
				txt = new Text(parent, SWT.BORDER);
				String v = ((o._intValue >> 16) & 0xFF) + "," + ((o._intValue >> 8) & 0xFF) + "," + (o._intValue & 0xFF);
				txt.setText(v);
				ld = new GridData();
				ld.grabExcessHorizontalSpace = true;
				ld.horizontalAlignment = GridData.FILL;
				txt.setLayoutData(ld);
				widgets.put(o, txt);
				Button browseColorButton = new Button(parent, SWT.PUSH);
				browseColorButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
		return widgets;
	}

	public void setOptions(Map<Option, Widget> _map) {
		for(Option o : _options) {
			Widget w = _map.get(o);
			if (w instanceof Button) {
				Button b = (Button) w;
				o._intValue = b.getSelection() ? 1 : 0;
			} else if (w instanceof Text) {
				Text t = (Text) w;
				if (o instanceof IntegerOption) {
					try {
						o._intValue = Integer.parseInt(t.getText());
					} catch (Exception e) {
						o._intValue = 0;
					}
				}
				o._value = t.getText();
			}
		}
	}


}
