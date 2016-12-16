package Semant;

import Absyn.*;
import ErrorMsg.*; 
import Translate.Level;
import Types.*;
import Util.BoolList;
import Symbol.Symbol;

public class Semant {
	private Env env;
	private Translate.Translate trans;
	private Translate.Level level = null;
	private java.util.Stack<Temp.Label> loopStack = new java.util.Stack<Temp.Label>();//用于循环嵌套的堆栈
	
	private Boolean TDecFlag=false,FDecFlag=false,TypeDecFlag=false,FuncDecFlag=false;
	
	public Semant(Translate.Translate t, ErrorMsg err) 
	{
		trans = t;
		level = new Level(t.frame);
		level = new Level(level, Symbol.symbol("main"), null);
		env = new Env(err, level);
	}

	public Frag.Frag transProg(Exp exp)
	{
		ExpTy et = transExp(exp);
		if(ErrorMsg.anyErrors)
		{
			System.out.println("编译终止，请检查错误。");
			return null;
		}
		trans.procEntryExit (level, et.exp, false); 
		level = level.parent;
		return trans.getResult(); 
	}

	//翻译语句
	public ExpTy transExp(Exp e)
	{
		if (e instanceof IntExp) return transExp((IntExp)e);
		if (e instanceof StringExp) return transExp((StringExp)e);
		if (e instanceof NilExp) return transExp((NilExp)e);
		if (e instanceof VarExp) return transExp((VarExp)e);
		if (e instanceof OpExp) return transExp((OpExp)e);
		if (e instanceof AssignExp) return transExp((AssignExp)e);
		if (e instanceof CallExp) return transExp((CallExp)e);
		if (e instanceof RecordExp) return transExp((RecordExp)e);
		if (e instanceof ArrayExp) return transExp((ArrayExp)e);
		if (e instanceof IfExp) return transExp((IfExp)e);
		if (e instanceof WhileExp) return transExp((WhileExp)e);
		if (e instanceof ForExp) return transExp((ForExp)e);
		if (e instanceof BreakExp) return transExp((BreakExp)e);
		if (e instanceof LetExp) return transExp((LetExp)e);
		if (e instanceof SeqExp) return transExp((SeqExp)e);
		return null;
	}
	//基本类型
	private ExpTy transExp(IntExp e) {
		return new ExpTy(trans.transIntExp(e.value), new INT());
	}

	private ExpTy transExp(StringExp e) {
		return new ExpTy(trans.transStringExp(e.value), new STRING());
	}

	private ExpTy transExp(NilExp e) {
		return new ExpTy(trans.transNilExp(), new NIL());
	}

	private ExpTy transExp(VarExp e) {
		return transVar(e.var);
	}

