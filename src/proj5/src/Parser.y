/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2001 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * This is a modified version of the example from                          *
 *   http://www.lincom-asg.com/~rjamison/byacc/                            *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%{
import java.io.*;
%}

%right  ASSIGN
%left   OR
%left   AND
%right  NOT
%left   EQ      NE
%left   LE      LT      GE      GT
%left   ADD     SUB
%left   MUL     DIV     MOD

%token <obj>    EQ   NE   LE   LT   GE   GT
%token <obj>    ADD  SUB  MUL  DIV  MOD
%token <obj>    OR   AND  NOT

%token <obj>    IDENT     INT_LIT   BOOL_LIT

%token <obj> BOOL  INT
%token <obj> FUNC  IF  THEN  ELSE  WHILE  PRINT  RETURN  CALL
%token <obj> BEGIN  END  LPAREN  RPAREN
%token <obj> ASSIGN  VAR  SEMI  COMMA  FUNCRET

// not needed at the moment
%token <obj> NEWLINE WHITESPACE COMMENT BLKCOMMENT

%type <obj> program   decl_list  decl
%type <obj> fun_decl  local_decls  local_decl  type_spec  prim_type
%type <obj> params  param_list  param  args  arg_list
%type <obj> stmt_list  stmt  assign_stmt  print_stmt  return_stmt  if_stmt  while_stmt  compound_stmt     
%type <obj> expr

%%
program         : decl_list                 { Debug("program -> decl_list"); $$ = program($1); }
                ;

decl_list       : decl_list decl            { Debug("decl_list -> decl_list decl"); $$ = decllist($1,$2); }
                |                           { Debug("decl_list -> eps"           ); $$ = decllist(); }
                ;

decl            : fun_decl                  { Debug("decl -> fun_decl"); $$ = decl($1); }
                ;

fun_decl        : FUNC IDENT LPAREN params RPAREN FUNCRET prim_type BEGIN local_decls
                        {
                            Debug("fun_decl -> FUNC ID(params)->prim_type BEGIN local_decls");
                            $<obj>$ = fundecl($2, $4, $7, $9          );
                        }
                    stmt_list END
                        {
                            Debug("                                           stmt_list END");
                            $$ =      fundecl($2, $4, $7, $9, $11, $12);
                        }
                ;

params          : param_list                { Debug("params -> param_list"); $$ = params____paramlist($1); }
                |                           { Debug("params -> eps"       ); $$ = params____eps(); }
                ;

param_list      : param_list COMMA param    { Debug("param_list -> param_list, param"); $$ = params____paramlist_param($1, $3); }
                | param                     { Debug("param_list -> param"            ); $$ = params____param($1); }
                ;

param           : VAR type_spec IDENT       { Debug("param -> type_spec"); $$ = params($2, $3); }
                ;

type_spec       : prim_type                 { Debug("type_spec -> prim_type"); $$ = typespec($1); }
                ;

prim_type       : INT                       { Debug("prim_type -> INT" ); $$ = primtype($1); }
                | BOOL                      { Debug("prim_type -> BOOL"); $$ = primtype($1); }
                ;

local_decls     : local_decls  local_decl   { Debug("local_decls -> local_decls local_decl"); $$ = localdecls($1, $2); }
                |                           { Debug("local_decls -> eps"                   ); $$ = localdecls(); }
                ;

local_decl      : VAR  type_spec  IDENT  SEMI { Debug("local_decl -> VAR type_spec IDENT SEMI"); $$ = localdecl($2, $3); }
                ;

stmt_list       : stmt_list stmt            { Debug("stmt_list -> stmt_list stmt"); $$ = stmtlist($1, $2); }
                |                           { Debug("stmt_list -> eps"           ); $$ = stmtlist(); }
                ;

stmt            : assign_stmt               { Debug("stmt -> assign_stmt"  ); $$ = stmt_assign  ($1); }
                | print_stmt                { Debug("stmt -> print_stmt"   ); $$ = stmt_print   ($1); }
                | return_stmt               { Debug("stmt -> return_stmt"  ); $$ = stmt_return  ($1); }
                | if_stmt                   { Debug("stmt -> if_stmt"      ); $$ = stmt_if      ($1); }
                | while_stmt                { Debug("stmt -> while_stmt"   ); $$ = stmt_while   ($1); }
                | compound_stmt             { Debug("stmt -> compound_stmt"); $$ = stmt_compound($1); }
                ;

