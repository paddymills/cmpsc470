
import java.io.BufferedReader;
import java.util.Arrays;

public class Lexer
{
    private static final char EOF       =  0;
    private static final int BUFFER_LEN = 10;


    private Parser          yyparser;       // parent parser object
    private BufferedReader  reader;         // input stream
    private char[]          buffer_a;       // input buffer a
    private char[]          buffer_b;       // input buffer b
    public int              lineno;         // line number
    public int              column;         // column

    private char[] currentbuffer;           // current reading buffer
    private char[] lexbegin_buffer;         // buffer lexbegin is on
    private int lexbegin;                   // position of lexbegin
    private int forward;                    // position of forward

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception
    {
        this.reader   = new BufferedReader(reader);
        buffer_a = new char[BUFFER_LEN];
        buffer_b = new char[BUFFER_LEN];

        this.yyparser = yyparser;
        lineno = 1;
        column = 1;

        readBuffer();
        lexbegin_buffer = currentbuffer;
    }

    private void switchBuffer() {
        if ( currentbuffer == buffer_a ) currentbuffer = buffer_b;
        else                             currentbuffer = buffer_a;
    }

    /*
     * reads into not-currentbuffer and switches currentbuffer to it
     */
    private void readBuffer() throws Exception {
        // switch buffers
        switchBuffer();

        // reset forward
        forward = 0;

        // read into buffer
        int chars_read = reader.read(currentbuffer, 0, BUFFER_LEN-1);

        // set last character to EOF
        currentbuffer[chars_read] = EOF;

        // System.out.println("Buffer read: " + chars_read);
        // System.out.println("buffer read: `" + new String(currentbuffer).replace('\n', '|') + "`");
    }

    private char getNextChar() throws Exception {
        // http://tutorials.jenkov.com/java-io/readers-writers.html
        int data = currentbuffer[forward];
        forward++;
        
        if (data == EOF) {
            // at end of buffer, so read and switch buffers
            if (forward == BUFFER_LEN) {
                readBuffer();
                return getNextChar();
            }
            
            else // not at end of buffer, so file has been completely read
                return EOF;
        }

        return (char)data;
    }

    private void retract() {
        // start of buffer: retract to previous buffer
        if ( forward == 0 ) {
            switchBuffer();
            forward = BUFFER_LEN - 2;
        }

        else
            forward--;
    }

    private String getLexeme() {
        // whole lexeme is on same buffer
        if ( lexbegin_buffer == currentbuffer ) {
            return new String( Arrays.copyOfRange(currentbuffer, lexbegin, forward) );
        }

        // lexbegin and forward on different buffers
        StringBuilder lexeme = new StringBuilder();
        lexeme.append( Arrays.copyOfRange(lexbegin_buffer, lexbegin, BUFFER_LEN-1) );
        lexeme.append( Arrays.copyOfRange(currentbuffer, 0, forward) );

        return lexeme.toString();
    }

    private void resetLexeme() {
        column += getLexeme().length();

        // set lexbegin to forward
        lexbegin = forward;

        // set lexbegin to be on the same buffer as forward
        lexbegin_buffer = currentbuffer;
    }

