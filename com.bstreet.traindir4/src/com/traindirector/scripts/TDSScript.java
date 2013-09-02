package com.traindirector.scripts;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.model.SignalAspect;

public class TDSScript extends Script {

	List<SignalAspect> _aspects;

	public TDSScript(String name) {
		super(name);
		_aspects = new ArrayList<SignalAspect>();
	}

	public boolean parse() {
		if(!load())
			return false;
		int offset = 0;
		_lineno = 1;
		String s = _body;
		while(offset < s.length()) {
			offset = skipBlank(s, offset);
			String sub = s.substring(offset);
			if (s.charAt(offset) == '#') {
				offset = scanLine(s, offset, null);
				continue;
			}
			if(s.startsWith("Aspect:", offset)) {
				offset = parseAspect(s, offset + 7);
				continue;
			}
			if(s.startsWith("OnInit:", offset) ||
				s.startsWith("OnCleared:", offset) ||
				s.startsWith("OnClick:", offset) ||
				s.startsWith("OnShunt:", offset) ||
				s.startsWith("OnUpdate:", offset) ||
				s.startsWith("OnAuto:", offset) ||
				s.startsWith("OnCross:", offset)) {
				_handlers = parseHandlers(offset);
				break;
			} else
				offset = scanLine(s, offset, null);
		}
		return _handlers != null;
	}
	
	public int parseAspect(String s, int offset) {
		StringBuilder sb = new StringBuilder();
		offset = skipBlank(s, offset);
		offset = scanLine(s, offset, sb);
		SignalAspect aspect = new SignalAspect();
		aspect._name = sb.toString();
		_aspects.add(aspect);
		while(offset < s.length()) {
			sb = new StringBuilder();
			offset = skipBlank(s, offset);
			offset = scanLine(s, offset, sb);
			String cmd = sb.toString();
			if(cmd.startsWith("IconE:")) {
				aspect._iconE = cmd.substring(6).trim().split(" ");
			} else if(cmd.startsWith("IconW:")) {
				aspect._iconW = cmd.substring(6).trim().split(" ");
			} else if(cmd.startsWith("IconN:")) {
				aspect._iconN = cmd.substring(6).trim().split(" ");
			} else if(cmd.startsWith("IconS:")) {
				aspect._iconS = cmd.substring(6).trim().split(" ");
			} else if(cmd.startsWith("Action:")) {
				aspect._action = cmd.substring(7).trim();
			} else
				break;
		}
		return offset;
	}

	public int parseAspects(String s, int offset) {
		while(offset < s.length()) {
			offset = skipBlank(s, offset);
			if (s.charAt(offset) == '#') {
				offset = scanLine(s, offset, null);
				continue;
			}
			if(s.startsWith("Aspect:", offset)) {
				offset = parseAspect(s, offset + 7);
				continue;
			}
			offset = scanLine(s, offset, null);
		}
		return offset;
	}
	
	public List<SignalAspect> getAspects() {
		return _aspects;
	}
}
