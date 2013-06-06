package com.traindirector.files;

import java.io.BufferedReader;
import java.io.IOException;

public class CSVFile extends TextFile {

	public String[] columnNames;
	public String[] columnValues;
	BufferedReader _rdr;
	
	public CSVFile(BufferedReader rdr) {
		_rdr = rdr;
	}
	
	public boolean readColumns() {
		String line;
		try {
			line = _rdr.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			line = null;
		}
		if (line == null)
			return false;
		// TODO: CSV escaped values
		columnNames = line.split(",");
		return true;
	}

	public boolean readLine() {
		String line;
		try {
			line = _rdr.readLine();
			if(line == null)
				return false;
			// TODO: CSV escaped values
			columnValues = line.split(",");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int findColumn(String name) {
		for(int i = 0; i < columnNames.length; ++i) {
			if (columnNames[i].equals(name))
				return i;
		}
		return -1;
	}

	public String getValue(int indx) {
		if (indx < columnValues.length)
			return columnValues[indx];
		return "?";
	}

	public int getValueHex(int index) {
		if(index >= columnValues.length)
			return 0;
		String val = columnValues[index];
		if(val.charAt(0) != '#' && !val.startsWith("0x"))
			val = "0x" + val;
		return Integer.decode(val);
	}

	public boolean getValueAsBool(int index) {
		if(getValueHex(index) != 0)
			return true;
		return false;
	}

}
