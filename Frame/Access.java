package Frame;

public abstract class Access { //形参和局部变量
	public abstract Tree.Exp exp(Tree.Exp framePtr);//以fp为起始地址返回变量
	public abstract Tree.Exp expFromStack(Tree.Exp stackPtr); //以sp为起始地址返回变量
}
