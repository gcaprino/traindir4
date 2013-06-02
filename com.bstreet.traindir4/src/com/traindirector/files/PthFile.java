package com.traindirector.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.traindirector.model.EntryExitPath;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.simulator.Simulator;

public class PthFile extends TextFile {

	Simulator _simulator;
	Territory _territory;

	public PthFile(Simulator simulator, Territory territory, String fname) {
		setFileName(fname, "pth");
	}
	
	public boolean load() {
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(_fileName));
			readFile(input);
			input.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void readFile(BufferedReader input) {
		String	line;
		EntryExitPath path = null;
		try {
			while((line = input.readLine()) != null) {
				int i;
				char ch;
				line = line.replace("\t", " ");
				i = skipBlanks(line, 0);
				if(i < 0 || line.charAt(i) == '#')
					continue;
				if(line.startsWith("Path:", i)) {
					i = skipBlanks(line, i + 5);
					if(i < 0)
						continue;
					if (path != null) {
						if (path._from == null || path._to == null || path._enter == null) {
							// TODO: report error
							System.out.println("Path is missing 'From:', 'To:' or 'Enter:'");
							continue;
						}
						_territory.addPath(path);
					}
					path = new EntryExitPath();
					continue;
				}
				if (path == null) {
					continue;
				}
				if (line.startsWith("From: ", i)) {
					i = skipBlanks(line, i + 6);
					path._from = line.substring(i);
					continue;
				}
				if (line.startsWith("To: ")) {
					i = skipBlanks(line, i + 3);
					path._to = line.substring(i);
					continue;
				}
				if (line.startsWith("Times: ")) {
					i = skipBlanks(line, i + 7);
					String[] elements = line.substring(i).split(" ");
					if (elements.length != 2) {
						System.out.println("Times line does not have times + entry: '" + line + "'");
						continue;
					}
					String[] times = elements[0].split("/");
					for (i = 0; i < times.length; ++i) {
						if (i >= Track.NSPEEDS)
							break;
						try {
							path._times[i] = Integer.parseInt(times[i]);
						} catch (Exception e) {
							e.printStackTrace();
							path._times[i] = 0;
						}
					}
					path._enter = elements[1];
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
