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
    parser.yylval = new ParserVal((Object)yytext());
    lineno = yyline   + 1;
    column = yycolumn + 1;
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

"func"                              { consume(); return Parser.FUNC   ; }
"call"                              { consume(); return Parser.CALL   ; }
"return"                            { consume(); return Parser.RETURN ; }
"var"                               { consume(); return Parser.VAR    ; }
"if"                                { consume(); return Parser.IF     ; }
"else"                              { consume(); return Parser.ELSE   ; }
"while"                             { consume(); return Parser.WHILE  ; }
"print"                             { consume(); return Parser.PRINT  ; }
"sizeof"                            { consume(); return Parser.SIZEOF ; }
"elemof"                            { consume(); return Parser.ELEMOF ; }
"int"                               { consume(); return Parser.INT    ; }
"bool"                              { consume(); return Parser.BOOL   ; }
"new"                               { consume(); return Parser.NEW    ; }

"{"                                 { consume(); return Parser.BEGIN  ; }
"}"                                 { consume(); return Parser.END    ; }
"("                                 { consume(); return Parser.LPAREN ; }
")"                                 { consume(); return Parser.RPAREN ; }
"["                                 { consume(); return Parser.LBRACKET; }
"]"                                 { consume(); return Parser.RBRACKET; }
"->"                                { consume(); return Parser.FUNCRET; }
"<-"                                { consume(); return Parser.ASSIGN ; }
";"                                 { consume(); return Parser.SEMI   ; }
","                                 { consume(); return Parser.COMMA  ; }

{relop}                             { consume(); return Parser.RELOP  ; }
{termop}                            { consume(); return Parser.TERMOP ; }
{exprop}                            { consume(); return Parser.EXPROP ; }
{int}                               { consume(); return Parser.INT_LIT; }
{bool}                              { consume(); return Parser.BOOL_LIT; }
{identifier}                        { consume(); return Parser.IDENT  ; }
{linecomment}                       { consume(); return Parser.COMMENT; /* skip */ }
{newline}                           { consume(); return Parser.NEWLINE; /* skip */ }
{whitespace}                        { consume(); return Parser.WHITESPACE; /* skip */ }
{blockcomment}                      { consume(); return Parser.BLKCOMMENT; /* skip */ }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
