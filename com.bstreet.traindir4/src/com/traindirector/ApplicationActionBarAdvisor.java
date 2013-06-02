package com.traindirector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.uiactions.AssignAction;
import com.traindirector.uiactions.ChangeLanguageAction;
import com.traindirector.uiactions.CloseSimulationAction;
import com.traindirector.uiactions.EditAction;
import com.traindirector.uiactions.FastSimulationAction;
import com.traindirector.uiactions.ItineraryAction;
import com.traindirector.uiactions.OpenSimulationAction;
import com.traindirector.uiactions.PreferencesAction;
import com.traindirector.uiactions.RestartSimulationAction;
import com.traindirector.uiactions.RunSimulationAction;
import com.traindirector.uiactions.SaveLayoutChangesAction;
import com.traindirector.uiactions.SaveSimulationAction;
import com.traindirector.uiactions.ShowAlertsViewAction;
import com.traindirector.uiactions.ShowEditToolsAction;
import com.traindirector.uiactions.ShowScheduleViewAction;
import com.traindirector.uiactions.ShowTrainStopsViewAction;
import com.traindirector.uiactions.SlowSimulationAction;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the
 * actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction newWindowAction;
    private Action messagePopupAction;
    IWorkbenchWindow _window;
    public Label _statusLine;
    
    OpenSimulationAction openSimulationAction;
    CloseSimulationAction closeSimulationAction;
    SaveSimulationAction saveSimulationAction;
    EditAction editAction;
    SaveLayoutChangesAction saveLayoutChangesAction;
    RunSimulationAction runSimulationAction;
    FastSimulationAction fastSimulationAction;
    SlowSimulationAction slowSimulationAction;
    RestartSimulationAction restartSimulationAction;
    ShowScheduleViewAction showScheduleViewAction;
    ShowAlertsViewAction showAlertsViewAction;
    ShowEditToolsAction showEditToolsAction;
    ShowTrainStopsViewAction showTrainStopsViewAction;
    ChangeLanguageAction changeLanguageAction;
    PreferencesAction preferencesAction;
    ItineraryAction itineraryAction;
    AssignAction assignAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }
    
    protected void makeActions(final IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.

    	_window = window;

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        register(openSimulationAction = new OpenSimulationAction("Open Simulation"));
        register(closeSimulationAction = new CloseSimulationAction(window, "Close Simulation"));
        register(saveSimulationAction = new SaveSimulationAction(window, "Save Simulation State"));
        register(editAction = new EditAction(window, "Edit Simulation Layout"));
        register(saveLayoutChangesAction = new SaveLayoutChangesAction(window, "Save Layout"));
        register(runSimulationAction = new RunSimulationAction(window, "Run/Stop simulation"));
        register(fastSimulationAction = new FastSimulationAction(window, "Faster simulation"));
        register(slowSimulationAction = new SlowSimulationAction(window, "Slower simulation"));
        register(restartSimulationAction = new RestartSimulationAction(window, "Restart simulation"));
        register(showAlertsViewAction = new ShowAlertsViewAction(window, "Show alerts"));
        register(showScheduleViewAction = new ShowScheduleViewAction(window, "Show schedule"));
        register(showTrainStopsViewAction = new ShowTrainStopsViewAction(window, "Show train stops"));
        register(showEditToolsAction = new ShowEditToolsAction(window, "Show edit tools"));
        register(changeLanguageAction = new ChangeLanguageAction(window, "Change language"));
        register(preferencesAction = new PreferencesAction(window, "Edit preferences..."));
        register(itineraryAction = new ItineraryAction(window, "Itineraries..."));
        register(assignAction = new AssignAction(window, "Assign..."));

        /*
        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);
        
        openViewAction = new OpenViewAction(window, "Open Another Message View", View.ID);
        register(openViewAction);
        
        messagePopupAction = new MessagePopupAction("Open Message", window);
        register(messagePopupAction);
        */
    }
    
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
        MenuManager runMenu = new MenuManager("&Run", "Run");
        MenuManager viewMenu = new MenuManager("&View", "View");
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(runMenu);
        menuBar.add(viewMenu);
        // Add a group marker indicating where action set menus will appear.
        //menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);

        // File
        fileMenu.add(openSimulationAction);
        fileMenu.add(saveSimulationAction);
        fileMenu.add(closeSimulationAction);
        fileMenu.add(exitAction);

        editMenu.add(editAction);
        editMenu.add(saveLayoutChangesAction);
        editMenu.add(preferencesAction);
        
        runMenu.add(runSimulationAction);
        runMenu.add(fastSimulationAction);
        runMenu.add(slowSimulationAction);
        runMenu.add(restartSimulationAction);
        runMenu.add(itineraryAction);
        runMenu.add(assignAction);
        
        viewMenu.add(showScheduleViewAction);
        viewMenu.add(showAlertsViewAction);
        viewMenu.add(showTrainStopsViewAction);
        viewMenu.add(showEditToolsAction);
        
        // Help
        helpMenu.add(aboutAction);
        helpMenu.add(changeLanguageAction);
        
        /*
        fileMenu.add(newWindowAction);
        fileMenu.add(new Separator());
        fileMenu.add(messagePopupAction);
        fileMenu.add(openViewAction);
        fileMenu.add(new Separator());
        */
        
    }
    
    class MyStatus extends ControlContribution {

		protected MyStatus(String id) {
			super(id);
		}

		@Override
		public Control createControl(Composite parent) {
			_statusLine = new Label(parent, SWT.NONE);
			_statusLine.setText("                                                                                                ");
			return _statusLine;
		}
    }
    
    
    protected void fillCoolBar(ICoolBarManager coolBar) {
    	IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    	coolBar.add(new ToolBarContributionItem(toolbar, "main"));   
    	toolbar.add(openSimulationAction);
    	toolbar.add(runSimulationAction);
    	toolbar.add(fastSimulationAction);
    	toolbar.add(slowSimulationAction);
    	MyStatus myStatus = new MyStatus("Status line");
    	toolbar.add(myStatus);
    	/*
        toolbar.add(messagePopupAction);
        */
    	CGEventDispatcher.getInstance().addListener(new CGEventListener(TimeSliceEvent.class) {
    		public void handle(CGEvent event, Object target) {
    			if(_window == null)
    				return;
    			final Shell shell = _window.getShell();
    			if(shell == null || shell.isDisposed())
    				return;
    			shell.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if(shell.isDisposed())
							return;
						shell.setText(Application._simulator.getHeader());
					}
    			});
    		}
    	});
    }
}
