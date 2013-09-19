package com.traindirector.scripts;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.traindirector.commands.DoCommand;
import com.traindirector.model.Direction;
import com.traindirector.model.Signal;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Track;
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackPath;
import com.traindirector.model.Train;
import com.traindirector.simulator.PathFinder;
import com.traindirector.simulator.Simulator;

public class Interpreter {

	public static final int MAXNESTING = 10;


	boolean _forCond;
	boolean _forAddr;
	int _stackPtr;
	Stack<List<Statement>> _scopes;
	int[] _scopesIndexes;

	// run-time environment

	Track _track;
	Train _train;
	Signal _signal;
	// Itinerary _itinerary;
	
	StringBuilder expr_buff;
	
	public Interpreter() {
		_forCond = _forAddr = false;
		_stackPtr = 0;
		_scopes = new Stack<List<Statement>>();
		_scopesIndexes = new int[MAXNESTING];
		_track = null;
		_train = null;
		_signal = null;
		expr_buff = new StringBuilder();
	}

	void Execute(Statement stmt) {
		ExprValue   result = new ExprValue(NodeOp.None);
		boolean	    valid;

		List<Statement> body = stmt._body;
		int curStmt = 0;
		_scopes.push(body);
		_stackPtr = 0;
		while(curStmt < body.size()) {
			stmt = body.get(curStmt);
			expr_buff = new StringBuilder("" + stmt._lineno);	// TODO: how to clear without creating a new object?
			expr_buff.append(": ");
		    _scopesIndexes[_stackPtr] = curStmt;

		    switch(stmt._type) {
		    case 'B':
				++_stackPtr;
				body = stmt._body;
				_scopes.push(body);
				curStmt = 0;
				continue;

		    case 'I':
				try {
					expr_buff.append("if ");
				    result._op = NodeOp.None;
				    _forCond = true;
				    valid = Evaluate(stmt._expr, result);
				    if(!valid) {
						if(stmt._elseBody != null) {
							expr_buff.append(" . else");
						    Trace(expr_buff);
						    ++_stackPtr;
						    body = stmt._elseBody;
						    _scopes.push(body);
						    curStmt = 0;
						    continue;
						}
						expr_buff.append(" . false");
				    } else if(result._op == NodeOp.Number) {
						if(result._val != 0) {
							expr_buff.append(" . true");
						    Trace(expr_buff);
						    ++_stackPtr;
						    body = stmt._body;
						    _scopes.push(body);
						    curStmt = 0;
						    continue;
						}
						if(stmt._elseBody != null) {
							expr_buff.append(" . else");
						    Trace(expr_buff);
						    ++_stackPtr;
						    body = stmt._elseBody;
						    _scopes.push(body);
						    curStmt = 0;
						    continue;
						}
						expr_buff.append(" . false");
				    } else
				    	expr_buff.append(" * Result not a number");
				    Trace(expr_buff);
				} catch(Exception e) {
				    //abort();
					e.printStackTrace();
				}
				break;
	
		    case 'R':
		    	return;

		    case 'D':
				try {
					String cmd = stmt._text.replace("@", _train != null ? _train._name : "@");
					DoCommand dcmd = new DoCommand(cmd.trim());
					Simulator.INSTANCE.addCommand(dcmd);
				} catch(Exception e) {
				    e.printStackTrace();
				}
				break;

		    case 'E':
				try {
				    result._op = NodeOp.None;
				    _forCond = false;
				    expr_buff = new StringBuilder();
				    Evaluate(stmt._expr, result);
				    Trace(expr_buff);
				} catch(Exception e) {
				    e.printStackTrace();
				}
		    }
		    while(++curStmt >= body.size()) {
		    	if(_stackPtr == 0)
		    		return;
		    	body = _scopes.pop();
		    	curStmt = _scopesIndexes[--_stackPtr];
		    }
		}

	}

	private void Trace(StringBuilder buff) {
		// TODO: use preferences to enable/disable tracing
		//System.out.println(buff.toString());
	}

