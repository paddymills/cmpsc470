import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

enum DataType{
    Bool,
    Int
}

abstract class DataTypeInfo {
    public DataType type;

    public void set_int_type()    { this.type = DataType.Int; }
    public void set_bool_type()   { this.type = DataType.Bool; }
    public boolean is_int_type()  { return this.type == DataType.Int; }
    public boolean is_bool_type() { return this.type == DataType.Bool; }
    
    public void set_type(Object other) { this.type = ((DataTypeInfo) other).type; }
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
}

public class ParseTreeInfo
{
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
