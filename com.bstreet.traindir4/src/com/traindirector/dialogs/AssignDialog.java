package com.traindirector.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;

public class AssignDialog extends TitleAreaDialog {

	public static final int ASSIGN = 1;
	public static final int SHUNT = 2;
	public static final int ASSIGN_AND_SHUNT = 3;
	public static final int REVERSE_AND_ASSIGN = 4;
	public static final int SPLIT = 5;
	public static final int PROPERTIES = 6;
	public static final int CLOSE = 7;

	Table _table;
	Button assignButton;
	Button shuntButton;
	Button assignAndShuntButton;
	Button reverseAndAssignButton;
	Button splitButton;
	Button propertiesButton;
	Button cancelButton;
	Train _train;
	Simulator _simulator;
	
	public String _selectedTrain;
	
	public AssignDialog(Shell parentShell, Train train, Simulator simulator) {
		super(parentShell);
		_train = train;
		_simulator = simulator;
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Assign Rolling Stock");
		// Set the message
		setMessage("Select a train from the list", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		// The text fields will grow with the size of the dialog
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;

		
		_table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		_table.setLayoutData(gridData);
		_table.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveInput();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		TableColumn col = new TableColumn(_table, SWT.NONE);
		col.setText("Train");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Departure");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Platform");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Destination");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Arrival");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Notes");
		col.setWidth(140);
		
		_table.setHeaderVisible(true);

		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		((GridLayout) parent.getLayout()).numColumns = 1;
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		//createOkButton(parent, ASSIGN, "&Assign", true);

		assignButton = createButton(parent, ASSIGN, "&Assign", false);
		assignButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(ASSIGN);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;

		shuntButton = createButton(parent, SHUNT, "S&hunt", false);
		shuntButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(SHUNT);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
		
		assignAndShuntButton = createButton(parent, ASSIGN_AND_SHUNT, "Assign&+Shunt", false);
		assignAndShuntButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(ASSIGN_AND_SHUNT);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
		

		reverseAndAssignButton = createButton(parent, REVERSE_AND_ASSIGN, "&Reverse+Assign", false);
		reverseAndAssignButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(REVERSE_AND_ASSIGN);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
		
		splitButton = createButton(parent, SPLIT, "Sp&lit", false);
		splitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(SPLIT);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
		
		propertiesButton = createButton(parent, PROPERTIES, "&Properties", false);
		propertiesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(PROPERTIES);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
		


		cancelButton = createButton(parent, CLOSE, "&Cancel", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CLOSE);
				close();
			}
		});
		((GridLayout) parent.getLayout()).numColumns = 1;
	}

	protected Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		setButtonLayoutData(button);
		assignButton = button;
		return button;
	}

	private boolean isValidInput() {
		boolean valid = true;
		int i = _table.getSelectionCount();
		if (i < 1) {
			setErrorMessage("Select a train from the list");
			valid = false;
		}
		assignButton.setEnabled(valid);
		return valid;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// Copy selected item because the UI gets disposed
	// and the TableItems are not accessible any more.
	private void saveInput() {
		TableItem[] items = _table.getSelection();
		if(items.length > 0)
			_selectedTrain = items[0].getText();
		else
			_selectedTrain = null;
	}

	public void fillTable(List<Train> trains) {
		for (Train train : trains) {
			TableItem item = new TableItem(_table, SWT.NONE);
			item.setText(train._name);
			String tim = TDTime.toString(train._timeOut);
			item.setText(1, tim);
			item.setText(1, train._entrance);
			item.setText(2, train._exit);
			tim = TDTime.toString(train._timeOut);
			item.setText(3, tim);
			item.setText(4, train._name);
			item.setText(5, "" + train._speed);
			item.setText(6, "" + train._minDel);
			item.setText(7, "" + train._minLate);
			item.setText(8, train.getStatusAsString());
			item.setText(9, train.getNotesAsString());
		}
		if (trains.size() > 0) {
			_table.setSelection(0);
			saveInput();
		} else {
			setMessage("There are no trains departing from this station");
			assignButton.setEnabled(false);
			assignAndShuntButton.setEnabled(false);
			reverseAndAssignButton.setEnabled(false);
		}
		if(_train._status != TrainStatus.ARRIVED) {
			assignButton.setEnabled(false);
			assignAndShuntButton.setEnabled(false);
			reverseAndAssignButton.setEnabled(false);
		}
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getSelectedTrain() {
		return _selectedTrain;
	}
}
