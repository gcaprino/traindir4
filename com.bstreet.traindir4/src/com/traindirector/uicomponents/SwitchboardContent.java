package com.traindirector.uicomponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.traindirector.dialogs.PropertyDialog;
import com.traindirector.dialogs.TextOption;
import com.traindirector.model.Switchboard;
import com.traindirector.model.Switchboard.SwitchboardCell;
import com.traindirector.options.Option;
import com.traindirector.simulator.Simulator;

public class SwitchboardContent extends WebContent {

	private Switchboard currentSwitchboard;
	boolean _isEditing;

	public SwitchboardContent() {
		super("switchboard");
		List<Switchboard> swblist = Simulator.INSTANCE._switchboards;
		if(swblist != null && !swblist.isEmpty())
			currentSwitchboard = swblist.get(0);
		_urlBase = "tdir:/";
	}
	
	public void setCurrentSwitchboard(Switchboard swb) {
		currentSwitchboard = swb;
	}

	@Override
	public boolean doLink(String location) {
		if (location.equals("about:blank"))
			return false;
		Switchboard swb = null;
		if (!_isEditing) {
			return doSwitchboardLink(location);
		}
		//_isEditing = true;
		final String cmd = super.getCmd(location);
        if (cmd.startsWith("sb-edit")) {
        	location = cmd.substring(7).trim();
        	if (location.startsWith("-e ")) {
        		location = location.substring(3).trim();
            	swb = Simulator.INSTANCE.findSwitchboard(location);
            	if (swb == null) {
            		// TODO: impossible
            		return true;
            	}
            	doSwitchBoardNameDialog(swb);
                return true;
        	}
        	if (location.isEmpty()) {
        		doSwitchBoardNameDialog(null);
        		return true;
        	}
        	swb = Simulator.INSTANCE.findSwitchboard(location);
        	if (swb == null) {
        		// TODO: impossible
        		return true;
        	}
        	setCurrentSwitchboard(swb);
            return true;
        } else if(cmd.startsWith("sb-cell")) {
        	doSwitchBoardCellDialog(cmd.substring(7).trim());
            return true;
        } else if(cmd.startsWith("sb-browser")) {
        	// TODO: open an external web browser that points to our server
        } else {
        	String[] elements = cmd.split("/");
        	if (elements.length < 1)
        		return false;
        	swb = Simulator.INSTANCE.findSwitchboard(elements[0]);
        	if (swb == null) {
        		Simulator.INSTANCE.alert("Switchboard '" + elements[0] + "' not found.");
        		return true;
        	}
        	currentSwitchboard = swb;
        	if (elements.length == 3) {
        		int x = 0;
        		int y = 0;
        		try {
        			x = Integer.parseInt(elements[1]);
        			y = Integer.parseInt(elements[2]);
        			swb.select(x, y);
        		} catch (Exception e) {
            		Simulator.INSTANCE.alert("Switchboard '" + elements[0] + "': illegal cell reference '" + elements[1] + "/" + elements[2] + "'");
        		}
        		return true;
        	}
        	return true;
        }
		return false;
	}

