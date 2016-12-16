package Frame;

import Temp.*;

public abstract class Frame implements TempMap{
	public abstract Frame newFrame(Label name, Util.BoolList formals);
	public Label name = null; //名称
	public AccessList formals = null; //局部变量、参数的列表
	public abstract Access allocLocal(boolean escape); //分配新本地变量
	public abstract Tree.Exp externCall(String func, Tree.ExpList args); //外部函数
	public abstract Temp FP(); //帧指针
	public abstract Temp SP(); //栈指针
	public abstract Temp RA(); //返回地址
	public abstract Temp RV(); //返回值
	public abstract java.util.HashSet registers();  //寄存器列表
	public abstract Tree.Stm procEntryExit1(Tree.Stm body);  //添加额外函数调用指令
	public abstract Assem.InstrList procEntryExit2(Assem.InstrList body); 
	public abstract Assem.InstrList procEntryExit3(Assem.InstrList body); 
	public abstract String string(Label label, String value); 
	public abstract Assem.InstrList codegen(Tree.Stm s);  //生成MIPS指令用
}
