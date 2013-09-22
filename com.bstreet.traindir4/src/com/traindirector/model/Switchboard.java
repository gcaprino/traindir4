package com.traindirector.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.traindirector.simulator.Simulator;

public class Switchboard {

    public static final int MAX_SWBD_X = 40;
    public static final int MAX_SWBD_Y = 40;
    
    public String _name;
    public String _filename;

    public class SwitchboardCell {
        TDPosition _pos;
        String _itinerary;        // linked itinerary
        String _text;         // text to draw, if any
        String  _aspect;       // current aspect

        public SwitchboardCell(TDPosition pos) {
            _pos = pos;
            _aspect = null;
            _text = null;
            _itinerary = null;
        }
        
        public String getAspect() {
            if (_aspect == null)
                return "blank";
            return _aspect;
        }
        
        public String toHTML(String urlBase) {
            StringBuilder str = new StringBuilder();
            Itinerary   it = Simulator.INSTANCE._territory.findItinerary(_itinerary);

            if(it == null) {
                str.append("<td class=\"empty\">");
                str.append(_text);
                str.append("</td>\n");
                return str.toString();
            }

            str.append("<td class=\"");
            if(it.canSelect())
                str.append("available");
            else if(it.isSelected())
                str.append("selected");
            else
                str.append("locked");
            str.append("\"><a href=\"");
            str.append(String.format("%s/%d/%d\">", urlBase, _pos._x, _pos._y));
            str.append(_text);
            str.append("</a></td>\n");
            return str.toString();
        }
        
        public String getItinerary() {
        	return _itinerary;
        }
        
        public String getText() {
        	return _text;
        }

		public void setText(String text) {
			_text = text;
		}

		public void setItinerary(String value) {
			_itinerary = value;
		}
}
    
    public class SwitchboardAspect {
        String _name;
        String _bgColor;
        String _action;
        String[] _icons;
    }
    
    List<SwitchboardCell> _cells;
    List<SwitchboardAspect> _aspects;


    public Switchboard() {
        _cells = new ArrayList<SwitchboardCell>();
        _aspects = new ArrayList<SwitchboardAspect>();
    }

    //  Aspect: name
    //     Action: locked/free/avail
    //     Bgcolor: color/#rrggbb
    //     Icons: icon1 [icon2 ...]
    
    
    public String parseAspect(BufferedReader in, String name) throws IOException {
    	String line = null;
        SwitchboardAspect aspect = new SwitchboardAspect();
        aspect._name = name;
        _aspects.add(aspect);
        while(true) {
            line = in.readLine().trim();
            if(line == null)
                break;
            if (line.startsWith("Icons:")) {
                aspect._icons = line.substring(6).split(" ");
            } else if(line.startsWith("Action:")) {
                aspect._action = line.substring(7).trim();
            } else if(line.startsWith("Bgcolor:")) {
                aspect._bgColor = line.substring(8).trim();
            } else if(line.startsWith("Aspect:")) {
            	aspect = new SwitchboardAspect();
            	aspect._name = line.substring(7).trim();
            	_aspects.add(aspect);
            } else {
                break;
            }
        }
        return line;
    }
    
    //  Cell: x, y
    //     Itinerary:  name
    //     Text:   string
    
    public String parseCell(BufferedReader in, String coord) throws IOException {
        TDPosition pos = new TDPosition(coord);
        SwitchboardCell cell = new SwitchboardCell(pos);
        String line = null;
        while(true) {
            line = in.readLine().trim();
            if(line == null)
                break;
            if (line.startsWith("Itinerary:")) {
                cell._itinerary = line.substring(10).trim();
            } else if(line.startsWith("Text:")) {
                cell._text = line.substring(5).trim();
            } else if(line.startsWith("Cell:")) {
            	_cells.add(cell);
            	pos = new TDPosition(line.substring(5).trim());
            	cell = new SwitchboardCell(pos);
            } else {
                break;
            }
        }
        _cells.add(cell);
        return line;
    }

    public SwitchboardAspect getAspect(String name) {
        for(SwitchboardAspect aspect : _aspects) {
            if (aspect._name.equals(name))
                return aspect;
        }
        return null;
    }

