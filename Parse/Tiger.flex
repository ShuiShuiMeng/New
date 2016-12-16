package Parse;
import ErrorMsg.ErrorMsg;

%% 

%implements Lexer
%function nextToken
%type java_cup.runtime.Symbol
%char

%{
StringBuffer string = new StringBuffer();
int commentcount;
private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
  errorMsg.error(pos,s);
}

private void err(String s) {
  err(yychar,s);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

private ErrorMsg errorMsg;

Yylex(java.io.InputStream s, ErrorMsg.ErrorMsg e) {
  this(s);
  errorMsg=e;
}

%}

%eofval{
	{
	 if (yystate()==COMMENT) err("Comment symbol dont match!");
	 if (yystate()==STRING)	 err("String error!");
	 if (yystate()==STRING1) err("String error!");
	 return tok(sym.EOF, null);
        }
%eofval}       

LineTerminator = \n|\r|\r\n|\n\r
WhiteSpace = \n|\r|\r\n|\f|\t|\n\r
DecIntegerLiteral = [0-9]+
Identifier = [a-zA-Z][:jletterdigit:]*


%state STRING
%state STRING1
%state COMMENT

%%
<YYINITIAL> {
{LineTerminator} 
			{}
{WhiteSpace} {}

let 		{return tok(sym.LET, null);}
function 	{return tok(sym.FUNCTION, null);}
if			{return tok(sym.IF, null);}
else		{return tok(sym.ELSE, null);}
nil			{return tok(sym.NIL, null);}
for			{return tok(sym.FOR, null);}
to			{return tok(sym.TO, null);}
while		{return tok(sym.WHILE, null);}
do			{return tok(sym.DO, null);}
array		{return tok(sym.ARRAY, null);}
of			{return tok(sym.OF, null);}
in 			{return tok(sym.IN, null);}
end			{return tok(sym.END, null);}
then		{return tok(sym.THEN, null);}
type		{return tok(sym.TYPE, null);}
var			{return tok(sym.VAR, null);}
break		{return tok(sym.BREAK, null);}
","			{return tok(sym.COMMA, null);}

\"			{string.setLength(0);yybegin(STRING);}
"/*"		{commentcount=1;yybegin(COMMENT);}
" "  		{}

"+"			{return tok(sym.PLUS, null);}
"-"			{return tok(sym.MINUS, null);}
"*"			{return tok(sym.TIMES, null);}
"/"			{return tok(sym.DIVIDE, null);}
"&"			{return tok(sym.AND, null);}
"|"			{return tok(sym.OR, null);}
"="			{return tok(sym.EQ, null);}
"<>"		{return tok(sym.NEQ, null);}
">"			{return tok(sym.GT, null);}
">="		{return tok(sym.GE, null);}
"<"			{return tok(sym.LT, null);}
"<="		{return tok(sym.LE, null);}
":"			{return tok(sym.COLON, null);}
":="		{return tok(sym.ASSIGN, null);}
"."			{return tok(sym.DOT, null);}
";"			{return tok(sym.SEMICOLON, null);}
"["			{return tok(sym.LBRACK, null);}
"]"			{return tok(sym.RBRACK, null);}
"{"			{return tok(sym.LBRACE, null);}
"}"			{return tok(sym.RBRACE, null);}
"("			{return tok(sym.LPAREN, null);}
")"			{return tok(sym.RPAREN, null);}

{Identifier} {return tok(sym.ID, yytext());}
{DecIntegerLiteral}
			{return tok(sym.INT, new Integer(yytext()));}
[^]			{err("Illegal character < "+yytext()+" >!");}
}

<STRING> {
\"			{yybegin(YYINITIAL);return tok(sym.STRING, string.toString());}
[^\n\t\"\\] {string.append(yytext());}
\\t  		{string.append('\t');}
\\n         {string.append('\n');}
\\\"  		{string.append('\"');}
\\\\		{string.append('\\');}
\\[0-9][0-9][0-9]
			{int d= new Integer(yytext().substring(1,4));
			if(d>=0&&d<=255) string.append((char)d);
			else err("The escape sequence <"+yytext()+"> is out of range");
			}
{LineTerminator} {err("String error!");}
\\			{yybegin(STRING1);}
}

<COMMENT> {
"/*"		{commentcount++;}
"*/"		{commentcount--; if(commentcount==0) yybegin(YYINITIAL);}
[^]			{}
}

<STRING1> 	{
{WhiteSpace} {}
\\ 			{yybegin(STRING);}
\"			{err("\\dont match");}
[^]			{string.append(yytext());}
}


