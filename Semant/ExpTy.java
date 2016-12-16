package Semant;
import Translate.*;
import Types.*;

public class ExpTy {
	Exp exp;
	Type ty;
	ExpTy(Exp e, Type t)
	{
		exp = e;
		ty = t;
	}
}
