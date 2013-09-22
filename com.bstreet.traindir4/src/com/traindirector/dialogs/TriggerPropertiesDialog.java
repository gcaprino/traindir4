package com.traindirector.dialogs;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.traindirector.model.TriggerTrack;

public class TriggerPropertiesDialog extends TitleAreaDialog {

	public static int CLEAR = -1;	// not OK nor CANCEL

	Button okButton;
	Button clearButton;
	Button cancelButton;
	
	Combo	_actionList;
	Text	_action;
	Text	_position;
	Text	_probabilities;
	Button	_hidden;
	TriggerTrack _trigger;
	
	String[] _actions = {
			"click x,y",
			"rclick x,y",
			"ctrlclick x,y",
			"fast",
			"slow",
			"shunt train",
			"reverse train",
			"traininfo train",
			"stationinfo train",
			"accelerate speed train",
			"decelerate speed train",
			"assign train",
			"play sound",
			"itinerary",
			"script",
	};
	
	public TriggerPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Properties");
		// Set the message
		setMessage("Element properties", IMessageProvider.INFORMATION);
//		Image titleImage = null; 
	//	ImageDescriptor descr = Activator.getImageDescriptor("/icons/direction30x30black.png");
		//titleImage = descr.createImage();
		//this.setTitleImage(titleImage);
	}

	public boolean open(TriggerTrack trigger) {
		_trigger = trigger;
		final boolean[] result = new boolean[1];
		result[0] = false;
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = TriggerPropertiesDialog.this.open() != TitleAreaDialog.CANCEL;
			}
		});
    	return result[0];
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		GridData ld;
		
		Label	label = new Label(parent, SWT.NONE);
		label.setText("Trigger at " + _trigger._position.toString());
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.horizontalSpan = 2;
		label.setLayoutData(ld);
		
		label = new Label(parent, SWT.NONE);
		label.setText("Action:   ('@' in action = name of triggering train)");
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.horizontalSpan = 2;
		label.setLayoutData(ld);
		
		_actionList = new Combo(parent, SWT.DROP_DOWN);
		for (String a : _actions) {
			_actionList.add(a);
		}
		_actionList.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = _actionList.getSelectionIndex();
				_action.setText(_actions[idx]);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		_action = new Text(parent, SWT.BORDER);
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.minimumWidth = 200;
		_action.setLayoutData(ld);
		
		label = new Label(parent, SWT.NONE);
		label.setText("Linked to track at coord :");
		
		_position = new Text(parent, SWT.BORDER);
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.minimumWidth = 200;
		_position.setLayoutData(ld);
		
		label = new Label(parent, SWT.NONE);
		label.setText("Probabilities for action :");
		
		_probabilities = new Text(parent, SWT.BORDER);
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.minimumWidth = 200;
		_probabilities.setLayoutData(ld);

		_hidden = new Button(parent, SWT.CHECK);
		_hidden.setText("Hidden");
		ld = new GridData();
		ld.grabExcessHorizontalSpace = true;
		ld.horizontalSpan = 2;
		_hidden.setLayoutData(ld);
		showValues();
		return parent;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;

		parent.setLayoutData(gridData);
		createOkButton(parent, OK, "&Select", true);

		cancelButton = createButton(parent, CANCEL, "Cl&ose", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
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
		okButton = button;
		return button;
	}

	private boolean isValidInput() {
		boolean valid = true;
		if (_action.getText().isEmpty()) 
			valid = false;
		if (_position.getText().isEmpty())
			valid = false;
		okButton.setEnabled(valid);
		return valid;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void showValues() {
		_hidden.setSelection(_trigger._invisible);
		_position.setText(_trigger._position.toString());
		_probabilities.setText(_trigger.speedsToString());
		_action.setText(_trigger._station == null ? "" : _trigger._station);
	}

	// Copy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	private void saveInput() {
		_trigger.readSpeeds(_probabilities.getText());
		_trigger._invisible = _hidden.getSelection();
		_trigger._position.fromString(_position.getText(), 0);
		_trigger._station = _action.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}
}

