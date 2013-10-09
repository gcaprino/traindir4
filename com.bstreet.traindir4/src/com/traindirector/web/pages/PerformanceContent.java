package com.traindirector.web.pages;

import java.util.HashMap;
import java.util.Map;

import com.traindirector.editors.ReportContent;
import com.traindirector.model.PerformanceCounters;
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

	    PerformanceCounters perf_tot = sim._performanceCounters;
	    PerformanceCounters perf_vals = sim._performanceMultipliers;

        sb = new StringBuilder();
        sb.append("<table><tr><td valign=\"top\"");
        sb.append("Time");
        sb.append(" : ");
        sb.append(TDTime.toString(Simulator.INSTANCE._simulatedTime));
        sb.append("</td>");
        String eol = "<br>\n";
    	if(sim._runDay != 0) {
    	    sb.append(String.format("%s : %s",  "Day", TDTime.getDayOfWeek(sim._runDay)));
    	}
    	// TODO: sb.append(String.format("%s : %d%s", "Total points", run_points, eol));
    	sb.append(String.format("%s : %d%s",  "Total min. of delayed entry", sim._totalDelay/ 60, eol));
    	sb.append(String.format("%s : %d%s", "Total min. trains arrived late", sim._totalLate, eol));
	    sb.append(String.format("<br><a href=\"save_perf_text\">%s</a>", "Save as text"));
	    /*
	    if(performance_hide_canceled) {
	    	sb.append(String.format("<br><a href=\"performance_toggle_canceled\">%s</a>", "(show canceled trains)"));
	    } else {
	    	sb.append(String.format("<br><a href=\"performance_toggle_canceled\">%s</a>", "(hide canceled trains)"));
	    }
	    */
	    sb.append(("</td><td valign=top>\n"));
	    sb.append(String.format("<table><tr><td valign=top>%s</td>\n", "Wrong destinations"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.wrong_dest, perf_vals.wrong_dest));
	    sb.append(String.format("<td>%d</td></tr>\n", perf_tot.wrong_dest * perf_vals.wrong_dest));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Late trains"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.late_trains, perf_vals.late_trains));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.late_trains * perf_vals.late_trains));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Wrong platforms"));
	    sb.append(String.format("<td valign=top align=right>%d&nbsp;x</td><td valign=top align=right>%d&nbsp;=</td>",
			    perf_tot.wrong_platform, perf_vals.wrong_platform));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.wrong_platform * perf_vals.wrong_platform));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Commands denied"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.denied, perf_vals.denied));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.denied * perf_vals.denied));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Trains waiting at signals"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.waiting_train, perf_vals.waiting_train));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.waiting_train * perf_vals.waiting_train));

	    sb.append(String.format("</table></td><td valign=top><table>"));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Thrown switches"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.thrown_switch, perf_vals.thrown_switch));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.thrown_switch * perf_vals.thrown_switch));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Cleared signals"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.cleared_signal, perf_vals.cleared_signal));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.cleared_signal * perf_vals.cleared_signal));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Reversed trains"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.turned_train, perf_vals.turned_train));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.turned_train * perf_vals.turned_train));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Missed station stops"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.nmissed_stops, perf_vals.nmissed_stops));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.nmissed_stops * perf_vals.nmissed_stops));
	    sb.append(String.format("<tr><td valign=top>%s</td>\n", "Wrong stock assignments"));
	    sb.append(String.format("<td align=right valign=top>%d&nbsp;x</td><td align=right valign=top>%d&nbsp;=</td>",
			    perf_tot.wrong_assign, perf_vals.wrong_assign));
	    sb.append(String.format("<td valign=top>%d</td></tr>\n", perf_tot.wrong_assign * perf_vals.wrong_assign));
	    sb.append("</table></td></tr></table>");

	    values.put("$statistics", sb.toString());
        return processTemplate(values, "performance.html");
	}
}
