package com.traindirector.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.traindirector.model.Signal;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.simulator.Simulator;

public class Script {

	String _name;							// could be null for scripts embedded in the .trk file
	public String _body;
	Map<String, Statement> _handlers;		// handlers for the various events
	
	public Script(String name) {
		_name = name;
	}
	
	public boolean load() {
		BufferedReader input = null;
		String line;
		StringBuilder sb = new StringBuilder();

		String scriptPath = Simulator.INSTANCE.getFilePath(_name);
		if (scriptPath == null)
			return false;
		try {
			input = new BufferedReader(new FileReader(scriptPath));
			while((line = input.readLine()) != null) {
				sb.append(line.replace('\t', ' '));
				sb.append('\n');
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		_body = sb.toString();
		return true;
	}
	
	public int skipBlank(String s, int offset) {
		while (offset < s.length() && (s.charAt(offset) == ' '))
			++offset;
		return offset;
	}

	public int skipToNextToken(String s, int offset) {
		while (offset < s.length()) {
			char ch = s.charAt(offset);
			if (ch == '#') {
				// skip to end of line
				while (offset < s.length() && ch != '\n') {
					ch = s.charAt(++offset);
				}
				continue;
			}
			if (ch != ' ' && ch != '\n')
				break;
			++offset;
		}
		return offset;
	}

	// parse a script that has been loaded into memory in the _body field.
	// This is typically for a Track or Trigger.
	// Signals override this method to load the script from a file
	// and parse the aspects before parsing the handlers

	public boolean parse() {
		_handlers = parseHandlers(0);
		return _handlers != null;
	}

	// _body must already be loaded, either from a file (a .tds file)
	// or from the .trk file (a track script)
	
	public Map<String, Statement> parseHandlers(int offset) {
		Map<String, Statement> handlers = new HashMap<String, Statement>();
		while(offset < _body.length()) {
			StringBuilder line = new StringBuilder();
			String s;
			offset = scanLine(_body, offset, line);
			s = line.toString();
			if(s.endsWith(":")) {
				String handlerName = s.substring(0, s.length() - 1);	// remove end ':'
				Statement handlerBody = new Statement();
				offset = parse(_body, offset, handlerBody);
				handlers.put(handlerName, handlerBody);
			}
		}
		return handlers;
	}
	
	public int parse(String s, int offset, Statement body) {
		Statement stmt = null;
		
		body._type = 'B';
		while(offset < s.length()) {
			offset = skipBlank(s, offset);
			if (offset >= s.length())
				break;
			if (s.charAt(offset) == '\n') {
				++offset;
				continue;
			}
			if (s.startsWith("if", offset)) {
				offset = skipBlank(s, offset + 2);
				stmt = addStatement(body);
				stmt._type = 'I';
				offset = parseExpression(stmt, s, offset);	// fill stmt._expr
				body = stmt;
			} else if(s.startsWith("else", offset)) {
				offset = skipToNextToken(s, offset + 4);
				do {
					if (body._type != 'I')
						return -1;
					if (!body._isElse)
						break;
				} while ((body = body._parent) != null);
				if (body == null)
					return -1;
				body._isElse = true;
			} else if(s.startsWith("end", offset)) {
				offset += 3;
				if (body._parent == null)
					break;
				body = body._parent;
				offset = skipToNextToken(s, offset);
			} else if(s.startsWith("return", offset)) {
				stmt = addStatement(body);
				stmt._type = 'R';
				offset = skipToNextToken(s, offset + 6);
			} else if(s.startsWith("do", offset)) {
				stmt = addStatement(body);
				stmt._type = 'D';
				StringBuilder sb = new StringBuilder();
				offset = scanLine(s, offset + 2, sb);
				stmt._text = sb.toString();
			} else {		// treat as an expression
				stmt = addStatement(body);
				stmt._type = 'E';
				offset = parseExpression(stmt, s, offset);
				offset = scanLine(s, offset, null);	// ignore illegal characters or EOL after expression
			}
		}
		return offset;
	}

	protected int scanLine(String s, int i, StringBuilder sb) {
		while(i < s.length()) {
			char ch = s.charAt(i);
			++i;
			if (ch == '\n') {
				break;
			}
			if(sb != null)
				sb.append(ch);
		}
		return i;
	}

	private int parseExpression(Statement stmt, String s, int offset) {
		ExprNode n = null, n1 = null, n2 = null;
		ExprNode root = null;
		
		while (offset < s.length()) {
			n = new ExprNode(NodeOp.None);
			int newOffset = parseToken(s, offset, n);
			if (newOffset < 0)
				break;
			offset = newOffset;
			switch(n._op) { 
			
			case TrackRef:
			case TrainRef:
			case SwitchRef:
			case SignalRef:
				n._txt = null;
				n._x = n._y = 0;
				if(s.charAt(offset) == '.') {
					if(root == null)
						root = n;
					break;
				}
				StringBuilder sb = new StringBuilder();
				offset = scanWord(s, offset, sb);
				if(!sb.toString().startsWith("(")) {
					// TODO: error: expected (
					return -1;
				}
				n1 = new ExprNode(NodeOp.None);
				offset = parseToken(s, offset, n1);
				if (n1._op == NodeOp.String) {
					n._txt = n1._txt;
					n1 = null;
				} else {
					if (n1._op != NodeOp.Number) {
						// TODO: error: expected number
						return -1;
					}
					sb = new StringBuilder();
					offset = scanWord(s, offset, sb);
					if (!sb.toString().startsWith(",")) {
						// TODO: error: expected ,
						return -1;
					}
					n2 = new ExprNode(NodeOp.None);
					offset = parseToken(s, offset, n2);
					if (n2._op != NodeOp.Number) {
						// TODO: error: expected y number
						return -1;
					}
					n._x = n1._val;
					n._y = n2._val;
					n1 = null;
					n2 = null;
				}
				if (root == null)
					root = n;
				sb = new StringBuilder();
				offset = scanWord(s, offset, sb);
				if (!sb.toString().startsWith(")")) {
					// TODO: error: expected )
					return -1;
				}
				break;
				
			case NextSignalRef:
			case NextApproachRef:
				
				if (root == null)
					root = n;
				break;
				
			case Dot:
				if (root == null) {
					// TODO: error: missing left reference
					n2 = new ExprNode(NodeOp.None);
					offset = parseToken(s, offset, n2);
					if (n2._op == NodeOp.None) {
						return -1;
					}
				} else {
					switch(root._op) {
					case TrackRef:
					case SwitchRef:
					case SignalRef:
					case NextSignalRef:
					case NextApproachRef:
					case TrainRef:
					case Dot:
						
						break;
						
					default:
						
						// TODO: error: invalid . for left expression
						return -1;
					}
					n2 = new ExprNode(NodeOp.None);
					offset = parseToken(s, offset, n2);
					if(n2._op == NodeOp.None) {
						return -1;
					}
					if(n2._op == NodeOp.NextSignalRef || n2._op == NodeOp.NextApproachRef) {
						n._left = root;
						n._right = n2;
						n2._txt = (n2._op == NodeOp.NextSignalRef) ? "next" : "nextApproach";
						root = n;
						continue;
					}
					if(n2._op != NodeOp.String) {
						// TODO: error: right of . must be a name
						return -1;
					}
				}
				n._left = root;
				n._right = n2;
				root = n;
				break;
				
			case Equal:
			case NotEqual:
			case Greater:
			case Less:
			case GreaterEqual:
			case LessEqual:
				
				if (root == null) {
					//TODO: error: missing left expression
					return -1;
				}
				n2 = new ExprNode(NodeOp.None);
				offset = parseToken(s, offset, n2);
				if(n2._op == NodeOp.None) {
					return -1;
				}
				n._left = root;
				n._right = n2;
				root = n;
				break;
				
			case And:
			case Or:
				
				if (root == null) {
					// TODO: error: missing left expression
					return -1;
				}
				offset = parseExpression(stmt, s, offset);
				n2 = stmt._expr;
				if(n2 == null || n2._op == NodeOp.None) {
					return -1;
				}
				n._left = root;
				n._right = n2;
				root = n;
				stmt._expr = root;
				return offset;
				
			default:
				if(root == null)
					root = n;
			}
		}
		stmt._expr = root;
		return offset;
	}

	private int parseToken(String s, int offset, ExprNode n) {
		StringBuilder sb = new StringBuilder();
		String	word;
		
		offset = scanWord(s, offset, sb);
		word = sb.toString();
		if (word.startsWith("Switch", offset)) {
			n._op = NodeOp.SwitchRef;
		} else if(word.startsWith("Track")) {
			n._op = NodeOp.TrackRef;
		} else if(word.startsWith("Signal")) {
			n._op = NodeOp.SignalRef;
		} else if(word.startsWith("Train")) {
			n._op = NodeOp.TrainRef;
		} else if(word.startsWith("next")) {
			n._op = NodeOp.NextSignalRef;
		} else if(word.startsWith("nextApproach")) {
			n._op = NodeOp.NextApproachRef;
		} else if(word.startsWith("and")) {
			n._op = NodeOp.And;
		} else if(word.startsWith("or")) {
			n._op = NodeOp.Or;
		} else if(word.startsWith("random")) {
			n._op = NodeOp.Random;
		} else if(word.startsWith("time")) {
			n._op = NodeOp.Time;
		} else if(word.startsWith("=")) {
			n._op = NodeOp.Equal;
		} else if(word.startsWith("!")) {
			n._op = NodeOp.NotEqual;
		} else if(word.startsWith(">")) {
			n._op = NodeOp.Greater;
		} else if(word.startsWith("<")) {
			n._op = NodeOp.Less;
		} else if(word.startsWith(".")) {
			n._op = NodeOp.Dot;
		} else if(word.charAt(0) >= '0' && word.charAt(0) <= '9') {
			n._op = NodeOp.Number;
			n._val = Integer.parseInt(word);
		} else if(isAlnum(word.charAt(0))) {
			n._op = NodeOp.String;
			n._txt = word;
		} else
		    return -1;
		return offset;
	}

	private int scanWord(String s, int offset, StringBuilder sb) {
		char ch;
		offset = skipBlank(s, offset);
		if (offset >= s.length())
			return offset;
		ch = s.charAt(offset);
		if (isAlnum(ch) || ch == '@') {
			while(offset < s.length() && isAlnum(ch = s.charAt(offset)) || ch == '@') {
				sb.append(s.charAt(offset));
				offset++;
			}
			offset = skipBlank(s, offset);
			return offset;
		}
		if(ch >= '0' && ch <= '9') {
			while(offset < s.length() && (ch = s.charAt(offset)) >= '0' && ch <= '9') {
				sb.append(ch);
				++offset;
			}
			return skipBlank(s, offset);
		}
		sb.append(ch);
		return ++offset;
	}

	private boolean isAlnum(char c) {
		if(c >= 'A' && c <= 'Z') return true;
		if(c >= 'a' && c <= 'z') return true;
		if(c >= '0' && c <= '9') return true;
		if(c == '_') return true;
		return false;
	}

	private Statement addStatement(Statement body) {
		Statement s = new Statement();
		if (body._isElse)
			body.addElseStatement(s);
		else
			body.addStatement(s);
		s._parent = body;
		return s;
	}

	public boolean handle(String key, Track track, Train train) {
		if(_handlers == null)
			return false;
		Statement stmt = _handlers.get(key);
		if(stmt == null)
			return false;
		Interpreter interpreter = new Interpreter();
		if (track instanceof Signal)
			interpreter._signal = (Signal)track;
		else
			interpreter._track = track;
		interpreter._train = train;
		interpreter.Execute(stmt);
		return true;
	}
}