	boolean Evaluate(ExprNode n, ExprValue result) {
		
		ExprValue left = new ExprValue(NodeOp.None);
		ExprValue right = new ExprValue(NodeOp.None);
		
		Track track = null;
		int val;
		boolean oldForAddress;
		
		if(n == null)
			return false;
		switch(n._op) {
		
		case LinkedRef:
			if (_track == null || _track._wlink == null || _track._wlink.isNull()) {
				return false;
			}
			result._track = find_track(_track._wlink._x, _track._wlink._y);
			if(result._track == null)
				return false;
	        result._op = result._track instanceof Switch ? NodeOp.SwitchRef : NodeOp.SignalRef;
	        expr_buff.append(_track._position.toString());
	        expr_buff.append(" linked to ");
			expr_buff.append(result._track._position.toString());
	        return true;
        
		case SignalRef:
			if(n._txt != null && !n._txt.isEmpty()) {
				// signal by name
				// TODO: not implemented, yet
				System.out.println("Signal by name not implemented " + n._txt);
				return false;
			}
			
		case SwitchRef:
		case TrackRef:
			
			TraceCoord(n._x, n._y);
			if(n._x == 0 && n._y == 0) {
				result._op = n._op;
				result._track = _track;
				expr_buff.append(result._track._position.toString());
				return true;
			}

		    track = find_track(n._x, n._y);
		    if(track == null) {
		    	expr_buff.append("=no track(" + n._x + "," + n._y + ")");
		    	return false;
		    }
		    result._op = n._op;
		    if(result._op == NodeOp.SignalRef && track instanceof Signal) {
			    expr_buff.append("Signal");
		    	result._signal = (Signal )track;
		    } else {
			    expr_buff.append(track instanceof Switch ? "Switch" : "Track");
		    	result._track = track;
		    }
			expr_buff.append(track._position.toString());
		    return true;

		case TrainRef:

		    result._op = NodeOp.TrainRef;
		    if(n._txt == null || n._txt.isEmpty()) {
				if(n._x == 0 && n._y == 0) {
				    result._train = _train;
				} else if((result._train = findTrain(n._x, n._y)) == null) {
					expr_buff.append("Train(");
					expr_buff.append(n._x);
					expr_buff.append(", ");
					expr_buff.append(n._y);
					expr_buff.append(") - not found");
				    return false;
				}
				expr_buff.append("Train(");
				expr_buff.append(result._train._name);
				expr_buff.append(")");
				return true;
		    }
		    result._train = findTrainNamed(n._txt);
			expr_buff.append("Train(");
			expr_buff.append(result._train._name);
			expr_buff.append(")");
		    return result._train != null;

		case String:

		    result._op = n._op;
		    result._txt = n._txt;
		    expr_buff.append(result._txt);
		    return true;

		case Number:

		    result._op = n._op;
		    result._txt = n._txt;
		    result._val = n._val;
		    expr_buff.append(result._val);
		    return true;

		case Random:

		    result._op = NodeOp.Number;
		    /// TODO: result._val = Random.rand() % 100;
		    expr_buff.append("random ");
		    return true;

		case Time:

		    result._op = NodeOp.Number;
		    /// TODO result._val = ((current_time / 3600) % 24) * 100 + ((current_time / 60) % 60);
		    expr_buff.append("time ");
		    return true;

		case Equal:

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(_forCond) {
				
				// conditionals return false in case of expression error
	
				if(!Evaluate(n._left, left))
				    return true;
				expr_buff.append(" = ");
				if(!Evaluate(n._right, right))	    // virtual
				    return true;
	
				val = 0;
				if(left._op == right._op) {
				    switch(left._op) {
				    case String:
				    	val = left._txt.compareTo(right._txt) == 0 ? 1 : 0;
					break;
	
				    case Number:
				    	val = (left._val == right._val) ? 1 : 0;
				    }
				}
				result._op = NodeOp.Number;
				result._val = val;
				expr_buff.append("{");
				expr_buff.append(val);
				expr_buff.append("}");
				return true;
		    }
		    oldForAddress = _forAddr;
		    _forAddr = true;
		    if(!Evaluate(n._left, result)) {
				_forAddr = oldForAddress;
				return false;
		    }
		    _forAddr = oldForAddress;
		    expr_buff.append(" = ");
		    if(!Evaluate(n._right, right))	    // virtual
		    	return false;
		    switch(result._op) {
		    
		    case SignalRef:
				if(result._signal == null)
				    return false;
				result._signal.setPropertyValue(result._txt, right);
				break;

		    case TrackRef:
		    case SwitchRef:
		    case Addr:
			
				if(result._track == null)
				    return false;
				result._track.setPropertyValue(result._txt, right);
				return true;
	
			    case TrainRef:
	
				if(result._train == null)
				    return false;
				result._train.setPropertyValue(result._txt, right);
				return false;
	
		    }
		    return true;

		case NotEqual:

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(!Evaluate(n._left, left)) {
				result._val = 1;	// invalid expressions never match
				return true;
		    }
		    expr_buff.append(" != ");
		    if(!Evaluate(n._right, right))
		    	return true;
		    val = 0;
		    if(left._op == right._op) {
				switch(left._op) {
				case String:
				    val = left._txt.compareTo(right._txt) != 0 ? 1 : 0;
				    break;
	
				case Number:
				    val = left._val != right._val ? 1 : 0;
				}
		    }
		    result._val = val;
		    expr_buff.append("{");
		    expr_buff.append(val);
		    expr_buff.append("}");
		    return true;

		case Greater:

		    // conditionals return false in case of expression error

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(!Evaluate(n._left, left))
		    	return true;
		    expr_buff.append(" > ");
		    if(!Evaluate(n._right, right))
		    	return true;
		    val = 0;
		    if(left._op == right._op) {
				switch(left._op) {
				case String:
				    val = left._txt.compareTo(right._txt) > 0 ? 1 : 0;
				    break;
	
				case Number:
				    val = left._val > right._val ? 1 : 0;
				}
		    }
		    result._val = val;
		    expr_buff.append("{");
		    expr_buff.append(val);
		    expr_buff.append("}");
		    return true;

		case Less:

		    // conditionals return false in case of expression error

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(!Evaluate(n._left, left))
		    	return true;
		    expr_buff.append(" < ");
		    if(!Evaluate(n._right, right))
		    	return true;
		    val = 0;
		    if(left._op == right._op) {
				switch(left._op) {
				case String:
				    val = left._txt.compareTo(right._txt) < 0 ? 1 : 0;
				    break;
	
				case Number:
				    val = left._val < right._val ? 1 : 0;
				}
		    }
		    result._val = val;
		    expr_buff.append("{");
		    expr_buff.append(val);
		    expr_buff.append("}");
		    return true;

		case Or:

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(Evaluate(n._left, right) && right._op == NodeOp.Number && right._val != 0) {
				result._val = 1;
				return true;
		    }
		    expr_buff.append(" or ");
		    // note: invalid expressions evaluate to false (0)
		    if(Evaluate(n._right, right) && right._op == NodeOp.Number && right._val != 0)
		    	result._val = 1;
		    else
		    	result._val = 0;
		    return true;

		case And:

		    result._op = NodeOp.Number;
		    result._val = 0;
		    if(Evaluate(n._left, right) && right._op == NodeOp.Number && right._val == 0)
		    	return true;
		    expr_buff.append(" and ");
		    // note: invalid expressions evaluate to false (0)
		    if(Evaluate(n._right, right) && right._op == NodeOp.Number && right._val == 0)
		    	return true;
		    result._val = 1;
		    return true;
		    
		    //
		    //
		    //
		    
		case NextSignalRef:
			
			if(_signal == null)
				return false;
			Signal nextSignal = getNextSignal(_signal);
			if (nextSignal == null)
				return false;
			result._op = NodeOp.SignalRef;
			result._signal = nextSignal;
		    expr_buff.append("NextSignal");
		    expr_buff.append(result._signal._position.toString());
			return true;

		case NextApproachRef:
			
			if (_signal == null)
				return false;
			expr_buff.append("NextApproach");
			expr_buff.append(_signal._position.toString());
			if (!getApproach(_signal, result)) {
				expr_buff.append(" - not found ");
				return false;
			}
			result._op = NodeOp.SignalRef;
			expr_buff.append(" - found ");
		    expr_buff.append(result._signal._position.toString());
			return true;

		case Dot:
			
			result._op = NodeOp.Addr;
			if (n._left == null) {
				// TODO: use Interpreter._op to know the default object
				if (this._signal != null) {
					result._signal = this._signal;	// .property -> this._signal
					result._op = NodeOp.SignalRef;
				} else if (this._train != null) {
					result._train = this._train;
					result._op = NodeOp.TrainRef;
				} else if (this._track != null) {
					result._track = this._track;
					result._op = NodeOp.TrackRef;
				}
			} else if(n._left != null && n._left._op == NodeOp.Dot) {
				boolean oldForAddr = _forAddr;
				_forAddr = true;
				if(!Evaluate(n._left, result)) {	// <signal>.<property>
					_forAddr = oldForAddr;
					return false;
				}
				_forAddr = oldForAddr;
				//if (result._op != NodeOp.SignalRef) {
					//return false;
				//}
			} else {
				if(!Evaluate(n._left, result))
					return false;
			}
			expr_buff.append(".");
			if(n._right != null) {
				switch(n._right._op) {
				// TODO: LinkedRef?
				case SignalRef:
				case NextSignalRef:
					result._signal = getNextSignal(result._signal);
					if(result._signal == null) {
						// No next signal for .
						return false;
					}
				    expr_buff.append(n._right._op == NodeOp.SignalRef ? "Signal" : "NextSignal");
				    expr_buff.append(result._signal._position.toString());
					break;
					
				case NextApproachRef:
					if(!getApproach(result._signal, result)) {
						return false;
					}
					result._op = NodeOp.SignalRef;
				    expr_buff.append("NextApproach");
				    expr_buff.append(result._signal._position.toString());
					break;
				
				case LinkedRef:
                    if(result._track == null) {
                        return false;
                    }
                    result._signal = null;
                    Track linkedTrack = result._track._wlinkTrack;
                    if(linkedTrack == null) {
                    	expr_buff.append("no linked track for " + result._track._position.toString());
                    	return false;
                    }
                	expr_buff.append("linked to " + result._track._position.toString());
                    if(linkedTrack instanceof Signal) {
                    	result._signal = (Signal)linkedTrack;
                    	result._op = NodeOp.SignalRef;
                    } else {
	                    result._track = linkedTrack;
	                    result._op = NodeOp.TrackRef;
                    }
				}
			}
			result._txt = (n._right != null && n._right._txt != null) ? n._right._txt : n._txt;
			String property = result._txt;
			if(_forAddr) {
				if (property != null)
					expr_buff.append("." + property);
				return true;
			}
			if (property == null || property.isEmpty())
				return false;
			expr_buff.append(property);
			switch(result._op) {
			case SwitchRef:
				if(property.compareTo("thrown") == 0) {
					result._op = NodeOp.Number;
					if(result._track == null || !(result._track instanceof Switch))
						result._val = 0;
					else {
						Switch sw = (Switch)result._track;
						result._val = sw.isThrown() ? 1 : 0;
					}
					return true;
				}
			
			case Addr:
			case TrackRef:
				if (result._track == null)
					return false;
				return result._track.getPropertyValue(property, result);
				
			case SignalRef:
				if (result._signal == null)
					return false;
				return result._signal.getPropertyValue(property, result);
				
			case TrainRef:
				if (result._train == null)
					return false;
				return result._train.getPropertyValue(property, result);
			}
		    return false;
		}
		return false;
	}
	
