package com.traindirector.uicomponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.model.Switchboard;
import com.traindirector.model.Switchboard.SwitchboardCell;
import com.traindirector.simulator.Simulator;

public class SwitchboardContent extends WebContent {

	private Switchboard currentSwitchboard;
	
	public SwitchboardContent() {
		List<Switchboard> swblist = Simulator.INSTANCE._switchboards;
		if(swblist != null && !swblist.isEmpty())
			currentSwitchboard = swblist.get(0);
	}
	
	@Override
	public boolean doLink(String location) {
        if (location.startsWith("sb-edit")) {
            return true;
        } else if(location.startsWith("sb-cell")) {
            return true;
        }
		return false;
	}

	@Override
	public String getHTML() {
		Simulator sim = Simulator.INSTANCE;
		List<Switchboard> swblist = Simulator.INSTANCE._switchboards;
		if(currentSwitchboard == null && swblist != null && !swblist.isEmpty())
			currentSwitchboard = swblist.get(0);
		
		File homePageFile = getResourceFile("/html/en/switchboard.html");
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

//          TODO:  $serverPort
            
            StringBuilder sblist = new StringBuilder();
            for (Switchboard sb : swblist) {
        	    if(sb == currentSwitchboard) {
        			sblist.append("<tr><td bgcolor=\"#c0ffc0\">");
        			sblist.append(sb._name);
        			sblist.append("&nbsp;&nbsp;&nbsp;<a href=\"sb-edit -e ");
        			sblist.append(sb._filename);
        			sblist.append("\">");
        			sblist.append("change");
        			sblist.append("</a></td></tr>\n");
        		} else {
        			sblist.append("<tr><td bgcolor=\"#e0e0e0\">");
        			sblist.append("<a href=\"sb-edit ");
        			sblist.append(sb._filename);
        			sblist.append("\">");
        			sblist.append(sb._name);
        			sblist.append("</a></td></tr>\n");
        		}
            }
            values.put("$swblist", sblist.toString());
            
            StringBuilder cells = new StringBuilder("<tr>\n");
            int i;
            for(i = 0; i < Switchboard.MAX_SWBD_X; ++i) {
            	cells.append("<th width='70'>" + i + "</td>\n");
            }
            cells.append("</tr>\n");
            if (currentSwitchboard == null) {
            	cells.append("<tr><td>No selected switchboard.</td></tr>\n");
            } else {
            	for (int y = 0; y < Switchboard.MAX_SWBD_Y; ++y) {
            		cells.append("<tr><td width='40'>" + y + "</td>\n");
            		for (i = 0; i < Switchboard.MAX_SWBD_X; ++i) {
            			SwitchboardCell cell = currentSwitchboard.find(i, y);
            			cells.append("<td width='70' align='center' valign='top'><a href='tdir:sb-cell " + i + "," + y + "'>");
            			if(cell == null) {
            				cells.append("?</a></td>\n");
            			} else {
            				cells.append(cell.getText() + "</a></td>\n");
            			}
            		}
            	}
            }
            values.put("swbcells", cells.toString());
            return replaceContent(content, values);
		}
		return sim.getVersion();
	}
}
