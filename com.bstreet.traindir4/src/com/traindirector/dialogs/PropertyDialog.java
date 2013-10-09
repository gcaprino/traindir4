package com.traindirector.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.traindirector.Application;
import com.traindirector.options.Option;

public class PropertyDialog extends TitleAreaDialog {

	public static int CLEAR = -1;	// not OK nor CANCEL

	Button okButton;
	Button clearButton;
	Button cancelButton;
	
	List<Option> _fields;
	Map<Option, Widget> _widgets;
	
	public PropertyDialog(Shell parentShell, List<Option> fields) {
		super(parentShell);
		_fields = fields;
		_widgets = new HashMap<Option, Widget>();
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

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		_widgets = Application.getSimulator()._options.createOptionsWidgets(parent, _fields);
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
		okButton.setEnabled(valid);
		return valid;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	// Copy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	private void saveInput() {
		for(Option o : _fields) {
			Widget w = _widgets.get(o);
			if (w == null)	// impossible
				continue;
			if (w instanceof Text) {
				Text txt = (Text) w;
				o._value = txt.getText();
			} else if (w instanceof Button) {
				Button b = (Button) w;
				o._intValue = b.getSelection() ? 1 : 0;
			}
		}
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public boolean openInDisplayThread() {
		final int[] result = new int[1];
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = open();
			}
		});
		return result[0] != CANCEL;
	}
}
