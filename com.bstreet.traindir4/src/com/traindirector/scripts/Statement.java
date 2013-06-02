package com.traindirector.scripts;

import java.util.ArrayList;
import java.util.List;

public class Statement {

	public Statement() {
		_parent = null;
		_isElse = false;
		_type = 0;
		_text = null; // todo: remove?
		_expr = null;
		_body = null;
		_elseBody = null;
	}

	public void dispose() {
		_parent = null;
		_text = null;
		_expr = null;
		if (_body != null)
			_body.clear();
		if (_elseBody != null)
			_elseBody.clear();
	}

	public void addStatement(Statement s) {
		if (_body == null)
			_body = new ArrayList<Statement>();
		_body.add(s);
	}

	public void addElseStatement(Statement s) {
		if (_elseBody == null)
			_elseBody = new ArrayList<Statement>();
		_elseBody.add(s);
	}

	List<Statement> _body, _elseBody;
	Statement _parent;
	int _type;
	boolean _isElse;
	String _text;
	ExprNode _expr;

}
