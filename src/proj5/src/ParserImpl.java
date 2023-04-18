import java.util.*;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ParserImpl {
    public static Boolean _debug = true;

    void Debug(String message) {
        if (_debug)
            System.out.println(message);
    }

    // This is for chained symbol table.
    // This includes the global scope only at this moment.
    Env env = new Env(null);
    // this stores the root of parse tree, which will be used to print parse tree
    // and run the parse tree
    ParseTree.Program parsetree_program = null;

    Object program(Object s1) throws Exception {
        // production rule: program -> decl_list

        // 1. check if decllist has main function having no parameters and returns int
        // type
        // 2. assign the root, whose type is ParseTree.Program, to parsetree_program
        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) s1;
        parsetree_program = new ParseTree.Program(decllist);
        return parsetree_program;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object decllist(Object s1, Object s2) throws Exception {
        // production rule: decl_list -> decl_list decl

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) s1;
        ParseTree.FuncDecl decl = (ParseTree.FuncDecl) s2;
        decllist.add(decl);
        return decllist;
    }

    Object decllist() throws Exception {
        // production rule: decl_list -> epsilon

        return new ArrayList<ParseTree.FuncDecl>();
    }

    Object decl(Object s1) throws Exception {
        // production rule: decl -> fun_decl
        
        return s1;
    }

    Object primtype(Object s1) throws Exception {
        // production rule: primtype -> INT | BOOL

        // ParseTree.TypeSpec typespec = new ParseTree.TypeSpec("int");
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec(s1.toString());
        return typespec;
    }

    Object typespec(Object s1) {
        // production rule: typespec -> prim_type

        ParseTree.TypeSpec primtype = (ParseTree.TypeSpec) s1;
        return primtype;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object fundecl(Object s2, Object s4, Object s7, Object s9) throws Exception {
        // 1. add function_type_info object (name, return type, params) into the global
        // scope of env
        // 2. create a new symbol table on top of env
        // 3. add parameters into top-local scope of env
        // 4. etc.
        return null;
    }

    Object fundecl(Object s2, Object s4, Object s7, Object s9, Object s11, Object s12) throws Exception {
        // 1. check if this function has at least one return type
        // 2. etc.
        // 3. create and return funcdecl node
        Token id = (Token) s2;
        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>) s4;
        ParseTree.TypeSpec rettype = (ParseTree.TypeSpec) s7;
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>) s9;
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>) s11;
        Token end = (Token) s12;
        ParseTree.FuncDecl funcdecl = new ParseTree.FuncDecl(id.lexeme, rettype, params, localdecls, stmtlist);
        return funcdecl;
    }

    Object params() throws Exception {
        return new ArrayList<ParseTree.Param>();
    }

    Object stmtlist(Object s1, Object s2) throws Exception {
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>) s1;
        ParseTree.Stmt stmt = (ParseTree.Stmt) s2;
        stmtlist.add(stmt);
        return stmtlist;
    }

    Object stmtlist() throws Exception {
        return new ArrayList<ParseTree.Stmt>();
    }

    Object stmt_assign(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.AssignStmt);
        return s1;
    }

    Object stmt_return(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.ReturnStmt);
        return s1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object assignstmt(Object s1, Object s2, Object s3) throws Exception {
        // 1. check if ident.value_type matches with expr.value_type
        // 2. etc.
        // e. create and return node
        Token id = (Token) s1;
        Token assign = (Token) s2;
        ParseTree.Expr expr = (ParseTree.Expr) s3;
        Object id_type = env.Get(id.lexeme);
        {
            // check if expr.type matches with id_type
            if (id_type.equals("int")
                    && (expr instanceof ParseTree.ExprIntLit)) {
            } // ok
            else if (id_type.equals("int")
                    && (expr instanceof ParseTree.ExprCall)
                    && (env.Get(((ParseTree.ExprCall) expr).ident).equals("func()->int"))) {
            } // ok
            else {
                throw new Exception("semantic error");
            }
        }
        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr);
        stmt.ident_reladdr = 1;
        return stmt;
    }

    Object returnstmt(Object s2) throws Exception {
        // 1. check if expr.value_type matches with the current function return type
        // 2. etc.
        // 3. create and return node
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.ReturnStmt(expr);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object localdecls(Object s1, Object s2) {
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>) s1;
        ParseTree.LocalDecl localdecl = (ParseTree.LocalDecl) s2;
        localdecls.add(localdecl);
        return localdecls;
    }

    Object localdecls() throws Exception {
        return new ArrayList<ParseTree.LocalDecl>();
    }

    Object localdecl(Object s2, Object s3) {
        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) s2;
        Token id = (Token) s3;
        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.reladdr = 1;
        return localdecl;
    }

    Object args() throws Exception {
        return new ArrayList<ParseTree.Expr>();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object expr_add(Object s1, Object s2, Object s3) throws Exception {
        // 1. check if expr1.value_type matches with the expr2.value_type
        // 2. etc.
        // 3. create and return node that has value_type
        ParseTree.Expr expr1 = (ParseTree.Expr) s1;
        Token oper = (Token) s2;
        ParseTree.Expr expr2 = (ParseTree.Expr) s3;
        // check if expr1.type matches with expr2.type
        return new ParseTree.ExprAdd(expr1, expr2);
    }

    Object expr_eq(Object s1, Object s2, Object s3) throws Exception {
        // 1. check if expr1.value_type matches with the expr2.value_type
        // 2. etc.
        // 3. create and return node that has value_type
        ParseTree.Expr expr1 = (ParseTree.Expr) s1;
        Token oper = (Token) s2;
        ParseTree.Expr expr2 = (ParseTree.Expr) s3;
        // check if expr1.type matches with expr2.type
        return new ParseTree.ExprEq(expr1, expr2);
    }

    Object expr_paren(Object s1, Object s2, Object s3) throws Exception {
        // 1. create and return node whose value_type is the same to the expr.value_type
        Token lparen = (Token) s1;
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        Token rparen = (Token) s3;
        return new ParseTree.ExprParen(expr);
    }

    Object expr_id(Object s1) throws Exception {
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is variable type
        // 3. etc.
        // 4. create and return node that has the value_type of the id.lexeme
        Token id = (Token) s1;
        ParseTree.ExprIdent expr = new ParseTree.ExprIdent(id.lexeme);
        expr.reladdr = 1;
        return expr;
    }

    Object expr_call(Object s2, Object s4) throws Exception {
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is function type
        // 3. check if the number and types of env(id.lexeme).params match with those of
        // args
        // 4. etc.
        // 5. create and return node that has the value_type of
        // env(id.lexeme).return_type
        Token id = (Token) s2;
        ArrayList<ParseTree.Arg> args = (ArrayList<ParseTree.Arg>) s4;
        Object func_attr = env.Get(id.lexeme);
        {
            // check if argument types match with function param types
            if (env.Get(id.lexeme).equals("func()->int")
                    && (args.size() == 0)) {
            } // ok
            else {
                throw new Exception("semantic error");
            }
        }
        return new ParseTree.ExprCall(id.lexeme, args);
    }

    Object expr_int(Object s1) throws Exception {
        // 1. create and return node that has int type
        Token token = (Token) s1;
        int value = Integer.parseInt(token.lexeme);
        return new ParseTree.ExprIntLit(value);
    }
}
