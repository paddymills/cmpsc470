import java.lang.reflect.Array;
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

    Object program(Object dl) throws Exception {
        // production rule: program -> decl_list

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) dl;
        // 1. check if decllist has main function having no parameters and returns int
        // type
        // 2. assign the root, whose type is ParseTree.Program, to parsetree_program
        for (ParseTree.FuncDecl func : decllist) {
            if ( func.ident == "main" && func.params.isEmpty() && func.rettype.info.is_int_type() ) {
                parsetree_program = new ParseTree.Program(decllist);
        
                return parsetree_program;
            }
        }

        throw new Exception("no function `int main()` declared");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object decllist(Object dl, Object d) throws Exception {
        // production rule: decl_list -> decl_list decl

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) dl;
        ParseTree.FuncDecl decl = (ParseTree.FuncDecl) d;
        decllist.add(decl);

        return decllist;
    }

    Object decllist() throws Exception {
        // production rule: decl_list -> epsilon

        return new ArrayList<ParseTree.FuncDecl>();
    }

    Object decl(Object fd) throws Exception {
        // production rule: decl -> fun_decl
        
        return (ParseTree.FuncDecl) fd;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object fundecl(Object id, Object parameters, Object return_type, Object locals) throws Exception {
        // 1. add function_type_info object (name, return type, params) into the global
        // scope of env
        // 2. create a new symbol table on top of env
        // 3. add parameters into top-local scope of env
        // 4. etc.
        return null;
    }

    Object fundecl(Object name, Object parameters, Object return_type, Object locals, Object statements) throws Exception {
        // 1. check if this function has at least one return type
        // 2. etc.
        // 3. create and return funcdecl node
        Token id = (Token) name;
        ArrayList<ParseTree.Param> params         = (ArrayList<ParseTree.Param>) parameters;
        ParseTree.TypeSpec rettype                = (ParseTree.TypeSpec) return_type;
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>) locals;
        ArrayList<ParseTree.Stmt> stmtlist        = (ArrayList<ParseTree.Stmt>) statements;
        
        ParseTree.FuncDecl funcdecl = new ParseTree.FuncDecl(id.lexeme, rettype, params, localdecls, stmtlist);
        funcdecl.info.set_type(rettype.info);

        return funcdecl;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object params(Object parameter_list) throws Exception {
        // production rule: params -> param_list

        return (ArrayList<ParseTree.Param>) parameter_list;
    }

    Object params() throws Exception {
        // production rule: params -> epsilon

        return new ArrayList<ParseTree.Param>();
    }

    Object param_list(Object parameter_list, Object parameter) throws Exception {
        // production rule: param_list ->  param_list COMMA param

        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>) parameter_list;
        ParseTree.Param param = (ParseTree.Param) parameter;
        params.add(param);

        return params;
    }

    Object param_list(Object parameter) throws Exception {
        // production rule: param_list ->  param

        return (ParseTree.Param) parameter;
    }

    Object param(Object type, Object ident) throws Exception {
        // production rule: param -> VAR type_spec IDENT

        // TODO: handle IDENT in symbol table
        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) type;
        Token token = (Token) ident;

        ParseTree.Param _param = new ParseTree.Param(token.lexeme, typespec);
        _param.info.set_type(typespec.info);

        return _param;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object typespec(Object primitive_type) {
        // production rule: typespec -> prim_type

        return (ParseTree.TypeSpec) primitive_type;
    }

    Object primtype(Object prim) throws Exception {
        // production rule: primtype -> INT | BOOL

        String keyword = prim.toString();
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec(keyword);

        typespec.info.set_type(keyword);

        return typespec;
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
        // TODO: handle symbol table

        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) s2;
        Token id = (Token) s3;
        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.reladdr = 1;
        localdecl.info.set_type(typespec);

        return localdecl;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    Object stmt_print(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object stmt_return(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.ReturnStmt);
        return s1;
    }

    Object stmt_if(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.IfStmt);
        return s1;
    }

    Object stmt_while(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.WhileStmt);
        return s1;
    }

    Object stmt_compound(Object s1) throws Exception {
        assert (s1 instanceof ParseTree.CompoundStmt);
        return s1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object assignstmt(Object s1, Object s3) throws Exception {
        // 1. check if ident.value_type matches with expr.value_type
        // 2. etc.
        // e. create and return node
        Token id = (Token) s1;
        ParseTree.Expr expr = (ParseTree.Expr) s3;
        Object id_type = env.get(id.lexeme);
        {
            // check if expr.type matches with id_type
            if (id_type.equals("int")
                    && (expr instanceof ParseTree.ExprIntLit)) {
            } // ok
            else if (id_type.equals("int")
                    && (expr instanceof ParseTree.ExprCall)
                    && (env.get(((ParseTree.ExprCall) expr).ident).equals("func()->int"))) {
            } // ok
            else {
                throw new Exception("semantic error");
            }
        }
        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr);
        stmt.ident_reladdr = 1;
        return stmt;
    }

    Object printstmt(Object s2) throws Exception {
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.PrintStmt(expr);
    }


    Object returnstmt(Object s2) throws Exception {
        // 1. check if expr.value_type matches with the current function return type
        // 2. etc.
        // 3. create and return node
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.ReturnStmt(expr);
    }

    Object ifstmt(Object expression, Object if_statement, Object else_statement) throws Exception {
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt if_s = (ParseTree.Stmt) if_statement;
        ParseTree.Stmt else_s = (ParseTree.Stmt) else_statement;

        return new ParseTree.IfStmt(expr, if_s, else_s);
    }

    Object whilestmt(Object expression, Object statement) throws Exception {
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt st = (ParseTree.Stmt) statement;
        return new ParseTree.WhileStmt(expr, st);
    }

    Object compoundstmt(Object locals, Object stmts) throws Exception {
        
        ArrayList<ParseTree.LocalDecl> ld = (ArrayList<ParseTree.LocalDecl>) locals;
        ArrayList<ParseTree.Stmt> s = (ArrayList<ParseTree.Stmt>) stmts;

        return new ParseTree.CompoundStmt(ld, s);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object args(Object args) throws Exception {
        return (ArrayList<ParseTree.Arg>) args;
    }

    Object args() throws Exception {
        return new ArrayList<ParseTree.Arg>();
    }

    Object arglist(Object args, Object exp) throws Exception {
        
        ParseTree.Expr _exp = (ParseTree.Expr) exp;
        ParseTree.Arg arg = new ParseTree.Arg(_exp);
        arg.info.set_type(_exp.info);
        
        ArrayList<ParseTree.Arg> arglist = (ArrayList<ParseTree.Arg>) args;
        arglist.add(arg);

        return arglist;
    }

    Object arglist(Object exp) throws Exception {
        ParseTree.Expr _exp = (ParseTree.Expr) exp;
        ParseTree.Arg arg = new ParseTree.Arg(_exp);
        arg.info.set_type(_exp.info);
        
        ArrayList<ParseTree.Arg> arglist = new ArrayList<ParseTree.Arg>();
        arglist.add(arg);

        return arglist;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    Object expr_oper(Object s1, Object op, Object s3) throws Exception {
        // 1. check if expr1.value_type matches with the expr2.value_type
        // 2. etc.
        // 3. create and return node that has value_type
        ParseTree.Expr expr1 = (ParseTree.Expr) s1;
        Token oper = (Token) op;
        ParseTree.Expr expr2 = (ParseTree.Expr) s3;

        // check if expr1.type matches with expr2.type
        if ( expr1.info.type != expr2.info.type )
            throw new Exception("mismatch in expression types");
        
        ParseTree.Expr expr;
        switch (oper.lexeme) {
            case "+" : { expr = new ParseTree.ExprAdd(expr1, expr2); break; }
            case "-" : { expr = new ParseTree.ExprSub(expr1, expr2); break; }
            case "*" : { expr = new ParseTree.ExprMul(expr1, expr2); break; }
            case "/" : { expr = new ParseTree.ExprDiv(expr1, expr2); break; }
            case "%" : { expr = new ParseTree.ExprMod(expr1, expr2); break; }
            case "=" : { expr = new ParseTree.ExprEq (expr1, expr2); break; }
            case "!=": { expr = new ParseTree.ExprNe (expr1, expr2); break; }
            case "<=": { expr = new ParseTree.ExprLe (expr1, expr2); break; }
            case "<" : { expr = new ParseTree.ExprLt (expr1, expr2); break; }
            case ">=": { expr = new ParseTree.ExprGe (expr1, expr2); break; }
            case ">" : { expr = new ParseTree.ExprGt (expr1, expr2); break; }
            case "&&": { expr = new ParseTree.ExprAnd(expr1, expr2); break; }
            case "||": { expr = new ParseTree.ExprOr (expr1, expr2); break; }
        
            default:
                throw new Exception("no operation matched for `" + oper.lexeme + "`");
        }

        expr.info.set_type(expr1.info);

        return expr;
    }

    Object expr_not(Object s2) throws Exception {
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        ParseTree.Expr expr = new ParseTree.ExprNot(inner_expr);
        expr.info.set_type(inner_expr);
        
        return expr;
    }

    Object expr_paren(Object s2) throws Exception {
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        ParseTree.Expr expr =  new ParseTree.ExprParen(inner_expr);
        expr.info.set_type(inner_expr);

        return expr;
    }

    Object expr_id(Object s1) throws Exception {
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is variable type
        // 3. etc.
        // 4. create and return node that has the value_type of the id.lexeme
        Token id = (Token) s1;

        Object ident_attr = env.get(id.toString());
        if ( ident_attr == null )
            throw new Exception("Identifier " + id.toString() + " is not in the symbol table");

        ParseTree.ExprIdent expr = new ParseTree.ExprIdent(id.lexeme);
        expr.reladdr = 1;
        expr.info.set_type(ident_attr);

        return expr;
    }

    Object expr_int(Object s1) throws Exception {
        // 1. create and return node that has int type
        Token token = (Token) s1;
        ParseTree.ExprIntLit int_literal =  new ParseTree.ExprIntLit(token.parseInt());
        int_literal.info.set_int_type();

        return int_literal;
    }

    Object expr_bool(Object s1) throws Exception {
        // 1. create and return node that has int type
        Token token = (Token) s1;
        if ( token.lexeme != "true" && token.lexeme != "false" )
            throw new Exception("invalid bool literal: " + token.lexeme);

        ParseTree.ExprBoolLit bool_literal = new ParseTree.ExprBoolLit( token.lexeme == "true" );
        bool_literal.info.set_bool_type();

        return bool_literal;
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
        ParseTree.FuncDecl func_attr = (ParseTree.FuncDecl) env.get(id.lexeme);
        {
            // check if argument types match with function param types
            if (env.get(id.lexeme).equals("func()->int")
                    && (args.size() == 0)) {
            } // ok
            else {
                throw new Exception("semantic error");
            }
        }
        ParseTree.ExprCall expr = new ParseTree.ExprCall(id.lexeme, args);
        expr.info.set_type(func_attr.info);

        return expr;
    }
}
