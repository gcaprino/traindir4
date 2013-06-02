package com.traindirector.uicomponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;

public class WebContent {

    public File getResourceFile(String filePath) {
        try {
        	URL res = getClass().getResource(filePath);
        	if (res == null)
        		return null;
            URL path = FileLocator.toFileURL(res);
            if (path != null && path.getFile() != null) {
                File scriptfile = new File(path.getFile());
                if (scriptfile.exists()) {
                    return scriptfile;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getFileContent(File file) {
    	List<String> content = new ArrayList<String>();
    	if (file == null)
    		return content;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null) {
            	content.add(line);
            }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return content;
    }

    public String replaceContent(List<String> input, Map<String, String> values) {
        StringBuffer output = new StringBuffer();

        for (String line : input) {
            for (String k : values.keySet()) {
                String value = values.get(k);
                int index = line.indexOf(k);
                if (index < 0)
                    continue;
                String t = line.substring(0, index) + value;
                t += line.substring(index + k.length());
                line = t;
            }
            output.append(line);
        }
        return output.toString();
    }

	public boolean doLink(String location) {
		return false;
	}

	public String getHTML() {
		return "<html><body>Empty page.</body></html>";
	}

}
