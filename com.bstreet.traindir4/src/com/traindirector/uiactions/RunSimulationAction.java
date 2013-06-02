package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.RunStopCommand;
import com.traindirector.simulator.Simulator;

public class RunSimulationAction extends Action {

	public RunSimulationAction(IWorkbenchWindow window, String label) {
		super(label, IAction.AS_CHECK_BOX);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_RUN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_RUN);
		setImageDescriptor(Activator.getImageDescriptor("/icons/play16x16black.png"));
	}
	
	@Override
	public boolean isChecked() {
		return Simulator.INSTANCE.isRunning();
	}

    @Override
	public void run() {
		RunStopCommand cmd = new RunStopCommand();
		if(Simulator.INSTANCE.isRunning()) {
			this.setChecked(false);
			//setImageDescriptor(Activator.getImageDescriptor("/icons/pause16x16black.png"));
		} else {
			this.setChecked(true);
			//setImageDescriptor(Activator.getImageDescriptor("/icons/play16x16black.png"));
		}
		Application.getSimulator().addCommand(cmd);
	}
}
