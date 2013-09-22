package com.traindirector.uicomponents;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.traindirector.files.TextScanner;

public class XPM {

	public int		width;
	public int		height;
	public int		nColors;
	public int		nChars;
	
	public RGB[]	colors;
	public ImageData data;
	
	@SuppressWarnings("unused")
	private XPM() {
		
	}
	
	public XPM(String[] xpm) {
		parse(xpm);
	}

	public void parse(String[] xpm) {
		int	i, j, d = 0;
		
		String[] spec = new String[4];
		TextScanner scan = new TextScanner(xpm[0]);
		for(i = 0; i < spec.length; ++i) {
			scan.skipBlanks();
			scan.scanString();
			spec[i] = scan._stringValue;
		}
		try {
			width = Integer.parseInt(spec[0]);
			height = Integer.parseInt(spec[1]);
			nColors = Integer.parseInt(spec[2]);
			nChars = Integer.parseInt(spec[3]);
			colors = new RGB[nColors];
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		char[] colorChar = new char[nColors]; 
		int transparentIndex = -1;
		int transparentRGB = 0x000000;

		for(i = 0; i < nColors; ++i) {
			int r = 0;
			int g = 0;
			int b = 0;
			String xpmLine = xpm[i + 1];
			for(j = 1; j < xpmLine.length() && xpmLine.charAt(j) != 'c'; ++j);
			while(++j < xpmLine.length() && xpmLine.charAt(j) == ' ');
			if(j >= xpmLine.length()) {
				continue;
			}
			String colName = xpmLine.substring(j);
			if(xpmLine.charAt(j) == '#') {
				int val = 0;
				try {
					val = Integer.parseInt(colName.substring(1), 16);
				} catch(Exception e) {
					e.printStackTrace();
				}
				r = val >> 16;
				g = (val >> 8) & 0xFF;
				b = (val & 0xFF);
			} else {
				if(colName.equalsIgnoreCase("green")) {
					r = 0;
					g = 255;
					b = 0;
				} else if(colName.equalsIgnoreCase("red")) {
					r = 255;
					g = 0;
					b = 0;
				} else if(colName.equalsIgnoreCase("None")) {
					r = -1;
				}
			}
			colorChar[d] = xpmLine.charAt(0);
			if(r != -1) {
				colors[d] = new RGB(r, g, b);
				int xt = (r << 16) | (g << 8) | b;
				if(transparentRGB < xt)
					transparentRGB = xt + 1;
			} else {
				transparentIndex = d;
			}
			++d;
		}
		if(transparentIndex != -1) {
			colors[transparentIndex] = new RGB((transparentRGB >> 16) & 0xff,
					(transparentRGB >> 8) & 0xFF, (transparentRGB & 0xFF));
		}
		
		PaletteData palette = new PaletteData(colors);
		data = new ImageData(width, height, 8, palette);
		data.transparentPixel = transparentIndex;
		for(i = 0; i < height; ++i) {
			int iw;
			for(iw = 0; iw < width; ++iw) {
				int iy = 1 + nColors + i;
				if(iy >= xpm.length)	// less rows than specified in xpm header
					continue;
				char cc = xpm[1 + nColors + i].charAt(iw);
				for(int xx = 0; xx < d; ++xx) {
					if(colorChar[xx] == cc) {
						data.setPixel(iw, i, xx);
						break;
					}
				}
			}
		}

	}
}
