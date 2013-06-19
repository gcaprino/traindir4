package com.traindirector.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.commands.ShowTrainStopsCommand;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.model.Schedule;
import com.traindirector.model.Train;
import com.traindirector.simulator.ColorFactory;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class ScheduleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.bstreet.cg.traindirector.views.ScheduleView";

	private Table _table;
	private Action showCancelledAction;
	private Action showArrivedAction;
	private Action doubleClickAction;

	public Color readyColorFg;
	public Color readyColorBg;
	public Color runningColorFg;
	public Color runningColorBg;
	public Color stoppedColorFg;
	public Color stoppedColorBg;
	public Color waitingColorFg;
	public Color waitingColorBg;
	public Color delayedColorFg;
	public Color delayedColorBg;
	public Color derailedColorFg;
	public Color derailedColorBg;
	public Color arrivedColorFg;
	public Color arrivedColorBg;
	public Color cancelledColorFg;
	public Color cancelledColorBg;
	
	public Simulator _simulator;
	public ColorFactory _colorFactory;

	public boolean _showCancelled = true;
	public boolean _showArrived = true;

	/**
	 * The constructor.
	 */
	public ScheduleView() {
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
		col.setText("Entry");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("From");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("To");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Exit");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Train");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Speed");
		col.setWidth(50);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Min.Del.");
		col.setWidth(60);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Min.Late");
		col.setWidth(60);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Status");
		col.setWidth(200);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Notes");
		col.setWidth(200);
		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(_table, "com.bstreet.traindirector.scheduleView");
		_colorFactory = Simulator.INSTANCE._colorFactory;
		
		getColors();

		makeActions();
		hookContextMenu();
		contributeToActionBars();
		hookDoubleClickAction();
		CGEventDispatcher.getInstance().addListener(new CGEventListener(LoadEndEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator) {
					_simulator = (Simulator)target;
					_table.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							fillScheduleTable();
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
							updateScheduleTable();
						}
					});
				}
			}
		});
	}

	private void fillScheduleTable() {
		if(_simulator == null)
			return;
		synchronized(_simulator._schedule) {
			Schedule schedule = _simulator._schedule;
			// TODO: get currently selected item, to keep it visible after the table has been refilled
			_table.removeAll();
			for (Train train : schedule._trains) {
				if (!_showCancelled && train.isCancelled())
					continue;
				if (!_showArrived && train.isArrived())
					continue;
				TableItem item = new TableItem(_table, SWT.NONE);
				item.setData("train", train);
				String tim = TDTime.toString(train._timeIn);
				item.setText(tim);
				item.setText(1, train._entrance != null ? train._entrance : "?");
				item.setText(2, train._exit != null ? train._exit : "?");
				tim = TDTime.toString(train._timeOut);
				item.setText(3, tim);
				item.setText(4, train._name);
				item.setText(5, "" + train._speed);
				item.setText(6, "" + train._minDel);
				item.setText(7, "" + train._minLate);
				item.setText(8, train.getStatusAsString());
				item.setText(9, train.getNotesAsString());
				setItemColors(item, train);
			}
		}
	}

	private void updateScheduleTable() {
		if(_simulator == null)
			return;
		synchronized(_simulator._schedule) {
			int nRows = _table.getItemCount();
			for (int i = 0; i < nRows; ++i) {
				TableItem item = _table.getItem(i);
				Train train = (Train) item.getData("train");
				if (train == null)
					continue;
				if (!_showArrived && train.isArrived()) {
					_table.remove(i);
					--i;
					continue;
				}
				item.setText(5, "" + train._speed);
				item.setText(6, "" + train._minDel);
				item.setText(7, "" + train._minLate);
				item.setText(8, train.getStatusAsString());
				item.setText(9, train.getRunInfo());
				setItemColors(item, train);
			}
		}
	}

	private void setItemColors(TableItem item, Train train) {
		Color fg = null;
		Color bg = null;
		switch (train._status){ 
		case WAITING:
			fg = waitingColorFg;
			bg = waitingColorBg;
			break;
		case DELAYED:
			fg = delayedColorFg;
			bg = delayedColorBg;
			break;
		case DERAILED:
			fg = derailedColorFg;
			bg = derailedColorBg;
			break;
		case ARRIVED:
			fg = arrivedColorFg;
			bg = arrivedColorBg;
			break;
		case STOPPED:
			fg = stoppedColorFg;
			bg = stoppedColorBg;
			break;
		case CANCELLED:
			fg = cancelledColorFg;
			bg = cancelledColorBg;
			break;
		case READY:
			fg = readyColorFg;
			bg = readyColorBg;
			break;
		case RUNNING:
			fg = runningColorFg;
			bg = runningColorBg;
			break;
		default:
			fg = derailedColorFg;
			bg = derailedColorBg;
		}
		if(!item.getBackground().equals(bg))
			item.setBackground(bg);
		if(!item.getForeground().equals(fg))
			item.setForeground(fg);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ScheduleView.this.fillContextMenu(manager);
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
		manager.add(showCancelledAction);
		manager.add(new Separator());
		manager.add(showArrivedAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(showCancelledAction);
		manager.add(showArrivedAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(showCancelledAction);
		manager.add(showArrivedAction);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void makeActions() {
		showCancelledAction = new Action("Show Cancelled", Action.AS_CHECK_BOX) {
			public void run() {
				// Show Arrived
				_showCancelled = showCancelledAction.isChecked();
				fillScheduleTable();
			}
		};
		showCancelledAction.setToolTipText("Show or hide cancelled trains");
		showCancelledAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		showCancelledAction.setChecked(_showCancelled);
		
		showArrivedAction = new Action("Show Arrived", Action.AS_CHECK_BOX) {
			public void run() {
				// Show Arrived
				_showArrived = showArrivedAction.isChecked();
				fillScheduleTable();
			}
		};
		showArrivedAction.setToolTipText("Show or hide arrived trains");
		showArrivedAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		showArrivedAction.setChecked(_showArrived);

		// TODO: segui primo treno attivo
		// TODO: segui ultimo treno attivo
		// TODO: assign
		// TODO: train properties
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
				TableItem[] items = _table.getSelection();
				if (items.length < 1)
					return;
				Train train = (Train) items[0].getData("train");
				if (train == null)
					return;
				ShowTrainStopsCommand cmd = new ShowTrainStopsCommand(train._name);
				_simulator.addCommand(cmd);
			}
		});
	}

	protected Color getColor(String pref, int r, int g, int b) {
		Color col = _colorFactory.get(pref);
		if (col == null)
			col = _colorFactory.get(r, g, b);
		return col;
	}

	protected void getColors() {
		readyColorFg = getColor("schedule.colors.ready.fg", 0, 0, 255);
		readyColorBg = getColor("schedule.colors.ready.bg", 255, 255, 255);
		runningColorFg = getColor("schedule.colors.running.fg", 0, 0, 0);
		runningColorBg = getColor("schedule.colors.running.bg", 255, 255, 255);
		stoppedColorFg = getColor("schedule.colors.stopped.fg", 0, 0, 0);
		stoppedColorBg = getColor("schedule.colors.stopped.bg", 255, 255, 255);
		waitingColorFg = getColor("schedule.colors.waiting.fg", 0, 0, 0);
		waitingColorBg = getColor("schedule.colors.waiting.bg", 255, 192, 0);
		delayedColorFg = getColor("schedule.colors.delayed.fg", 0, 0, 0);
		delayedColorBg = getColor("schedule.colors.delayed.bg", 255, 255, 0);
		arrivedColorFg = getColor("schedule.colors.arrived.fg", 0, 192, 0);
		arrivedColorBg = getColor("schedule.colors.arrived.bg", 255, 255, 255);
		derailedColorFg = getColor("schedule.colors.derailed.fg", 255, 0, 0);
		derailedColorBg = getColor("schedule.colors.derailed.bg", 255, 240, 240);
		cancelledColorFg = getColor("schedule.colors.cancelled.fg", 192, 192, 192);
		cancelledColorBg = getColor("schedule.colors.cancelled.bg", 255, 255, 255);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
}