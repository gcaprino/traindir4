package com.traindirector.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.traindirector.model.Switchboard;
import com.traindirector.simulator.Simulator;

public class SwbFile extends TextFile {

	Simulator _simulator;
	Switchboard _switchboard;

	public SwbFile(Simulator simulator) {
		_simulator = simulator;
	}
	
	public void readFile(String fname) {
		BufferedReader input;
		try {
			_switchboard = _simulator.createSwitchboard(fname);
			input = _simulator._fileManager.getReaderForFile(fname + ".swb");
			if (input != null) {
				readFile(input);
				input.close();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readFile(BufferedReader input) {
		String	line;
		try {
			line = input.readLine();
			while(line != null) {
				if(line.startsWith("Name:")) {
					_switchboard._name = line.substring(5).trim();
					line = input.readLine();
				} else if(line.startsWith("Cell:")) {
					line = _switchboard.parseCell(input, line.substring(6).trim());
				} else if(line.startsWith("Aspect:")) {
					line = _switchboard.parseAspect(input, line.substring(6).trim());
				} else {
					line = input.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveFile(BufferedWriter out) {
		for(Switchboard sw : _simulator._switchboards) {
//			out.append("Layout: " + trkFileName + eol);
//			out.append(String.format("CurrentTimeMultiplier:%d\nStartTime:%ld\nShowSpeeds:%d\nShowBlocks:%d\nBeenOnAlert:%d\nRunPoints:%d\nTotalDelay:%d\nTotalLate:%d\nTimeMultiplier:%d\nSimulatedTime:%ld\n",

		}
		/*
void	SaveSwitchBoards(wxFFile& file)
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


		 */
	}
}
