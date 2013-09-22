package com.traindirector.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZippedFile {

	String _fname;
	ZipFile _zipFile;
	List<ZipEntry> _entries;

	public ZippedFile(String fname) {
		_fname = fname;
	}

	public void load() {
		_entries = new ArrayList<ZipEntry>();
		try {
			_zipFile = new ZipFile(_fname);
			ZipInputStream zis = new ZipInputStream(new FileInputStream(_fname));
			System.out.println("Gathering zip entries...");
			byte[] buffer = new byte[4096];
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				if (ze.isDirectory()) { // Ignore directory-only entries stored
										// in archive.
					continue;
				}
				_entries.add(ze);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
