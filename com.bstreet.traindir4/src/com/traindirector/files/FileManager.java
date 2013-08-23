package com.traindirector.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.Path;

import com.traindirector.Application;
import com.traindirector.dialogs.TextOption;
import com.traindirector.simulator.Simulator;

public class FileManager {

	String	_baseFile;
	ZipFile _zipFile;
	Simulator _simulator;
	Map<String, String> _entriesMap; // map original name to lower case name 
	
	public FileManager(Simulator simulator, String base) {
		_simulator = simulator;
		_baseFile = base;
		if (base.endsWith(".zip" ) || base.endsWith(".ZIP")) {
			try {
				_zipFile = new ZipFile(base);
				listAllEntries();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				_zipFile = null;
			} catch (IOException e) {
				e.printStackTrace();
				_zipFile = null;
			}
		}
		// TODO: add .rar,  .tar,   .tgz
	}

	public String asExtension(String ext) {
		if(ext.charAt(0) == '.')
			ext = ext.substring(1);
		String fname = _baseFile;
		int indx = fname.lastIndexOf('.');
		if (indx >= 0) {
			fname = fname.substring(0, indx + 1) + ext;
		}
		return fname;
	}

	public BufferedReader getReaderFor(String extension) {
		String schName = asExtension(extension);
		return getReaderForFile(schName);
	}
	
	public BufferedReader getReaderForFile(String schName) {
		BufferedReader input = null;
		if (_zipFile != null) {
			Path path = new Path(schName);
			String entryName = path.lastSegment();
			if(schName.startsWith(_simulator._baseDir)) {
				entryName = schName.substring(_simulator._baseDir.length() + 1);
				entryName = entryName.replace("\\", "/");
			}
			String realName = _entriesMap.get(entryName.toLowerCase());
			if (realName == null) {
				System.out.println("Not found entry " + entryName + " in map for file " + _zipFile.getName());
//				return null;
			} else {
				ZipEntry zipEntry = _zipFile.getEntry(realName);
				if (zipEntry != null) {
					try {
						InputStream inputStream = _zipFile.getInputStream(zipEntry);
						BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
						return br;
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Not found entry " + realName + " in file " + _zipFile.getName());
					//return null;
				}
			}
		}
		try {
			input = new BufferedReader(new FileReader(schName));
			return input;
		} catch (FileNotFoundException e) {
			String fileName = _simulator._baseDir + File.separator + schName;
			try {
				input = new BufferedReader(new FileReader(fileName));
			} catch (FileNotFoundException e1) {
				TextOption pathsOption = Application.getSimulator()._options._scriptsPaths;
				if(pathsOption == null || pathsOption._value == null || pathsOption._value.isEmpty())
					return null;
				String[] paths = pathsOption._value.split(";");
				for(String path : paths) {
					fileName = path + File.separator + schName;
					try {
						input = new BufferedReader(new FileReader(fileName));
						return input;
					} catch (FileNotFoundException e2) {
					}
				}
				return null;
			}
			return input;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void close() {
		if (_zipFile != null)
			try {
				_zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		_zipFile = null;
	}

	/*
	 * We need to keep a lower case version of each entry's name,
	 * so that we can find the entry regardless on how it is spelled
	 * in the referring file (e.g. image.XPM vs. image.xpm) 
	 */

	private void listAllEntries() {
		_entriesMap = new HashMap<String, String>();
		Enumeration<? extends ZipEntry> e = _zipFile.entries();
		while(e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			_entriesMap.put(entry.getName().toLowerCase(), entry.getName());
		}
	}
}
