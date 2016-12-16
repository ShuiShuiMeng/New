package Main;
import Absyn.*;
import RegAlloc.RegAlloc;
import Semant.*;
import Parse.*;

public class Main{
    static java.io.PrintStream irOut;
    static java.io.PrintStream stOut;
    static java.io.PrintStream lxOut;
    
	public static void main(String[] argv) throws java.io.IOException 
	{
		String filename = "test49.tig";
		ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);  
		java.io.FileInputStream inp = new java.io.FileInputStream(filename);
		//Mips指令文件
		java.io.PrintStream out = new java.io.PrintStream(new java.io.FileOutputStream("result/"+filename + ".s"));
		//Ir Tree文件
	    irOut = new java.io.PrintStream(new java.io.FileOutputStream("result/"+filename + ".ir"));
	    //抽象语法树文件
	    stOut = new java.io.PrintStream(new java.io.FileOutputStream("result/"+filename + ".st"));
		//词法分析结果
	    lxOut = new java.io.PrintStream(new java.io.FileOutputStream("result/"+filename + ".lx"));
	    
	    System.out.println("开始词法分析......");
	    Lexer lexer = new Yylex(inp, errorMsg);
	    java_cup.runtime.Symbol tok;
	    try
	    {
	    	do 
	    	{ 
	         tok=lexer.nextToken();
	         lxOut.println(symnames[tok.sym] + " " + tok.left);
	    	}
	    	while (tok.sym != sym.EOF);
	    	inp.close(); 
	      }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    System.out.println("\n结束词法分析");
	    
	    System.out.println("\n开始语法分析......");
	    inp = new java.io.FileInputStream(filename);
	    lexer = new Yylex(inp, errorMsg);
	    Grm p = new Grm(lexer, errorMsg);
	    Print pr = new Print(stOut);
	    try 
	    {
	    	p.parse();  
	    }
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
		}
	    System.out.println("\n打印抽象语法树......");
	    pr.prExp(p.parseResult, 0);
	    System.out.println("\n打印完毕");
	    System.out.println("\n结束语法分析......");
		
	    System.out.println("\n开始语义分析......");
	    Frame.Frame frame = new MIPS.MipsFrame();
	    Translate.Translate translator = new Translate.Translate(frame);
	    Semant smt = new Semant(translator, errorMsg);
	    Frag.Frag frags = smt.transProg(p.parseResult);
	    if(ErrorMsg.ErrorMsg.anyErrors==false) 
	    	System.out.println("分析结束，无语义和语法错误\n开始翻译......");
	    else return;

	    out.println(".globl main"); 
	    for(Frag.Frag f = frags; f!=null; f=f.next)
	        if (f instanceof Frag.ProcFrag)
	           emitProc(out, (Frag.ProcFrag)f);
	        else if (f instanceof Frag.DataFrag)
	           out.print("\n.data\n" +((Frag.DataFrag)f).data);
	    java.io.BufferedReader rt = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream("runtime.s")));
	    String data = null;
	    while((data = rt.readLine())!=null)
	    {
	    out.println(data);
	    }
	    out.close();
	    System.out.println("\n翻译结束");    
	}
	 
	static void emitProc(java.io.PrintStream out, Frag.ProcFrag f)
	 {
		Tree.Print print = new Tree.Print(irOut); 

		Tree.StmList stms = Canon.Canon.linearize(f.body); 
		Canon.BasicBlocks b = new Canon.BasicBlocks(stms); 
		Tree.StmList traced = (new Canon.TraceSchedule(b)).stms; 
		prStmList(print,traced);
		Assem.InstrList instrs = codegen(f.frame, traced); 
		instrs = f.frame.procEntryExit2(instrs); 
		RegAlloc regAlloc = new RegAlloc(f.frame, instrs);
		instrs = f.frame.procEntryExit3(instrs); 
		Temp.TempMap tempmap = new Temp.CombineMap(f.frame, regAlloc); 
		//以下生成MIPS指令
		out.println("\n.text"); 
		for (Assem.InstrList p = instrs; p != null; p = p.tail) 
		    out.println(p.head.format(tempmap));
	  }
	//打印表达式列表的函数
	  static void prStmList(Tree.Print print, Tree.StmList stms) 
	  {
	      for(Tree.StmList l = stms; l!=null; l=l.tail)
	   	  print.prStm(l.head);
	  }
	 //由中间表示树的表达式列表产生指令列表,按层次依次调用各级codegen
	  static Assem.InstrList codegen(Frame.Frame f, Tree.StmList stms) 
	  {
		  Assem.InstrList first = null, last = null;
	 	  for(Tree.StmList s = stms; s != null; s = s.tail) 
	 	  {
	 		 Assem.InstrList i = f.codegen(s.head);
	 		 if (last == null) 
		     {	first = last = i;	}
		     else 
		     {
		    	 while (last.tail != null)
		    		 last = last.tail;
		    	 last = last.tail = i;
		     }
	 	  }
	 	  return first;
	  }
	  //用于输出词法分析的字符串常量表
	  static String symnames[] = new String[100];
	  static {
	     
	     symnames[sym.FUNCTION] = "FUNCTION";
	     symnames[sym.EOF] = "EOF";
	     symnames[sym.INT] = "INT";
	     symnames[sym.GT] = "GT";
	     symnames[sym.DIVIDE] = "DIVIDE";
	     symnames[sym.COLON] = "COLON";
	     symnames[sym.ELSE] = "ELSE";
	     symnames[sym.OR] = "OR";
	     symnames[sym.NIL] = "NIL";
	     symnames[sym.DO] = "DO";
	     symnames[sym.GE] = "GE";
	     symnames[sym.error] = "error";
	     symnames[sym.LT] = "LT";
	     symnames[sym.OF] = "OF";
	     symnames[sym.MINUS] = "MINUS";
	     symnames[sym.ARRAY] = "ARRAY";
	     symnames[sym.TYPE] = "TYPE";
	     symnames[sym.FOR] = "FOR";
	     symnames[sym.TO] = "TO";
	     symnames[sym.TIMES] = "TIMES";
	     symnames[sym.COMMA] = "COMMA";
	     symnames[sym.LE] = "LE";
	     symnames[sym.IN] = "IN";
	     symnames[sym.END] = "END";
	     symnames[sym.ASSIGN] = "ASSIGN";
	     symnames[sym.STRING] = "STRING";
	     symnames[sym.DOT] = "DOT";
	     symnames[sym.LPAREN] = "LPAREN";
	     symnames[sym.RPAREN] = "RPAREN";
	     symnames[sym.IF] = "IF";
	     symnames[sym.SEMICOLON] = "SEMICOLON";
	     symnames[sym.ID] = "ID";
	     symnames[sym.WHILE] = "WHILE";
	     symnames[sym.LBRACK] = "LBRACK";
	     symnames[sym.RBRACK] = "RBRACK";
	     symnames[sym.NEQ] = "NEQ";
	     symnames[sym.VAR] = "VAR";
	     symnames[sym.BREAK] = "BREAK";
	     symnames[sym.AND] = "AND";
	     symnames[sym.PLUS] = "PLUS";
	     symnames[sym.LBRACE] = "LBRACE";
	     symnames[sym.RBRACE] = "RBRACE";
	     symnames[sym.LET] = "LET";
	     symnames[sym.THEN] = "THEN";
	     symnames[sym.EQ] = "EQ";
	   }
}


