import java.lang.reflect.Array;
import java.util.*;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ParserImpl {
    public static Boolean _debug = true;

    public static void Debug(String message) {
        if (_debug)
            System.out.println(message);
    }

    // This is for chained symbol table.
    // This includes the global scope only at this moment.
    Env env = new Env(null);
    // this stores the root of parse tree, which will be used to print parse tree
    // and run the parse tree
    ParseTree.Program parsetree_program = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.Program program(Object _decllist) throws Exception {
        // production rule: program -> decl_list

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) _decllist;
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

    ArrayList<ParseTree.FuncDecl> decllist(Object _decllist, Object _decl) throws Exception {
        // production rule: decl_list -> decl_list decl

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) _decllist;
        ParseTree.FuncDecl decl                = (ParseTree.FuncDecl) _decl;
        decllist.add(decl);

        return decllist;
    }

    ArrayList<ParseTree.FuncDecl> decllist() throws Exception {
        // production rule: decl_list -> epsilon

        return new ArrayList<ParseTree.FuncDecl>();
    }

    ParseTree.FuncDecl decl(Object _func_decl) throws Exception {
        // production rule: decl -> fun_decl
        
        return (ParseTree.FuncDecl) _func_decl;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.FuncDecl fundecl(String id, Object parameters, Object return_type, Object _locals) throws Exception {
        // 1. add function_type_info object (name, return type, params) into the global
        // scope of env
        // 2. create a new symbol table on top of env
        // 3. add parameters into top-local scope of env
        // 4. etc.
        
        ArrayList<ParseTree.Param> params     = (ArrayList<ParseTree.Param>) parameters;
        ParseTree.TypeSpec rettype            = (ParseTree.TypeSpec) return_type;
        ArrayList<ParseTree.LocalDecl> locals = (ArrayList<ParseTree.LocalDecl>) _locals;
        ParseTree.FuncDecl decl = new ParseTree.FuncDecl(id, rettype, params, locals, null);

        env.put(id, decl); // add function to current stack symbol table
        env = new Env(env);     // add new symbol table for new stack frame

        // add parameters to new stack frame
        for (ParseTree.Param param : params) {
            env.put(param.ident, param);
        }

        // add local declarations to stack frame symbol table
        for (ParseTree.LocalDecl local : locals) {
            env.put(local.ident, local);
        }

        return null;
    }

    ParseTree.FuncDecl fundecl(String ident, Object parameters, Object return_type, Object locals, Object statements) throws Exception {
        // 1. check if this function has at least one return type
        // 2. etc.
        // 3. create and return funcdecl node
        Token id                                  =  new Token(ident);
        ArrayList<ParseTree.Param> params         = (ArrayList<ParseTree.Param>) parameters;
        ParseTree.TypeSpec rettype                = (ParseTree.TypeSpec) return_type;
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>) locals;
        ArrayList<ParseTree.Stmt> stmtlist        = (ArrayList<ParseTree.Stmt>) statements;

        ParseTree.FuncDecl funcdecl = new ParseTree.FuncDecl(id.lexeme, rettype, params, localdecls, stmtlist);
        funcdecl.info.set_type(rettype.info);

        env.put(id.lexeme, funcdecl);

        // leaving stack frame, pop symbol table 
        env = env.prev;

        return funcdecl;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.Param> params(Object parameter_list) throws Exception {
        // production rule: params -> param_list

        return (ArrayList<ParseTree.Param>) parameter_list;
    }

    ArrayList<ParseTree.Param> params() throws Exception {
        // production rule: params -> epsilon

        return new ArrayList<ParseTree.Param>();
    }

    ArrayList<ParseTree.Param> param_list(Object _param_list, Object _param) throws Exception {
        // production rule: param_list ->  param_list COMMA param

        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>) _param_list;
        ParseTree.Param param             = (ParseTree.Param) _param;
        params.add(param);

        return params;
    }

    ArrayList<ParseTree.Param> param_list(Object _param) throws Exception {
        // production rule: param_list ->  param

        ArrayList<ParseTree.Param> params = new ArrayList<ParseTree.Param>();
        params.add((ParseTree.Param) _param);

        return params;
    }

    ParseTree.Param param(String ident, Object type) throws Exception {
        // production rule: param -> VAR type_spec IDENT

        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) type;
        Token token                 = new Token(ident);
        
        // put IDENT in symbol table
        env.put(token.lexeme, typespec);

        ParseTree.Param _param = new ParseTree.Param(token.lexeme, typespec);
        _param.info.set_type(typespec.info);

        return _param;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.TypeSpec typespec(Object _primtype) {
        // production rule: typespec -> prim_type

        return (ParseTree.TypeSpec) _primtype;
    }

    ParseTree.TypeSpec primtype(String keyword) throws Exception {
        // production rule: primtype -> INT | BOOL

        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec(keyword);
        typespec.info.set_type(keyword);

        return typespec;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.LocalDecl> localdecls(Object _ldecls, Object _ldecl) {
        // production rule: local_decls -> local_decls local_decl

        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>) _ldecls;
        ParseTree.LocalDecl localdecl             = (ParseTree.LocalDecl) _ldecl;
        localdecls.add(localdecl);

        return localdecls;
    }

    ArrayList<ParseTree.LocalDecl> localdecls() throws Exception {
        // production rule: local_decls -> epslilon
        return new ArrayList<ParseTree.LocalDecl>();
    }

    ParseTree.LocalDecl localdecl(String ident, Object type) {
        // production rule: local_decl -> VAR type_spec IDENT SEMI

        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) type;
        Token id = new Token(ident);

        // add declaration to symbol table
        env.put(id.lexeme, typespec);

        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.reladdr = 1;
        localdecl.info.set_type(typespec.info);

        return localdecl;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.Stmt> stmtlist(Object s1, Object s2) throws Exception {
        // production rule: tmt_list -> stmt_list stmt

        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>) s1;
        ParseTree.Stmt stmt = (ParseTree.Stmt) s2;
        stmtlist.add(stmt);

        return stmtlist;
    }

    ArrayList<ParseTree.Stmt> stmtlist() throws Exception {
        // production rule: stmt_list -> epsilon
        
        return new ArrayList<ParseTree.Stmt>();
    }

    Object stmt_assign(Object s1) throws Exception {
        // production rule: stmt -> assign_stmt
        
        assert (s1 instanceof ParseTree.AssignStmt);
        return s1;
    }

    Object stmt_print(Object s1) throws Exception {
        // production rule: stmt -> print_stmt
        
        assert (s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object stmt_return(Object s1) throws Exception {
        // production rule: stmt -> return_stmt
        
        assert (s1 instanceof ParseTree.ReturnStmt);
        return s1;
    }

    Object stmt_if(Object s1) throws Exception {
        // production rule: stmt -> if_stmt
        
        assert (s1 instanceof ParseTree.IfStmt);
        return s1;
    }

    Object stmt_while(Object s1) throws Exception {
        // production rule: stmt -> while_stmt
        
        assert (s1 instanceof ParseTree.WhileStmt);
        return s1;
    }

    Object stmt_compound(Object s1) throws Exception {
        // production rule: stmt -> compound_stmt
        
        assert (s1 instanceof ParseTree.CompoundStmt);
        return s1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.AssignStmt assignstmt(String ident, Object expression) throws Exception {
        // production rule: IDENT <- expr SEMI
        
        // 1. check if ident.value_type matches with expr.value_type
        // 2. etc.
        // e. create and return node
        assert expression != null;

        Token id = new Token(ident);
        ParseTree.Expr expr       = (ParseTree.Expr) expression;
        ParseTree.LocalDecl ident_attr = (ParseTree.LocalDecl) env.get(id.lexeme);
        {
            // check if expr.type matches with id_type
            // if (id_type.equals("int")
            //         && (expr instanceof ParseTree.ExprIntLit)) {
            // } // ok
            // else if (id_type.equals("int")
            //         && (expr instanceof ParseTree.ExprCall)
            //         && (env.get(((ParseTree.ExprCall) expr).ident).equals("func()->int"))) {
            // } // ok

            if ( ident_attr == null )
                throw new Exception("Identifier `" + id.lexeme + "` is not in the symbol table");
            if ( ident_attr.info.equals(expr.info) );

            else {
                throw new Exception("semantic error");
            }
        }
        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr);
        stmt.ident_reladdr = 1;
        return stmt;
    }

    ParseTree.PrintStmt printstmt(Object s2) throws Exception {
        // production rule: PRINT expr SEMI
        
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.PrintStmt(expr);
    }


    ParseTree.ReturnStmt returnstmt(Object s2) throws Exception {
        // production rule: RETURN expr SEMI
        
        // 1. check if expr.value_type matches with the current function return type
        // 2. etc.
        // 3. create and return node
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.ReturnStmt(expr);
    }

    ParseTree.IfStmt ifstmt(Object expression, Object if_statement, Object else_statement) throws Exception {
        // production rule: IF (expr) stmt ELSE stmt
        
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt if_s = (ParseTree.Stmt) if_statement;
        ParseTree.Stmt else_s = (ParseTree.Stmt) else_statement;

        return new ParseTree.IfStmt(expr, if_s, else_s);
    }

    ParseTree.WhileStmt whilestmt(Object expression, Object statement) throws Exception {
        // production rule: WHILE (expr) stmt
        
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt st = (ParseTree.Stmt) statement;
        return new ParseTree.WhileStmt(expr, st);
    }

    ParseTree.CompoundStmt compoundstmt(Object _locals) throws Exception {
        // production rule: { local_decls stmt_list }
        
        ArrayList<ParseTree.LocalDecl> locals = (ArrayList<ParseTree.LocalDecl>) _locals;
        
        env = new Env(env);     // add new symbol table for new stack frame

        // add local declarations to stack frame symbol table
        for (ParseTree.LocalDecl local : locals) {
            env.put(local.ident, local);
        }

        return null;
    }

    ParseTree.CompoundStmt compoundstmt(Object _locals, Object _stmts) throws Exception {
        // production rule: { local_decls stmt_list }
        
        ArrayList<ParseTree.LocalDecl> locals = (ArrayList<ParseTree.LocalDecl>) _locals;
        ArrayList<ParseTree.Stmt> stmts       = (ArrayList<ParseTree.Stmt>) _stmts;

        // leaving stack frame, pop symbol table 
        env = env.prev;

        return new ParseTree.CompoundStmt(locals, stmts);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.Arg> args(Object args) throws Exception {
        // production rule: args -> arg_list
        
        return (ArrayList<ParseTree.Arg>) args;
    }

    ArrayList<ParseTree.Arg> args() throws Exception {
        // production rule: args -> epsilon

        return new ArrayList<ParseTree.Arg>();
    }

    ArrayList<ParseTree.Arg> arglist(Object args, Object exp) throws Exception {
        // production rule: arg_list -> arg_list, expr
        
        
        ParseTree.Expr _exp = (ParseTree.Expr) exp;
        ParseTree.Arg arg = new ParseTree.Arg(_exp);
        arg.info.set_type(_exp.info);
        
        ArrayList<ParseTree.Arg> arglist = (ArrayList<ParseTree.Arg>) args;
        arglist.add(arg);

        return arglist;
    }

    ArrayList<ParseTree.Arg> arglist(Object exp) throws Exception {
        // production rule: arg_list -> expr
        
        ParseTree.Expr _exp = (ParseTree.Expr) exp;
        ParseTree.Arg arg = new ParseTree.Arg(_exp);
        arg.info.set_type(_exp.info);
        
        ArrayList<ParseTree.Arg> arglist = new ArrayList<ParseTree.Arg>();
        arglist.add(arg);

        return arglist;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.Expr expr_oper(Object s1, String op, Object s3) throws Exception {
        // production rules:
        //      -  expr -> expr + expr
        //      -  expr -> expr - expr
        //      -  expr -> expr * expr
        //      -  expr -> expr / expr
        //      -  expr -> expr % expr
        //      -  expr -> expr = expr
        //      -  expr -> expr != expr
        //      -  expr -> expr <= expr
        //      -  expr -> expr < expr
        //      -  expr -> expr >= expr
        //      -  expr -> expr > expr
        //      -  expr -> expr && expr
        //      -  expr -> expr || expr
        
        // 1. check if expr1.value_type matches with the expr2.value_type
        // 2. etc.
        // 3. create and return node that has value_type
        ParseTree.Expr expr1 = (ParseTree.Expr) s1;
        Token oper = new Token(op);
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

    ParseTree.ExprNot expr_not(Object s2) throws Exception {
        // production rule: expr -> NOT expr
        
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        ParseTree.ExprNot expr = new ParseTree.ExprNot(inner_expr);
        expr.info.set_type(inner_expr);
        
        return expr;
    }

    ParseTree.ExprParen expr_paren(Object s2) throws Exception {
        // production rule: expr -> (expr)
        
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        ParseTree.ExprParen expr =  new ParseTree.ExprParen(inner_expr);
        expr.info.set_type(inner_expr.info);

        return expr;
    }

    ParseTree.ExprIdent expr_id(String s1) throws Exception {
        // production rule: expr -> ident
        
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is variable type
        // 3. etc.
        // 4. create and return node that has the value_type of the id.lexeme
        Debug("expr -> IDENT(" + s1 + ")");
        Token id = new Token(s1);

        ParseTree.LocalDecl ident_attr = (ParseTree.LocalDecl) env.get(id.lexeme);
        if ( ident_attr == null )
            throw new Exception("Identifier `" + id.lexeme + "` is not in the symbol table");

        ParseTree.ExprIdent expr = new ParseTree.ExprIdent(id.lexeme);
        expr.reladdr = 1;
        expr.info.set_type(ident_attr.info);

        return expr;
    }

    ParseTree.ExprIntLit expr_int(Integer num) throws Exception {
        // production rule: expr -> int
        
        // 1. create and return node that has int type
        Token token = new Token(num);
        ParseTree.ExprIntLit int_literal =  new ParseTree.ExprIntLit(token.parseInt());
        int_literal.info.set_int_type();

        return int_literal;
    }

    ParseTree.ExprBoolLit expr_bool(String bool_value) throws Exception {
        // production rule: expr -> bool
        
        // 1. create and return node that has int type
        Token token = new Token(bool_value);
        
        ParseTree.ExprBoolLit bool_literal = new ParseTree.ExprBoolLit( token.lexeme == "true" );
        bool_literal.info.set_bool_type();
        
        return bool_literal;
    }

    ParseTree.ExprCall expr_call(String ident, Object _args) throws Exception {
        // production rule: expr -> CALL ident(args)
        
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is function type
        // 3. check if the number and types of env(id.lexeme).params match with those of
        // args
        // 4. etc.
        // 5. create and return node that has the value_type of
        // env(id.lexeme).return_type
        Token id = new Token(ident);
        ArrayList<ParseTree.Arg> args = (ArrayList<ParseTree.Arg>) _args;
        Object attr                   = env.get(id.lexeme);

        // check that lexeme found in symbol table
        if ( attr == null )
                throw new Exception("Identifier `" + id.lexeme + "` is not in the symbol table");

        // check that symbol table entry is a function
        if ( !(attr instanceof ParseTree.FuncDecl) )
            throw new Exception("Call made to non-function type `" + id.lexeme + "`");

        ParseTree.FuncDecl func_attr  = (ParseTree.FuncDecl) env.get(id.lexeme);
        {
            // check if argument types match with function param types
            if ( func_attr.params.size() != args.size() )
                throw new Exception("Function parameters and args differ in length.");

            for (int i = 0; i < args.size(); i++) {
                if ( !func_attr.params.get(i).info.equals(args.get(i).info) )
                    throw new Exception("Function call parameter/argument mismatch at index " + i);
            }

            // if (func_attr.info.equals("func()->int")
            //         && (args.size() == 0)) {
            // } // ok
            // else {
            //     throw new Exception("semantic error");
            // }
        }
        ParseTree.ExprCall expr = new ParseTree.ExprCall(id.lexeme, args);
        expr.info.set_type(func_attr);

        return expr;
    }
}
