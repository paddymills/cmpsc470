public class Parser
{
    public static final int PRINT       = 10; // "print"
    public static final int FUNC        = 11; // "func"
    public static final int VAR         = 12; // "var"
    public static final int VOID        = 13; // "void"
    public static final int BOOL        = 14; // "bool"
    public static final int INT         = 15; // "int"
    public static final int FLOAT       = 16; // "float"
    public static final int STRUCT      = 17; // "struct"
    public static final int SIZE        = 18; // "size"
    public static final int NEW         = 19; // "new"
    public static final int IF          = 20; // "if"
    public static final int ELSE        = 22; // "else"
    public static final int BEGIN       = 23; // "{"
    public static final int END         = 24; // "}"
    public static final int WHILE       = 25; // "while"
    public static final int RETURN      = 26; // "return"
    public static final int BREAK       = 27; // "break"
    public static final int CONTINUE    = 28; // "continue"
    public static final int LPAREN      = 29; // "("
    public static final int RPAREN      = 30; // ")"
    public static final int LBRACKET    = 31; // "["
    public static final int RBRACKET    = 32; // "]"
    public static final int SEMI        = 33; // ";"
    public static final int COMMA       = 34; // ","
    public static final int DOT         = 35; // "."
    public static final int ADDR        = 36; // "&"
    public static final int ASSIGN      = 38; // "<-"
    public static final int FUNCRET     = 39; // "->"
    public static final int OP          = 40; // "+", "-", "*", "/", "and", "or", "not"
    public static final int RELOP       = 41; // "=", "!=", "<", ">", "<=", ">="
    public static final int BOOL_LIT    = 42; // "true", "false"
    public static final int INT_LIT     = 43; // {int}
    public static final int FLOAT_LIT   = 44; // {float}
    public static final int IDENT       = 45; // {identifier}

    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        this.compiler = compiler;
        this.lexer    = new Lexer(r, this);
    }

    Lexer            lexer;
    Compiler         compiler;
    public ParserVal yylval;

    public int yyparse() throws java.io.IOException
    {
        while ( true )
        {
            int token = lexer.yylex();
            if(token == 0)
            {
                // EOF is reached
                return 0;
            }
            if(token == -1)
            {
                // error
                return -1;
            }

            Object attr = yylval.obj;
            System.out.println("<token-id:" + token + ", token-attr:" + attr + ", lineno:" + lexer.lineno + ", col:" + lexer.column + ">");
        }
    }
}
