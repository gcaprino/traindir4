package com.traindirector.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.ResetEvent;
import com.traindirector.events.ShowTrainStopsEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;
import com.traindirector.uicomponents.UIUtils;

public class TrainStopsView extends ViewPart {

	public static final String ID = "com.traindirector.trainstops";

	private Table _table;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Train _train;
	public Simulator _simulator;

	public TrainStopsView() {
		super();
		_train = null;
	}


	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_table = new Table(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		_table.setHeaderVisible(true);
		_table.setLinesVisible(true);
		TableColumn col = new TableColumn(_table,  SWT.NONE);
		col.setText("Station");
		col.setWidth(180);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Arrival");
		col.setWidth(70);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Departure");
		col.setWidth(70);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Platform");
		col.setWidth(30);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Min. Late");
		col.setWidth(30);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(_table, "com.bstreet.traindirector.trainStopsView");
		
		UIUtils.restoreColumnSizes(_table, "TrainStops");

//		makeActions();
		hookContextMenu();
//		contributeToActionBars();
		hookDoubleClickAction();
		CGEventDispatcher.getInstance().addListener(new CGEventListener(LoadEndEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator) {
					_simulator = (Simulator)target;
					if(_table == null || _table.isDisposed())
						return;
					_table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							_table.removeAll();
							if (_train != null) { // we were showing a train before reloading the scenario
								Train newTrain = _simulator._schedule.findTrainNamed(_train._name);
								if (newTrain != null) {
									// update the data for the same train
									_train = newTrain;
									updateScheduleTable(_train);
								}
							}
						}
					});
				}
			}
		});
		CGEventDispatcher.getInstance().addListener(new CGEventListener(TimeSliceEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator) {
					_simulator = (Simulator)target;
					if(_table == null || _table.isDisposed())
						return;
					_table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (_train != null)
								updateScheduleTable(_train);
						}
					});
				}
			}
		});
		CGEventDispatcher.getInstance().addListener(new CGEventListener(ShowTrainStopsEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator && event instanceof ShowTrainStopsEvent) {
					_simulator = (Simulator)target;
					final ShowTrainStopsEvent tsevent = (ShowTrainStopsEvent) event;
					if(_table == null || _table.isDisposed())
						return;
					_table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							updateScheduleTable(tsevent._train);
						}
					});
				}
			}
		});
		CGEventDispatcher.getInstance().addListener(new CGEventListener(ResetEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator) {
					_simulator = (Simulator)target;
					if(_table == null || _table.isDisposed())
						return;
					_table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							_table.removeAll();
						}
					});
				}
			}
		});
	}
	
	// station | arrival | departure | platform | min. late

	private void updateScheduleTable(Train train) {
		if(_simulator == null)
			return;
		int row = 0;
		TableItem item;
		Color localColor = _simulator._colorFactory.get("trainstops.local");
		if (localColor == null)
			localColor = _simulator._colorFactory.get(0, 0, 255);
		Color nonLocalColor = _simulator._colorFactory.get("trainstops.nonlocal");
		if (nonLocalColor == null)
			nonLocalColor = _simulator._colorFactory.get(128, 128, 128);
		Color lateColor = _simulator._colorFactory.get("trainstops.late");
		if (lateColor == null)
			lateColor = _simulator._colorFactory.get(255, 0, 0);
		for (TrainStop stop : train._stops) {
			String stationName = stop._station;
			int index = stationName.indexOf('@');
			if (index >= 0)
				stationName = stationName.substring(0, index);
			boolean localStation = _simulator._territory.findStation(stationName) != null;
			String arrivalString = TDTime.toString(stop._arrival);
			String departureString = TDTime.toString(stop._departure);
			if (stop._arrival == 0)
				arrivalString = "";
			if (stop._departure == 0)
				departureString = "";
			if (row < _table.getItemCount())
				item = _table.getItem(row);
			else
				item = new TableItem(_table, SWT.NONE);
			if (!item.getText(0).equals(stationName))
				item.setText(stationName);
			if (!item.getText(1).equals(arrivalString))
				item.setText(1, arrivalString);
			if (!item.getText(2).equals(departureString))
				item.setText(2, departureString);
			if (index >= 0 && !item.getText(3).equals(stop._station.substring(index + 1)))
				item.setText(3, stop._station.substring(index + 1));
			String lateString = String.format("%d", stop._delay);
			if (!item.getText(4).equals(lateString))
				item.setText(4, lateString);
			item.setForeground(localStation ? (stop._delay > 0 ? lateColor : localColor) : nonLocalColor);
			++row;
		}
		while(row < _table.getItemCount())
			_table.remove(row);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TrainStopsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(_table);
		_table.setMenu(menu);
//		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
//				ISelection selection = viewer.getSelection();
//				Object obj = ((IStructuredSelection)selection).getFirstElement();
//				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		_table.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// single click
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// double click
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			_table.getShell(),
			"Schedule",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}


}
