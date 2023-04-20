import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class ParseTreeInfo
{
    enum DataType{
        Bool,
        Int
    }

    public static abstract class DataTypeInfo {
        public DataType type;

        public String toString() {
            assert this.type != null;

            if ( this.is_bool_type() )
                return "bool";

            return "int";
        }
    
        public void set_int_type()    { this.type = DataType.Int; }
        public void set_bool_type()   { this.type = DataType.Bool; }
        public boolean is_int_type()  { return this.type == DataType.Int; }
        public boolean is_bool_type() { return this.type == DataType.Bool; }
        
        public void set_type(Object other) { this.type = ((DataTypeInfo) other).type; }
        public void set_type(ParseTree.TypeSpec other)  { this.set_type(other.info); }
        public void set_type(ParseTree.FuncDecl other)  { this.set_type(other.info); }
        public void set_type(ParseTree.Param other)     { this.set_type(other.info); }
        public void set_type(ParseTree.LocalDecl other) { this.set_type(other.info); }
        public void set_type(ParseTree.Arg other)       { this.set_type(other.info); }
        public void set_type(ParseTree.Expr other)      { this.set_type(other.info); }

        public void set_type(String keyword) throws Exception {
            switch (keyword) {
                case "int":
                    this.set_int_type();
                    break;
                case "bool":
                    this.set_bool_type();
                    break;
            
                default:
                    throw new Exception("Unexpected primitive time: " + keyword);
            }
        }

        public boolean expr_matches_type(ParseTree.Expr expr) {
            // match literals
            if ( this.is_int_type()  && (expr instanceof ParseTree.ExprIntLit ) ) return true;
            if ( this.is_bool_type() && (expr instanceof ParseTree.ExprBoolLit) ) return true; 

            return false;
        }

        public boolean equals(DataTypeInfo other) {
            return this.type == other.type;
        }
    }
    // Use this classes to store information into parse tree node (subclasses of ParseTree.Node)
    // You should not modify ParseTree.java
    public static class ProgramInfo { }
    public static class StmtStmtInfo { }
    
    public static class TypeSpecInfo extends DataTypeInfo { }
    public static class FuncDeclInfo extends DataTypeInfo { }
    public static class ParamInfo extends DataTypeInfo { }
    public static class LocalDeclInfo extends DataTypeInfo { }
    public static class ArgInfo extends DataTypeInfo { }
    public static class ExprInfo extends DataTypeInfo { }
}
