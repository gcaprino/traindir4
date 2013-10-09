package com.traindirector.web.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.editors.ReportContent;
import com.traindirector.model.Track;
import com.traindirector.simulator.Simulator;

public class StationsListContent extends ReportContent {

	public StationsListContent() {
		super("stations");
	}

	public String getHTML() {
        Simulator sim = Simulator.INSTANCE;
        Map<String, String> values = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
    	List<Track> stations = sim._territory.getAllStations();
    	// TODO: sort
    	for (Track station : stations) {
    		sb.append("<tr>");
    		appendColumn(sb, "<a href=\"/station/" + station._station + "\">" + station._station + "</a>");
    		sb.append("</tr>\n");
    	}
    	values.put("$table", sb.toString());
    	return processTemplate(values, "stationsList.html");
	}
}
