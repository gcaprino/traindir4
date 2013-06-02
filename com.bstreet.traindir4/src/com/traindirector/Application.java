package com.traindirector;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.traindirector.simulator.Simulator;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	public static Display _display;
	public static Simulator _simulator;
	public static ApplicationWorkbenchAdvisor _workbenchAdvisor;
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		_display = PlatformUI.createDisplay();
		_simulator = new Simulator();
		try {
			_workbenchAdvisor = new ApplicationWorkbenchAdvisor();
			int returnCode = PlatformUI.createAndRunWorkbench(_display, _workbenchAdvisor);
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			_display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
	
	public static Simulator getSimulator() {
		return _simulator;
	}
}
