package com.traindirector.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.traindirector.Application;
import com.traindirector.dialogs.AssignDialog;
import com.traindirector.dialogs.ItineraryProperties;
import com.traindirector.dialogs.PropertyDialog;
import com.traindirector.dialogs.TextOption;
import com.traindirector.dialogs.TriggerPropertiesDialog;
import com.traindirector.model.Itinerary;
import com.traindirector.model.ItineraryButton;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TriggerTrack;
import com.traindirector.options.BooleanOption;
import com.traindirector.options.FileOption;
import com.traindirector.options.Option;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class ClickCommand extends SimulatorCommand {

	boolean _leftButton;
	boolean _shiftKey;
	boolean _altKey;
	boolean _ctrlKey;
	TDPosition _pos;
	List<Option> _options;
	static TriggerPropertiesDialog _triggerPropertiesDialog;

	public ClickCommand(TDPosition pos) {
		_pos = pos;
		_leftButton = true;
		_shiftKey = false;
		_altKey = false;
		_ctrlKey = false;
	}
	
	public boolean isLeftClick() {
		return _leftButton;
	}
	
	public boolean isRightClick() {
		return !_leftButton;
	}
	
	public boolean isControlClick() {
		return _ctrlKey;
	}
	
	public boolean isShiftClick() {
		return _shiftKey;
	}
	
	public boolean isAltClick() {
		return _altKey;
	}

	public void setLeftClick(boolean b) {
		_leftButton = b;
	}
	
	public void setAltKey(boolean b) {
		_altKey = b;
	}
	
	public void setCtrlKey(boolean b) {
		_ctrlKey = b;
	}
	
	public void setShiftKey(boolean b) {
		_shiftKey = b;
	}
	
	public void handle() {
		if(Simulator.getEditingItineraries()) {
			handleEditingItineraries();
			return;
		}
	    if(Simulator.getEditing()) {
	        handleEditing();
	        return;
	    }
		Train train = _simulator._schedule.findAnyTrainAt(_pos);
		if (train != null) {
			if(isRightClick()) {
				if (rightClickOnTrain(train))
					return;
			} else {
				if (leftClickOnTrain(train))
					return;
			}
		}
		Track track = _simulator._territory.findTrack(_pos);
		if (track == null) {
			return;
		}
		if(isRightClick()) {
			track.onRightClick();
		} else if(isAltClick()) {
			track.onAltClick();
		} else if(isControlClick()) {
			track.onCtrlClick();
		} else if(isShiftClick()) {
			track.onShiftClick();
		} else {
			if(track instanceof Signal) {
				Signal signal = (Signal) track;
				SignalAspect oldAspect = signal.getAspect();
				signal.onClick();
				SignalAspect newAspect = signal.getAspect();
				if(oldAspect._action.compareTo(SignalAspect.STOP) == 0 &&
						newAspect._action.compareTo(SignalAspect.STOP) != 0) {
					// the signal has been cleared
					signal._aspectChanged = true;
					// TODO: call onCleared
				} else if(!oldAspect._action.equals(newAspect._action ))
					signal._aspectChanged = true;
				_simulator._signalsChanged = true;
				_simulator.updateSignals(signal);
				_simulator._signalsChanged = true;
			} else
				track.onClick();
		}
	}

	private void handleEditingItineraries() {
		Track track = _simulator._territory.findTrack(_pos);
		if (track == null)
			return;
		if (track instanceof Signal) {
			Signal signal = (Signal) track;
			if (isRightClick()) {
				Itinerary itinerary = new Itinerary();
				itinerary._signame = signal._position.toString();
				_simulator._territory._itineraries.add(itinerary);
				ItineraryProperties props = new ItineraryProperties(itinerary);
				if(!props.open())
					_simulator._territory._itineraries.remove(itinerary);
			} else {
				signal.onClick();
			}
			return;
		}
		if (track instanceof Switch) {
			track.onClick();
		}
	}

	private void handleEditing() {
	    Track track;
	    Switch sw;
	    Signal signal;
	    Territory territory = _simulator._territory;
	    
	    if(isRightClick()) {
	        
	        // Open properties dialog based on object type
	        track = territory.findTrack(_pos);
	        if(track == null)
	        	return;
	        
	        if(track instanceof Switch) {
	        	return; // switches have no properties
	        } else if(track instanceof Signal) {
	        	editSignalProperties((Signal)track);
	        } else if(track instanceof TriggerTrack) {
	        	editTriggerProperties((TriggerTrack)track);
	        } else if(track instanceof ItineraryButton) {
	        	editItineraryButton((ItineraryButton)track);
	        } else {
	        	editTrackProperties(track);
	        }
	    } else {
	        int type = _simulator.getEditorTrackType();
	        int dir = _simulator.getEditorTrackDirection();
	        track = territory.findTrack(_pos);
	        if(track != null) {
	            territory.remove(track);
	        }
	        switch(type) {
	        case 0:    // delete
	            break;
	        case 1:    // tracks
	            track = new Track(_pos);
	            track._direction = TrackDirection.parse(dir);
	            territory.add(track);
	            break;
	        case 2:    // switches
                sw = new Switch(_pos);
                sw._direction = TrackDirection.parse(dir);
                territory.add(sw);
	            break;
	        case 3:    // signals
                signal = _simulator._signalFactory.newInstance(dir < 4 ? 1 : 2);
                signal._position = _pos;
                signal._direction = TrackDirection.parse(dir);
                signal.setAspectFromName(SignalAspect.RED);
                signal._status = TrackStatus.FREE; // RED
                if(dir >= 4)
                    signal._fleeted = true;
                territory.add(signal);
                if(!_simulator._options._autoLink.isSet())
                    break;
                int dx = 0;
                int dy = 0;
                switch(dir) {
                case 0:
                case 4:
                    dx = 0;
                    dy = -1;
                    break;
                case 1:
                case 5:
                    dx = 0;
                    dy = 1;
                    break;
                case 2:
                case 6:
                    dx = 1;
                    dy = 0;
                    break;
                case 3:
                case 7:
                    dx = -1;
                    dy = 0;
                    break;
                }
                if(_simulator._options._linkToLeft.isSet()) {
                    dx *= -1;
                    dy *= -1;
                }
                track = territory.findTrack(new TDPosition(_pos._x + dx, _pos._y * dy));
                if(track == null)
                    break;
                signal.linkedTo(track);
	            break;
	        case 4:    // items
	            break;
	        case 5:    // actions
	            break;
	        }
	    }
    }

    private void editTrackProperties(Track track) {
    	_options = new ArrayList<Option>();
    	Option o = new TextOption("length", "Track Length (m) :");
    	o._value = "" + track._length;
    	_options.add(o); // 0
    	o = new TextOption("station", "Station name :");
    	o._value = track._station;
    	_options.add(o); // 1
    	o = new TextOption("km", "Lm. :");
    	o._value = "" + track._km;
    	_options.add(o); // 2
    	o = new TextOption("speeds", "Speed(s) :");
    	if(track._speed == null)
    		track._speed = new int[Track.NSPEEDS];
    	o._value = track.speedsToString();
    	_options.add(o); // 3
    	o = new TextOption("eastlink", "Linked to east :");
    	if(track._elink != null)
    		o._value = "" + track._elink._x + "," + track._elink._y;
    	_options.add(o); // 4
    	o = new TextOption("westlink", "Linked to west :");
    	if(track._wlink != null)
    		o._value = "" + track._wlink._x + "," + track._wlink._y;
    	_options.add(o); // 5
    	
    	o = new BooleanOption("hidden", "Hidden");
    	o._intValue = track._invisible ? 1 : 0;
    	_options.add(o); // 6
    	o = new BooleanOption("highsignal", "Don't stop if shunting");
    	o._intValue = (track._flags & Track.DONTSTOPSHUNTERS) != 0 ? 1 : 0;
    	_options.add(o); // 7

    	// TODO: add script button
    	
    	final int[] result = new int[1];
    	final PropertyDialog dialog = new PropertyDialog(null, _options);
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = dialog.open();
			}
		});
    	if(result[0] == PropertyDialog.CANCEL)
    		return;
    	
    	track._length = Integer.parseInt(_options.get(0)._value);
    	track._station = _options.get(1)._value;
    	track._km = Integer.parseInt(_options.get(2)._value);	// TODO: parse km.meters
    	String[] spds = _options.get(3)._value.split("/");
    	for(int i = 0; i < track._speed.length; ++i) {
    		if(i >= spds.length)
    			track._speed[i] = 0;
    		else
    			track._speed[i] = Integer.parseInt(spds[i]);
    	}
		track._elink = new TDPosition(_options.get(4)._value);
		track._wlink = new TDPosition(_options.get(5)._value);
		track._invisible = _options.get(6)._intValue != 0;
		track._flags &= ~Track.DONTSTOPSHUNTERS;
		if(_options.get(7)._intValue != 0)
			track._flags |= Track.DONTSTOPSHUNTERS;
	}

	private void editItineraryButton(ItineraryButton track) {
	}

	private void editTriggerProperties(TriggerTrack track) {
		if (_triggerPropertiesDialog == null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					_triggerPropertiesDialog = new TriggerPropertiesDialog(Application._display.getActiveShell());
				}
			});
		}
		_triggerPropertiesDialog.open(track);
	}

	private void editSignalProperties(Signal signal) {
    	_options = new ArrayList<Option>();
    	String name = signal._station;
    	if((name == null || name.isEmpty()) && signal._position != null)
    		name = signal._position.toString();
    	addTextOption("Signal name :", name);	// 0
    	name = "";
    	if(signal._wlink != null) {
    		name = "" + signal._wlink._x + "," + signal._wlink._y;
    	}
    	addTextOption("Linked to track at :", name); // 1
    	
    	name = "";
    	if(signal._blockedBy != null) {
    		name = "" + signal._blockedBy._x + "," + signal._blockedBy._y;
    	}
    	addTextOption("Blocked by :", name); // 2
    	
    	addBooleanOption("Signal is always red", signal._fixedred); // 3
    	addBooleanOption("Signal has square frame", signal._signalx); // 4
    	addBooleanOption("No penalty for train stopping at this signal", signal._nopenalty); // 5
    	addBooleanOption("No penalty for un-necessary clicks", signal._noClickPenalty); // 6
    	addBooleanOption("Hidden", signal._invisible); // 7
    	addBooleanOption("Intermediate", signal._intermediate); // 8
    	FileOption fo = new FileOption("script", "Script file :"); // 9
    	if(signal._script != null)
    		fo._value = signal._scriptFile;
    	_options.add(fo);

    	final int[] result = new int[1];
    	final PropertyDialog dialog = new PropertyDialog(null, _options);
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = dialog.open();
			}
		});
    	if(result[0] == PropertyDialog.CANCEL)
    		return;
    	
    	signal._station = _options.get(0)._value;
    	// TODO: read coords for linked and blocked
    	signal._fixedred = _options.get(3)._intValue != 0;
    	signal._signalx = _options.get(4)._intValue != 0;
    	signal._nopenalty = _options.get(5)._intValue != 0;
    	signal._noClickPenalty = _options.get(6)._intValue != 0;
    	signal._invisible = _options.get(7)._intValue != 0;
    	signal._intermediate = _options.get(8)._intValue != 0;
	}

	private void addTextOption(String descr, String value) {
    	TextOption o = new TextOption("", descr);
    	o._value = value;
    	_options.add(o); // 0
	}
	
	private void addBooleanOption(String descr, boolean value) {
    	BooleanOption o = new BooleanOption("", descr);
    	o._intValue = value ? 1 : 0;
    	_options.add(o);
	}
	
	private boolean leftClickOnTrain(final Train train) {
		Application._display.syncExec(new Runnable() {
			@Override
			public void run() {
				if(MessageDialog.openQuestion(Application._display.getActiveShell(), "Reverse Train", "Reverse train direction?")) {
					_simulator.addCommand(new ReverseCommand(train));
				}
			}
		});
		return false;
	}

	private boolean rightClickOnTrain(final Train train) {
		final boolean[] result = new boolean[1];
		if (train.isArrived()) {
			Application._display.syncExec(new Runnable() {
				@Override
				public void run() {
					result[0] = doAssignDialog(train);
				}
			});
			return result[0];
		}
	    if(train._speed > 0) {
			if(!train._shunting) {
			    _simulator.alert("You must wait for train to stop.");
			    return false;
			}
			_simulator.alert("Train stopped.");
			train._outOf = null;
			_simulator._trainRunner.trainAtStation(train, train._position); // revert from shunting to stopped
			return true;
	    }
	    
	    if(!_simulator.ask("Proceed to next station?"))
			return false;
	    _simulator.addCommand(new ShuntCommand(train));
	    return true;
	}
	
	private boolean doAssignDialog(Train train) {
		Shell shell = Application._display.getActiveShell();
		if (train._speed != 0) {
			MessageDialog.openError(shell, "Error", "Train must be stopped.");
			return false;
		}
		AssignDialog dialog = new AssignDialog(shell, train, _simulator);
		Track station = train.getStoppedAt();
		List<Train> departingTrains = _simulator._schedule.getTrainsDepartingFrom(station == null ? null : station._station);
		dialog.create();
		dialog.fillTable(departingTrains);
		int result = dialog.open();
		SimulatorCommand cmd = null;
		String selectedTrain = null;
		Train to = null;
		int length = 0;
		switch (result) {
		case AssignDialog.ASSIGN:
			selectedTrain = dialog.getSelectedTrain();
			if (selectedTrain == null)	// impossible
				return false;
			to = _simulator._schedule.findTrainNamed(selectedTrain);
			cmd = new AssignCommand(train, to);
			break;
		case AssignDialog.SHUNT:
			cmd = new ShuntCommand(train);
			break;
		case AssignDialog.ASSIGN_AND_SHUNT:
			selectedTrain = dialog.getSelectedTrain();
			if (selectedTrain == null)	// impossible
				return false;
			to = _simulator._schedule.findTrainNamed(selectedTrain);
			cmd = new AssignCommand(train, to);
			_simulator.addCommand(cmd);
			cmd = new ShuntCommand(to);
			break;
		case AssignDialog.REVERSE_AND_ASSIGN:
			cmd = new ReverseCommand(train);
			_simulator.addCommand(cmd);
			selectedTrain = dialog.getSelectedTrain();
			if (selectedTrain == null)	// impossible
				return false;
			to = _simulator._schedule.findTrainNamed(selectedTrain);
			cmd = new AssignCommand(train, to);
			break;

		case AssignDialog.SPLIT:
			// TODO: open split dialog
			cmd = new SplitCommand(train, length);
			break;

		case AssignDialog.PROPERTIES:
		case AssignDialog.CLOSE:
			return false;
		}
//		icmd._deselect = result == AssignDialog.CLEAR;
		if(cmd != null) {
			_simulator.addCommand(cmd);
			return true;
		}
		return false;
	}
}
