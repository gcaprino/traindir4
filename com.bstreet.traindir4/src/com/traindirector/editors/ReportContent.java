package com.traindirector.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.WebContent;
import com.traindirector.web.pages.PerformanceContent;
import com.traindirector.web.pages.StationInfoContent;
import com.traindirector.web.pages.StationsListContent;
import com.traindirector.web.pages.TrainInfoContent;

public class ReportContent extends WebContent {

    String _type;
    
    public ReportContent(String endpoint) {
    	super(endpoint);
    }

    public void setType(String type) {
        _type = type;
    }
    
    @Override
    public String getHTML() {
        if (_type != null && _type.startsWith("/traininfo/")) {
        	TrainInfoContent c = new TrainInfoContent();
        	c.doLink(_type.substring(11));
        	return c.getHTML();
        } else if(_type != null && _type.startsWith("/stations/")) {
        	StationsListContent c = new StationsListContent();
        	return c.getHTML();
        } else if(_type != null && _type.startsWith("/station/")) {
        	StationInfoContent c = new StationInfoContent();
        	c.doLink(_type.substring(9));
        	return c.getHTML();
        }
        // default is performance page
    	PerformanceContent c = new PerformanceContent();
    	return c.getHTML();
    }
    
    protected String processTemplate(Map<String, String> values, String pageName) {
		File homePageFile = getResourceFile("/html/en/" + pageName); // TODO: use current language
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
            values.put("$css", css.toString());
        }
        return replaceContent(content, values);
    }
    
    protected void appendColumn(StringBuilder sb, String string) {
        sb.append("<td>" + string + "</td>\n");
    }

    @Override
    public boolean doLink(String location) {
    	String cmd = location;
    	if (cmd.startsWith("about:"))
    		cmd = cmd.substring(6);
        if (cmd.startsWith("/traininfo/")) {
        	_type = cmd;
            return true;
        } else if(cmd.startsWith("/station/")) {
        	_type = cmd;
            return true;
        } else if(cmd.startsWith("/stations/")) {
        	_type = cmd;
        	return true;
        }
        return false;
    }

    public String getTrainLink(Train train) {
    	return "<a href=\"/traininfo/" + train._name + "\">" + train._name + "</a>";
    }

    public String getStationLink(String station) {
    	Territory territory = Simulator.INSTANCE._territory;
    	Track track = territory.findStationNamed(station);
    	if(track == null) {
    		return station;
    	}
    	return "<a href=\"/station/" + station + "\">" + station + "</a>";
    }
}
