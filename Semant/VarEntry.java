package Semant;

import Types.*;

public class VarEntry extends Entry{
	Type Ty;
	Translate.Access acc;
	boolean isFor;
	
	public VarEntry(Type ty, Translate.Access acc){ Ty = ty; this.acc = acc; this.isFor=false; }
	public VarEntry(Type ty, Translate.Access acc, boolean isf){ Ty = ty; this.acc = acc; this.isFor=isf; }
}


