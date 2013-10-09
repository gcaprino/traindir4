package com.traindirector.web.pages;

import java.util.HashMap;
import java.util.Map;

import com.traindirector.editors.ReportContent;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;

public class TrainInfoContent extends ReportContent {

	public String _trainName;
	
	public TrainInfoContent() {
		super("traininfo");
	}

	public boolean doLink(String cmd) {
		cmd = getCmd(cmd);
		_trainName = cmd;
		return true;
	}

	public String getHTML() {
        Simulator sim = Simulator.INSTANCE;
        Map<String, String> values = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
        Train train = sim._schedule.findTrainNamed(_trainName);
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
        return processTemplate(values, "trainInfo.html");
	}
}
