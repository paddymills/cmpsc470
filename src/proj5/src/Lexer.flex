/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%%

%class Lexer
%byaccj
%line
%column


%{

  public Parser   parser;
  public int      lineno;
  public int      column;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
  }

  private void consume() {
    parser.yylval = new ParserVal(yytext());
    lineno = yyline   + 1;
    column = yycolumn + 1;
  }

%}

bool        = true|false
int         = [0-9]+
identifier  = [a-zA-Z][a-zA-Z0-9_]*
newline     = \n
whitespace  = [ \t\r]+
linecomment = "##".*
blkcomment  = "#{"[^]*"}#"

%%

"func"                              { consume(); return Parser.FUNC       ; }
"call"                              { consume(); return Parser.CALL       ; }
"return"                            { consume(); return Parser.RETURN     ; }
"var"                               { consume(); return Parser.VAR        ; }
"if"                                { consume(); return Parser.IF         ; }
"else"                              { consume(); return Parser.ELSE       ; }
"while"                             { consume(); return Parser.WHILE      ; }
"int"                               { consume(); return Parser.INT        ; }
"bool"                              { consume(); return Parser.BOOL       ; }
"print"                             { consume(); return Parser.PRINT      ; }
"and"                               { consume(); return Parser.AND        ; }
"or"                                { consume(); return Parser.OR         ; }
"not"                               { consume(); return Parser.NOT        ; }

"{"                                 { consume(); return Parser.BEGIN      ; }
"}"                                 { consume(); return Parser.END        ; }
"("                                 { consume(); return Parser.LPAREN     ; }
")"                                 { consume(); return Parser.RPAREN     ; }
"<-"                                { consume(); return Parser.ASSIGN     ; }
"->"                                { consume(); return Parser.FUNCRET    ; }
"+"                                 { consume(); return Parser.ADD        ; }
"-"                                 { consume(); return Parser.SUB        ; }
"*"                                 { consume(); return Parser.MUL        ; }
"/"                                 { consume(); return Parser.DIV        ; }
"%"                                 { consume(); return Parser.MOD        ; }
"<"                                 { consume(); return Parser.LT         ; }
">"                                 { consume(); return Parser.GT         ; }
"<="                                { consume(); return Parser.LE         ; }
">="                                { consume(); return Parser.GE         ; }
"="                                 { consume(); return Parser.EQ         ; }
"!="                                { consume(); return Parser.NE         ; }
";"                                 { consume(); return Parser.SEMI       ; }
","                                 { consume(); return Parser.COMMA      ; }

{int}                               { consume(); return Parser.INT_LIT    ; }
{bool}                              { consume(); return Parser.BOOL_LIT   ; }
{identifier}                        { consume(); return Parser.IDENT      ; }
// {linecomment}                       { consume(); return Parser.COMMENT;    /* skip */ }
// {newline}                           { consume(); return Parser.NEWLINE;    /* skip */ }
// {whitespace}                        { consume(); return Parser.WHITESPACE; /* skip */ }
// {blkcomment}                        { consume(); return Parser.BLKCOMMENT; /* skip */ }
{linecomment}                       { consume(); /* skip */ }
{newline}                           { consume(); /* skip */ }
{whitespace}                        { consume(); /* skip */ }
{blkcomment}                        { consume(); /* skip */ }

\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
