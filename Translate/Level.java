package Translate;

import Symbol.Symbol;
import Util.*;

public class Level {
	public Level parent;
	public Frame.Frame frame;
	public AccessList formals = null;
	
	public Level(Level parent, Symbol name, BoolList fmls)
	{
		this.parent = parent;
		BoolList bl = new BoolList(true, fmls);
		this.frame = parent.frame.newFrame(new Temp.Label(name), bl);
		for (Frame.AccessList f = frame.formals; f != null; f = f.next)
			this.formals = new AccessList(new Access(this, f.head), this.formals);
	}
	public Level(Frame.Frame frm)
	{
		this.frame = frm;	this.parent = null;
	}
	public Access staticLink() 
	{
		return formals.head;
	}
	public Access allocLocal(boolean escape)
	{
		return new Access(this, frame.allocLocal(escape));	
	}
}