assign_stmt     : IDENT ASSIGN expr SEMI
                    { Debug("assign_stmt -> IDENT <- expr ;"); $$ = assignstmt($1,$2,$3); }
                ;

print_stmt      : PRINT expr SEMI
                    { Debug("print_stmt -> expr"); $$ = print_stmt($2); }
                ;

return_stmt     : RETURN expr SEMI
                    { Debug("return_stmt -> RETURN expr ;"); $$ = returnstmt($2); }
                ;

if_stmt         : IF  LPAREN  expr  RPAREN  stmt  ELSE  stmt
                    { Debug("if_stmt -> expr"); $$ = if_stmt($3, $5, $7); }
                ;

while_stmt      : WHILE  LPAREN  expr  RPAREN  stmt
                    { Debug("while_stmt -> expr"); $$ = while_stmt($3, $5); }
                ;

compound_stmt   : BEGIN  local_decls  stmt_list  END
                    { Debug("compound_stmt -> expr"); $$ = compound_stmt($2, $3); }
                ;

args            : arg_list { Debug("args -> arg_list"); $$ = args($1); }
                |          { Debug("args -> eps"     ); $$ = args(); }
                ;

arg_list        : arg_list  COMMA  expr { Debug("arg_list -> arg_list, expr"); $$ = arglist($1, $2); }
                | expr                  { Debug("arg_list -> expr"          ); $$ = arglist($1); }
                ;

expr            : expr  ADD  expr       { Debug("expr -> expr + expr");  $$ = expr_add($1, $3); }
                | expr  SUB  expr       { Debug("expr -> expr - expr");  $$ = expr_sub($1, $3); }
                | expr  MUL  expr       { Debug("expr -> expr * expr");  $$ = expr_mult($1, $3); }
                | expr  DIV  expr       { Debug("expr -> expr / expr");  $$ = expr_div($1, $3); }
                | expr  MOD  expr       { Debug("expr -> expr % expr");  $$ = expr_mod($1, $3); }
                | expr  EQ   expr       { Debug("expr -> expr = expr");  $$ = expr_eq($1, $3); }
                | expr  NE   expr       { Debug("expr -> expr != expr"); $$ = expr_ne($1, $3); }
                | expr  LE   expr       { Debug("expr -> expr <= expr"); $$ = expr_le($1, $3); }
                | expr  LT   expr       { Debug("expr -> expr < expr");  $$ = expr_lt($1, $3); }
                | expr  GE   expr       { Debug("expr -> expr >= expr"); $$ = expr_ge($1, $3); }
                | expr  GT  expr        { Debug("expr -> expr > expr");  $$ = expr_gt($1, $3); }
                | expr  AND  expr       { Debug("expr -> expr && expr"); $$ = expr_and($1, $3); }
                | expr  OR   expr       { Debug("expr -> expr || expr"); $$ = expr_or($1, $3); }
                | NOT  expr             { Debug("expr -> !expr");        $$ = expr_not($2); }
                | LPAREN  expr  RPAREN  { Debug("expr -> ( expr )");     $$ = expr_paren($2); }
                | IDENT                 { Debug("expr -> ident");        $$ = expr_id($1); }
                | INT_LIT               { Debug("expr -> int");          $$ = expr_int($1); }
                | BOOL_LIT              { Debug("expr -> bool");         $$ = expr_bool($1); }
                | CALL  IDENT  LPAREN  args  RPAREN
                    { Debug("expr -> CALL ident(args)"); $$ = expr_call($2, $4); }
                ;

%%
    private Lexer lexer;
    private Token last_token;

    private int yylex () {
        int yyl_return = -1;
        try {
            yylval = new ParserVal(0);
            yyl_return = lexer.yylex();
            last_token = (Token)yylval.obj;
        }
        catch (IOException e) {
            System.out.println("IO error :"+e);
        }
        return yyl_return;
    }


    public void yyerror (String error) {
        //System.out.println ("Error message for " + lexer.lineno+":"+lexer.column +" by Parser.yyerror(): " + error);
        int last_token_lineno = 0;
        int last_token_column = 0;
        System.out.println ("Error message by Parser.yyerror() at near " + last_token_lineno+":"+last_token_column + ": " + error);
    }


    public Parser(Reader r, boolean yydebug) {
        this.lexer   = new Lexer(r, this);
        this.yydebug = yydebug;
    }
