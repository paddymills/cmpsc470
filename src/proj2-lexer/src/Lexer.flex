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
%line
%column
%byaccj

%state PREPROC_NEEDS_ID, PREPROC_NEEDS_VAL, PREPROC_NEEDS_TERM
  

%{
  public static final int SKIP = 99;

  public Parser   parser;
  public int      lineno;
  public int      column;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
  }

  private int consume_parse(int parse_code) {
    // skip line -> just print it back out
    if ( parse_code == SKIP)
      System.out.print( yytext() );
      
    // else, set value for parser
    else
      parser.yylval = new ParserVal( (Object) yytext() ) ;

    // we will update the line and column
    //  (even though they are really only needed when returning a ParserVal)
    // remember, yyline/yycolumn are 0-indexed
    lineno = yyline   + 1;
    column = yycolumn + 1;

    return parse_code;
  }
%}

op          = [\+\-\*\/]|and|or|not
relop       = [<>]=?|[<>!]?=
int         = [0-9]+
float       = [0-9]+("."[0-9]+)?
identifier  = [a-zA-Z][a-zA-Z0-9_]*
newline     = \n
whitespace  = [ \t\r]+
linecomment = "##".*
blockcomment= "#{"(.|\n)*"}#"

%%
"#define" {
  System.out.print("#define found");
  yybegin(PREPROC_NEEDS_ID);
}

<PREPROC_NEEDS_ID> {
  {whitespace} {}
  {identifier} {
    System.out.print("<ID: " + yytext() + ", ");
    yybegin(PREPROC_NEEDS_VAL);
  }
  [^] { System.err.println("\nError: #define has no identifier"); }
}

<PREPROC_NEEDS_VAL> {
  {whitespace} {}
  [^ ]+ {
    System.out.print("VAL: " + yytext() + ">");
    yybegin(PREPROC_NEEDS_TERM);
  }
  [^] { System.err.println("\nError: #define has no value"); }
}

<PREPROC_NEEDS_TERM> {
  {whitespace} {}
  {newline} {
    System.out.print("\n");
    yybegin(YYINITIAL);
  }
  [^] { System.err.println("\nError: unexpected post-#define characters: " + yytext()); }
}

"print"         { return consume_parse( Parser.PRINT     ); }
"func"          { return consume_parse( Parser.FUNC      ); }
"var"           { return consume_parse( Parser.VAR       ); }
"void"          { return consume_parse( Parser.VOID      ); }
"bool"          { return consume_parse( Parser.BOOL      ); }
"int"           { return consume_parse( Parser.INT       ); }
"float"         { return consume_parse( Parser.FLOAT     ); }
"struct"        { return consume_parse( Parser.STRUCT    ); }
"size"          { return consume_parse( Parser.SIZE      ); }
"new"           { return consume_parse( Parser.NEW       ); }
"if"            { return consume_parse( Parser.IF        ); }
"else"          { return consume_parse( Parser.ELSE      ); }
"while"         { return consume_parse( Parser.WHILE     ); }
"return"        { return consume_parse( Parser.RETURN    ); }
"break"         { return consume_parse( Parser.BREAK     ); }
"continue"      { return consume_parse( Parser.CONTINUE  ); }
"true"          { return consume_parse( Parser.BOOL_LIT  ); }
"false"         { return consume_parse( Parser.BOOL_LIT  ); }

"{"             { return consume_parse( Parser.BEGIN     ); }
"}"             { return consume_parse( Parser.END       ); }
"("             { return consume_parse( Parser.LPAREN    ); }
")"             { return consume_parse( Parser.RPAREN    ); }
"["             { return consume_parse( Parser.LBRACKET  ); }
"]"             { return consume_parse( Parser.RBRACKET  ); }
";"             { return consume_parse( Parser.SEMI      ); }
","             { return consume_parse( Parser.COMMA     ); }
"."             { return consume_parse( Parser.DOT       ); }
"&"             { return consume_parse( Parser.ADDR      ); }
"<-"            { return consume_parse( Parser.ASSIGN    ); }
"->"            { return consume_parse( Parser.FUNCRET   ); }

{op}            { return consume_parse( Parser.OP        ); }
{relop}         { return consume_parse( Parser.RELOP     ); }
{int}           { return consume_parse( Parser.INT_LIT   ); }
{float}         { return consume_parse( Parser.FLOAT_LIT ); }
{identifier}    { return consume_parse( Parser.IDENT     ); }
{linecomment}   {        consume_parse( SKIP             ); }
{newline}       {        consume_parse( SKIP             ); }
{whitespace}    {        consume_parse( SKIP             ); }
{blockcomment}  {        consume_parse( SKIP             ); }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { return consume_parse( -1 ); }
