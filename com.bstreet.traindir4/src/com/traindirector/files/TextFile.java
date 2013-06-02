package com.traindirector.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class TextFile {
	
	protected int _intValue;
	protected String _stringValue;
	protected String _fileName;
	
	public TextFile() {
	}
	
	public void setFileName(String path, String ext) {
		IPath p = new Path(path);
		p = p.removeFileExtension();
		p = p.addFileExtension(ext);
		_fileName = p.toOSString();
	}
	
	public List<String> loadFileContent() {
		String line;
		List<String> lines = new ArrayList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(_fileName));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (FileNotFoundException e) {
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	public int scanInteger(String input, int offset) {
		_intValue = 0;
		StringBuilder val = new StringBuilder();
		char ch;
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch < '0' || ch > '9')
				break;
			val.append(ch);
			++offset;
		}
		if(val.length() < 1) {
			return offset;
		}
		_intValue = Integer.parseInt(val.toString());
		return skipBlanks(input, offset);
	}

	public int scanString(String input, int offset) {
		StringBuilder val = new StringBuilder();
		char ch;
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch == ' ' || ch == '\t')
				break;
			val.append(ch);
			++offset;
		}
		_stringValue = val.toString();
		if(val.length() < 1) {
			return offset;
		}
		return skipBlanks(input, offset);
	}

	public static int skipBlanks(String input, int offset) {
		while(offset < input.length()) {
			char ch = input.charAt(offset);
			if(ch != ' ' && ch != '\t')
				return offset;
			++offset;
		}
		if(offset >= input.length())
			return -1;
		return offset;
	}

}
