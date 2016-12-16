package Translate;

import Temp.*;
import Tree.*;

public abstract class Cx extends Exp{
	Tree.Exp unEx()
	{
		Temp r = new Temp();
		Label t = new Label();
		Label f = new Label();

		return new ESEQ(
				new SEQ(new MOVE(new TEMP(r), new CONST(1)),
				new SEQ(unCx(t, f),
				new SEQ(new LABEL(f),
				new SEQ(new MOVE(new TEMP(r), new CONST(0)),
				new LABEL(t))))),new TEMP(r));
	}
	abstract Stm unCx(Label t, Label f); 
	Stm unNx(){	return new Tree.Exp(unEx());} 
}
