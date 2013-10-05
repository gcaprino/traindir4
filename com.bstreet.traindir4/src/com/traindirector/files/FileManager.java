package com.traindirector.files;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	Map<String, byte[]> _byteStreams; // mainly used for sounds

	public FileManager(Simulator simulator, String base) {
		_simulator = simulator;
		_baseFile = base;
		_byteStreams = new HashMap<String, byte[]>();

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

	public InputStream getStreamFor(String fname) {
		InputStream stream = null;
		if (_zipFile != null) {
			Path path = new Path(fname);
			String entryName = path.lastSegment();
			if(fname.startsWith(_simulator._baseDir)) {
				entryName = fname.substring(_simulator._baseDir.length() + 1);
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
						return _zipFile.getInputStream(zipEntry);
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
			stream = new FileInputStream(fname);
			return stream;
		} catch (FileNotFoundException e) {
			String fileName = _simulator._baseDir + File.separator + fname;
			try {
				stream = new FileInputStream(fileName);
			} catch (FileNotFoundException e1) {
				TextOption pathsOption = Application.getSimulator()._options._scriptsPaths;
				if(pathsOption == null || pathsOption._value == null || pathsOption._value.isEmpty())
					return null;
				String[] paths = pathsOption._value.split(";");
				for(String path : paths) {
					fileName = path + File.separator + fname;
					try {
						stream = new FileInputStream(fileName);
						return stream;
					} catch (FileNotFoundException e2) {
					}
				}
				return null;
			}
			return stream;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public BufferedReader getReaderFor(String extension) {
		String schName = asExtension(extension);
		return getReaderForFile(schName);
	}
	
	public BufferedReader getReaderForFile(String schName) {
		InputStream stream = getStreamFor(schName);
		if (stream == null)
			return null;
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		return br;
	}

	public void close() {
		if (_zipFile != null)
			try {
				_zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		_zipFile = null;
		_byteStreams.clear();
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

	public InputStream getByteStreamFor(String fname) {
		
		ByteArrayInputStream stream = null;
		byte[] content = _byteStreams.get(fname);
		if (content != null)
			return new ByteArrayInputStream(content);

		File file = new File(fname);
		if (!file.canRead())
			return null;
		
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);
			_byteStreams.put(fname, b);
			stream = new ByteArrayInputStream(b);
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}
		return stream;
	}

	public String getTextContentFor(String fname) {
		BufferedReader reader = getReaderForFile(fname);
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
