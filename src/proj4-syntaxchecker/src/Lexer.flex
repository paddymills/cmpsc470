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

exprop      = [\+\-]|or
termop      = [\*\/]|and
relop       = [<>]=?|[<>!]?=
bool        = true|false
int         = [0-9]+
identifier  = [a-zA-Z][a-zA-Z0-9_]*
newline     = \n
whitespace  = [ \t\r]+
linecomment = "##".*
blockcomment= "#{"[^]*"}#"

%%

"func"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.FUNC   ; }
"call"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.CALL   ; }
"return"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.RETURN ; }
"var"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.VAR    ; }
"if"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.IF     ; }
"else"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.ELSE   ; }
"while"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.WHILE  ; }
"print"                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.PRINT  ; }
"sizeof"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.SIZEOF ; }
"elemof"                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.ELEMOF ; }
"int"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.INT    ; }
"bool"                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL   ; }
"new"                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.NEW    ; }

"{"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.BEGIN  ; }
"}"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.END    ; }
"("                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LPAREN ; }
")"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RPAREN ; }
"["                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.LBRACKET; }
"]"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.RBRACKET; }
"->"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.FUNCRET; }
"<-"                                { parser.yylval = new ParserVal((Object)yytext()); return Parser.ASSIGN ; }
"+"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.EXPROP ; }
"*"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.TERMOP ; }
";"                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.SEMI   ; }
","                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.COMMA  ; }
"."                                 { parser.yylval = new ParserVal((Object)yytext()); return Parser.DOT    ; }

{relop}                             { parser.yylval = new ParserVal((Object)yytext()); return Parser.RELOP  ; }
{termop}                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.TERMOP ; }
{exprop}                            { parser.yylval = new ParserVal((Object)yytext()); return Parser.EXPROP ; }
{int}                               { parser.yylval = new ParserVal((Object)yytext()); return Parser.INT_LIT; }
{bool}                              { parser.yylval = new ParserVal((Object)yytext()); return Parser.BOOL_LIT; }
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()); return Parser.IDENT  ; }
{linecomment}                       { /* skip */ }
{newline}                           { /* skip */ }
{whitespace}                        { /* skip */ }
{blockcomment}                      { /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