	//检查算术表达式
	private ExpTy transExp(OpExp e) {
		ExpTy el = transExp(e.left);
		ExpTy er = transExp(e.right);
		if(el==null || er==null) {return null;}

		if(e.oper==OpExp.EQ || e.oper == OpExp.NE)
		{
			//两个nil
			if(el.ty.actual() instanceof NIL && er.ty.actual() instanceof NIL)
			{
				env.errorMsg.error(e.pos, "两个NIL类型不能作比较。");
				return null;
			}
			//有void
			else if(el.ty.actual() instanceof VOID || er.ty.actual() instanceof VOID)
			{
				env.errorMsg.error(e.pos, "不能与VOID类型作比较。");
				return null;
			}
			//一个nil一个record
			else if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof RECORD)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			else if (el.ty.actual() instanceof RECORD && er.ty.actual() instanceof NIL)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			//类型一致
			else if (el.ty.coerceTo(er.ty))
			{
				if (el.ty.actual() instanceof STRING && e.oper == OpExp.EQ)
				{
					return new ExpTy(trans.transStringRelExp(level, e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
				}
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			}

			else {
				env.errorMsg.error(e.pos, "相等不等运算符两边的类型不一致。");
				return null;
			}	
		}
		//运算符为< <= > >=
		else if (e.oper > OpExp.NE)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			else if (el.ty.actual() instanceof STRING && er.ty.actual() instanceof STRING)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new STRING());
			else {
				env.errorMsg.error(e.pos, "比较运算符两边类型不一致。");
				return null;
			}
		}
		//运算符为+ - * /
		else if(e.oper < OpExp.EQ)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			else {
				env.errorMsg.error(e.pos, "算术运算符两边类型不一致。");
				return null;
			}
		}

		return new ExpTy(trans.transOpExp(e.oper, el.exp, er.exp), new INT());
	}
	
	//赋值表达式
	private ExpTy transExp(AssignExp e)
	{
		int pos=e.pos;
		Var var=e.var;
		Exp exp=e.exp;
		ExpTy er=transExp(exp);
		//右值为void
		if (er.ty.actual() instanceof VOID)
		{
			env.errorMsg.error(pos, "右值不能为VOID类型。");
			return null;
		}
		//左值为简单变量
		if(var instanceof SimpleVar)
		{
			SimpleVar ev = (SimpleVar)var;
			Entry entry = (Entry)(env.vEnv.get(ev.name));

			if (entry instanceof VarEntry && ((VarEntry)entry).isFor)
			{
				env.errorMsg.error(pos, "循环变量不能被赋值。");
				return null;
			}
		}

		ExpTy vr = transVar(var);
		if (!er.ty.coerceTo(vr.ty))
		{
			env.errorMsg.error(pos, er.ty.actual().getClass().getSimpleName()+"类型的值不能赋值给"+vr.ty.actual().getClass().getSimpleName()+"类型的变量");
			return null;	
		}
		return new ExpTy(trans.transAssignExp(vr.exp, er.exp), new VOID());
	}
	//函数
	private ExpTy transExp(CallExp e)
	{
		FuncEntry fe;
		Object obj = env.vEnv.get(e.func);

		if(obj==null || !(obj instanceof FuncEntry))
		{
			env.errorMsg.error(e.pos, "函数"+e.func.toString()+"无定义。");
			return null;
		}
		ExpList ex=e.args;
		fe = (FuncEntry)obj;
		RECORD rc = fe.paramlist;

		while (ex!=null)
		{
			if (rc==null)
			{
				env.errorMsg.error(e.pos, "参数表不一致,调用函数时传入了多余参数。");
				return null;
			}
			if(!transExp(ex.head).ty.coerceTo(rc.fieldType))
			{
				env.errorMsg.error(e.pos, "参数表类型不一致。");
			}
			ex = ex.tail;
			rc = rc.tail;
		}
		if(ex == null && !(RECORD.isNull(rc)))
		{
			env.errorMsg.error(e.pos, "参数表不一致,调用函数时缺少了参数。");
			return null;
		}
		//逐个实参
		java.util.ArrayList<Translate.Exp> arr1 = new java.util.ArrayList<Translate.Exp>();
		for (ExpList i=e.args; i!=null; i=i.tail)
			arr1.add(transExp(i.head).exp);
		//标准库函数
		if (obj instanceof StdFuncEntry)
		{
			StdFuncEntry sf = (StdFuncEntry)obj;
			return new ExpTy(trans.transStdCallExp(level, sf.label, arr1), sf.returnTy);
		}
		return new ExpTy(trans.transCallExp(level, fe.level, fe.label, arr1), fe.returnTy);
	}
	//record
	private ExpTy transExp(RecordExp e)
	{	//翻译
		Type t =(Type)env.tEnv.get(e.typ);
		//查表
		if (t == null || !(t.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "记录类型不存在。");
			return null;
		}
		//
		FieldExpList fe = e.fields;
		RECORD rc = (RECORD)(t.actual());
		if (fe == null && rc != null)
		{
			env.errorMsg.error(e.pos, "记录类型中的成员变量不一致。");
			return null;
		}
		
		while (fe != null)
		{	
			ExpTy ie = transExp(fe.init);
			if (rc == null || ie == null ||!ie.ty.coerceTo(rc.fieldType) || fe.name != rc.fieldName)
			{
				env.errorMsg.error(e.pos, "记录类型中的成员变量不一致。");
				return null;
			}
			fe = fe.tail;
			rc = rc.tail;
		}
		//参数传入
		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (FieldExpList i = e.fields; i != null; i = i.tail)
			arrl.add(transExp(i.init).exp);
		return new ExpTy(trans.transRecordExp(level, arrl), t.actual()); 
	}
	//array
	private ExpTy transExp(ArrayExp e)
	{
		//翻译定义
		Type ty = (Type)env.tEnv.get(e.typ);
		if (ty == null || !(ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "数组不存在。");
			return null;
		}
		//翻译下标
		ExpTy size = transExp(e.size);
		if (!(size.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "数组长度不是整型。");
			return null;
		}	
		ARRAY ar = (ARRAY)ty.actual();
		//翻译类型
		ExpTy ini = transExp(e.init);
		if (!ini.ty.coerceTo(ar.element.actual()))
		{
			env.errorMsg.error(e.pos, "初始值的类型与数组元素的类型不一致。");
			return null;
		}
		return new ExpTy(trans.transArrayExp(level, ini.exp, size.exp), new ARRAY(ar.element));			
	}
	//IF
	private ExpTy transExp(IfExp e)
	{
		//翻译test
		ExpTy testET = transExp(e.test);
		if (e.test == null || testET == null || !(testET.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "if语句中的条件表达式不是整型。");
			return null;
		}
		//翻译then
		ExpTy thenET = transExp(e.thenclause);
		if (e.elseclause == null && (!(thenET.ty.actual() instanceof VOID)))
		{
			env.errorMsg.error(e.pos, "if语句不应该有返回值。");
			return null;
		}	
		//翻译else
		ExpTy elseET = transExp(e.elseclause);
		if (e.elseclause != null && !thenET.ty.coerceTo(elseET.ty))
		{
			env.errorMsg.error(e.pos, "两个分支类型不一致。");
			return null;
		}
		else if(elseET == null)
			return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, trans.transNoExp()), thenET.ty);
		return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, elseET.exp), thenET.ty);

	}

	public ExpTy transExp(WhileExp e) {

		ExpTy test_ty = transExp(e.test);
		if (test_ty == null) return null;

		if(!(test_ty.ty.actual() instanceof INT)) {
			env.errorMsg.error(e.pos, "循环条件不是整型。");
			return null;
		}

		Temp.Label done = new Temp.Label();
		loopStack.push(done);
		ExpTy body_ty = transExp(e.body);
		loopStack.pop();
		if(body_ty == null) return null;

		if(!(body_ty.ty.actual() instanceof VOID)) {
			env.errorMsg.error(e.pos, "While循环不应有返回值。");
			return null;
		}
		return new ExpTy(trans.transWhileExp(test_ty.exp, body_ty.exp, done), new VOID());
	}
	//翻译for循环
	private ExpTy transExp(ForExp e)
	{
		
		boolean flag = false;//标记循环体是否为空
		//循环变量必须是整数类型
		if (!(transExp(e.hi).ty.actual() instanceof INT) || !(transExp(e.var.init).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "循环变量不是整型。");
		}

		env.vEnv.beginScope();
		Temp.Label label = new Temp.Label();//定义循环的入口
		loopStack.push(label);

		Translate.Access acc = level.allocLocal(true);
		env.vEnv.put(e.var.name, new VarEntry(new INT(), acc, true));
	
		ExpTy var_ty = transExp(e.var.init);
		ExpTy hi_ty = transExp(e.hi);
		ExpTy body_ty = transExp(e.body);
		
		if (body_ty == null)	flag = true;
		loopStack.pop();	
		env.vEnv.endScope();
		
		if (flag)	return null;
		return new ExpTy(trans.transForExp(level, acc, var_ty.exp, hi_ty.exp, body_ty.exp, label), new VOID());
	}
	//翻译break语句
	private ExpTy transExp(BreakExp e)
	{
		
		if (loopStack.isEmpty())
		{
			env.errorMsg.error(e.pos, "Break语句未在循环中。");
			return null;
		}
		return new ExpTy(trans.transBreakExp(loopStack.peek()), new VOID());
	}
	//翻译let-in-end语句
	private ExpTy transExp(LetExp e)
	{
		Translate.Exp ex = null;
		env.vEnv.beginScope();
		env.tEnv.beginScope();	
		ExpTy td = transDecList(e.decs);

		if (td != null)
			ex = td.exp;
		ExpTy tb = transExp(e.body);

		if (tb == null)
			ex = trans.stmcat(ex, null);
		else if (tb.ty.actual() instanceof VOID)
			ex = trans.stmcat(ex, tb.exp);
		else 
			ex = trans.exprcat(ex, tb.exp);

		env.tEnv.endScope();
		env.vEnv.endScope();
		return new ExpTy(ex, tb.ty);
	}
	//翻译表达式序列
	private ExpTy transExp(SeqExp e)
	{
		Translate.Exp ex = null;
		for (ExpList t = e.list; t != null; t = t.tail)
		{
			ExpTy x = transExp(t.head);

			if (t.tail == null)
			{	
				if(x!=null)
				{
					if (x.ty.actual() instanceof VOID)
						ex = trans.stmcat(ex, x.exp);
					else ex = trans.exprcat(ex, x.exp);
					
				}
				if(x!=null) return new ExpTy(ex, x.ty);
				else return new ExpTy(ex, new VOID());
			}
			ex = trans.stmcat(ex, x.exp);	
		}
		return null;
	}

	//翻译变量
	public ExpTy transVar(Var e)
	{
		if (e instanceof SimpleVar) return transVar((SimpleVar)e);
		if (e instanceof SubscriptVar) return transVar((SubscriptVar)e);
		if (e instanceof FieldVar) return transVar((FieldVar)e);
		return null;
	}
	//翻译简单变量
	private ExpTy transVar(SimpleVar e)
	{
		Entry ex = (Entry)env.vEnv.get(e.name);
		if (ex == null || !(ex instanceof VarEntry))
		{
			env.errorMsg.error(e.pos, "变量 '"+e.name.toString()+"' 无定义。");
			return null;
		}
		VarEntry evx = (VarEntry)ex;
		return new ExpTy(trans.transSimpleVar(evx.acc, level), evx.Ty);
	}
	//翻译下标变量	
	private ExpTy transVar(SubscriptVar e)
	{	
		ExpTy ei = transExp(e.index);
		if (!(ei.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "下标不是整型。");
			return null;
		}	

		ExpTy ev = transVar(e.var);
		if (ev == null || !(ev.ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "数组不存在。");
			return null;
		}
		ARRAY ae = (ARRAY)(ev.ty.actual());
		return new ExpTy(trans.transSubscriptVar(ev.exp, ei.exp), ae.element);
	}
	//翻译域变量
	private ExpTy transVar(FieldVar e)
	{
		ExpTy et = transVar(e.var);
		if (!(et.ty.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "变量类型不是记录类型。");
			return null;
		}

		RECORD rc = (RECORD)(et.ty.actual());
		int count = 1;
		while (rc != null)
		{
			if (rc.fieldName == e.field)
			{
				return new ExpTy(trans.transFieldVar(et.exp, count), rc.fieldType);
			}
			count++;
			rc = rc.tail;
		}
		env.errorMsg.error(e.pos, "域变量不存在。");
		return null;
	}

	/*翻译类型*/
	public Type transTy(Ty e)
	{
		if (e instanceof ArrayTy) return transTy((ArrayTy)e);
		if (e instanceof RecordTy) return transTy((RecordTy)e);
		if (e instanceof NameTy) return transTy((NameTy)e);
		return null;
	}	
	//翻译未知类型  NameTy
	private Type transTy(NameTy e)
	{
		if (e == null) return new VOID();
		
		Type ty = (Type)env.tEnv.get(e.name);
		if (ty == null)
		{
			env.errorMsg.error(e.pos, "类型无定义。");
			return null;
		}
		return ty;
	}
	//翻译数组类型 ArrayTy
	private ARRAY transTy(ArrayTy e)
	{
		Type ty = (Type)env.tEnv.get(e.typ);
		if (ty == null)
		{
			env.errorMsg.error(e.pos, "类型不存在。");
			return null;
		}
		return new ARRAY(ty);	
	}
	//翻译记录类型 RecordTy
	private RECORD transTy(RecordTy e)
	{
		RECORD rc = new RECORD(),  r = new RECORD();
		if (e == null || e.fields == null)
		{
			rc.gen(null, null, null);
			return rc;
		}
		
		FieldList fl = e.fields;
		boolean flag = true;
		while (fl!=null)
		{
			if(env.tEnv.get(fl.typ) == null)
			{
				env.errorMsg.error(e.pos, "域类型不存在。");
				return null;
			}

			rc.gen(fl.name, (Type)env.tEnv.get(fl.typ), new RECORD());
			if (flag) {r = rc; flag = false;}
			if (fl.tail == null) rc.tail = null;
			rc = rc.tail;
			fl = fl.tail;
		}

		return r;
	}

	/*翻译声明*/
	public Translate.Exp transDec(Dec e)
	{
		if (e instanceof VarDec) 
		{
			if(TypeDecFlag==true) {
				TDecFlag=true;
			}
			if(FuncDecFlag==true) {
				FDecFlag=true;
			}
				return transDec((VarDec)e);
		}
		if (e instanceof TypeDec) 
		{
			if(TypeDecFlag==false) {
				TypeDecFlag=true;
				return transDec((TypeDec)e);
			}
			if(TDecFlag==true){
				env.errorMsg.error(e.pos, "类型定义被中途打断");
				return null;
			}
		}
		if (e instanceof FunctionDec) 
		{
			if(FuncDecFlag==false) {
				FuncDecFlag=true;
				return transDec((FunctionDec)e);
			}
			if(FDecFlag==true){
				env.errorMsg.error(e.pos, "函数定义被中途打断");
				return null;
			}
		}
		return null;
	}

	private Translate.Exp transDec(VarDec e)
	{
		//初始值不能为nil
		if (e.typ == null && e.init instanceof NilExp)
		{
			env.errorMsg.error(e.pos, "初始值不能赋值为Nil。");
			return null;
		}

		//翻译初始值
		ExpTy et = transExp(e.init);
		//除记录类型外,其他变量定义必需赋初始值
		if (et == null && e.init==null)	
		{
			env.errorMsg.error(e.pos,"定义变量必须赋初始值。");
			return null;
		}
		if(et == null) 
			{
			//这里特别处理初始值赋值为()的情况
			et=new ExpTy(trans.transNilExp(), new NIL());
			e.init=new NilExp(e.pos);
		}
		//若初始值与变量类型不匹配则报错
		if (e.typ != null && !(transExp(e.init).ty.coerceTo((Type)env.tEnv.get(e.typ.name))))
		{
			env.errorMsg.error(e.pos,"初始值与变量类型不匹配。");
			return null;
		}
		if (e.init == null )
		{
			env.errorMsg.error(e.pos, "定义变量必须赋初始值。");
			return null;
		}
		Translate.Access acc = level.allocLocal(true);
		//为变量分配空间
		if (e.typ != null)
		{
			env.vEnv.put(e.name, new VarEntry((Type)env.tEnv.get(e.typ.name), acc));
		}
		//将变量加入入口符号表,分简略申明与长申明两种,简略申明不用写明变量类型,其类型由初始值定义
		else
		{
			env.vEnv.put(e.name, new VarEntry(transExp(e.init).ty, acc));
		}
		return trans.transAssignExp(trans.transSimpleVar(acc, level), et.exp);
	}

	//翻译类型申明语句
	private Translate.Exp transDec(TypeDec e)
	{
		for (TypeDec i = e; i != null; i = i.next)
		{
			env.tEnv.put(i.name, new NAME(i.name));
			((NAME)env.tEnv.get(i.name)).bind(transTy(i.ty).actual());
			NAME field = (NAME)env.tEnv.get(i.name);
			if(field.isLoop() == true) 
			{
				env.errorMsg.error(i.pos, "类型循环定义。");
				return null;
			}
			
		}	
	//将类型放入类型符号表
	for (TypeDec i = e; i != null; i = i.next)
		env.tEnv.put(i.name, transTy(i.ty));
		
		return trans.transNoExp();
	}

	private Translate.Exp transDec(FunctionDec e)
	{
		//翻译函数申明
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		ExpTy et = null;
		//检查重复申明,分为普通函数与标准库函数
		for (FunctionDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{
				env.errorMsg.error(e.pos, "在同一个块中重复定义函数。");
				return null;
			}
			if (env.stdFuncSet.contains(i.name))
			{
				env.errorMsg.error(e.pos, "与标准库函数重名。");
				return null;
			}
			
			RecordTy rt = new RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return null;
			//后检查参数列表,与记录类型RecordTy的检查完全相同,得到 RECORD 类型的形参列表
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.beginScope();
			Translate.AccessList al = level.formals.next;
			for (RECORD j = r; j!= null; j = j.tail)
			{
				if (j.fieldName != null)
				{
					env.vEnv.put(j.fieldName, new VarEntry(j.fieldType, al.head));
					al = al.next;
				}
			}			
			et = transExp(i.body);
			//翻译函数体
			if (et == null)
			{	env.vEnv.endScope();	return null;	}
			if(!(et.ty.coerceTo((transTy(i.result).actual()))))
			{
				env.errorMsg.error(i.pos,"函数定义中返回值类型不匹配");
				return null;
			}
			//着检查函数返回值,如果没有返回值则设置成 void 
			//判断是否为void,若不为void则要将返回值存入$v0寄存器
			if (!(et.ty.actual() instanceof VOID)) 
				trans.procEntryExit(level, et.exp, true);
			else 
				trans.procEntryExit(level, et.exp, false);
			
			env.vEnv.endScope();
			level = level.parent;
			//回到原来的层
			hs.add(i.name);
		}
		return trans.transNoExp();
	}
	//翻译申明列表
	private ExpTy transDecList(DecList e)
	{
		Translate.Exp ex = null;
		for (DecList i = e; i!= null; i = i.tail)
			transDec0(i.head);
		//申明的翻译要进行两轮,第一轮中要将所有符号放入符号表
		for (DecList i = e; i!= null; i = i.tail)
		{
			ex = trans.stmcat(ex, transDec(i.head));
		}

		return new ExpTy(ex, new VOID());
	}
	
	public void transDec0(Dec e)
	{
		if (e instanceof VarDec) transDec0((VarDec)e);
		if (e instanceof TypeDec) transDec0((TypeDec)e);
		if (e instanceof FunctionDec) transDec0((FunctionDec)e);
	}
	
	private void transDec0(VarDec e) {}	
	
	//检查是否有重复定义
	private void transDec0(TypeDec e)
	{
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		
		for (TypeDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{ 
				env.errorMsg.error(e.pos, "在同一个块中重复定义类型。");
				return ;
			}
			hs.add(i.name);
			env.tEnv.put(i.name, new NAME(i.name));
		}
	}
	
	private void transDec0(FunctionDec e)
	{
		for (FunctionDec i = e; i != null; i = i.next)
		{
			RecordTy rt = new RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return;
			//后检查参数列表,与记录类型RecordTy的检查完全相同,得到 RECORD 类型的形参列表
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.put(i.name, new FuncEntry(level, new Temp.Label(i.name), r, transTy(i.result)));
			level = level.parent;
		}
	}


}
