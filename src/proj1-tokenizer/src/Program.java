public class Program {
    public static void main(String[] args) throws Exception
    {
        //java.io.Reader r = new java.io.StringReader
        //("func main()->int\n"
        //+"{\n"
        //+"    var int a;\n"
        //+"    a <- bcd + 2 * 3;\n"
        //+"    b <- 1.234\n"
        //+"    print a;\n"
        //+"}\n"
        //);
        //
        //  args = new String[] { "proj1-minic-tokenizer\\src\\test1.minc" };

        if(args.length <= 0)
            return;
        java.io.Reader r = new java.io.FileReader(args[0]);

        Compiler compiler = new Compiler(r);
        compiler.Compile();
    }
}
