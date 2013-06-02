package com.traindirector.commands;

import java.nio.channels.ShutdownChannelGroupException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.traindirector.Application;
import com.traindirector.dialogs.AssignDialog;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
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
				
			} else
				track.onClick();
		}
	}

	private boolean leftClickOnTrain(final Train train) {
		Application._display.syncExec(new Runnable() {
			@Override
			public void run() {
				if(MessageDialog.openQuestion(Application._display.getActiveShell(), "Reverse Train", "Reverse train direction?")) {
					ReverseCommand rcmd = new ReverseCommand(train);
					_simulator.addCommand(rcmd);
				}
			}
		});
		return false;
	}

	private boolean rightClickOnTrain(final Train train) {
		final boolean[] result = new boolean[1];
		Application._display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = doAssignDialog(train);
			}
		});
		return result[0];
	}
	
	private boolean doAssignDialog(Train train) {
		Shell shell = Application._display.getActiveShell();
		if (train._speed != 0) {
			MessageDialog.openError(shell, "Error", "Train must be stopped.");
			return false;
		}
		AssignDialog dialog = new AssignDialog(shell, train, _simulator);
		List<Train> departingTrains = _simulator._schedule.getTrainsDepartingFrom(train._position._station);
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