	private Signal getNextSignal(Signal signal) {
		Signal	sig = signal;
		Track	t;
		Track t1;
		Direction dir;

		if(sig._controls == null)
		    return null;

		PathFinder finder = new PathFinder();
		TDPosition position = signal._controls._position;
		TrackPath path = finder.find(position, signal.getDirectionFrom(signal._direction));
		if(path == null)
		    return null;
		t = path.getTrackAt(path.getTrackCount(0) - 1);
		dir = path.getDirectionAt(path.getTrackCount(0) - 1);

		dir = t.walk(dir);
		TDPosition nextPos = dir.offset(t._position);
		t1 = Simulator.INSTANCE._territory.findTrack(nextPos);
		if(t1 == null)
			return null;
		signal = Simulator.INSTANCE._territory.findSignalLinkedTo(t1, dir);
		if (signal == null)
			return null;
//		wxSnprintf(expr_buff + wxStrlen(expr_buff), sizeof(expr_buff)/sizeof(wxChar) - wxStrlen(expr_buff),
//		    wxT("(%d,%d)"), sig->x, sig->y);
		return signal;
	}

	private boolean getApproach(Signal signal, ExprValue result) {
		return signal.getApproach(result);
	}

	private Train findTrainNamed(String name) {
		return Simulator.INSTANCE._schedule.findTrainNamed(name);
	}

	private Train findTrain(int x, int y) {
		TDPosition pos = new TDPosition(x, y);
		return Simulator.INSTANCE._schedule.findTrainAt(pos);
	}

	private Track find_track(int x, int y) {
		TDPosition pos = new TDPosition(x, y);
		Track track = Simulator.INSTANCE._territory.findTrack(pos);
		if (track == null)
			track = Simulator.INSTANCE._territory.findSwitch(pos);
		return track;
	}

	private void TraceCoord(int _x, int _y) {
		// TODO Auto-generated method stub
		
	}

	void TraceCoord(int x, int y, String label) {

	}
	
	

}
