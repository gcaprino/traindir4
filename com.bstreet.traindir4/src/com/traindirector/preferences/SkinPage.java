package com.traindirector.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.traindirector.Application;
import com.traindirector.dialogs.ColorOption;
import com.traindirector.options.Option;
import com.traindirector.options.OptionsManager;

public class SkinPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public SkinPage() {
		// TODO Auto-generated constructor stub
	}

	public SkinPage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public SkinPage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	List<Option> _options;
	Map<Option, Widget> _map;
	
	@Override
	public void init(IWorkbench workbench) {
		_options = new ArrayList<Option>();
		_map = new HashMap<Option, Widget>();
		OptionsManager opt = Application.getSimulator()._options;
		_options.add(opt._readyColorFg);
		_options.add(opt._readyColorBg);
		_options.add(opt._stoppedColorFg);
		_options.add(opt._stoppedColorBg);
		_options.add(opt._runningColorFg);
		_options.add(opt._runningColorBg);
		_options.add(opt._waitingColorFg);
		_options.add(opt._waitingColorBg);
		_options.add(opt._delayedColorFg);
		_options.add(opt._delayedColorBg);
		_options.add(opt._arrivedColorFg);
		_options.add(opt._arrivedColorBg);
		_options.add(opt._derailedColorFg);
		_options.add(opt._derailedColorBg);
		_options.add(opt._cancelledColorFg);
		_options.add(opt._cancelledColorBg);
		
		_options.add(opt._background);
		_options.add(opt._freeTrackColor);
		_options.add(opt._reservedTrackColor);
		_options.add(opt._shuntingTrackColor);
		_options.add(opt._occupiedTrackColor);
		_options.add(opt._workingTrackColor);
		_options.add(opt._textTrackColor);
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		// layout.horizontalAlignment = GridData.FILL;
		parent.setLayout(layout);

		_map = Application.getSimulator()._options.createOptionsWidgets(parent, _options);
		return parent;
	}
	
	@Override
	public boolean performOk() {
		Application.getSimulator()._options.setOptions(_map);
		return true;
	}

}