	private boolean doSwitchboardLink(String location) {
		String[] elements = location.split("/");
		int i = 1;
		try {
			while (i < elements.length && elements[i].isEmpty()) ++i;
			if (i >= elements.length) return false;
			if (elements[i].equals("switchboard")) {
				while (++i < elements.length && elements[i].isEmpty());
				if (i >= elements.length) return false;
			}
			Switchboard swb = Simulator.INSTANCE.findSwitchboard(elements[i]);
			if (swb == null)
				return false;
			currentSwitchboard = swb;
			while (++i < elements.length && elements[i].isEmpty());
			if (i >= elements.length) return false;
			int x = Integer.parseInt(elements[i]);
			while (++i < elements.length && elements[i].isEmpty());
			if (i >= elements.length) return false;
			int y = Integer.parseInt(elements[i]);
			swb.select(x, y);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void doSwitchBoardNameDialog(Switchboard swb) {
		List<Option> _options = new ArrayList<Option>();
		Option o = new TextOption("name", "Visible name:");
		if (swb != null)
			o.set(swb._name);
		_options.add(o); // 0
		o = new TextOption("file", "File name:");
		if (swb != null)
			o.set(swb._filename);
		_options.add(o); // 1
		
		PropertyDialog dialog = new PropertyDialog(null, _options);
		if (!dialog.openInDisplayThread())
			return;
		if (swb == null) {
			swb = new Switchboard();
			Simulator.INSTANCE._switchboards.add(swb);
		}
		swb._name = _options.get(0)._value;
		swb._filename = _options.get(1)._value;
	}

	protected void doSwitchBoardCellDialog(final String which) {
		String[] rowcol = which.split(",");
		if (rowcol.length != 2)
			return;
		try {
			int i = Integer.parseInt(rowcol[0]);
			int y = Integer.parseInt(rowcol[1]);
			SwitchboardCell cell = currentSwitchboard.find(i, y);
			List<Option> _options = new ArrayList<Option>();
			Option o = new TextOption("label", "Label:");
			if (cell != null)
				o.set(cell.getText());
			_options.add(o); // 0
			o = new TextOption("itin", "Itinerary:");
			if (cell != null)
				o.set(cell.getItinerary());
			_options.add(o); // 1
			
			PropertyDialog dialog = new PropertyDialog(null, _options);
			if (!dialog.openInDisplayThread())
				return;
			currentSwitchboard.change(i, y, _options.get(0)._value, _options.get(1)._value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getHTML() {
		
		// TODO: handle edit vs. operation mode

		Simulator sim = Simulator.INSTANCE;
		List<Switchboard> swblist = Simulator.INSTANCE._switchboards;
		if(currentSwitchboard == null && swblist != null && !swblist.isEmpty())
			currentSwitchboard = swblist.get(0);
		
		if (!_isEditing) {
			return createExternalHtmlPage(_urlBase);
		}
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
            values.put("css", css.toString());

//          TODO:  $serverPort
            
            StringBuilder sblist = new StringBuilder();
            for (Switchboard sb : swblist) {
        	    if(sb == currentSwitchboard) {
        			sblist.append("<tr><td bgcolor=\"#c0ffc0\">");
        			sblist.append(sb._name);
        			sblist.append("&nbsp;&nbsp;&nbsp;<a href=\"" + _urlBase + "sb-edit -e ");
        			sblist.append(sb._filename);
        			sblist.append("\">");
        			sblist.append("change");
        			sblist.append("</a></td></tr>\n");
        		} else {
        			sblist.append("<tr><td bgcolor=\"#e0e0e0\">");
        			sblist.append("<a href=\"" + _urlBase + "sb-edit ");
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
            			cells.append("<td width='70' align='center' valign='top'><a href='" + _urlBase + "sb-cell " + i + "," + y + "'>");
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

	private String createExternalHtmlPage(String oldUrlBase) {
		int indx = oldUrlBase.indexOf("/switchboard/");
		String parentUrl = oldUrlBase.substring(0, indx + 13);
		String cmd = oldUrlBase.substring(indx + 13);
		if (cmd.isEmpty()) {
			currentSwitchboard = Simulator.INSTANCE._switchboards.get(0);
		} else {
			int indx2 = cmd.indexOf("/");
			String swbName = cmd;
			if (indx2 > 0)
				swbName = cmd.substring(0, indx2);
			currentSwitchboard = Simulator.INSTANCE.findSwitchboard(swbName);
			if (currentSwitchboard == null)
				currentSwitchboard = Simulator.INSTANCE._switchboards.get(0);
		}
		String urlBase = oldUrlBase.substring(0, indx + 13);
		urlBase += currentSwitchboard._filename + "/";
		File swbHeaderFile = getResourceFile("/html/en/swbHeader.html");
		List<String> content = getFileContent(swbHeaderFile);
		BufferedReader tdstyle = Simulator.INSTANCE._fileManager.getReaderForFile("tdstyle.css");
		if (tdstyle != null) {
			String line;
			try {
				while((line = tdstyle.readLine()) != null)
					content.add(line);
				tdstyle.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		content.add("</head>\n<body>\n");
		content.add(String.format(
				"<form name=\"iform\" method=\"get\" action=\"%sswitchboard/%s\">",
				oldUrlBase, currentSwitchboard._filename, currentSwitchboard._name));
        content.add("<input type=\"text\" name=\"i\"></form><br>\n");
		content.add("<ul class=\"navbar\">\n<br /><br />");
		for (Switchboard ss : Simulator.INSTANCE._switchboards) {
			if (ss == currentSwitchboard) {
				content.add("<li class='selected'>");
				content.add(ss._name);
				content.add("</li>\n");
			} else {
				content.add("<li class='other'>");
				content.add(String.format("<a href=\"%s%s\">%s</a>",
                    parentUrl, ss._filename, ss._name));
				content.add("</li>\n");
			}
		}
		content.add("</ul>\n");
		StringBuilder css = new StringBuilder();
		for (String s : content) {
			css.append(s);
			css.append('\n');
		}
		String board = currentSwitchboard.toHTML(urlBase);
		css.append(board);
		swbHeaderFile = getResourceFile("/html/en/swbFooter.html");
		List<String> footer = getFileContent(swbHeaderFile);
		for (String s : footer) {
			css.append(s);
			css.append("\n");
		}
		return css.toString();
	}

	public void setEditing(boolean value) {
		_isEditing = value;
	}
}
