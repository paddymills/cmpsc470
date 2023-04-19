public class Token
{
    public String lexeme;

    public Token(String lexeme) {
        this.lexeme = lexeme;
    }

    public Token(Integer lexeme) {
        this.lexeme = lexeme.toString();
    }

    public int parseInt() throws Exception {
        return Integer.parseInt(lexeme);

    }
}
