package com.traindirector.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XPMFile {

	String _path;
	String[] _lines;

	public XPMFile(String path) {
		_path = path;
	}
	
	public String[] getLines() {
		return _lines;
	}
	
	public boolean load() {
		File f = new File(_path);
		if (!f.canRead())
			return false;
		
		List<String> lines = new ArrayList<String>();
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(f));
			String line = null;
			while((line = input.readLine()) != null) {
				int j;
				for (j = 0; j < line.length() && line.charAt(j) != '"'; ++j);
				if (++j >= line.length())
					continue;
				int k = j;
				while(k < line.length() && line.charAt(k) != '"')
					++k;
				lines.add(line.substring(j, k));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null)
					input.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		_lines = new String[lines.size()];
		lines.toArray(_lines);
		return true;

		/*
		wxImage	*img = 0;

		// now analyze the lines to check if the image is correct

		int	nRows, nColumns, nColors, depth, x, y, c;
		if(sscanf(pattern[0], "%d %d %d %d", &nColumns, &nRows, &nColors, &depth) != 4) {
		    wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("Error loading '%s' - not a valid XPM file."), fname);
		    traindir->layout_error(buff);
		    goto done;
		}
		if(nRows > i - 1 - nColors) {
		    char	cbuff[256];
		    wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("%s: Warning: too many lines in XPM header. Truncated."), fname);
		    traindir->layout_error(buff);
		    snprintf(cbuff, sizeof(cbuff)/sizeof(char), "%d %d %d %d", nColumns, i - 1 - nColors, nColors, depth);
		    free(pattern[0]);
		    pattern[0] = strdup(cbuff);
		}
		for(y = nColors + 1; y < i; ++y) {  // check each pixel row
		    for(x = 0; x < nColumns; ++x) {
			bool valid = false;
			if(!pattern[y][x])
			    break;
			for(c = 0; c < nColors; ++c) {
			    if(pattern[c + 1][0] == pattern[y][x]) {
				valid = true;
				break;
			    }
			}
			if(!valid) {
			    pattern[y][x] = pattern[1][0];  // force first color (hopefully "None")
			    wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("%s: Warning: bad color key (y=%d,x=%d). Replaced."), fname, y, x);
			    traindir->layout_error(buff);
			}
		    }
		}
		try {
		    img = new wxImage(pattern);
		    if(!img->Ok()) {
			wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("Error loading '%s'"), fname);
			traindir->layout_error(buff);
			delete img;
			img = 0;
		    }
		} catch(...) {
		    wxSnprintf(buff, sizeof(buff)/sizeof(wxChar), wxT("Error loading '%s' - not a valid XPM file."), fname);
		    traindir->layout_error(buff);
		}
	done:
		for(i = 0; pattern[i]; ++i)
		    free(pattern[i]);
		free(pattern);
		return (void *)img;
*/
	}
}
