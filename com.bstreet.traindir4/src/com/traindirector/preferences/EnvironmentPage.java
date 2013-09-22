package com.traindirector.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.traindirector.Application;
import com.traindirector.options.Option;
import com.traindirector.options.OptionsManager;

public class EnvironmentPage extends PreferencePage implements
IWorkbenchPreferencePage {

	List<Option> _options;
	Map<Option, Widget> _map;
	
	@Override
	public void init(IWorkbench workbench) {
		_options = new ArrayList<Option>();
		_map = new HashMap<Option, Widget>();
		OptionsManager opt = Application.getSimulator()._options;
		_options.add(opt._alertSoundPath);
		_options.add(opt._enterSoundPath);
		_options.add(opt._scriptsPaths);
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
