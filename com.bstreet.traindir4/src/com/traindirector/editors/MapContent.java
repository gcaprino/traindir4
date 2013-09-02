package com.traindirector.editors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.Application;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.WebContent;

public class MapContent extends WebContent {
	String _name;
	String _html = "";

    public void setName(String type) {
        _name = type;
    }
    
    @Override
    public String getHTML() {
    	return _html;
    }

    @Override
    public boolean doLink(String location) {
    	Simulator sim = Application.getSimulator();
    	if(location.startsWith("about:"))
    		location = location.substring(6);
    	if(location.startsWith("showinfo "))
    		location = location.substring(9);
    	if(location.equals("blank"))
    		return false;
//    	if(sim._fileManager == null)
//    		return false;
    	/*
    	BufferedReader rdr = sim._fileManager.getReaderForFile(location);
    	if (rdr == null) {
    		_html = "<html><body>This scenario has no additional information.</body></html>\n";
    		return false;
    	}
    	StringBuilder sb = new StringBuilder();
    	String line;
    	try {
			while((line = rdr.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				rdr.close();
			} catch (IOException e) {
			}
		}
    	_html = sb.toString();
    	sb = new StringBuilder();
    	int indx = 0;
    	while((indx = _html.indexOf("img src=\"", indx)) > 0) {
    		indx += 9;
    		if (!_html.substring(indx).startsWith("http"))
    			_html = _html.substring(0, indx) + sim._baseDir + File.separator + _html.substring(indx);
    	}
    	*/
    	String center = "-34.397, 150.644";
    	String zoom = "8";
    	File mapFile = getResourceFile("/html/en/map1.html");
    	List<String> lines = getFileContent(mapFile);
    	Map<String, String> values = new HashMap<String, String>();
    	
    	values.put("$center", center);
    	values.put("$zoom", zoom);
    	_html = replaceContent(lines, values);
    	return true;
    }

}
