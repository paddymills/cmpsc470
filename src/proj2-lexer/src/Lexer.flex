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
%}

op          = [+-*/]|and|or|not
relop       = [<>]=?|[<>!]?=
int         = [0-9]+
float       = [0-9]+("."[0-9]+)?
identifier  = [a-zA-Z][a-zA-Z0-9_]*
newline     = \n
whitespace  = [ \t\r]+
linecomment = "##".*
blockcomment= "#{"(.|\n)*"}#"

%%

"print"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.PRINT   ; }
"func"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.FUNC    ; }
"var"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.VAR     ; }
"void"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.VOID    ; }
"bool"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL    ; }
"int"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.INT     ; }
"float"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.FLOAT   ; }
"struct"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.STRUCT  ; }
"size"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.SIZE    ; }
"new"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NEW     ; }
"if"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.IF      ; }
"else"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.ELSE    ; }
"while"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.WHILE   ; }
"return"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.RETURN  ; }
"break"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.BREAK   ; }
"continue"                          { parser.yylval = new ParserVal((Object)yytext()); return Parser.CONTINUE; }
"true"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL_LIT; }
"false"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL_LIT; }

"{"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.BEGIN   ; }
"}"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.END     ; }
"("                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LPAREN  ; }
")"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RPAREN  ; }
"["                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LBRACKET; }
"]"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RBRACKET; }
";"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.SEMI    ; }
","                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.COMMA   ; }
"."                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.DOT     ; }
"&"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.ADDR    ; }
"<-"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.ASSIGN  ; }
"->"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.FUNCRET ; }


{op}                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.OP      ; }
{relop}                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.RELOP   ; }

{int}                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.INT_LIT ; }
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.IDENT   ; }
{linecomment}                       { System.out.println("line comment: \""   +yytext()+"\""); /* skip */ }
{newline}                           { System.out.println("newline"                          ); /* skip */ }
{whitespace}                        { System.out.println("whitespace: \""+yytext()+"\""     ); /* skip */ }
{blockcomment}                      { System.out.println("block comment begin \""           );
                                      System.out.println(yytext()                           );
                                      System.out.println("\" block comment end"             ); /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
