package com.traindirector.dialogs;

import org.eclipse.swt.graphics.Color;

import com.traindirector.options.Option;

public class ColorOption extends Option {

	
	public Color _color;

	public ColorOption(String name, String descr, int r, int g, int b) {
		super(name, descr);
		_intValue = (r << 16) | (g << 8) | b;
		_color = null;
	}

}
