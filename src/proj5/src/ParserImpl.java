import java.lang.reflect.Array;
import java.util.*;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ParserImpl {
    public static Boolean _debug = true;

    public static int lineno;
    public static int column;

    public static void Debug(String message) {
        if (_debug)
            System.out.println(message);
    }

    public static class ParserImplException extends Exception {
        public ParserImplException(String message) {
            super( String.format("[Error at %d:%d] %s", lineno, column, message) );
        }
        public ParserImplException(String message, Token token) {
            super( String.format("[Error at %d:%d] %s", token.lineno, token.column, message) );
        }
        public ParserImplException(String message, int line, int col) {
            super( String.format("[Error at %d:%d] %s", line, col, message));
        }
    }

    public static void set_loc(int line, int col) {
        lineno = line;
        column = col;
    }

    // This is for chained symbol table.
    // This includes the global scope only at this moment.
    Env env = new Env(null);
    // this stores the root of parse tree, which will be used to print parse tree
    // and run the parse tree
    ParseTree.Program parsetree_program = null;

    // for counting the relative address:
    //      - for i-th (starting at 1) parameter: =-{i}
    //      - for i-th (starting at 1) local var: ={i}
    int reladdr_count = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.Program program(Object _decllist) throws Exception {
        // production rule: program -> decl_list

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>) _decllist;
        // 1. check if decllist has main function having no parameters and returns int
        // type
        // 2. assign the root, whose type is ParseTree.Program, to parsetree_program
        for (ParseTree.FuncDecl func : decllist) {
            if (
                func.ident.equals("main") &&
                func.params.size() == 0 &&
                func.rettype.info.is_int_type()
            ) {
                parsetree_program = new ParseTree.Program(decllist);

                boolean found_return = false;
                for (ParseTree.Stmt stmt : func.stmtlist) {
                    try {
                        ParseTree.ReturnStmt rs = (ParseTree.ReturnStmt) stmt;
                        
                        if ( rs.expr.info.is_int_type() ) {
                            found_return = true;
                            break;
                        }
                    } catch (Exception e) {}
                }

                if ( !found_return )
                    throw new ParserImplException("The function main() should return at least one int value.");
        
                return parsetree_program;
            }
        }

        throw new Exception("The program must have one main function that returns int type and has no parameters.");
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

    ParseTree.FuncDecl fundecl(Object func, Object _id, Object parameters, Object return_type) throws Exception {
        // 1. add function_type_info object (name, return type, params) into the global
        // scope of env
        // 2. create a new symbol table on top of env
        // 3. add parameters into top-local scope of env
        // 4. etc.
        
        Token id = (Token) _id;
        ArrayList<ParseTree.Param> params     = (ArrayList<ParseTree.Param>) parameters;
        ParseTree.TypeSpec rettype            = (ParseTree.TypeSpec) return_type;

        ParseTree.FuncDecl decl = new ParseTree.FuncDecl(id.lexeme, rettype, params, null, null);
        decl.info.set_type(rettype);
        decl.info.set_token(id);

        // Debug("\tfn <" + id.lexeme + "({" + params.size() +  "}) -> " + rettype.typename +">");

        if ( env.get(id.lexeme) != null )
            throw new ParserImplException(String.format("The function %s() is already defined.", id.lexeme), (Token) func);

        env.put(id.lexeme, decl); // add function to current stack symbol table
        env = new Env(env);       // add new symbol table for new stack frame

        // add parameters to new stack frame
        for (ParseTree.Param param : params) {
            env.put(param.ident, param);
        }

        return decl;
    }


    // ParseTree.FuncDecl fundecl(Object func, Object _id, Object parameters, Object return_type, Object _locals) throws Exception {
    ParseTree.FuncDecl fundecl(Object func_decl, Object _locals) throws Exception {
        // 1. add function_type_info object (name, return type, params) into the global
        // scope of env
        // 2. create a new symbol table on top of env
        // 3. add parameters into top-local scope of env
        // 4. etc.
        
        ArrayList<ParseTree.LocalDecl> locals = (ArrayList<ParseTree.LocalDecl>) _locals;

        ParseTree.FuncDecl decl = (ParseTree.FuncDecl) func_decl;
        decl.localdecls = locals;

        // add local declarations to stack frame symbol table
        ParseTree.FuncDecl current_func = (ParseTree.FuncDecl) env.get_current_func();
        for (int index = 0; index < locals.size(); index++) {
            ParseTree.LocalDecl local = locals.get(index);

            local.reladdr = current_func.info.next_reladdr();
            env.put(local.ident, local);
        }

        return decl;
    }

    ParseTree.FuncDecl fundecl(Object func_decl, Object statements, Object end) throws Exception {
        // 1. check if this function has at least one return type
        // 2. etc.
        // 3. create and return funcdecl node
        
        ArrayList<ParseTree.Stmt> stmtlist        = (ArrayList<ParseTree.Stmt>) statements;

        ParseTree.FuncDecl decl = (ParseTree.FuncDecl) func_decl;
        decl.stmtlist = stmtlist;

        boolean found_return = false;
        for (ParseTree.Stmt stmt : stmtlist) {
            if ( found_return )
                break;

            if ( stmt instanceof ParseTree.ReturnStmt ) {
                ParseTree.ReturnStmt rstmt = (ParseTree.ReturnStmt) stmt;
                if ( rstmt.info.equals(decl.rettype.info) ) {
                    found_return = true;
                }
            }
            else if ( stmt instanceof ParseTree.IfStmt ) {
                found_return = ((ParseTree.IfStmt) stmt).info.type != null;
            }
            else if ( stmt instanceof ParseTree.WhileStmt ) {
                found_return = ((ParseTree.WhileStmt) stmt).info.type != null;
            }
        }

        if ( !found_return )
            throw new ParserImplException(String.format("The function %s() should return at least one %s value.", decl.ident, decl.rettype.typename), (Token) end);


        // leaving stack frame, pop symbol table 
        env = env.prev;
        
        return decl;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.Param> params(Object parameter_list) throws Exception {
        // production rule: params -> param_list

        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>) parameter_list;
        for (int index = 0; index < params.size(); index++) {
            ParseTree.Param param = params.get(index);

            param.reladdr = (index + 1) * -1;  
        }

        return params;
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

    ParseTree.Param param(Object ident, Object type) throws Exception {
        // production rule: param -> VAR type_spec IDENT

        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) type;
        Token token                 = (Token) ident;
        
        // put IDENT in symbol table
        env.put(token.lexeme, typespec);

        ParseTree.Param _param = new ParseTree.Param(token.lexeme, typespec);
        _param.info.set_type(typespec.info);
        _param.info.set_token(token);

        return _param;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.TypeSpec typespec(Object _primtype) {
        // production rule: typespec -> prim_type

        return (ParseTree.TypeSpec) _primtype;
    }

    ParseTree.TypeSpec primtype(Object keyword) throws Exception {
        // production rule: primtype -> INT | BOOL

        Token id = (Token) keyword;
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec(id.lexeme);
        typespec.info.set_type(id.lexeme);
        typespec.info.set_token(id);

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

    ParseTree.LocalDecl localdecl(Object var, Object ident, Object type) throws Exception {
        // production rule: local_decl -> VAR type_spec IDENT SEMI

        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec) type;
        Token id = (Token) ident;

        if ( env.current_frame_contains(id.lexeme) )
            throw new ParserImplException(String.format("The identifier %s is already defined.", id.lexeme), (Token) var);

        // add declaration to symbol table
        env.put(id.lexeme, typespec);

        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.reladdr = 0;
        localdecl.info.set_type(typespec.info);
        localdecl.info.set_token(id);

        return localdecl;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<ParseTree.Stmt> stmtlist(Object s1, Object s2) throws Exception {
        // production rule: stmt_list -> stmt_list stmt

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

    ParseTree.ReturnStmt stmt_return(Object s1) throws Exception {
        // production rule: stmt -> return_stmt
        
        assert (s1 instanceof ParseTree.ReturnStmt);
        return (ParseTree.ReturnStmt) s1;
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

    ParseTree.AssignStmt assignstmt(Object ident, Object assn, Object expression) throws Exception {
        // production rule: IDENT <- expr SEMI
        
        // 1. check if ident.value_type matches with expr.value_type
        // 2. etc.
        // e. create and return node
        assert expression != null;

        Token id = (Token) ident;
        ParseTree.Expr expr       = (ParseTree.Expr) expression;
        Object attr = env.get(id.lexeme);
        if ( attr == null )
            throw new ParserImplException(String.format("Cannot use an undefined variable %s.", id.lexeme), (Token) id);


        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr);
        stmt.info.set_type(expr);
        stmt.info.set_token(id);

        ParseTreeInfo.DataTypeInfo info = null;
        if ( attr instanceof ParseTree.LocalDecl ) {
            ParseTree.LocalDecl ld = (ParseTree.LocalDecl) attr;
            info = ld.info;
            stmt.ident_reladdr = ld.reladdr;
        }
        else if ( attr instanceof ParseTree.TypeSpec ) {
            ParseTree.TypeSpec ld = (ParseTree.TypeSpec) attr;
            info = ld.info;
            // stmt.ident_reladdr = ld.reladdr;
        }
        else if ( attr instanceof ParseTree.Param ) {
            ParseTree.Param ld = (ParseTree.Param) attr;
            info = ld.info;
            stmt.ident_reladdr = ld.reladdr;
        }
        else
            Debug("Got an unmatched type: " + attr.getClass());
            

        if ( info == null )
            throw new ParserImplException(String.format("Cannot use an undefined variable %s.", id.lexeme), expr.info.token);
        else if ( !info.equals(expr.info) ) {
                throw new ParserImplException(
                    String.format("Cannot assign %s value to %s variable %s.", expr.info.toString(), info.toString(), id.lexeme),
                    (Token) assn
                );
            }

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

        ParseTree.FuncDecl current_func = (ParseTree.FuncDecl) env.get_current_func();
        if ( current_func == null )
            throw new ParserImplException("Cannot call return outside of a function body");

        if ( !current_func.rettype.info.equals(expr.info) )
            throw new ParserImplException(
                String.format(
                    "The type of returning value (%s) should match with the return type (%s) of the function main().",
                    expr.info.toString(),
                    current_func.info.toString()
                ),
                expr.info.token.lineno,
                expr.info.token.column
            );

        ParseTree.ReturnStmt stmt = new ParseTree.ReturnStmt(expr);
        stmt.info.set_type(expr);

        return stmt;
    }

    ParseTree.IfStmt ifstmt(Object expression) throws Exception {
        // production rule: IF (expr)
        
        ParseTree.Expr expr = (ParseTree.Expr) expression;

        if ( !expr.info.is_bool_type() )
            throw new ParserImplException("Use bool value to the check condition in if statement.", expr.info.token);

        return null;
    }

    ParseTree.IfStmt ifstmt(Object expression, Object if_statement, Object else_statement) throws Exception {
        // production rule: IF (expr) stmt ELSE stmt
        
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt if_s = (ParseTree.Stmt) if_statement;
        ParseTree.Stmt else_s = (ParseTree.Stmt) else_statement;

        ParseTree.IfStmt ifstatement = new ParseTree.IfStmt(expr, if_s, else_s);
        if ( !expr.info.is_bool_type() )
            throw new ParserImplException("Use bool value to the check condition in if statement.", expr.info.token);

        if ( if_s.info.type != null )
            ifstatement.info.set_type(if_s);
        else if ( else_s.info.type != null )
            ifstatement.info.set_type(else_s);

        return ifstatement;
    }

    ParseTree.WhileStmt whilestmt(Object expression) throws Exception {
        // production rule: WHILE (expr)
        ParseTree.Expr expr = (ParseTree.Expr) expression;

        if ( !expr.info.is_bool_type() )
            throw new ParserImplException("Use bool value to the check condition in while statement.", expr.info.token);

        return null;
    }

    ParseTree.WhileStmt whilestmt(Object expression, Object statement) throws Exception {
        // production rule: WHILE (expr) stmt
        
        ParseTree.Expr expr = (ParseTree.Expr) expression;
        ParseTree.Stmt st = (ParseTree.Stmt) statement;

        return new ParseTree.WhileStmt(expr, st);
    }

    ParseTree.CompoundStmt compoundstmt() throws Exception {
        env = new Env(env);     // add new symbol table for new stack frame

        return null;
    }

    ParseTree.CompoundStmt compoundstmt(Object _locals) throws Exception {
        // production rule: { local_decls stmt_list }
        
        ArrayList<ParseTree.LocalDecl> locals = (ArrayList<ParseTree.LocalDecl>) _locals;
        ParseTree.FuncDecl current_func = (ParseTree.FuncDecl) env.get_current_func();

        // add local declarations to stack frame symbol table
        for (int index = 0; index < locals.size(); index++) {
            ParseTree.LocalDecl local = locals.get(index);

            local.reladdr = current_func.info.next_reladdr();
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

        ParseTree.CompoundStmt compstmt = new ParseTree.CompoundStmt(locals, stmts);
        for (ParseTree.Stmt stmt : stmts) {
            if ( stmt.info.type != null )
                compstmt.info.set_type(stmt);
        }

        return compstmt;
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
        arg.info.set_token(_exp.info.token);
        
        ArrayList<ParseTree.Arg> arglist = (ArrayList<ParseTree.Arg>) args;
        arglist.add(arg);

        return arglist;
    }

    ArrayList<ParseTree.Arg> arglist(Object exp) throws Exception {
        // production rule: arg_list -> expr
        
        ParseTree.Expr _exp = (ParseTree.Expr) exp;
        ParseTree.Arg arg = new ParseTree.Arg(_exp);
        arg.info.set_type(_exp.info);
        arg.info.set_token(_exp.info.token);
        
        ArrayList<ParseTree.Arg> arglist = new ArrayList<ParseTree.Arg>();
        arglist.add(arg);

        return arglist;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ParseTree.Expr expr_oper(Object s1, Object op, Object s3) throws Exception {
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
        // Token oper = new Token(op);
        Token oper = (Token) op;
        ParseTree.Expr expr2 = (ParseTree.Expr) s3;

        // check if expr1.type matches with expr2.type
        if ( expr1.info.type != expr2.info.type ) {
            throw new ParserImplException("Cannot perform " + expr1.info.toString() + " " + oper.lexeme + " " + expr2.info.toString() + ".", oper.lineno, oper.column);
        }

        if ( expr1.info.is_bool_type() && expr2.info.is_bool_type() ) {
            switch (oper.lexeme) {
                case "+" :
                case "-" :
                case "*" :
                case "/" :
                case "%" :
                case "<=":
                case "<" :
                case ">=":
                case ">" :
                    throw new ParserImplException("Cannot perform bool " + oper.lexeme + " bool.", oper);
            }
        }
        
        ParseTree.Expr expr;
        switch (oper.lexeme) {
            case "+" :  { expr = new ParseTree.ExprAdd(expr1, expr2); expr.info.set_int_type();  break; }
            case "-" :  { expr = new ParseTree.ExprSub(expr1, expr2); expr.info.set_int_type();  break; }
            case "*" :  { expr = new ParseTree.ExprMul(expr1, expr2); expr.info.set_int_type();  break; }
            case "/" :  { expr = new ParseTree.ExprDiv(expr1, expr2); expr.info.set_int_type();  break; }
            case "%" :  { expr = new ParseTree.ExprMod(expr1, expr2); expr.info.set_int_type();  break; }
            case "=" :  { expr = new ParseTree.ExprEq (expr1, expr2); expr.info.set_bool_type(); break; }
            case "!=":  { expr = new ParseTree.ExprNe (expr1, expr2); expr.info.set_bool_type(); break; }
            case "<=":  { expr = new ParseTree.ExprLe (expr1, expr2); expr.info.set_bool_type(); break; }
            case "<" :  { expr = new ParseTree.ExprLt (expr1, expr2); expr.info.set_bool_type(); break; }
            case ">=":  { expr = new ParseTree.ExprGe (expr1, expr2); expr.info.set_bool_type(); break; }
            case ">" :  { expr = new ParseTree.ExprGt (expr1, expr2); expr.info.set_bool_type(); break; }
            case "and": { expr = new ParseTree.ExprAnd(expr1, expr2); expr.info.set_bool_type(); break; }
            case "or":  { expr = new ParseTree.ExprOr (expr1, expr2); expr.info.set_bool_type(); break; }

            default:
                throw new ParserImplException("no operation matched for " + oper.lexeme + ".", oper);
        }


        expr.info.set_token(expr1.info.token);


        // Debug(String.format(
        //     "New Expr <%s %s %s -> %s>",
        //     expr1.info.toString(),
        //     op,
        //     expr2.info.toString(),
        //     expr.info.toString()
        // ));

        // expr.info.set_type(expr1.info);

        return expr;
    }

    ParseTree.ExprNot expr_not(Object _not, Object s2) throws Exception {
        // production rule: expr -> NOT expr
        
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        
        if ( inner_expr.info.is_int_type() )
            throw new ParserImplException("Cannot perform not int.", (Token) _not);
        
        ParseTree.ExprNot expr = new ParseTree.ExprNot(inner_expr);
        expr.info.set_type(inner_expr);
        expr.info.set_token(inner_expr.info.token);
        
        return expr;
    }

    ParseTree.ExprParen expr_paren(Object s2) throws Exception {
        // production rule: expr -> (expr)
        
        ParseTree.Expr inner_expr = (ParseTree.Expr) s2;
        ParseTree.ExprParen expr =  new ParseTree.ExprParen(inner_expr);
        expr.info.set_type(inner_expr.info);
        expr.info.set_token(inner_expr.info.token);

        return expr;
    }

    ParseTree.ExprIdent expr_id(Object s1) throws Exception {
        // production rule: expr -> ident
        
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is variable type
        // 3. etc.
        // 4. create and return node that has the value_type of the id.lexeme
        Debug("expr -> IDENT(" + s1 + ")");

        Token id = (Token) s1;

        Object attr = env.get(id.lexeme);
        if ( attr == null )
            throw new ParserImplException("Cannot use an undefined variable " + id.lexeme + ".");

        if (attr instanceof ParseTree.FuncDecl)
            throw new ParserImplException(String.format("Cannot use the function %s() as a variable.", id.lexeme), id);

        ParseTree.ExprIdent expr = new ParseTree.ExprIdent(id.lexeme);
        expr.info.set_token(id);
        
        if ( attr instanceof ParseTree.LocalDecl ) {
            ParseTree.LocalDecl ident_attr = (ParseTree.LocalDecl) attr;
            expr.info.set_type(ident_attr.info);
            expr.reladdr = ident_attr.reladdr;
        }
        else if ( attr instanceof ParseTree.Param ) {
            ParseTree.Param ident_attr = (ParseTree.Param) attr;
            expr.info.set_type(ident_attr.info);
            expr.reladdr = ident_attr.reladdr;
        } 
        
        else {
            throw new ParserImplException(String.format("Unexpected expression type `%s` in expr_id()", attr.getClass()));
        }

        return expr;
    }

    ParseTree.ExprIntLit expr_int(Object _num) throws Exception {
        // production rule: expr -> int
        
        // 1. create and return node that has int type
        // Token token = new Token(num);
        Token token = (Token) _num;
        int num = Integer.parseInt(token.lexeme);

        ParseTree.ExprIntLit int_literal =  new ParseTree.ExprIntLit(num);
        int_literal.info.set_int_type();
        int_literal.info.set_token(token);

        return int_literal;
    }

    ParseTree.ExprBoolLit expr_bool(Object bool_value) throws Exception {
        // production rule: expr -> bool
        
        // 1. create and return node that has int type
        Token token = (Token) bool_value;
        
        ParseTree.ExprBoolLit bool_literal = new ParseTree.ExprBoolLit( token.lexeme.equals("true") );
        bool_literal.info.set_bool_type();
        bool_literal.info.set_token(token);
        
        return bool_literal;
    }

    ParseTree.ExprCall expr_call(Object call, Object ident, Object _args) throws Exception {
        // production rule: expr -> CALL ident(args)
        
        // 1. check if id.lexeme can be found in chained symbol tables
        // 2. check if it is function type
        // 3. check if the number and types of env(id.lexeme).params match with those of
        // args
        // 4. etc.
        // 5. create and return node that has the value_type of
        // env(id.lexeme).return_type
        Token id = (Token) ident;
        ArrayList<ParseTree.Arg> args = (ArrayList<ParseTree.Arg>) _args;
        Object attr                   = env.get(id.lexeme);

        // check that lexeme found in symbol table
        if ( attr == null )
                throw new ParserImplException(String.format("Cannot use an undefined function %s().", id.lexeme), (Token) call);

        // check that symbol table entry is a function
        if ( !(attr instanceof ParseTree.FuncDecl) )
            throw new ParserImplException(String.format("Cannot use a variable %s as a function.", id.lexeme), (Token) call);

        ParseTree.FuncDecl func_attr  = (ParseTree.FuncDecl) env.get(id.lexeme);
        {
            // check if argument types match with function param types
            if ( func_attr.params.size() != args.size() )
                throw new ParserImplException(
                    String.format("Cannot pass the incorrect number of arguments to %s().", id.lexeme),
                    (Token) call
                );

            for (int i = 0; i < args.size(); i++) {
                if ( !func_attr.params.get(i).info.equals(args.get(i).info) ) {
                    String nth = "" + (i+1);
                    switch (i+1) {
                        case 1:
                            nth = nth + "st";
                            break;
                        case 2:
                            nth = nth + "nd";
                            break;
                        case 3:
                            nth = nth + "rd";
                            break;
                        default:
                            nth = nth + "th";
                    }

                    throw new ParserImplException(
                        String.format("The %s argument of the function %s() should be %s type.", nth, id.lexeme, func_attr.params.get(i).info.toString()),
                        args.get(i).info.token
                    );

                }
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
        expr.info.set_token(id);

        return expr;
    }
}
