package com.traindirector.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import com.traindirector.Activator;
import com.traindirector.simulator.TDTime;

public class DaysDialog extends BaseDialog {

	public DaysDialog(Shell parentShell) {
		super(parentShell);
	}

	private Button[] dayButton = new Button[7];
	private int _selectedDay;

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("Day Selection");
		// Set the message
		setMessage("Not all trains run every day of the week.\nWhich day do you want to simulate.", IMessageProvider.WARNING);
		Image titleImage = null; 
		ImageDescriptor descr = Activator.getImageDescriptor("/icons/schedule16x16black.png");
		titleImage = descr.createImage();
		this.setTitleImage(titleImage);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		SelectionListener listener = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// radio group behavior
				for(int x = 0; x < TDTime.days.length; ++x) {
					if(e.widget == dayButton[x])
						_selectedDay = x;
					dayButton[x].setSelection(e.widget == dayButton[x]);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};

		for (int x = 0; x < TDTime.days.length; ++x) {
			dayButton[x] = new Button(parent, SWT.RADIO);
			dayButton[x].setText(TDTime.days[x]);
			dayButton[x].addSelectionListener(listener);
		}
		_selectedDay = 0;
		dayButton[0].setSelection(true);
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
	}
	
	public int getSelectedDay() {
		return _selectedDay;
	}
}
