public class Token
{
    public String lexeme;

    public Token(String lexeme) {
        this.lexeme = lexeme;
    }

    public int parseInt() throws Exception {
        return Integer.parseInt(lexeme);

    }
}