    public String toHTML(String urlBase) {
        int xMax = 0;
        int yMax = 0;
        int x, y;
        SwitchboardCell[][] grid = new SwitchboardCell[MAX_SWBD_X][MAX_SWBD_Y];

        for(SwitchboardCell cell : _cells) {
            if(cell._pos._x < MAX_SWBD_X && cell._pos._y < MAX_SWBD_Y) {
            grid[cell._pos._x][cell._pos._y] = cell;
            if(cell._pos._x + 1 > xMax)
                xMax = cell._pos._x + 1;
            if(cell._pos._y + 1 > yMax)
                yMax = cell._pos._y + 1;
            }
        }
        if(xMax == 0 || yMax == 0)
            return "No cells";

        StringBuilder str = new StringBuilder();
        SwitchboardCell cell;
        ++xMax;
        ++yMax;
        str.append("<table class=\"switchboard\">\n");
        for(y = 0; y < yMax; ++y) {
            str.append("<tr>\n");
            for(x = 0; x < xMax; ++x) {
                cell = grid[x][y];
                if(cell == null)
                    str.append("<td class=\"empty\">&nbsp;</td>\n");
                else
                    str.append(cell.toHTML(urlBase));
            }
            str.append("</tr>\n");
        }
        str.append("</table>\n");
        return str.toString();
    }
    
    public SwitchboardCell find(int x, int y) {
        for (SwitchboardCell cell : _cells) {
            if (cell._pos._x == x && cell._pos._y == y)
                return cell;
        }
        return null;
    }

    public boolean select(int x, int y) {
        SwitchboardCell cell = find(x, y);
        if (cell == null)
            return false;
        Itinerary it = Simulator.INSTANCE._territory.findItinerary(cell._itinerary);
        if(it == null)
            return false;
        if(it.canSelect())
            it.select();
        else if(it.isSelected())
            it.deselect(false);
        return true;

    }

    public void add(SwitchboardCell newCell) {
        for (SwitchboardCell cell : _cells) {
            if (cell == newCell)
                return;
        }
        _cells.add(newCell);
    }

    public void change(int x, int y, String name, String itinName) {
        SwitchboardCell cell = find(x, y);

        if(cell == null) {
            cell = new SwitchboardCell(new TDPosition(x, y));
            _cells.add(cell);
        }
        cell._text = name;
        cell._itinerary = itinName;
    }

    public void remove(SwitchboardCell oldCell) {
        _cells.remove(oldCell);
    }

