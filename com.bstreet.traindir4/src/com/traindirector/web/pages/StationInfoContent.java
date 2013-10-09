package com.traindirector.web.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.editors.ReportContent;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;

public class StationInfoContent extends ReportContent {

	String _stationName;

	public StationInfoContent() {
		super("station");
	}
	
	public boolean doLink(String cmd) {
		cmd = getCmd(cmd);
		_stationName = cmd;
		return true;
	}
	
	public String getHTML() {
        Simulator sim = Simulator.INSTANCE;
        Map<String, String> values = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        Track station = sim._territory.findStation(_stationName);
        if (station == null) {
        	values.put("$trains", "No such station: " + _stationName);
        } else if(sim._schedule._trains.isEmpty()) {
        	values.put("$trains", "This scenario has no trains");
        } else {
        	List<Train> trains = new ArrayList<Train>();
            for(Train train : sim._schedule._trains) {
            	if(train._entrance.equals(station._station))
            		trains.add(train);
            	else if(train._exit.equals(station._station))
            		trains.add(train);
            	else if(train.stopsAt(station))
            		trains.add(train);
            	else
            		continue;
            }
            // TODO: sort by arrival time
            for(Train train : trains) {
            	sb.append("<tr>");
            	if(train._entrance.equals(station._station)) {
                    appendColumn(sb, "<a href=\"/traininfo/" + train._name + "\">" + train._name + "</a>");
                    appendColumn(sb, "&nbsp;");
                    appendColumn(sb, "&nbsp;");
                    appendColumn(sb, getStationLink(train._entrance));
                    appendColumn(sb, TDTime.toString(train._timeIn));
            	} else if(train._exit.equals(station._station)) {
                    appendColumn(sb, "<a href=\"/traininfo/" + train._name + "\">" + train._name + "</a>");
                    appendColumn(sb, getStationLink(train._exit));
                    appendColumn(sb, TDTime.toString(train._timeOut));
                    appendColumn(sb, "&nbsp;");
                    appendColumn(sb, "&nbsp;");
            	} else {
            		for(TrainStop stop : train._stops){ 
            			if (stop._station.equals(station._station)) {
            				appendColumn(sb, "<a href=\"/traininfo/" + train._name + "\">" + train._name + "</a>");
            				appendColumn(sb, getStationLink(train._entrance));
            				appendColumn(sb, TDTime.toString(stop._arrival));
            				appendColumn(sb, getStationLink(train._exit));
            				appendColumn(sb, TDTime.toString(stop._departure));
            				break;
            			}
            		}
            	}
                sb.append("</tr>\n");
            }
        }
        values.put("$trains", sb.toString());
        return processTemplate(values, "stationInfo.html");
	}

}
