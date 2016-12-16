package Semant;

import Types.*;
import Symbol.*;
import ErrorMsg.*;
import Translate.Level;
import Util.BoolList;

public class Env {
	Table vEnv = null; 
	Table tEnv = null; 
	Level root = null;
	ErrorMsg errorMsg = null;
	//哈希表储存库函数symbol
	java.util.HashSet<Symbol> stdFuncSet = new java.util.HashSet<Symbol>(); 
	
	Env(ErrorMsg err, Level l)
	{
		errorMsg = err;
		root = l;
		initTEnv();
		initVEnv();
	}
	public void initTEnv() 
	{
		tEnv = new Table();
		tEnv.put(Symbol.symbol("int"), new INT());
		tEnv.put(Symbol.symbol("string"), new STRING());
	}
	public void initVEnv() 
	{
		vEnv = new Table();
		
		Symbol sym = null; 
		RECORD formals = null; //传入参数格式
		Type result = null; 
		Level level = null;
		//print(str:string)
		sym = Symbol.symbol("print");
		result = new VOID();
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//printi(i:int)
		sym = Symbol.symbol("printi");
		result = new VOID();
		formals = new RECORD(Symbol.symbol("i"), new INT(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//flush()
		sym = Symbol.symbol("flush");
		result = new VOID();
		formals = null;
		level = new Level(root, sym, null);
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//ord(str:string) : int
		sym = Symbol.symbol("ord");
		result = new INT();
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//chr(i:int) : string
		sym = Symbol.symbol("chr");
		result = new STRING();
		formals = new RECORD(Symbol.symbol("i"), new INT(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//getchar() : string
		sym = Symbol.symbol("getchar");
		result = new STRING();
		formals = null;
		level = new Level(root, sym, null);
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//size(str:string) : int
		sym = Symbol.symbol("size");
		result = new INT();
		formals = new RECORD(Symbol.symbol("str"), new STRING(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//substring(str:string, first:int, n:int) : string
		sym = Symbol.symbol("substring");
		result = new STRING();
		//反向建立
		formals = new RECORD(Symbol.symbol("n"), new INT(), null);
		formals = new RECORD(Symbol.symbol("first"), new INT(), formals);
		formals = new RECORD(Symbol.symbol("str"), new STRING(), formals);
		level = new Level(root, sym, new BoolList(true, new BoolList(true, new BoolList(true, null))));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//concat(str1:string, str2:string) : string
		sym = Symbol.symbol("concat");
		result = new STRING();
		formals = new RECORD(Symbol.symbol("str2"), new STRING(), null);
		formals = new RECORD(Symbol.symbol("str1"), new STRING(), formals);
		level = new Level(root, sym, new BoolList(true, new BoolList(true, null)));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//not(j:int) : int
		sym = Symbol.symbol("not");
		result = new INT();
		formals = new RECORD(Symbol.symbol("j"), new INT(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);
		//exit(k:int)
		sym = Symbol.symbol("exit");
		result = new VOID();
		formals = new RECORD(Symbol.symbol("k"), new INT(), null);
		level = new Level(root, sym, new BoolList(true, null));
		vEnv.put(sym, new StdFuncEntry(level, new Temp.Label(sym), formals, result));
		stdFuncSet.add(sym);	
	}
}
