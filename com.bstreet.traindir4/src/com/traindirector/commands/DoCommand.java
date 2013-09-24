package com.traindirector.commands;

import org.eclipse.swt.widgets.Display;

import com.traindirector.Application;
import com.traindirector.editors.InfoPage;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Train;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class DoCommand extends SimulatorCommand {

	String _cmd;
	
	public DoCommand(String cmd) {
		_cmd = cmd;
		_simulator = Application.getSimulator();
	}
	
	public void handle() {
		TDPosition pos;
		int offset = 0;
		
		if (_cmd.startsWith("quit")) {
			
		} else if(_cmd.startsWith("about")) {
			
		} else if(_cmd.startsWith("edititinerary")) {
			
		} else if(_cmd.startsWith("edit")) {
			
		} else if(_cmd.startsWith("noedit")) {
			
		} else if(_cmd.startsWith("stationsched")) {
			
		} else if(_cmd.startsWith("paths")) {
			
		} else if(_cmd.startsWith("fast")) {
			SpeedCommand scmd = new SpeedCommand(1);
			_simulator.addCommand(scmd);
		} else if(_cmd.startsWith("slow")) {
			SpeedCommand scmd = new SpeedCommand(-1);
			_simulator.addCommand(scmd);
		} else if(_cmd.startsWith("t0")) {
			RestartCommand rcmd = new RestartCommand();
			_simulator.addCommand(rcmd);
		} else if(_cmd.startsWith("speeds")) {
			
		} else if(_cmd.startsWith("traditional")) {
			
		} else if(_cmd.startsWith("graph")) {
			
		} else if(_cmd.startsWith("blocks")) {
			
		} else if(_cmd.startsWith("alerts")) {
			
		} else if(_cmd.startsWith("sched")) {
			
		} else if(_cmd.startsWith("run")) {
			RunStopCommand rscmd = new RunStopCommand();
			_simulator.addCommand(rscmd);
		} else if(_cmd.startsWith("greensigs")) {
			
		} else if(_cmd.startsWith("shunt")) {
			
		} else if(_cmd.startsWith("traininfopage")) {
			
		} else if(_cmd.startsWith("traininfo")) {
			
		} else if(_cmd.startsWith("shunt")) {
			
		} else if(_cmd.startsWith("decelerate")) {
			
		} else if(_cmd.startsWith("accelerate")) {
			
		} else if(_cmd.startsWith("stationinfopage")) {
			
		} else if(_cmd.startsWith("savestationinfopage")) {
			
		} else if(_cmd.startsWith("stationinfo")) {
			
		} else if(_cmd.startsWith("reverse")) {
			offset = skipBlanks(_cmd, 7);
			String trainName = _cmd.substring(offset);
			Train train = _simulator._schedule.findTrainNamed(trainName);
			if (train == null) {
				_simulator.alert(String.format("Train %s not found in reverse command.", trainName));
				return;
			}
			ReverseCommand rcmd = new ReverseCommand(train);
			rcmd.handle();
		} else if(_cmd.startsWith("new")) {
			
		} else if(_cmd.startsWith("save ")) {
			
		} else if(_cmd.startsWith("savegame ")) {
			
		} else if(_cmd.startsWith("restore ")) {
			
		} else if(_cmd.startsWith("open ") || _cmd.startsWith("load ")) {
			
		} else if(_cmd.startsWith("click ")) {
			offset = skipBlanks(_cmd, 6);
			if(offset >= _cmd.length())
				return;
			if (_cmd.charAt(offset) >= '0' && _cmd.charAt(offset) <= '9') {
				pos = new TDPosition();
				pos.fromString(_cmd, offset);
				ClickCommand ccmd = new ClickCommand(pos);
				_simulator.addCommand(ccmd);
				return;
			}
			ItineraryCommand icmd = new ItineraryCommand(_cmd.substring(offset));
			_simulator.addCommand(icmd);
		} else if(_cmd.startsWith("rclick ")) {
			offset = skipBlanks(_cmd, 7);
			if(offset >= _cmd.length())
				return;
			if (_cmd.charAt(offset) >= '0' && _cmd.charAt(offset) <= '9') {
				pos = new TDPosition();
				pos.fromString(_cmd, offset);
				ClickCommand ccmd = new ClickCommand(pos);
				ccmd._leftButton = false;
				_simulator.addCommand(ccmd);
				return;
			}
			ItineraryCommand icmd = new ItineraryCommand(_cmd.substring(offset));
			icmd._deselect = true;
			_simulator.addCommand(icmd);
		} else if(_cmd.startsWith("ctrlclick ")) {
			offset = skipBlanks(_cmd, 10);
			if(offset >= _cmd.length())
				return;
			if (_cmd.charAt(offset) >= '0' && _cmd.charAt(offset) <= '9') {
				pos = new TDPosition();
				pos.fromString(_cmd, offset);
				ClickCommand ccmd = new ClickCommand(pos);
				ccmd._ctrlKey = true;
				_simulator.addCommand(ccmd);
			}
			return;
		} else if(_cmd.startsWith("selecttool")) {
			
		} else if(_cmd.startsWith("itinerary")) {
			
		} else if(_cmd.startsWith("info")) {
			ShowInfoCommand cmd = new ShowInfoCommand(_simulator._baseFileName + ".htm");
			_simulator.addCommand(cmd);
		} else if(_cmd.startsWith("showinfo")) {
			String name = _cmd.substring(8).trim();
			ShowInfoCommand cmd = new ShowInfoCommand(name);
			_simulator.addCommand(cmd);
		} else if(_cmd.startsWith("sb-edit")) {
			
		} else if(_cmd.startsWith("sb-browser")) {
			
		} else if(_cmd.startsWith("sb-cell")) {
			
		} else if(_cmd.startsWith("performance")) {
			
		} else if(_cmd.startsWith("performance_toggle_canceled")) {
			
		} else if(_cmd.startsWith("options")) {
			
		} else if(_cmd.startsWith("assign")) {
			offset = skipBlanks(_cmd, 6);
			StringBuilder sb = new StringBuilder();
			while(offset < _cmd.length() && _cmd.charAt(offset) != ',') {
				sb.append(_cmd.charAt(offset++));
			}
			Train from = _simulator._schedule.findTrainNamed(sb.toString());
			if (from == null) {
				_simulator.alert(String.format("No train %s in assign command.", sb.toString()));
				return;
			}
			String destTrainName;
			if(offset < _cmd.length() && _cmd.charAt(offset) == ',') {
				offset = skipBlanks(_cmd, offset + 1);
				destTrainName = _cmd.substring(offset);
			} else {
				if (from._stock == null) {
					_simulator.alert(String.format("Train %s has no default stock assignment.", from._name));
					return;
				}
				destTrainName = from._stock;
			}
			Train toTrain = _simulator._schedule.findTrainNamed(destTrainName);
			if (toTrain == null) {
				_simulator.alert(String.format("Cannot assign train %s: destination train %s not in the schedule.",
						from._name, destTrainName));
				return;
			}
			AssignCommand acmd = new AssignCommand(from, toTrain);
			_simulator.addCommand(acmd);
		} else if(_cmd.startsWith("play")) {
			String path = _cmd.substring(4).trim();
			_simulator._soundPlayer.play(path);
		} else if(_cmd.startsWith("skip")) {
			
		} else if(_cmd.startsWith("save_perf_text")) {
			
		} else if(_cmd.startsWith("split")) {
			
		} else if(_cmd.startsWith("script")) {
			
		} else if(_cmd.startsWith("showalert")) {
			offset = skipBlanks(_cmd, 9);
			if (offset >= _cmd.length())
				return;
			_simulator.alert(_cmd.substring(offset));
		} else if(_cmd.startsWith("clearalert")) {
			
		} else if(_cmd.startsWith("switch")) {
			
		} else {
			_simulator.alert("Command not recognized: '" + _cmd + "'");
		}
		/*
		const wxChar	*p;
		Train	*t;
		Track	*trk;
		int	x, y, fl;
		wxChar	buff[256];

		if(!wxStrncmp(cmd, wxT("log"), 3)) {
		    if(!flog.IsOpened()) {
			if(!(flog.Open(wxT("log"), wxT("w"))))
			    do_alert(L("Cannot create log file."));
			return;
		    }
		    flog.Close();
		    return;
		}
		if(!wxStrncmp(cmd, wxT("replay"), 6)) {
		    for(p = cmd + 6; *p == ' ' || *p == '\t'; ++p);
		    wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("%s.log"), p);
		    if(!(frply = new TDFile(buff))) {
			do_alert(L("Cannot read log file."));
			return;
		    }
		    // replay commands are issued whenever the clock is updated
		    return;
		}
		if(flog.IsOpened())
		    flog.Write(wxString::Format(wxT("%ld,%s\n"), current_time, cmd));
		wxSnprintf(buff, sizeof(buff), wxT("%ld,%s\n"), current_time, cmd);
		if(sendToClients)
		    send_msg(buff);
		if(!wxStrncmp(cmd, wxT("quit"), 4))
		    main_quit_cmd();
		else if(!wxStrncmp(cmd, wxT("about"), 5)) {
		    about_dialog();
		} else if(!wxStrcmp(cmd, wxT("edititinerary"))) {
		    itinerary_cmd();
		} else if(!wxStrncmp(cmd, wxT("edit"), 4)) {
		    if(running)
			start_stop();
		    edit_cmd();
		} else if(!wxStrncmp(cmd, wxT("noedit"), 6))
		    noedit_cmd();
		else if(!wxStrncmp(cmd, wxT("stationsched"), 12))
		    station_sched_dialog(NULL);
		else if(!wxStrncmp(cmd, wxT("paths"), 5))
		    create_path_window();
		else if(!wxStrncmp(cmd, wxT("fast"), 4)) {
		    if(time_mults[cur_time_mult + 1] != -1)
			time_mult = time_mults[++cur_time_mult];
		    update_labels();
		} else if(!wxStrncmp(cmd, wxT("slow"), 4)) {
		    if(cur_time_mult > 0) {
			time_mult = time_mults[--cur_time_mult];
			update_labels();
		    }
		} else if(!wxStrncmp(cmd, wxT("t0"), 2)) {
		    if(cont(L("Do you want to restart the simulation?")) == ANSWER_YES) {
			if(!all_trains_everyday(schedule))
			    select_day_dialog();
			clear_delays();
			fill_schedule(schedule, 0);
		        wxSnprintf(status_line, sizeof(status_line)/sizeof(wxChar), L("Simulation restarted."));
		        trainsim_init();
			invalidate_field();
			update_button(wxT("stop"), L("Stop"));
			repaint_all();
		    }
		} else if(!wxStrncmp(cmd, wxT("speeds"), 6)) {
		    show_speeds = !show_speeds;
		    invalidate_field();
		    repaint_all();
		} else if(!wxStrncmp(cmd, wxT("traditional"), 6)) {
		    signal_traditional = !signal_traditional;
		    invalidate_field();
		    repaint_all();
		} else if(!wxStrncmp(cmd, wxT("graph"), 6)) {
		    create_tgraph();
		} else if(!wxStrncmp(cmd, wxT("blocks"), 6)) {
		    show_blocks = !show_blocks;
		    invalidate_field();
		    repaint_all();
		} else if(!wxStrncmp(cmd, wxT("alert"), 5)) {
		    beep_on_alert = !beep_on_alert;
		} else if(!wxStrncmp(cmd, wxT("sched"), 5)) {
		    create_schedule(0);
		} else if(!wxStrncmp(cmd, wxT("run"), 3)) {
		    start_stop();
		    update_button(wxT("run"), running ? L("Stop") : L("Start"));
		} else if(!wxStrncmp(cmd, wxT("newtrain"), 8)) {
		    create_train();
		} else if(!wxStrncmp(cmd, wxT("greensigs"), 9)) {
		    open_all_signals();
		} else if(!wxStrncmp(cmd, wxT("shunt"), 5)) {
		    cmd += 5;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    if(!(t = findTrainNamed(cmd)))
			return;
		    shunt_train(t);
		} else if(!wxStrncmp(cmd, wxT("traininfopage"), 13)) {
		    cmd += 13;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    if(!(t = findTrainNamed(cmd)))
			return;
		    ShowTrainInfo(t);
		} else if(!wxStrncmp(cmd, wxT("traininfo"), 9)) {
		    cmd += 9;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    if(!(t = findTrainNamed(cmd)))
			return;
		    train_info_dialog(t);
		} else if(!wxStrncmp(cmd, wxT("decelerate"), 10)) {
		    long    val;
		    wxChar *end;

		    cmd += 10;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    val = wxStrtol(cmd, &end, 0);
		    while(*end == ' ' || *end == '\t') ++end;
		    if(!(t = findTrainNamed(end)))
			return;
		    decelerate_train(t, val);
		} else if(!wxStrncmp(cmd, wxT("accelerate"), 10)) {
		    long    val;
		    wxChar *end;

		    cmd += 10;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    val = wxStrtol(cmd, &end, 0);
		    while(*end == ' ' || *end == '\t') ++end;
		    if(!(t = findTrainNamed(end)))
			return;
		    accelerate_train(t, val);
		} else if(!wxStrncmp(cmd, wxT("stationinfopage"), 15)) {
		    cmd += 15;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    ShowStationSchedule(cmd, false);
		} else if(!wxStrncmp(cmd, wxT("savestationinfopage"), 19)) {
		    cmd += 19;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    ShowStationSchedule(cmd, true);
		} else if(!wxStrncmp(cmd, wxT("stationinfo"), 11)) {
		    cmd += 11;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    station_sched_dialog(cmd);
		} else if(!wxStrncmp(cmd, wxT("reverse"), 7)) {
		    cmd += 7;
		    while(*cmd == ' ' || *cmd == '\t') ++cmd;
		    if(!(t = findTrainNamed(cmd)))
			return;
		    reverse_train(t);
		} else if(!wxStrncmp(cmd, wxT("new"), 3)) {
		    if(running)
			start_stop();
		    if(layout_modified) {
			if(ask_to_save_layout() < 0)	// cancel selected
			    return;
		    }
		    init_all();
		} else if(!wxStrncmp(cmd, wxT("save "), 5)) {
		    if(save_layout(cmd + 5, layout))
			wxSnprintf(status_line, sizeof(status_line)/sizeof(wxChar), wxT("%s '%s.trk'."), L("Layout saved in file"), cmd + 5);
		    repaint_labels();
		} else if(!wxStrncmp(cmd, wxT("savegame "), 9)) {
		    if(save_game(cmd + 9))
			wxSnprintf(status_line, sizeof(status_line)/sizeof(wxChar), wxT("%s '%s.sav'."), L("Game status saved in file"), cmd + 9);
		    repaint_labels();
		} else if(!wxStrncmp(cmd, wxT("restore "), 8)) {
		    if(layout_modified) {
			if(ask_to_save_layout() < 0)	// cancel selected
			    return;
		    }
		    restore_game(cmd + 8);
		    invalidate_field();
		    repaint_all();
		    fill_schedule(schedule, 0);
		    update_labels();
		} else if(!wxStrncmp(cmd, wxT("open"), 4) || !wxStrncmp(cmd, wxT("load"), 4)) {
		    fl = cmd[0] == 'o';		// open vs. load
		    cmd += 4;
		    load_new_scenario(cmd, fl);
		} else if(!wxStrncmp(cmd, wxT("puzzle"), 6)) {
		    cmd += 6;
		    load_new_scenario(cmd, 2);
		} else if(!wxStrncmp(cmd, wxT("rclick"), 6)) {
		    for(cmd += 6; *cmd == wxT(' ') || *cmd == wxT('\t'); ++cmd);
		    if(*cmd >= wxT('0') && *cmd <= wxT('9')) {
			wxChar *end;
			x = wxStrtol(cmd, &end, 10);
			if(*end == wxT(',')) ++end;
			y = wxStrtol(end, &end, 10);
		    } else {
			if(!(trk = findItineraryNamed(cmd)))
			    return;		//* impossible ?
			x = trk->x;
			y = trk->y;
		    }
		    track_selected1(x, y);
		} else if(!wxStrncmp(cmd, wxT("ctrlclick"), 9)) {
		    for(cmd += 9; *cmd == wxT(' ') || *cmd == wxT('\t'); ++cmd);
		    if(*cmd >= '0' && *cmd <= '9') {
			wxChar *end;
			x = wxStrtol(cmd, &end, 10);
			if(*end == wxT(',')) ++end;
			y = wxStrtol(end, &end, 10);
		    } else {
			if(!(trk = findItineraryNamed(cmd)))
			    return;		// impossible ?
			x = trk->x;
			y = trk->y;
		    }
		    Coord	coord(x, y);
		    track_control_selected(coord);
		} else if(!wxStrncmp(cmd, wxT("selecttool"), 10)) {
		    wxChar *end;
		    for(cmd += 10; *cmd == wxT(' ') || *cmd == wxT('\t'); ++cmd);
		    x = wxStrtol(cmd, &end, 10);
		    if(*end == wxT(',')) ++end;
		    y = wxStrtol(end, &end, 10);
		    tool_selected(x, y);
		} else if(!wxStrncmp(cmd, wxT("itinerary"), 9)) {
		    for(cmd += 9; *cmd == wxT(' ') || *cmd == wxT('\t'); ++cmd);
		    const wxChar *nameend = wxStrrchr(cmd, wxT('@'));
		    int	    namelen;
		    Itinerary *it;

		    if(nameend)
			namelen = nameend - cmd;
		    else
			namelen = wxStrlen(cmd);
		    for(it = itineraries; it; it = it->next) {
			if(!wxStrncmp(it->name, cmd, namelen) &&
			      wxStrlen(it->name) == namelen)
	 		    break;
		    }
		    if(it)
			itinerary_selected(it);
		} else if(!wxStrcmp(cmd, wxT("info"))) {
		    track_info_dialogue();
		} else if(!wxStrncmp(cmd, wxT("sb-edit"), 7)) {
		    SwitchboardEditCommand(cmd + 7);
		} else if(!wxStrncmp(cmd, wxT("sb-browser"), 10)) {
		    SwitchboardOpenBrowser(cmd + 10);
		} else if(!wxStrncmp(cmd, wxT("sb-cell"), 7)) {
		    SwitchboardCellCommand(cmd + 7);
		} else if(!wxStrcmp(cmd, wxT("performance"))) {
		    performance_dialog();
		} else if(!wxStrcmp(cmd, wxT("performance_toggle_canceled"))) {
		    performance_toggle_canceled();
		    performance_dialog();	// update page
		} else if(!wxStrcmp(cmd, wxT("options"))) {
		    options_dialog();
		    if(hard_counters)
			perf_vals = perf_hard;
		    else
			perf_vals = perf_easy;
		    invalidate_field();
		    repaint_all();
		    update_labels();
		    new_status_position();
		} else if(!wxStrncmp(cmd, wxT("assign"), 6)) {
		    Train   *t1;

		    for(cmd += 6; *cmd == ' ' || *cmd == '\t'; ++cmd);
		    x = 0;
		    while(*cmd && *cmd != ',') {
			buff[x++] = *cmd++;
		    }
		    buff[x] = 0;
		    if(!(t = findTrainNamed(buff))) {
			// trace(L("Cannot assign %s: train not found."));
			return;
		    }
		    if(*cmd == ',') {
			while(*++cmd == ' ' || *cmd == '\t');
		    } else {
			if(!t->stock) {
			    // trace(L("Train %s has no default stock assignment."));
			    return;
			}
			cmd = t->stock;
		    }
		    if(!(t1 = findTrainNamed(cmd))) {
			// trace(L("Cannot assign %s: train not found."));
			return;
		    }
		    save_assign_train(t1, t);
//		    invalidate_field();
//		    repaint_all();
		} else if(!wxStrncmp(cmd, wxT("play"), 4)) {
		    cmd += 4;
		    while(*cmd == ' ') ++cmd;
		    traindir->PlaySound(cmd);
		} else if(!wxStrcmp(cmd, wxT("skip"))) {
		    skip_to_next_event();
		} else if(!wxStrcmp(cmd, wxT("save_perf_text"))) {
		    traindir->SavePerfText();
		} else if(!wxStrncmp(cmd, wxT("split"), 5)) {
		    int length;

		    for(cmd += 5; *cmd == wxT(' ') || *cmd == wxT('\t'); ++cmd);
		    x = 0;
		    while(*cmd && *cmd != ',') {
			buff[x++] = *cmd++;
		    }
		    buff[x] = 0;
		    if(!(t = findTrainNamed(buff))) {
			// trace(L("Cannot split %s: train not found."));
			return;
		    }
		    if(*cmd == ',') {
			while(*++cmd == ' ' || *cmd == '\t');
			length = wxAtoi(cmd);
		    } else {
			length = 0;
		    }
		    split_train(t, length);
		} else if(!wxStrncmp(cmd, wxT("script"), 6)) {
		    wxChar *end;
		    for(cmd += 10; *cmd == ' ' || *cmd == '\t'; ++cmd);
		    x = wxStrtol(cmd, &end, 10);
		    if(*end == ',') ++end;
		    y = wxStrtol(end, &end, 10);
		    while(*end == ' ' || *end == '\t') ++end;
		    if(!*end)
			return;
		    trk = find_track(layout, x, y);
		    switch(trk->type) {
		    case TRACK:
		    case TRIGGER:
		    case SWITCH:
			trk->RunScript(end);
		    }

		} else if(match(&cmd, wxT("showinfo"))) {
		    TDFile	infoFile(cmd);

		    infoFile.SetExt(wxT(".htm"));
		    if(infoFile.Load()) {
			traindir->m_frame->ShowHtml(L("Scenario Info"), infoFile.content);
			info_page = infoFile.name.GetName();
		    }
		} else if(match(&cmd, wxT("clearalert"))) {
		    traindir->ClearAlert();
	        } else if(match(&cmd, wxT("switch"))) {
		    wxChar *end;
		    cmd = skip_blanks(cmd);
	            if(*cmd != '\'') {
		        x = wxStrtol(cmd, &end, 10);
		        if(*end == ',') ++end;
		        y = wxStrtol(end, &end, 10);
	                end = (wxChar *)skip_blanks(end);
	                if(*end) {
		            SwitchBoard *sw = FindSwitchBoard(end);
		            if(sw)
		                sw->Select(x, y);
	                }
	            } else {
	                end = (Char *)++cmd;
	                while(*end && *end != '\'')
	                    ++end;
	                *end++ = 0;
	                end = (wxChar *)skip_blanks(end);
	                if(*end) {
		            SwitchBoard *sw = FindSwitchBoard(end);
		            if(sw)
		                sw->Select(cmd);
	                }
	            }
	            server_command_done = true;
		} else {
		    wxSnprintf(status_line, sizeof(status_line)/sizeof(status_line[0]), wxT("Command: %s"), cmd);
		    repaint_labels();
		}
*/
	}
	
	public int skipBlanks(String s, int offset) {
		while(offset < s.length() && s.charAt(offset) == ' ')
			++offset;
		return offset;
	}
}
