import java.util.HashMap;
import java.util.Map;

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
        this.compiler       = compiler;
        this.lexer          = new Lexer(r, this);
        this.symbol_table   = new HashMap<Integer, Object>();
    }

    Lexer                       lexer;
    Compiler                    compiler;
    HashMap<Integer, Object>    symbol_table;
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
            int symbol_table_id = -1;

            // handle symbol table
            if ( token == IDENT )
                symbol_table_id = handle_symbol_table(attr);
                

            // print `<{token name}`
            System.out.print("<" + get_token_name(token));

            switch (token) {
                case BOOL_LIT:
                    System.out.print(", attr:" + attr);
                    break;

                case IDENT:
                    System.out.print(", attr:sym-id:" + symbol_table_id);
                    break;
            }

            // print `, {line}:{column}>`
            System.out.print(", " + lexer.lineno + ":" + lexer.column +">");
            
            // System.out.print("<token-id:" + token + ", token-attr:" + attr + ", lineno:" + lexer.lineno + ", col:" + lexer.column + ">");
        }
    }

    private String get_token_name(int token) {
        if ( token == PRINT    ) return "PRINT";
        if ( token == FUNC     ) return "FUNC";
        if ( token == VAR      ) return "VAR";
        if ( token == VOID     ) return "VOID";
        if ( token == BOOL     ) return "BOOL";
        if ( token == INT      ) return "INT";
        if ( token == FLOAT    ) return "FLOAT";
        if ( token == STRUCT   ) return "STRUCT";
        if ( token == SIZE     ) return "SIZE";
        if ( token == NEW      ) return "NEW";
        if ( token == IF       ) return "IF";
        if ( token == ELSE     ) return "ELSE";
        if ( token == WHILE    ) return "WHILE";
        if ( token == RETURN   ) return "RETURN";
        if ( token == BREAK    ) return "BREAK";
        if ( token == CONTINUE ) return "CONTINUE";

        if ( token == BOOL_LIT ) return "BOOL_VALUE";

        if ( token == BEGIN    ) return "BEGIN";
        if ( token == END      ) return "END";
        if ( token == LPAREN   ) return "LPAREN";
        if ( token == RPAREN   ) return "RPAREN";
        if ( token == LBRACKET ) return "LBRACKET";
        if ( token == RBRACKET ) return "RBRACKET";
        if ( token == SEMI     ) return "SEMI";
        if ( token == COMMA    ) return "COMMA";
        if ( token == DOT      ) return "DOT";
        if ( token == ADDR     ) return "ADDR";
        if ( token == ASSIGN   ) return "ASSIGN";
        if ( token == FUNCRET  ) return "FUNCRET";
        if ( token == OP       ) return "OP";
        if ( token == RELOP    ) return "RELOP";

        if ( token == INT_LIT  ) return "INT_VALUE";
        if ( token == FLOAT_LIT) return "FLOAT_VALUE";
        if ( token == IDENT    ) return "ID";

        // not needed as long as this gets called only from a keyword match
        return "unknown token with ID " + token;
    }

    private int handle_symbol_table(Object attr) {
        int id = 0;

        // check if in symbol table
        if ( !symbol_table.containsValue(attr) ) {
            // If not in symbol table, add
            id = symbol_table.size();
            symbol_table.put(id, attr);
            
            System.out.print("<<new symbol table entity [" + id + ", \"" + attr + "\"]>>");

            return id;
        }

        // else, search symbol table for attr
        // got inspiration from https://stackoverflow.com/questions/1383797/java-hashmap-how-to-get-key-from-value
        for ( Map.Entry<Integer,Object> entry : symbol_table.entrySet() ) {
            if ( entry.getValue().equals(attr) )
                return entry.getKey();
        }

        // should never happen, but lets make our editor happy
        return -1;
    }
}