	public void load(BufferedReader swbReader) {
		String line;
		
		try {
			line = swbReader.readLine();
			while(line != null) {
				line = line.replace("\t", " ").trim();
				if(line.startsWith("Aspect:")) {
					line = parseAspect(swbReader, line.substring(7).trim());
				} else if (line.startsWith("Cell:")) {
					line = parseCell(swbReader, line.substring(5).trim());
				} else if (line.startsWith("Name:")) {
					_name = line.substring(5).trim();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

/*
int pathIsBusy(Train *tr, Vector *path, int dir);
Vector  *findPath(Track *t, int dir);
Track   *findNextTrack(trkdir direction, int x, int y);
const Char *GetColorName(int color);

extern  void    ShowSwitchboard(void);
extern  bool    file_create(const wxChar *name, const wxChar *ext, wxFFile& fp);

SwitchBoard *switchBoards;

SwitchBoard *curSwitchBoard;        // TODO: move to a SwitchBoardCell field


bool    SwitchBoard::Select(int x, int y)
{
    SwitchBoardCell *cell = Find(x, y);

    if(!cell)
        return false;
    Itinerary *it = find_itinerary(cell->_itinerary);
    if(!it)
        return false;
    if(it->CanSelect())
        it->Select();
    else if(it->IsSelected())
        it->Deselect(false);
    return true;
}


//
//
//
//




SwitchBoard *FindSwitchBoard(const wxChar *name)
{
    SwitchBoard *sb;

    for(sb = switchBoards; sb; sb = sb->_next) {
        if(!wxStrcmp(name, sb->_fname))
        return sb;
    }
    return 0;
}


SwitchBoard *CreateSwitchBoard(const wxChar *name)
{
    SwitchBoard *sb = FindSwitchBoard(name);
    RemoveSwitchBoard(sb);
    sb = new SwitchBoard();
    sb->_name = name;
    sb->_fname = name;
    sb->_next = switchBoards;
    switchBoards = sb;
    return sb;
}


void    RemoveSwitchBoard(SwitchBoard *sb)
{
    SwitchBoard *old = 0;
    SwitchBoard *s;

    for(s = switchBoards; s && s != sb; s = s->_next)
        old = s;
    if(s) {
        if(!old)
        switchBoards = s->_next;
        else
        old->_next = s->_next;
    }
    if(sb)
        delete sb;
}


void    RemoveAllSwitchBoards()
{
    SwitchBoard *sb;

    while(switchBoards) {
        sb = switchBoards;
        switchBoards = sb->_next;
        delete sb;
    }
}


void    SaveSwitchBoards(wxFFile& file)
{
    SwitchBoard *sb;

    for(sb = switchBoards; sb; sb = sb->_next) {
        file.Write(wxString::Format(wxT("(switchboard %s)\n"), sb->_fname.c_str()));
        wxFFile file;
        if(!file_create(sb->_fname, wxT(".swb"), file))
        break;
        SwitchBoardCellAspect *asp;
        for(asp = sb->_aspects; asp; asp = asp->_next) {
        file.Write(wxString::Format(wxT("Aspect: %s\n"), asp->_name));
        file.Write(wxString::Format(wxT("Bgcolor: %s\n\n"), asp->_bgcolor));
        }
        SwitchBoardCell *cell;
            file.Write(wxString::Format(wxT("Name: %s\n"), sb->_name.c_str()));
        for(cell = sb->_cells; cell; cell = cell->_next) {
        file.Write(wxString::Format(wxT("Cell: %d,%d\n"), cell->_x, cell->_y));
        file.Write(wxString::Format(wxT("Itinerary: %s\n"), cell->_itinerary.c_str()));
        file.Write(wxString::Format(wxT("Text: %s\n\n"), cell->_text.c_str()));
        }
        file.Close();
    }
}


extern  void    switchboard_name_dialog(const wxChar *name);
extern  void    switchboard_cell_dialog(int x, int y);

#define MAX_NAME_LEN    256

void    SwitchboardEditCommand(const Char *cmd)
{
    SwitchBoard *sb;
    Char    buff[MAX_NAME_LEN];
    int i;

    while(*cmd == wxT(' '))
        ++cmd;
    if(!*cmd) {
        switchboard_name_dialog(0);
        ShowSwitchboard();
        return;
    }
    if(*cmd == wxT('-') && cmd[1] == wxT('a')) {
        cmd += 2;
        while(*cmd == wxT(' '))
        ++cmd;
        for(i = 0; i < MAX_NAME_LEN - 1 && *cmd && *cmd != wxT(' '); ++i)
        buff[i] = *cmd++;
        buff[i] = 0;
        sb = FindSwitchBoard(buff);
        if(!sb)
        curSwitchBoard = CreateSwitchBoard(buff);
        else
        curSwitchBoard = sb;
        while(*cmd == wxT(' '))
        ++cmd;
            if(*cmd)
            curSwitchBoard->_name = cmd;
        ShowSwitchboard();
        return;
    }
    if(*cmd == wxT('-') && cmd[1] == wxT('e')) {
        cmd += 2;
        while(*cmd == wxT(' '))
        ++cmd;
        sb = FindSwitchBoard(cmd);
        if(!sb)     // not there - nothing to do
        return;
        curSwitchBoard = sb;
        switchboard_name_dialog(cmd);
        ShowSwitchboard();
        return;
    }
    if(*cmd == wxT('-') && cmd[1] == wxT('d')) {
        cmd += 2;
        while(*cmd == wxT(' '))
        ++cmd;
        sb = FindSwitchBoard(cmd);
        if(!sb)     // not there - nothing to do
        return;
        RemoveSwitchBoard(sb);
        curSwitchBoard = switchBoards;
        ShowSwitchboard();
        return;
    }
    sb = FindSwitchBoard(cmd);
    if(!sb)     // not there - nothing to do
        return;
    curSwitchBoard = sb;
    ShowSwitchboard();
}

void    SwitchboardCellCommand(const Char *cmd)
{
    SwitchBoard *sb = curSwitchBoard;
    int x, y;

    if(!sb)         // impossible
        return;

    Char *p;

    while(*cmd == wxT(' '))
        ++cmd;
    x = wxStrtol(cmd, &p, 10);
    if(*p == wxT(','))
        ++p;
    y = wxStrtol(p, &p, 10);
    while(*p == wxT(' '))
        ++p;
    if(!*p) {
        switchboard_cell_dialog(x, y);
        ShowSwitchboard();
        return;
    }
    // label, itinName
}

 */
}
