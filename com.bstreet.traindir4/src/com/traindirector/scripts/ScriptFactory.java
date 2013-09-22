package com.traindirector.scripts;

import java.util.HashMap;
import java.util.Map;

public class ScriptFactory {

	Map<String, Script> _scripts;
	
	public ScriptFactory() {
		_scripts = new HashMap<String, Script>();
	}
	
	public void clear() {
		_scripts.clear();
	}
	
	public Script createInstance(String fname) {
		Script script = _scripts.get(fname);
		if (script != null)
			return script;
		if(fname.endsWith(".tds"))
			script = new TDSScript(fname);
		else
			script = new Script(fname);
		_scripts.put(fname, script);
		return script;
	}

}
