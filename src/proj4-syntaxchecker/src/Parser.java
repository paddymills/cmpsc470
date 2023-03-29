import java.util.List;
import java.util.ArrayList;

public class Parser
{
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    // --- Original set from startup ---
    // public static final int INT         = 11;
    // public static final int PTR         = 12;
    // public static final int BEGIN       = 13;
    // public static final int END         = 14;
    // public static final int LPAREN      = 15;
    // public static final int RPAREN      = 16;
    // public static final int ASSIGN      = 17;
    // public static final int EXPROP      = 18;
    // public static final int TERMOP      = 19;
    // public static final int SEMI        = 20;
    // public static final int INT_LIT     = 21;
    // public static final int IDENT       = 22;
    // public static final int FUNCRET     = 23;
    // public static final int FUNC        = 24;

    // my re-do
    public static final int FUNC            = 11;
    public static final int CALL            = 12;
    public static final int RETURN          = 13;
    public static final int VAR             = 14;
    public static final int IF              = 15;
    public static final int ELSE            = 16;
    public static final int WHILE           = 17;
    public static final int PRINT           = 18;
    public static final int SIZEOF          = 19;
    public static final int ELEMOF          = 20;
    public static final int BEGIN           = 21;
    public static final int END             = 22;
    public static final int LPAREN          = 23;
    public static final int RPAREN          = 24;
    public static final int LBRACKET        = 25;
    public static final int RBRACKET        = 26;
    public static final int INT             = 27;
    public static final int BOOL            = 28;
    public static final int NEW             = 29;
    public static final int ASSIGN          = 30;
    public static final int FUNCRET         = 31;
    public static final int RELOP           = 32;
    public static final int EXPROP          = 33;
    public static final int TERMOP          = 34;
    public static final int SEMI            = 35;
    public static final int COMMA           = 36;
    public static final int DOT             = 37;
    public static final int BOOL_LIT        = 38;
    public static final int INT_LIT         = 39;
    public static final int IDENT           = 40;
    public static final int COMMENT         = 41;
    public static final int NEWLINE         = 42;
    public static final int WHITESPACE      = 43;
    public static final int BLKCOMMENT      = 44;

    public class Token
    {
        public int       type;
        public ParserVal attr;
        public Token(int type, ParserVal attr) {
            this.type   = type;
            this.attr   = attr;
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String            _errormsg;
    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null;                  // _token is initially null
        Advance();                          // make _token to point the first token by calling Advance()
    }

