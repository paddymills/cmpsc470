public class Lexer
{
    private static final char EOF        =  0;

    private static final int ASCII_NUM_START = 48;
    private static final int ASCII_NUM_END = 57;
    private static final int ASCII_LOWER_START = 65;
    private static final int ASCII_LOWER_END = 90;
    private static final int ASCII_UPPER_START = 97;
    private static final int ASCII_UPPER_END = 122;


    private Parser          yyparser;       // parent parser object
    private java.io.Reader  reader;         // input stream
    public int              lineno;         // line number
    public int              column;         // column
    private int             current_col;    //current read column

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = reader;
        this.yyparser = yyparser;
        lineno = 1;
        column = 0;
        current_col = 0;
    }

    public char NextChar() throws Exception
    {
        // http://tutorials.jenkov.com/java-io/readers-writers.html
        int data = reader.read();
        current_col++;

        if(data == -1)
        {
            return EOF;
        }
        return (char)data;
    }
    public int Fail()
    {
        return -1;
    }

    // * If yylex reach to the end of file, return  0
    // * If there is an lexical error found, return -1
    // * If a proper lexeme is determined, return token <token-id, token-attribute> as follows:
    //   1. set token-attribute into yyparser.yylval
    //   2. return token-id defined in Parser
    //   token attribute can be lexeme, line number, colume, etc.
    public int yylex() throws Exception
    {
        int state = 0;

        while(true)
        {
            char c;
            switch(state)
            {
                case 0:
                    c = NextChar();
                    column = current_col;

                    if(c == ';') { state=   1; continue; }
                    if(c == EOF) { state=9999; continue; }

                    // the rest of the tokens
                    if(c == '+' || c == '-' || c == '*' || c == '/') { state=10; continue; }

                    if(c == '{') { 
                        yyparser.yylval = new ParserVal((Object)"{");   // set token attribute
                        return Parser.BEGIN;                            // return token type
                    }
                    if(c == '}') {
                        yyparser.yylval = new ParserVal((Object)"}");   // set token attribute
                        return Parser.END;                              // return token type
                    }

                    if(c == 'f') { state=2; continue; }
                    if(c == 'm') { state=3; continue; }

                    if(c == ' ') { continue; }
                    if (c == '\n') {
                        lineno++;
                        column = 0;
                        current_col = 0;

                        continue;
                    }

                    return Fail();
                case 1:
                    yyparser.yylval = new ParserVal((Object)";");   // set token-attribute to yyparser.yylval
                    return Parser.SEMI;                             // return token-name

                case 2:
                    c = NextChar();
                    if(c != 'u') { return Fail(); }
                    c = NextChar();
                    if(c != 'n') { return Fail(); }
                    c = NextChar();
                    if(c != 'c') { return Fail(); }

                    yyparser.yylval = new ParserVal((Object)"func");    // set token attribute
                    return Parser.FUNC;                                 // return token type
                case 3:
                    c = NextChar();
                    if(c != 'a') { return Fail(); }
                    c = NextChar();
                    if(c != 'i') { return Fail(); }
                    c = NextChar();
                    if(c != 'n') { return Fail(); }

                    yyparser.yylval = new ParserVal((Object)"main");    // set token attribute
                    return Parser.ID;                                   // return token type


                case 9999:
                    return EOF;                                     // return end-of-file symbol
            }
        }
    }
}
