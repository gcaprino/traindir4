package com.traindirector.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
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
import com.traindirector.Application;
import com.traindirector.commands.SaveAlertsCommand;
import com.traindirector.events.AlertEvent;
import com.traindirector.model.Alert;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.UIUtils;

public class AlertsView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.bstreet.cg.traindirector.views.AlertsView";

	private Action actionClear;
	private Action actionSave;

	Table	_table;
	static FileDialog saveDialog;

	public AlertsView() {
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		_table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		TableColumn col = new TableColumn(_table, SWT.NONE);
		col.setText("Time");
		col.setWidth(80);
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Message");
		col.setWidth(300);
		_table.setHeaderVisible(true);

		UIUtils.restoreColumnSizes(_table, "Alerts");

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(_table, "com.traindirector.alertsView");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		CGEventDispatcher.getInstance().addListener(new CGEventListener(AlertEvent.class) {
			public void handle(CGEvent e, Object target) {
				if (!(target instanceof Simulator))
					return;
				final Simulator simulator = (Simulator) target; 
				_table.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized(simulator._alerts) {
							refreshTable(simulator._alerts);
						}
					}
				});
			}
		});
	}

	public void refreshTable(List<Alert> alerts) {
		// TODO: only add alerts which are not there, yet
		_table.removeAll();
		for(Alert a : alerts) {
			TableItem item = new TableItem(_table, SWT.NONE);
			item.setText(a.getStrings());
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AlertsView.this.fillContextMenu(manager);
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
		manager.add(actionClear);
		manager.add(new Separator());
		manager.add(actionSave);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionClear);
		manager.add(actionSave);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionClear);
		manager.add(actionSave);
	}

	private void makeActions() {
		actionClear = new Action() {
			public void run() {
				Application.getSimulator().removeAllAlerts();
				_table.removeAll();
			}
		};
		actionClear.setText("Clear");
		actionClear.setToolTipText("Remove all alerts");
		actionClear.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		actionSave = new Action() {
			public void run() {
				saveAlerts();
			}
		};
		actionSave.setText("Save");
		actionSave.setToolTipText("Save alerts to a text file");
		actionSave.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	}

	private void hookDoubleClickAction() {
		/*
		_table.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
		*/
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		_table.setFocus();
	}


	public void saveAlerts() {
		Shell shell = Display.getDefault().getActiveShell();
		if (saveDialog == null) {
			saveDialog = new FileDialog(shell, SWT.SAVE);
			saveDialog.setFilterExtensions(new String[] { "*.txt", "*.*" });
			saveDialog.setFilterNames(new String[] { "Text File (*.txt)", "All files (*.*)" });
		}
		String fname = saveDialog.open();
		if (fname == null) {
			return;
		}
		SaveAlertsCommand cmd = new SaveAlertsCommand(fname);
		Application.getSimulator().addCommand(cmd);
	}
}
