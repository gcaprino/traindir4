package com.traindirector.commands;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.traindirector.Application;
import com.traindirector.dialogs.AssignDialog;
import com.traindirector.dialogs.ItineraryProperties;
import com.traindirector.model.Itinerary;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class ClickCommand extends SimulatorCommand {

	boolean _leftButton;
	boolean _shiftKey;
	boolean _altKey;
	boolean _ctrlKey;
	TDPosition _pos;

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
			InputDialog splitDialog = new InputDialog(shell, "Split train", "Position where to split the train (meters from the head)", "100", null);
			if (splitDialog.open() != InputDialog.OK)
				break;
			String value = splitDialog.getValue();
			try {
				length = Integer.parseInt(value);
			} catch (Exception e) {
				MessageDialog.open(MessageDialog.ERROR, shell, "Error", "Length must be a numeric value.", SWT.NONE);
				break;
			}
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
