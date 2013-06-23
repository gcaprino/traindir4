package com.traindirector.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.traindirector.Activator;
import com.traindirector.model.Itinerary;
import com.traindirector.uicomponents.ItinerariesTable;

public class ItineraryDialog extends BaseDialog {

	public static int CLEAR = -1;	// not OK nor CANCEL

	ItinerariesTable _table;
	Button clearButton;
	Button cancelButton;
	
	public String _selectedItinerary;
	
	public ItineraryDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Itinerary Selection");
		// Set the message
		setMessage("Select an Itinerary from the list", IMessageProvider.INFORMATION);
		Image titleImage = null; 
		ImageDescriptor descr = Activator.getImageDescriptor("/icons/direction30x30black.png");
		titleImage = descr.createImage();
		this.setTitleImage(titleImage);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		_table = new ItinerariesTable();
		_table.create(parent);
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

		clearButton = createButton(parent, CLEAR, "&Clear", false);
		clearButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveInput();
				setReturnCode(CLEAR);
				close();
			}
		});
		
		cancelButton = createButton(parent, CANCEL, "Cl&ose", false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	@Override
	protected boolean isValidInput() {
		boolean valid = true;
		int i = _table.getSelectionCount();
		if (i < 1) {
			setErrorMessage("Select one itinerary from the list");
			valid = false;
		}
		okButton.setEnabled(valid);
		clearButton.setEnabled(valid);
		return valid;
	}

	// Coyy textFields because the UI gets disposed
	// and the Text Fields are not accessible any more.
	@Override
	protected void saveInput() {
		TableItem[] items = _table.getSelection();
		if(items.length > 0)
			_selectedItinerary = (String) items[0].getText();
	}

	public void fillTable(List<Itinerary> itineraries) {
		_table.fill(itineraries);
		if (itineraries.size() == 0) {
			setErrorMessage("There are no itineraries defined in this layout");
			okButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}


}
