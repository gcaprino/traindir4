package com.traindirector.uicomponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import com.traindirector.commands.LoadCommand;
import com.traindirector.simulator.Simulator;
import com.traindirector.uiactions.OpenSimulationAction;

public class HomeContent extends WebContent {

	public HomeContent() {
		
	}
	
	@Override
	public String getHTML() {
		Simulator sim = Simulator.INSTANCE;
		
		File homePageFile = getResourceFile("/html/home.html");
		File cssFile = getResourceFile("/html/style.css");
		List<String> content = new ArrayList<String>();
		List<String> cssContent = getFileContent(cssFile);
		StringBuilder css = new StringBuilder();
		for (String s : cssContent) {
			css.append(s);
			css.append('\n');
		}
		if (homePageFile != null) {
			content = getFileContent(homePageFile);
            Map<String, String> values = new HashMap<String, String>();
            values.put("$version", sim.getVersion());
            values.put("$css", css.toString());
            StringBuilder sb = new StringBuilder();
            if (sim._oldSimulations.size() == 0) {
            	sb.append("You have not simulated any scenario, yet.");
            } else {
            	sb.append("You have played the following simulations:<br><br>");
	            for (int i = 0; i < sim._oldSimulations.size(); ++i) {
	            	String s = sim._oldSimulations.get(i);
	            	sb.append("<a href=\"/read/");
	            	sb.append(s);
	            	sb.append("\">");
	            	sb.append(s);
	            	sb.append("</a><br>");
	            }
            }
            values.put("$oldsims", sb.toString());
            return replaceContent(content, values);
		}
		return sim.getVersion();
	}
	
	@Override
	public boolean doLink(String location) {
		if (location.equals("tdir:open")) {
			OpenSimulationAction act = new OpenSimulationAction("Open");
			act.run();
			return true;
		} else if(location.startsWith("about:/read/")) {
			OpenSimulationAction.openSimulation(location.substring(12));
			return true;
		}
		return true;
	}
}
