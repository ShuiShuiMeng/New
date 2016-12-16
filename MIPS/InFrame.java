package MIPS;

import Tree.*;

public class InFrame extends Frame.Access {
	private MipsFrame frame; //帧֡
	public int offset;//֡偏移量
	
	public InFrame(MipsFrame frame, int offset) 
	{
		this.frame = frame;
		this.offset = offset;
	}
	//以fp为起始地址返回变量的IR树结点
	public Tree.Exp exp(Tree.Exp framePt) 
	{
		return new MEM(new BINOP(BINOP.PLUS, framePt, new CONST(offset)));
	}
	//以sp为起始地址返回变量的IR书结点
	//fp=sp+帧帧空间+4bytes
	public Tree.Exp expFromStack(Tree.Exp stackPtr) 
	{
		return new MEM(new BINOP(BINOP.PLUS, stackPtr, new CONST(offset - frame.allocDown - Translate.Library.WORDSIZE)));
	}
	
}