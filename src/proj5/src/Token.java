public class Token
{
    public String lexeme;
    public int lineno, column;

    public Token(String lexeme) {
        this.lexeme = lexeme;
    }

    public Token(String lexeme, int line, int col) {
        this.lexeme = lexeme;
        this.lineno = line;
        this.column = col;
    }

    public Token(Integer lexeme) {
        this.lexeme = lexeme.toString();
    }

    public int parseInt() throws Exception {
        return Integer.parseInt(lexeme);

    }
}
