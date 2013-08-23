package com.traindirector.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.traindirector.Application;
import com.traindirector.options.Option;
import com.traindirector.options.OptionsManager;

public class ServerPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public ServerPage() {
		// TODO Auto-generated constructor stub
	}

	public ServerPage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public ServerPage(String title, ImageDescriptor image) {
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
	}

	@Override
	protected Control createContents(Composite parent) {
		_map = Application.getSimulator()._options.createOptionsWidgets(parent, _options);
		return parent;
	}
	
	@Override
	public boolean performOk() {
		Application.getSimulator()._options.setOptions(_map);
		return true;
	}

}
