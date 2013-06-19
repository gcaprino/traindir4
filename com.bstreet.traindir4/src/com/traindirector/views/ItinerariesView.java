package com.traindirector.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.traindirector.dialogs.ItineraryProperties;
import com.traindirector.model.Itinerary;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.ItinerariesTable;

public class ItinerariesView extends ViewPart {
    public static String ID = "com.traindirector.views.itineraries";

    private ItinerariesTable _table;
	private IAction deleteItineraryAction;
	private IAction propertiesAction;
	private IAction saveAction;

	private Action doubleClickAction;

	public ItinerariesView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
        _table = new ItinerariesTable();
        _table.create(parent);
        _table.fill(Simulator.INSTANCE._territory._itineraries);
        
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		//hookDoubleClickAction();
		
        /*
        _table._table.addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		*/
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(deleteItineraryAction);
		manager.add(propertiesAction);
		manager.add(saveAction);
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ItinerariesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(_table._table);
		_table._table.setMenu(menu);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(deleteItineraryAction);
		manager.add(propertiesAction);
		manager.add(saveAction);
		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public void setFocus() {
		_table._table.setFocus();
	}
	
	private void askDelete() {
		Shell shell = Display.getDefault().getActiveShell();
		TableItem[] items = _table.getSelection();
		if (items.length < 1) {
			MessageDialog.openWarning(shell, "Warning", "No itinerary was selected in the table.");
			return;
		}
		if(!MessageDialog.openConfirm(shell, "Delete", "Do you really want to delete the selected itinerary?")) {
			return;
		}
		for(TableItem item : items) {
			Itinerary itin = (Itinerary) item.getData("itin");
			if (itin == null) {
				continue;
			}
			Simulator.INSTANCE._territory._itineraries.remove(itin);
		}
		_table.fill(Simulator.INSTANCE._territory._itineraries);
	}
	
	private void showProperties() {
		Shell shell = Display.getDefault().getActiveShell();
		TableItem[] items = _table.getSelection();
		if (items.length < 1) {
			MessageDialog.openWarning(shell, "Warning", "No itinerary was selected in the table.");
			return;
		}
		Itinerary itin = (Itinerary) items[0].getData("itin");
		if (itin == null)
			return;
		ItineraryProperties props = new ItineraryProperties(itin);
		props.open();
	}
	
	private void doSave() {
		
	}

	private void makeActions() {
		deleteItineraryAction = new Action("Delete", Action.AS_PUSH_BUTTON) {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						askDelete();
					}
				});
			}
		};
		deleteItineraryAction.setToolTipText("Delete the selected itinerary");
		deleteItineraryAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		propertiesAction = new Action("Properties", Action.AS_PUSH_BUTTON) {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						showProperties();
					}
				});
			}
		};
		propertiesAction.setToolTipText("Show the properties of the selected itinerary");
		propertiesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		saveAction = new Action("Save", Action.AS_PUSH_BUTTON) {
			public void run() {
			}
		};
		saveAction.setToolTipText("Save the selected itinerary");
		saveAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		doubleClickAction = new Action() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						showProperties();
					}
				});
			}
		};

	}
}
