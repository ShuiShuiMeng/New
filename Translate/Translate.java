package Translate;

import Tree.*;


public class Translate {	
	private Frag.Frag frags = null;
	public Frame.Frame frame = null;
	
	public Translate(Frame.Frame f)
	{
		frame = f;
	}
	public Frag.Frag getResult()
	{	
		return frags;	
	}
	public void addFrag(Frag.Frag frag) 
	{
		frag.next = frags;
		frags = frag;
	}
	public void procEntryExit(Level level, Exp body, boolean returnValue) 
	{
		Stm b = null;
		if (returnValue)
		{
			b = new MOVE(new TEMP(level.frame.RV()), body.unEx());
		}
		else
			b = body.unNx();
		b = level.frame.procEntryExit1(b);
		addFrag(new Frag.ProcFrag(b, level.frame));
	}
	public Exp transNoExp()
	{
		return new Ex(new CONST(0));
	}
	public Exp transIntExp(int value)
	{
		return new Ex(new CONST(value));	
	}
	public Exp transStringExp(String string)
	{
		Temp.Label lab = new Temp.Label();
		addFrag(new Frag.DataFrag(lab, frame.string(lab, string)));
		return new Ex(new NAME(lab));
	}
	public Exp transNilExp()
	{	
		return new Ex(new CONST(0));		
	}
	public Exp transOpExp(int oper, Exp left, Exp right)
	{	
		if (oper >= BINOP.PLUS && oper <= BINOP.DIV)
			return new Ex(new BINOP(oper, left.unEx(), right.unEx()));
		
		return new RelCx(oper, left, right);
	}
	public Exp transStringRelExp(Level currentL, int oper, Exp left, Exp right)
	{
		Tree.Exp comp = currentL.frame.externCall("stringEqual", new ExpList(left.unEx(), new ExpList(right.unEx(), null)));
		return new RelCx(oper, new Ex(comp), new Ex( new CONST(1)));
	}
	public Exp transAssignExp(Exp lvalue, Exp exp)
	{
		return new Nx(new MOVE(lvalue.unEx(), exp.unEx()));
	}
	public Exp transCallExp(Level currentL, Level dest, Temp.Label name, java.util.ArrayList<Exp> args_value)
	{
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i)
		{
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
		}

		Level l = currentL;
		Tree.Exp currentFP = new TEMP(l.frame.FP()); 
		while (dest.parent != l) 
		{
			currentFP = l.staticLink().acc.exp(currentFP);
			l = l.parent;
		}

		args = new ExpList(currentFP, args);
		
		return new Ex(new CALL(new NAME(name), args));
	}
	public Exp transStdCallExp(Level currentL, Temp.Label name, java.util.ArrayList<Exp> args_value)
	{
	
		ExpList args = null;
		for (int i = args_value.size() - 1; i >= 0; --i)
			args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
	
		return new Ex(currentL.frame.externCall(name.toString(), args));
	}
	public Exp stmcat(Exp e1, Exp e2)
	{
		if (e1 == null)
			{
			if(e2!=null) return new Nx(e2.unNx());
			else return transNoExp();
			}
		else if (e2 == null)
				return new Nx(e1.unNx());
		else return new Nx(new SEQ(e1.unNx(), e2.unNx()));
	}
	public Exp exprcat(Exp e1, Exp e2)
	{
		if (e1 == null)
		{
			return new Ex(e2.unEx());
		}
		else 
		{
			return new Ex(new ESEQ(e1.unNx(), e2.unEx()));
		}
	}
	public Exp transRecordExp(Level currentL, java.util.ArrayList<Exp> field) 
	{
		Temp.Temp addr = new Temp.Temp();
		Tree.Exp rec_id = currentL.frame.externCall("allocRecord", new ExpList(new CONST((field.size() == 0 ? 1 : field.size()) * Library.WORDSIZE), null));
		
		Stm stm = transNoExp().unNx();

		for (int i = field.size() - 1; i >= 0; --i)
		{
			Tree.Exp offset = new BINOP(BINOP.PLUS, new TEMP(addr),new CONST(i * Library.WORDSIZE));
			Tree.Exp value = (field.get(i)).unEx();
			stm = new SEQ(new MOVE(new MEM(offset), value), stm);
	
		}
	
		return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), rec_id), stm), new TEMP(addr)));
	}
	public Exp transArrayExp(Level currentL, Exp init, Exp size)
	{
		Tree.Exp alloc = currentL.frame.externCall("initArray", new ExpList(size.unEx(), new ExpList(init.unEx(), null)));
		return new Ex(alloc);
	}
	public Exp transIfExp(Exp test, Exp e1, Exp e2)
	{
		return new IfExp(test, e1, e2);
	}
	public Exp transWhileExp(Exp test, Exp body, Temp.Label out)
	{
		return new WhileExp(test, body, out);
	}
	public Exp transForExp(Level currentL, Access var, Exp low, Exp high, Exp body, Temp.Label out)
	{
		return new ForExp(currentL, var, low, high, body, out);
	}
	public Exp transBreakExp(Temp.Label l)
	{
		return new Nx(new JUMP(l));
	}
	public Exp transSimpleVar(Access acc, Level currentL)
	{
		Tree.Exp e = new TEMP(currentL.frame.FP());
		Level l = currentL;

		while (l != acc.home)		
		{
			e = l.staticLink().acc.exp(e);
			l = l.parent;
		}
		return new Ex(acc.acc.exp(e));
	}
	public Exp transSubscriptVar(Exp var, Exp index)
	{
		Tree.Exp arr_addr = var.unEx();
		Tree.Exp arr_offset = new BINOP(BINOP.MUL, index.unEx(), new CONST(Library.WORDSIZE));
		return new Ex(new MEM(new BINOP(BINOP.PLUS, arr_addr, arr_offset)));
	}
	public Exp transFieldVar(Exp var, int fig)
	{

		Tree.Exp rec_addr = var.unEx(); 
		Tree.Exp rec_offset = new CONST(fig * Library.WORDSIZE);
		return new Ex(new MEM(new BINOP(BINOP.PLUS, rec_addr, rec_offset)));
	}
}
