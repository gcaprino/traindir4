package com.traindirector.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorFactory {
	
	Color bgColor;
	Color fgColor;
	Color redColor;
	Color greenColor;
	Color orangeColor;
	Color whiteColor;
	Color blackColor;

	List<Color> _colors;
	Display _display;
	
	Map<String, Color> _colorPreferences;
	
	public ColorFactory(Display display) {
		_display = display;
		bgColor = new Color(display, new RGB(128, 128, 128));
		fgColor = new Color(display, new RGB(0, 0, 0));
		blackColor = new Color(display, new RGB(0, 0, 0));
		whiteColor = new Color(display, new RGB(255, 255, 255));
		redColor = new Color(display, new RGB(255, 128, 0));
		greenColor = new Color(display, new RGB(0, 255, 0));
		orangeColor = new Color(display, new RGB(255, 128, 0));
		
		_colors = new ArrayList<Color>();
		_colorPreferences = new HashMap<String, Color>();
	}

	public Color get(int r, int g, int b) {
		RGB rgb = null;
		for (Color c : _colors) {
			rgb = c.getRGB();
			if(rgb.red == r && rgb.green == g && rgb.blue == b)
				return c;
		}
		rgb = new RGB(r, g, b);
		Color color = new Color(_display, rgb);
		_colors.add(color);
		return color;
	}

	public Color getBackgroundColor() {
		return get(192, 192, 192);
	}

	public Color set(String prefName, int r, int g, int b) {
		Color col = get(r, g, b);
		_colorPreferences.put(prefName, col);
		return col;
	}

	public void set(String prefName, int rgb) {
		_colorPreferences.put(prefName, get((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
	}

	public Color get(String pref) {
		return _colorPreferences.get(pref);
	}

}
