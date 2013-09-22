package com.traindirector.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.traindirector.Application;
import com.traindirector.options.Option;
import com.traindirector.options.OptionsManager;

public class OptionsPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	List<Option> _options;
	Map<Option, Widget> _map;

	public OptionsPage() {
		setDescription("Change various simulation options");
		_map = new HashMap<Option, Widget>();
		_options = new ArrayList<Option>();
		OptionsManager opt = Application.getSimulator()._options;
		_options.add(opt._terseStatus);
		_options.add(opt._beepOnAlert);
		_options.add(opt._beepOnEnter);
		_options.add(opt._showSpeeds);
		_options.add(opt._autoLink);
		_options.add(opt._linkToLeft);
		_options.add(opt._showGrid);
		_options.add(opt._showBlocks);
		_options.add(opt._showSeconds);
		_options.add(opt._traditionalSignals);
		_options.add(opt._hardCounters);
		_options.add(opt._showLinks);
		_options.add(opt._showScripts);
		_options.add(opt._showIcons);
		_options.add(opt._showTooltip);
		_options.add(opt._randomDelays);
		_options.add(opt._playSynchronously);
		//_options.add(opt._swapHeadTail);
		_options.add(opt._drawTrainNames);
		_options.add(opt._noTrainNamesColors);
	}

	@Override
	public boolean performOk() {
		Application.getSimulator()._options.setOptions(_map);
		return true;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Control createContents(Composite parent) {
		_map = Application.getSimulator()._options.createOptionsWidgets(parent, _options);
		return parent;
	}

}
