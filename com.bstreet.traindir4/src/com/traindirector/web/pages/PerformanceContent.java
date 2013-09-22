package com.traindirector.web.pages;

import java.util.HashMap;
import java.util.Map;

import com.traindirector.editors.ReportContent;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDTime;

public class PerformanceContent extends ReportContent {

	public PerformanceContent() {
		super("performance");
	}

	public String getHTML() {
		Simulator sim = Simulator.INSTANCE;
		StringBuilder sb = new StringBuilder();
        Map<String, String> values = new HashMap<String, String>();

        if (sim._schedule._trains.isEmpty()) {
            sb.append("You have not simulated any scenario, yet, or this scenario has no trains scheduled.");
        } else {
            for (Train train : sim._schedule._trains) {
                sb.append("<tr>");
                appendColumn(sb, getTrainLink(train));
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
        return processTemplate(values, "performance.html");
	}
}