    private int lexerFailure() {
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
        resetLexeme();

        while(true)
        {
            char c;
            switch(state)
            {
                case 0:
                    c = getNextChar();

                    if ( Character.isLetter(c) ) { state = 1; continue; }   // ID or keyword
                    // if ( c == '_'              ) { state = 2; continue; }   // keyword
                    if ( Character.isDigit(c)  ) { state = 5; continue; }   // numeric

                    // single state tokens
                    // brackets, semicolon, comma, and operators
                    if ( c == '(') { state = 10; continue; }
                    if ( c == ')') { state = 11; continue; }
                    if ( c == '{') { state = 12; continue; }
                    if ( c == '}') { state = 13; continue; }
                    if ( c == ';') { state = 14; continue; }
                    if ( c == ',') { state = 15; continue; }
                    if ( c == '+') { state = 16; continue; }
                    if ( c == '-') { state = 17; continue; }
                    if ( c == '*') { state = 16; continue; }
                    if ( c == '/') { state = 16; continue; }

                    // relative operators
                    if ( c == '<') { state = 20; continue; }
                    if ( c == '>') { state = 20; continue; }
                    if ( c == '=') { state = 21; continue; }
                    if ( c == '!') { state = 22; continue; }

                    if (c == ' ' ) { state = 97; continue; }    // skip spaces
                    if (c == '\t') { state = 97; continue; }    // skip tabs
                    if (c == '\n') { state = 98; continue; }
                    if (c == EOF ) { state = 99; continue; }

                    return lexerFailure();
                case 1: // could be keyword
                    c = getNextChar();

                    // underscore or number -> can only be ID now
                    if ( c == '_' || Character.isDigit(c) ) { state = 2; continue; }
                    
                    // letter found, still might be a keyword
                    if ( Character.isLetter(c) )
                        continue;
                    
                    // else: end of lexeme
                    retract();

                    String lexeme = getLexeme();
                    yyparser.yylval = new ParserVal((Object) lexeme);    // set token attribute

                    // return token type
                    if      ( lexeme.equals("int")   ) return Parser.INT;
                    else if ( lexeme.equals("print") ) return Parser.PRINT;
                    else if ( lexeme.equals("var")   ) return Parser.VAR;
                    else if ( lexeme.equals("func")  ) return Parser.FUNC;
                    else if ( lexeme.equals("if")    ) return Parser.IF;
                    else if ( lexeme.equals("else")  ) return Parser.ELSE;
                    else if ( lexeme.equals("while") ) return Parser.WHILE;
                    else if ( lexeme.equals("void")  ) return Parser.VOID;
                    
                    else return Parser.ID;

                case 2: // underscore, letters or numbers(except first character): keyword
                    c = getNextChar();

                    if ( c == '_' || Character.isLetter(c) || Character.isDigit(c) )
                        continue;
                    
                    // else: end of lexeme found
                    retract();
                    yyparser.yylval = new ParserVal((Object) getLexeme());    // set token attribute
                    return Parser.ID;
                    
                case 5:     // number: integer or float
                    c = getNextChar();
                    if ( Character.isDigit(c) ) continue;

                    else if ( c == '.' ) { state = 6; continue; }

                    // else, end of lexeme
                    retract();
                    state = 8;
                    continue;
                case 6:     // decimal encountered, validate that digits follow
                    c = getNextChar();
                    if ( Character.isDigit(c) ) { state = 7; continue; }

                    return lexerFailure();
                case 7:     // decimal digits
                    c = getNextChar();
                    if ( Character.isDigit(c) ) continue;

                    // else: end of lexeme
                    retract();
                    state = 8;
                    continue;
                case 8:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.NUM;                                       // return token type
                    
                case 10:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.LPAREN;                                    // return token type
                case 11:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.RPAREN;                                    // return token type
                case 12:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.BEGIN;                                     // return token type
                case 13:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.END;                                       // return token type
                case 14:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.SEMI;                                      // return token type
                case 15:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.COMMA;                                     // return token type
                case 16:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.OP;                                        // return token type

                case 17:    // - or ->
                    c = getNextChar();

                    // function return: ->
                    if ( c == '>' ) { state = 18; continue; }

                    // else: - sign so retract and goto OP
                    retract();
                    state = 16;
                    continue;
                case 18:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.FUNCRET;                                   // return token type
                    

                case 20:    // <-, <, >, <=, >=
                    c = getNextChar();
                    
                    if ( c == '-' ) { state = 23; continue; }   // assingment: <-
                    if ( c != '=' ) retract();                  // < or >

                    // regardless, end of lexeme
                    state = 21;
                    continue;
                case 21:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.RELOP;                                     // return token type
                case 22:    // !=
                    c = getNextChar();
                    if ( c == '=' ) {
                        state = 21;
                        continue;
                    }

                    // else: fail because `!` is not a token
                    return lexerFailure();
                case 23:
                    yyparser.yylval = new ParserVal((Object) getLexeme());   // set token attribute
                    return Parser.ASSIGN;                                    // return token type

                case 97:    // empty space
                    resetLexeme();
                    state = 0;

                    continue;
                case 98:    // new line
                    resetLexeme();
                    state = 0;

                    lineno++;   // next line
                    column = 1; // reset column


                    continue;
                case 99:
                    return EOF;                                     // return end-of-file symbol
            }
        }
    }
}
