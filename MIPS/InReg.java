package MIPS;
import Temp.Temp;

public class InReg extends Frame.Access 
{
	private Temp reg;
	public InReg() {reg = new Temp();}
	//寄存器中无所谓fp和sp，不管何种方式，总是返回那个寄存器
	public Tree.Exp exp(Tree.Exp framePtr) {return new Tree.TEMP(reg);}
	public Tree.Exp expFromStack(Tree.Exp stackPtr) {return new Tree.TEMP(reg);}
}