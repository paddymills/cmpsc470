/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.util.HashMap;
import java.io.StringReader;

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

  private HashMap<String,String> preproc_items;
  private String preproc_key;

  // for when we do a Reader switch, we need to store where the reader stopped
  private boolean is_on_secondary_reader = false;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;

    this.preproc_items = new HashMap<String,String>();
  }

  private int consume_parse(int parse_code) throws java.io.IOException {      
    // set value for parser
    parser.yylval = new ParserVal( (Object) yytext() );

    // check if we are on secondary buffer (because of preprocessor)
    //  if so, switch back
    if ( is_on_secondary_reader == true ) {
      yypopStream();
      is_on_secondary_reader = false;
    } 
    
    else {
      // update the line and column
      //  (even though they are really only needed when returning a ParserVal)
      //       ( we only do this if not on secondary stream,
      //         because if we are, these values were set when handling the preprocessor replacement )
      // remember, yyline/yycolumn are 0-indexed
      lineno = yyline   + 1;
      column = yycolumn + 1;
    }

    // skip line -> just print it back out
    if ( parse_code == SKIP) {
      System.out.print( yytext() );
      return 0; // will do nothing because all skip lines return nothing on a call to yylex();
    }

    return parse_code;
  }

  private void add_preproc(String val) {
    preproc_items.put(preproc_key, val);
  }

  private int handle_id(int parse_code) throws java.io.IOException {
    // check if token is a #define variable
    if ( preproc_items.containsKey(yytext()) ) {
      // get value to replace identifier with
      String replace_with = preproc_items.get(yytext());

      // save current line and column numbers
      //  (consume_parse will not do this if we are on our secondary stream
      //     because the numbers will be wrong)
      lineno = yyline   + 1;
      column = yycolumn + 1;

      // switch lexer to read from new buffer that has replacement text
      //  consume_parse will tell lexer to switch back after token is parsed
      yypushStream( new StringReader(replace_with) );
      is_on_secondary_reader = true;

      // parse replaced item
      return yylex();
    }

    // else: not a #define variable, so consume and return as usual
    return consume_parse( parse_code );
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
  yybegin(PREPROC_NEEDS_ID);
}

<PREPROC_NEEDS_ID> {
  {whitespace} {}
  {identifier} {
    preproc_key = yytext();
    yybegin(PREPROC_NEEDS_VAL);
  }
  [^] { System.err.println("\nError: #define has no identifier"); }
}

<PREPROC_NEEDS_VAL> {
  {whitespace} {}
  [^ \n]+ {
    add_preproc( yytext() );
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
{identifier}    { return handle_id    ( Parser.IDENT     ); }
{linecomment}   {        consume_parse( SKIP             ); }
{newline}       {        consume_parse( SKIP             ); }
{whitespace}    {        consume_parse( SKIP             ); }
{blockcomment}  {        consume_parse( SKIP             ); }


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { return consume_parse( -1 ); }
