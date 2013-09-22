package com.traindirector.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;
import com.traindirector.uicomponents.WebContent;

public class ReportContent extends WebContent {

    String _type;
    
    public ReportContent() {
    }

    public void setType(String type) {
        _type = type;
    }
    
    @Override
    public String getHTML() {
        String pageName = "performance.html";
        Simulator sim = Simulator.INSTANCE;
        Map<String, String> values = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
        
        if (_type != null && _type.startsWith("/traininfo/")) {
            pageName = "trainInfo.html";
            Train train = sim._schedule.findTrainNamed(_type.substring(11));
            if (train == null) {
                
            } else {
                for (TrainStop stop : train._stops) {
                    sb.append("<tr>");
                    appendColumn(sb, "<a href=\"/station/" + stop._station + "\">" + stop._station + "</a>");
                    appendColumn(sb, TDTime.toString(stop._arrival));
                    appendColumn(sb, TDTime.toString(stop._departure));
                    appendColumn(sb, "" + stop._minstop);
                    sb.append("</tr>\n");
                }
                sb.append("</table>\n");
                if (train._days != 0) {
                    sb.append("Runs on: ");
                    for(int i = 0; i < 7; ++i)
                        if ((train._days & (1 << i)) != 0)
                            sb.append("" + (i + 1));
                    sb.append("<br>\n");
                }
                if (train._notes != null && !train._notes.isEmpty()) {
                    sb.append("Notes:<br>\n");
                    for (String s : train._notes) {
                        sb.append(s);
                        sb.append("<br>");
                    }
                }
            }
            values.put("$table", sb.toString());
        } else if(_type != null && _type.startsWith("/stations/")) {
        	List<Track> stations = sim._territory.getAllStations();
        	// TODO: sort
        	for (Track station : stations) {
        		sb.append("<tr>");
        		appendColumn(sb, "<a href=\"/station/" + station._station + "\">" + station._station + "</a>");
        		sb.append("</tr>\n");
        	}
        	pageName = "stationsList.html";
        	values.put("$table", sb.toString());
        } else if(_type != null && _type.startsWith("/station/")) {
            Track station = sim._territory.findStation(_type.substring(9));
            if (station == null) {
            	values.put("$trains", "No such station: " + _type.substring(9));
            } else if(sim._schedule._trains.isEmpty()) {
            	values.put("$trains", "This scenario has no trains");
            } else {
            	List<Train> trains = new ArrayList<Train>();
            	int arrTime;
            	int depTime;
            	String fromStation, toStation;
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
            pageName = "stationInfo.html";
            values.put("$trains", sb.toString());
        } else if (_type == null || _type.startsWith("/performance/")) { // default is performance page
            if (sim._schedule._trains.isEmpty()) {
                sb.append("You have not simulated any scenario, yet, or this scenario has no trains scheduled.");
            } else {
                for (Train train : sim._schedule._trains) {
                    sb.append("<tr>");
                    appendColumn(sb, "<a href=\"/traininfo/" + train._name + "\">" + train._name + "</a>");
                    appendColumn(sb, getStationLink(train._entrance));
                    appendColumn(sb, TDTime.toString(train._timeIn));
                    appendColumn(sb, getStationLink(train._exit));
                    appendColumn(sb, TDTime.toString(train._timeOut));
                    appendColumn(sb, "" + train._minDel);
                    appendColumn(sb, "" + train._minLate);
                    StringBuilder col = new StringBuilder(train._status.toString());
                    for (TrainStop stop : train._stops) {
                        if (stop._delay == 0)
                            continue;
                        String line = "<br>" + ((stop._delay > 0) ? "+" : " ") + stop._delay + " : " + stop._station;
                        col.append(line);
                    }
                    sb.append("<td>" + col + "</td>");
                    sb.append("</tr>\n");
                }
            }
            values.put("$trains", sb.toString());
        }
        File homePageFile = getResourceFile("/html/en/" + pageName);
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
    
    private void appendColumn(StringBuilder sb, String string) {
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

    public String getStationLink(String station) {
    	Territory territory = Simulator.INSTANCE._territory;
    	Track track = territory.findStationNamed(station);
    	if(track == null) {
    		return station;
    	}
    	return "<a href=\"/station/" + station + "\">" + station + "</a>";
    }
}