    public void Advance() throws Exception
    {
        int token_type = _lexer.yylex();                                    // get next/first token from lexer
        if(token_type ==  0)      _token = new Token(ENDMARKER , null);     // if  0 => token is endmarker
        else if(token_type == -1) _token = new Token(LEXERROR  , yylval);   // if -1 => there is a lex error
        else                      _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception
    {
        boolean match = (token_type == _token.type);
        String lexeme = "";

        if(match == false)                          // if token does not match
            throw new Exception("\"" + expected_token(token_type) + "\" is expected instead of \"" + expected_token(_token.type) + "\" at " + (_lexer.lineno+1) + ":" + _lexer.column + ".");  // throw exception (indicating parsing error in this assignment)

        if(_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
    }

    public int yyparse() throws Exception
    {
        try
        {
            _parsetree = program();
            return 0;
        }
        catch(Exception e)
        {
            _errormsg = e.getMessage();
            return -1;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //      program -> decl_list
    //    decl_list -> decl_list'
    //   decl_list' -> fun_decl decl_list'  |  eps
    //     fun_decl -> FUNC IDENT LPAREN params RPAREN FUNCRET prim_type BEGIN local_decls stmt_list END
    //    prim_type -> INT
    //       params -> eps
    //  local_decls -> local_decls'
    // local_decls' -> eps
    //    stmt_list -> stmt_list'
    //   stmt_list' -> eps
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public ParseTree.Program program() throws Exception
    {
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("error 1");
    }
    public List<ParseTree.FuncDecl> decl_list() throws Exception
    {
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("error 2");
    }
    public List<ParseTree.FuncDecl> decl_list_() throws Exception
    {
        switch(_token.type)
        {
            case FUNC:
                ParseTree.FuncDecl       v1 = fun_decl  ();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("error 3");
    }
    public ParseTree.FuncDecl fun_decl() throws Exception
    {
        switch(_token.type)
        {
            case FUNC:
                                                Match(FUNC)   ;
                String                    v02 = Match(IDENT)  ;
                                                Match(LPAREN) ;
                List<ParseTree.Param>     v04 = params()      ;
                                                Match(RPAREN) ;
                                                Match(FUNCRET);
                ParseTree.PrimType        v07 = prim_type()   ;
                                                Match(BEGIN)  ;
                List<ParseTree.LocalDecl> v09 = local_decls() ;
                List<ParseTree.Stmt>      v10 = stmt_list()   ;
                                                Match(END)    ;
                return new ParseTree.FuncDecl(v02,v07,v04,v09,v10);
        }
        throw new Exception("error 4");
    }
    public List<ParseTree.Param> params() throws Exception
    {
        switch(_token.type)
        {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
            case INT:
            case BOOL:
                return param_list();
        }
        throw new Exception("error 5");
    }

    public List<ParseTree.Param> param_list() throws Exception
    {
        switch(_token.type)
        {
            case INT:
            case BOOL:
            {
                ParseTree.Param       p1 = param();
                List<ParseTree.Param> p2 = param_list_();
                p2.add(0, p1);
                return p2;
            }
        }
        throw new Exception("error 6");
    }
    public List<ParseTree.Param> param_list_() throws Exception
    {
        switch(_token.type)
        {
            case COMMA:
            {
                Match(COMMA);
                ParseTree.Param       p1 = param();
                List<ParseTree.Param> p2 = param_list_();
                p2.add(0, p1);
                return p2;
            }
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("error 7");
    }
    public ParseTree.Param param() throws Exception
    {
        switch(_token.type)
        {
            case INT:
            case BOOL:
            {
                ParseTree.TypeSpec ts = type_spec();
                String             id = Match(IDENT);
                return new ParseTree.Param(id, ts);
            }
        }
        throw new Exception("error 8");
    }
    public ParseTree.TypeSpec type_spec() throws Exception
    {
        switch(_token.type)
        {
            case INT:
            case BOOL:
            {
                ParseTree.PrimType  pt = prim_type();
                ParseTree.TypeSpec_ ts = type_spec_();
                return new ParseTree.TypeSpec(pt, ts);
            }
        }
        throw new Exception("error 9");
    }
    public ParseTree.TypeSpec_ type_spec_() throws Exception
    {
        switch(_token.type)
        {
            case LBRACKET:
                Match(LBRACKET);
                Match(RBRACKET);
                return new ParseTree.TypeSpec_Array();
            case IDENT:
                return new ParseTree.TypeSpec_Value();
        }
        // return TypeSpec_Value or TypeSpec_Array
        throw new Exception("error 10");
    }

    public ParseTree.PrimType prim_type() throws Exception
    {
        switch(_token.type)
        {
            case INT:
            {
                Match(INT);
                return new ParseTree.PrimTypeInt();
            }
            case BOOL:
            {
                Match(BOOL);
                return new ParseTree.PrimTypeBool();
            }
        }
        throw new Exception("error 11");
    }
    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        switch(_token.type)
        {
            case RETURN:
            case VAR:
            case IF:
            case WHILE:
            case PRINT:
            case BEGIN:
            case END:
            case IDENT:
                return local_decls_();
        }
        throw new Exception("error 12");
    }
    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        switch(_token.type)
        {
            case VAR:
            {
                ParseTree.LocalDecl       ld = local_decl();
                List<ParseTree.LocalDecl> lds = local_decls_();
                lds.add(0, ld);
                return lds;
            }
            case RETURN:
            case IF:
            case WHILE:
            case PRINT:
            case BEGIN:
            case END:
            case IDENT:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("error 13");
    }
    public ParseTree.LocalDecl local_decl() throws Exception
    {
        switch(_token.type)
        {
            case VAR:
            {
                                          Match(VAR);
                ParseTree.TypeSpec ts   = type_spec();
                String             id   = Match(IDENT);
                                          Match(SEMI);

                return new ParseTree.LocalDecl(id, ts);
            }
        }
        throw new Exception("error14");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        switch(_token.type)
        {
            case RETURN:
            case IF:
            case WHILE:
            case PRINT:
            case BEGIN:
            case END:
            case IDENT:
                return stmt_list_();
        }
        throw new Exception("error 15");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        switch(_token.type)
        {
            case RETURN:
            case IF:
            case WHILE:
            case PRINT:
            case BEGIN:
            case IDENT:
            {
                ParseTree.Stmt       stmt  = stmt();
                List<ParseTree.Stmt> stmts = stmt_list_();
                stmts.add(0, stmt);
                return stmts;
            }
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("error 16");
    }

    public ParseTree.Stmt stmt() throws Exception
    {
        switch(_token.type)
        {
            case RETURN:
                return return_stmt();
            case IF:
                return if_stmt();
            case WHILE:
                return while_stmt();
            case PRINT:
                return print_stmt();
            case BEGIN:
                return compound_stmt();
            case IDENT:
                return assign_stmt();
        }
        throw new Exception("error 17");
    }
    public ParseTree.StmtAssign assign_stmt() throws Exception
    {
        switch(_token.type)
        {
            case IDENT:
            {
                String           id = Match(IDENT);
                                      Match(ASSIGN);
                ParseTree.Expr expr = expr();
                                      Match(SEMI);

                return new ParseTree.StmtAssign(id, expr);
            }
        }
        throw new Exception("error 18");
    }
    public ParseTree.StmtPrint print_stmt() throws Exception
    {
        switch(_token.type)
        {
            case PRINT:
            {
                                      Match(PRINT);
                ParseTree.Expr expr = expr();
                                      Match(SEMI);

                return new ParseTree.StmtPrint(expr);
            }

        }
        throw new Exception("error 19");
    }
    public ParseTree.StmtReturn return_stmt() throws Exception
    {
        switch(_token.type)
        {
            case RETURN:
            {
                                      Match(RETURN);
                ParseTree.Expr expr = expr();
                                      Match(SEMI);

                return new ParseTree.StmtReturn(expr);
            }
        }
        throw new Exception("error 20");
    }
    public ParseTree.StmtIf if_stmt() throws Exception
    {
        switch(_token.type)
        {
            case IF:
            {
                                      Match(IF);
                                      Match(LPAREN);
                ParseTree.Expr expr = expr();
                                      Match(RPAREN);
                ParseTree.Stmt s1   = stmt();
                                      Match(ELSE);
                ParseTree.Stmt s2   = stmt();

                return new ParseTree.StmtIf(expr, s1, s2);
            }
        }
        throw new Exception("error 21");
    }
    public ParseTree.StmtWhile while_stmt() throws Exception
    {
        switch(_token.type)
        {
            case WHILE:
            {
                                      Match(WHILE);
                                      Match(LPAREN);
                ParseTree.Expr expr = expr();
                                      Match(RPAREN);
                ParseTree.Stmt stmt = stmt();

                return new ParseTree.StmtWhile(expr, stmt);
            }
        }
        throw new Exception("error 22");
    }
    public ParseTree.StmtCompound compound_stmt() throws Exception
    {
        switch(_token.type)
        {
            case BEGIN:
            {
                                               Match(BEGIN);
                List<ParseTree.LocalDecl> ld = local_decls();
                List<ParseTree.Stmt>      sl = stmt_list();
                                               Match(END);

                return new ParseTree.StmtCompound(ld, sl);
            }
        }
        throw new Exception("error 23");
    }
    public List<ParseTree.Arg> args() throws Exception
    {
        switch(_token.type)
        {
            case CALL:
            case SIZEOF:
            case ELEMOF:
            case NEW:
            case LPAREN:
            case BOOL_LIT:
            case INT_LIT:
            case IDENT:
                return arg_list();
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("error 24");
    }
    public List<ParseTree.Arg> arg_list() throws Exception
    {
        switch(_token.type)
        {
            case CALL:
            case SIZEOF:
            case ELEMOF:
            case NEW:
            case LPAREN:
            case BOOL_LIT:
            case INT_LIT:
            case IDENT:
            {
                ParseTree.Expr      expr = expr();
                List<ParseTree.Arg> al   = arg_list_();

                al.add(0, new ParseTree.Arg(expr));
                return al;
            }
        }
        throw new Exception("error 25");
    }
    public List<ParseTree.Arg> arg_list_() throws Exception
    {
        switch(_token.type)
        {
            case COMMA:
            {
                Match(COMMA);
                ParseTree.Expr      expr = expr();
                List<ParseTree.Arg> al   = arg_list_();

                al.add(0, new ParseTree.Arg(expr));
                return al;
            }
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("error 26");
    }
    public ParseTree.Expr expr() throws Exception
    {
        switch(_token.type)
        {
            case CALL:
            case SIZEOF:
            case ELEMOF:
            case NEW:
            case LPAREN:
            case BOOL_LIT:
            case INT_LIT:
            case IDENT:
            {
                ParseTree.Term  term = term();
                ParseTree.Expr_ expr = expr_();

                return new ParseTree.Expr(term, expr);
            }
        }
        throw new Exception("error 27");
    }
    public ParseTree.Expr_ expr_() throws Exception
    {
        switch(_token.type)
        {
            case RELOP:
            {
                String relop         = Match(RELOP);
                ParseTree.Term  term = term();
                ParseTree.Expr_ expr = expr_();

                return new ParseTree.Expr_(relop, term, expr);
            }
            case EXPROP:
            {
                String exprop        = Match(EXPROP);
                ParseTree.Term  term = term();
                ParseTree.Expr_ expr = expr_();

                return new ParseTree.Expr_(exprop, term, expr);
            }

            case RPAREN:
                return null;
        }
        throw new Exception("error 28");
    }
    public ParseTree.Term term() throws Exception
    {
        switch(_token.type)
        {
            case CALL:
            case SIZEOF:
            case ELEMOF:
            case NEW:
            case LPAREN:
            case BOOL_LIT:
            case INT_LIT:
            case IDENT:
            {
                ParseTree.Factor factor = factor();
                ParseTree.Term_  term   = term_();

                return new ParseTree.Term(factor, term);
            }

        }
        throw new Exception("error 29");
    }
    public ParseTree.Term_ term_() throws Exception
    {
        switch(_token.type)
        {
            case TERMOP:
            {
                String           termop = Match(TERMOP);
                ParseTree.Factor factor = factor();
                ParseTree.Term_  term   = term_();
    
                return new ParseTree.Term_(termop, factor, term);
            }
            case RELOP:
            case EXPROP:
            case SEMI:
                return null;
        }
        throw new Exception("error 30");
    }
    public ParseTree.Factor factor() throws Exception
    {
        switch(_token.type)
        {
            case CALL:
            {
                                        Match(CALL);
                String id             = Match(IDENT);
                                        Match(LPAREN);
                List<ParseTree.Arg> a = args();
                                        Match(RPAREN);

                return new ParseTree.FactorCall(id, a);
            }
            case SIZEOF:
            {
                            Match(SIZEOF);
                String id = Match(IDENT);

                return new ParseTree.FactorSizeof(id);
            }
            case ELEMOF:
            {
                                      Match(ELEMOF);
                String         id   = Match(IDENT);
                                      Match(LBRACKET);
                ParseTree.Expr expr = expr();
                                      Match(RBRACKET);

                return new ParseTree.FactorElemof(id, expr);
            }
            case NEW:
            {
                                       Match(NEW);
                ParseTree.PrimType p = prim_type();
                                       Match(LBRACKET);
                ParseTree.Expr expr  = expr();
                                       Match(RBRACKET);

                return new ParseTree.FactorNew(p, expr);
            }
            case LPAREN:
            {
                                      Match(LPAREN);
                ParseTree.Expr expr = expr();
                                      Match(RPAREN);

                return new ParseTree.FactorParen(expr);
            }
            case BOOL_LIT:
                return new ParseTree.FactorBoolLit( Boolean.parseBoolean(Match(BOOL_LIT)) );
            case INT_LIT:
                return new ParseTree.FactorIntLit( Integer.parseInt(Match(INT_LIT)) );
            case IDENT:
                return new ParseTree.FactorIdent( Match(IDENT) );
        }
        // returns FactorParen or FactorIdent or FactorIntLit or ... or FactorSizeof
        throw new Exception("error 31");
    }

    private String expected_token(int token) {
        if ( token == FUNC      ) return "func";
        if ( token == CALL      ) return "call";
        if ( token == RETURN    ) return "return";
        if ( token == VAR       ) return "var";
        if ( token == IF        ) return "if";
        if ( token == ELSE      ) return "else";
        if ( token == WHILE     ) return "while";
        if ( token == PRINT     ) return "print";
        if ( token == SIZEOF    ) return "sizeof";
        if ( token == ELEMOF    ) return "elemof";
        if ( token == BEGIN     ) return "{";
        if ( token == END       ) return "}";
        if ( token == LPAREN    ) return "(";
        if ( token == RPAREN    ) return "}";
        if ( token == LBRACKET  ) return "[";
        if ( token == RBRACKET  ) return "]";
        if ( token == INT       ) return "int";
        if ( token == BOOL      ) return "bool";
        if ( token == NEW       ) return "new";
        if ( token == ASSIGN    ) return "<-";
        if ( token == FUNCRET   ) return "->";
        if ( token == RELOP     ) return "one of <|>|<=|>=|=|!=";
        if ( token == EXPROP    ) return "one of +,=,or";
        if ( token == TERMOP    ) return "*,/,and";
        if ( token == SEMI      ) return ";";
        if ( token == COMMA     ) return ",";
        if ( token == DOT       ) return ".";
        if ( token == BOOL_LIT  ) return "true or false";
        if ( token == INT_LIT   ) return "an integer";
        if ( token == IDENT     ) return "{identifier}";

        // not needed as long as this gets called only from a keyword match
        return "unknown token with ID " + token;
    }
}
