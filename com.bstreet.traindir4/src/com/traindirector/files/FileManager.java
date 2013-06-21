package com.traindirector.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.Path;

import com.traindirector.simulator.Simulator;

public class FileManager {

	String	_baseFile;
	ZipFile _zipFile;
	Simulator _simulator;
	
	public FileManager(Simulator simulator, String base) {
		_simulator = simulator;
		_baseFile = base;
		if (base.endsWith(".zip" ) || base.endsWith(".ZIP")) {
			try {
				_zipFile = new ZipFile(base);
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
			ZipEntry zipEntry = _zipFile.getEntry(entryName);
			if (zipEntry != null) {
				try {
					InputStream inputStream = _zipFile.getInputStream(zipEntry);
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					return br;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Not found entry " + entryName + " in file " + _zipFile.getName());
				return null;
			}
		}
		try {
			input = new BufferedReader(new FileReader(schName));
			return input;
		} catch (FileNotFoundException e) {
			return null;
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
}
