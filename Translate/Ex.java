package Translate;

import Temp.*;

public class Ex extends Exp{
	Tree.Exp exp;
	public Ex(Tree.Exp e){	exp = e;	} 
	Tree.Exp unEx(){	return exp;	}
	Tree.Stm unNx(){	return new Tree.Exp(exp);	} 
	Tree.Stm unCx(Label t, Label f)
	{	return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);	}
}
