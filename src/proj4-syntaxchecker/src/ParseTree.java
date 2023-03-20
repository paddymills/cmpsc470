import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class ParseTree
{
    public static abstract class Node
    {
        abstract public String[] ToStringList() throws Exception; // This is used to print conde with indentation and comments
    }
    public static abstract class NodeString extends Node
    {
        abstract public String ToString();
        public String[] ToStringList() throws Exception
        {
            return new String[] { ToString() };
        }
    }
    public static <T> String NodeListToString(List<? extends NodeString> nodes, String separator)
    {
        String str = "";
        for(int i=0; i<nodes.size(); i++)
        {
            if(i == 0) str +=           nodes.get(i).ToString();
            else       str += separator+nodes.get(i).ToString();
        }
        return str;
    }

    public static class Program extends Node
    {
        //public ParseTreeInfo.ProgramInfo info = new ParseTreeInfo.ProgramInfo();
        public List<FuncDecl> funcs;
        public Program(List<FuncDecl> funcs)
        {
            this.funcs = funcs;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            for(var func : funcs)
            {
                for(String str : func.ToStringList())
                    strs.add(str);
            }
            return strs.toArray(String[]::new);
        }
    }
    public static class FuncDecl extends Node
    {
        //public ParseTreeInfo.FuncDefnInfo info = new ParseTreeInfo.FuncDefnInfo();
        public String          ident     ;
        public PrimType        rettype   ;
        public List<Param    > params    ;
        public List<LocalDecl> localdecls;
        public List<Stmt     > stmtlist  ;
        public FuncDecl(String ident, PrimType rettype, List<Param> params, List<LocalDecl> localdecls, List<Stmt> stmtlist)
        {
            this.ident      = ident     ;
            this.rettype    = rettype   ;
            this.params     = params    ;
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public String[] ToStringList() throws Exception
        {
            String head = "func " + ident + "(" + NodeListToString(params,", ") + ")->" + rettype.ToString();

            ArrayList<String> strs = new ArrayList<String>();
            strs.add(head);
            strs.add("{");
            for(var localdecl : localdecls)
                strs.add("    " + localdecl.ToString());
            for(var stmt : stmtlist)
                for(String str : stmt.ToStringList())
                    strs.add("    "+str);
            strs.add("}");

            return strs.toArray(String[]::new);
        }
    }
    public static class Param extends NodeString
    {
        //public ParseTreeInfo.ParamInfo info = new ParseTreeInfo.ParamInfo();
        public String   ident   ;
        public TypeSpec typespec;
        public Param(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String ToString() { return typespec.ToString() + " " + ident; }
    }
    public static class TypeSpec extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public PrimType  type;
        public TypeSpec_ spec;
        public TypeSpec(PrimType type, TypeSpec_ spec) { this.type = type; this.spec = spec; }
        public String ToString() { return type.ToString()+spec.ToString(); }
    }
    public abstract static class TypeSpec_ extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        abstract public String ToString();
    }
    public static class TypeSpec_Value extends TypeSpec_
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public String ToString() { return ""; }
    }
    public static class TypeSpec_Array extends TypeSpec_
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public String ToString() { return "[]"; }
    }
    public abstract static class PrimType extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        abstract public String ToString();
    }
    public static class PrimTypeInt extends PrimType
    {
        public String ToString() { return "int"; }
    }
    public static class PrimTypeBool extends PrimType
    {
        public String ToString() { return "bool"; }
    }
    public static class LocalDecl extends Node
    {
        //public ParseTreeInfo.LocalDeclInfo info = new ParseTreeInfo.LocalDeclInfo();
        public String   ident   ;
        public TypeSpec typespec;
        public LocalDecl(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String[] ToStringList() throws Exception { return new String[] { ToString() }; }
        public String ToString()
        {
            String str ="var " + typespec.ToString() + " " + ident + ";";
            return str;
        }
    }
    public abstract static class Stmt extends Node
    {
        //public ParseTreeInfo.StmtStmtInfo info = new ParseTreeInfo.StmtStmtInfo();
        abstract public String[] ToStringList() throws Exception;
    }
    public static class StmtAssign extends Stmt
    {
        public String  ident;
        public Expr    expr ;
        public StmtAssign(String ident, Expr expr)
        {
            this.ident = ident;
            this.expr  = expr ;
        }
        public String[] ToStringList() throws Exception 
        {
            String str = ident;
            str  += " <- " + expr.ToString() + ";";
            return new String[] { str };
        }
    }
    public static class StmtPrint extends Stmt
    {
        public Expr expr;
        public StmtPrint(Expr expr)
        {
            this.expr = expr;
        }
        public String[] ToStringList() throws Exception
        {
            return new String[]
            {
                "print " + expr.ToString() + ";"
            };
        }
    }
    public static class StmtReturn extends Stmt
    {
        public Expr expr;
        public StmtReturn(Expr expr)
        {
            this.expr = expr;
        }
        public String[] ToStringList() throws Exception
        {
            return new String[]
            {
                "return " + expr.ToString() + ";"
            };
        }
    }
    public static class StmtIf extends Stmt
    {
        public Expr cond    ;
        public Stmt thenstmt;
        public Stmt elsestmt;
        public StmtIf(Expr cond, Stmt thenstmt, Stmt elsestmt)
        {
            this.cond     = cond    ;
            this.thenstmt = thenstmt;
            this.elsestmt = elsestmt;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("if( " + cond.ToString() + " )");
            for(String str : thenstmt.ToStringList())
                strs.add("    "+str);
            strs.add("else");
            for(String str : elsestmt.ToStringList())
                strs.add("    "+str);
            return strs.toArray(String[]::new);
        }
    }
    public static class StmtWhile extends Stmt
    {
        public Expr cond     ;
        public Stmt whilestmt;
        public StmtWhile(Expr cond, Stmt whilestmt)
        {
            this.cond      = cond     ;
            this.whilestmt = whilestmt;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("while( " + cond.ToString() + " )");
            for(String str : whilestmt.ToStringList())
                strs.add("    "+str);
            return strs.toArray(String[]::new);
        }
    }
    public static class StmtCompound extends Stmt
    {
        public List<LocalDecl> localdecls;
        public List<Stmt     > stmtlist  ;
        public StmtCompound(List<LocalDecl> localdecls, List<Stmt> stmtlist)
        {
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("{");
            for(LocalDecl localdecl : localdecls)
                strs.add("    " + localdecl.ToString());
            for(Stmt stmt : stmtlist)
                for(String str : stmt.ToStringList())
                    strs.add("    "+str);
            strs.add("}");
            return strs.toArray(String[]::new);
        }
    }
    public static class Arg extends NodeString
    {
        //public ParseTreeInfo.ArgInfo info = new ParseTreeInfo.ArgInfo();
        public Expr expr;
        public Arg(Expr expr)    { this.expr = expr;       }
        public String ToString() { return expr.ToString(); }
    }
    public static class Expr extends NodeString
    {
        public Term  term ;
        public Expr_ expr_;
        public Expr(Term term, Expr_ expr_)
        {
            this.term  = term ;
            this.expr_ = expr_;
        }
        public String ToString()
        {
            String str = term.ToString();
            if(expr_ != null) str += expr_.ToString();
            return str;
        }
    }
    public static class Expr_ extends NodeString
    {
        public String op   ;
        public Term   term ;
        public Expr_  expr_;
        public Expr_(String op, Term term, Expr_ expr_)
        {
            this.op    = op   ;
            this.term  = term ;
            this.expr_ = expr_;
        }
        public String ToString()
        {
            String opstr = (op.equals("or")) ? " or " : op;
            String str = opstr + term.ToString();
            if(expr_ != null) str += expr_.ToString();
            return str;
        }
    }
    public static class Term extends NodeString
    {
        public Factor factor;
        public Term_  term_ ;
        public Term(Factor factor, Term_  term_)
        {
            this.factor = factor;
            this.term_  = term_ ;
        }
        public String ToString()
        {
            String str = factor.ToString();
            if(term_ != null) str += term_.ToString();
            return str;
        }
    }
    public static class Term_ extends NodeString
    {
        public String op;
        public Factor factor;
        public Term_  term_ ;
        public Term_(String op, Factor factor, Term_  term_)
        {
            this.op     = op    ;
            this.factor = factor;
            this.term_  = term_ ;
        }
        public String ToString()
        {
            String opstr = (op.equals("and")) ? " and " : op;
            String str = opstr + factor.ToString();
            if(term_ != null) str += term_.ToString();
            return str;
        }
    }
    public static abstract class Factor extends NodeString
    {
    }
    public static class FactorParen extends Factor
    {
        public Expr expr;
        public FactorParen(Expr expr) { this.expr = expr; }
        public String ToString() { return "(" + expr.ToString() + ")"; }
    }
    public static class FactorIdent extends Factor
    {
        public String ident;
        public FactorIdent(String ident) { this.ident = ident; }
        public String ToString() { return ident; }
    }
    public static class FactorIntLit extends Factor
    {
        public Integer intlit;
        public FactorIntLit(int intlit) { this.intlit = intlit; }
        public String ToString() { return intlit.toString(); }
    }
    public static class FactorBoolLit extends Factor
    {
        public Boolean boollit;
        public FactorBoolLit(boolean boollit) { this.boollit = boollit; }
        public String ToString() { return boollit.toString(); }
    }
    public static class FactorCall extends Factor
    {
        public String    ident;
        public List<Arg> args ;
        public FactorCall(String ident, List<Arg> args) { this.ident = ident; this.args = args; }
        public String ToString()
        {
            String str = "call " + ident + "(" + NodeListToString(args,",") + ")";
            return str;
        }
    }
    public static class FactorNew extends Factor
    {
        public PrimType type;
        public Expr     expr;
        public FactorNew(PrimType type, Expr expr) { this.type = type; this.expr = expr; }
        public String ToString()
        {
            String str = "new " + type.ToString() + "(" + expr.ToString() + ")";
            return str;
        }
    }
    public static class FactorElemof extends Factor
    {
        public String    ident;
        public Expr      index;
        public FactorElemof(String ident, Expr index) { this.ident = ident; this.index = index; }
        public String ToString()
        {
            String str = "elemof " + ident + "[" + index.ToString() + "]";
            return str;
        }
    }
    public static class FactorSizeof extends Factor
    {
        public String    ident;
        public FactorSizeof(String ident) { this.ident = ident; }
        public String ToString()
        {
            String str = "sizeof " + ident;
            return str;
        }
    }
}
