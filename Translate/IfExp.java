package Translate;

import Tree.*;

public class IfExp extends Exp {
	private Exp test; 
	private Exp e1; 
	private Exp e2; 
	
	IfExp(Exp test, Exp e1, Exp e2) {
		this.test = test;
		this.e1 = e1;
		this.e2 = e2;
	}

	Tree.Exp unEx()
	{
		Temp.Temp r = new Temp.Temp();
		Temp.Label join = new Temp.Label();
		Temp.Label t = new Temp.Label();
		Temp.Label f = new Temp.Label();
		Tree.Exp tmp = new ESEQ(new SEQ(test.unCx(t, f),
								new SEQ(new LABEL(t),
								new SEQ(new MOVE(new TEMP(r), e1.unEx()),
								new SEQ(new JUMP(join),
								new SEQ(new LABEL(f),
								new SEQ(new MOVE(new TEMP(r), e2.unEx()),
								new LABEL(join))))))),
						new TEMP(r));
		return tmp;
	}

	Stm unNx() 
	{
		Temp.Label join = new Temp.Label();
		Temp.Label t = new Temp.Label();
		Temp.Label f = new Temp.Label();

		if (e2 == null)
			return new SEQ(test.unCx(t, join),new SEQ(new LABEL(t),new SEQ(e1.unNx(),new LABEL(join))));
		else return new SEQ(test.unCx(t, f),
							new SEQ(new LABEL(t),
							new SEQ(e1.unNx(),
							new SEQ(new JUMP(join),
							new SEQ(new LABEL(f),
							new SEQ(e2.unNx(),
							new LABEL(join)))))));			
	}

	Stm unCx(Temp.Label t, Temp.Label f)
	{
		return new CJUMP(CJUMP.NE, unEx(), new CONST(0), t, f);
	}
}
