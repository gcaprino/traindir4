package com.traindirector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
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
import com.traindirector.uiactions.CoordBarsAction;
import com.traindirector.uiactions.EditAction;
import com.traindirector.uiactions.FastSimulationAction;
import com.traindirector.uiactions.InfoAction;
import com.traindirector.uiactions.ItinerariesAction;
import com.traindirector.uiactions.ItineraryAction;
import com.traindirector.uiactions.LateGraphAction;
import com.traindirector.uiactions.NewAction;
import com.traindirector.uiactions.OpenSimulationAction;
import com.traindirector.uiactions.PlatformOccupancyAction;
import com.traindirector.uiactions.PreferencesAction;
import com.traindirector.uiactions.RestartSimulationAction;
import com.traindirector.uiactions.RestoreSimulationAction;
import com.traindirector.uiactions.RunSimulationAction;
import com.traindirector.uiactions.SaveLayoutChangesAction;
import com.traindirector.uiactions.SaveSimulationAction;
import com.traindirector.uiactions.SetSignalsToGreenAction;
import com.traindirector.uiactions.ShowAlertsViewAction;
import com.traindirector.uiactions.ShowEditToolsAction;
import com.traindirector.uiactions.ShowInfoPageAction;
import com.traindirector.uiactions.ShowLayoutPageAction;
import com.traindirector.uiactions.ShowMapAction;
import com.traindirector.uiactions.ShowPerformanceAction;
import com.traindirector.uiactions.ShowScheduleViewAction;
import com.traindirector.uiactions.ShowTraceViewAction;
import com.traindirector.uiactions.ShowTrainStopsViewAction;
import com.traindirector.uiactions.ShowWelcomePageAction;
import com.traindirector.uiactions.SkipAheadAction;
import com.traindirector.uiactions.SlowSimulationAction;
import com.traindirector.uiactions.StationScheduleAction;
import com.traindirector.uiactions.StationsListAction;
import com.traindirector.uiactions.StatusBarAction;
import com.traindirector.uiactions.SwitchBoardAction;
import com.traindirector.uiactions.TimeDistanceGraphAction;
import com.traindirector.uiactions.ZoomInAction;
import com.traindirector.uiactions.ZoomOutAction;

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
    public IWorkbenchWindow _window;
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
    ShowTraceViewAction showTraceViewAction;
    ShowEditToolsAction showEditToolsAction;
    ShowTrainStopsViewAction showTrainStopsViewAction;
    ShowWelcomePageAction showWelcomePageAction;
    ShowMapAction showMapAction;
    ChangeLanguageAction changeLanguageAction;
    PreferencesAction preferencesAction;
    ItineraryAction itineraryAction;
    AssignAction assignAction;
    PlatformOccupancyAction platformOccupancyAction;
	private IAction statusBarAction;
	private IAction coordBarsAction;
	private IAction zoomOutAction;
	private IAction zoomInAction;
	private IAction showInfoPageAction;
	private IAction showLayoutViewAction;
	private IAction performanceAction;
	private IAction setSignalToGreenAction;
	private IAction stationScheduleAction;
	private IAction skipAheadAction;
	private IAction lateGraphAction;
	private IAction timeDistanceAction;
	private IAction stationsListAction;
	private IAction infoAction;
	private IAction newAction;
	private IAction editSwitchboardAction;
	private IAction editItineraryAction;
	private IAction restoreSimulationAction;

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
        register(showTraceViewAction = new ShowTraceViewAction(window, "Show trace"));
        register(showScheduleViewAction = new ShowScheduleViewAction(window, "Show schedule"));
        register(showTrainStopsViewAction = new ShowTrainStopsViewAction(window, "Show train stops"));
        register(showEditToolsAction = new ShowEditToolsAction(window, "Show edit tools"));
        register(showMapAction = new ShowMapAction(window, "Show Map"));
        register(changeLanguageAction = new ChangeLanguageAction(window, "Change language"));
        register(preferencesAction = new PreferencesAction(window, "Edit preferences..."));
        register(itineraryAction = new ItineraryAction(window, "Itineraries..."));
        register(assignAction = new AssignAction(window, "Assign..."));

    	register(statusBarAction = new StatusBarAction(window, "Show Status Bar"));
    	register(coordBarsAction = new CoordBarsAction(window, "Show Coordinate Bars"));
    	register(zoomOutAction = new ZoomOutAction(window, "Zoom Out"));
    	register(zoomInAction = new ZoomInAction(window, "Zoom In"));
    	register(showInfoPageAction = new ShowInfoPageAction(window, "Show Info"));
    	register(showWelcomePageAction = new ShowWelcomePageAction(window, "Show Welcome Page"));
    	register(showLayoutViewAction = new ShowLayoutPageAction(window, "Show Layout"));
    	register(performanceAction = new ShowPerformanceAction(window, "Performance..."));
    	register(setSignalToGreenAction = new SetSignalsToGreenAction(window, "Set Signals to Green"));
    	register(stationScheduleAction = new StationScheduleAction(window, "Station Schedule"));
    	register(skipAheadAction = new SkipAheadAction(window, "Skip Ahead"));
    	register(lateGraphAction = new LateGraphAction(window, "Late Graph"));
    	register(timeDistanceAction = new TimeDistanceGraphAction(window, "Time-Distance Graph"));
        register(platformOccupancyAction = new PlatformOccupancyAction(window, "Platform Occupancy Graph"));
    	register(stationsListAction = new StationsListAction(window, "Stations List"));
    	register(infoAction = new InfoAction(window, "Info"));
    	register(newAction = new NewAction(window, "New..."));
    	register(editSwitchboardAction = new SwitchBoardAction(window, "Switchboard"));
    	register(editItineraryAction = new ItinerariesAction(window, "Itineraries"));
    	register(restoreSimulationAction = new RestoreSimulationAction(window, "Restore..."));
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
        fileMenu.add(restoreSimulationAction);
        fileMenu.add(closeSimulationAction);
        // TODO: print
        fileMenu.add(exitAction);

        editMenu.add(editAction);
        editMenu.add(editItineraryAction);
        editMenu.add(editSwitchboardAction);
        editMenu.add(saveLayoutChangesAction);
        editMenu.add(preferencesAction);
        editMenu.add(newAction);
        editMenu.add(infoAction);
        editMenu.add(stationsListAction);
        
        runMenu.add(runSimulationAction);
        runMenu.add(timeDistanceAction);
        runMenu.add(lateGraphAction);
        runMenu.add(restartSimulationAction);
        runMenu.add(fastSimulationAction);
        runMenu.add(slowSimulationAction);
        runMenu.add(skipAheadAction);
        runMenu.add(stationScheduleAction);
        runMenu.add(setSignalToGreenAction);
        runMenu.add(itineraryAction);
        runMenu.add(performanceAction);
        runMenu.add(assignAction);
        
        viewMenu.add(showWelcomePageAction);
        viewMenu.add(showLayoutViewAction);
        viewMenu.add(showScheduleViewAction);
        viewMenu.add(showAlertsViewAction);
        viewMenu.add(showTrainStopsViewAction);
        viewMenu.add(showInfoPageAction);
        viewMenu.add(platformOccupancyAction);
        //viewMenu.add(showMapAction);
        //viewMenu.add(showEditToolsAction);
        fileMenu.add(new Separator());
        viewMenu.add(zoomInAction);
        viewMenu.add(zoomOutAction);
        fileMenu.add(new Separator());
        viewMenu.add(coordBarsAction);
        viewMenu.add(statusBarAction);
        
        // Help
        helpMenu.add(aboutAction);
        helpMenu.add(changeLanguageAction);
        helpMenu.add(showTraceViewAction);
        
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
