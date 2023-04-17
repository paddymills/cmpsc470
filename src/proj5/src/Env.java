import java.util.ArrayList;
import java.util.HashMap;

public class Env
{
    public Env prev;
    private HashMap<String, Object> table;
    
    public Env(Env prev) {
        this.prev = prev;
    }
    
    public void put(String name, Object value) {
        // first call: initialize table HashMap
        if ( this.table == null )
            this.table = new HashMap<String, Object>();

        this.table.put(name, value);
    }

    public Object get(String name) {
        if ( this.table != null && this.table.containsKey(name) )
            return this.table.get(name);

        if ( this.prev != null )
            return this.prev.Get(name);

        return null;
    }
    public void Put(String name, Object value) {
        // call the actual function is not named like a class
        this.put(name, value);
    }

    public Object Get(String name) {
        // call the actual function is not named like a class
        return this.get(name);

        // this is a fake implementation
        // For the real implementation, I recommend to return a class object
        //   since the identifier's type can be variable or function
        //   whose detailed attributes will be different
        // if(name.equals("a") == true) return "int";
        // if(name.equals("b") == true) return "bool";
        // if(name.equals("testfunc") == true) return "func()->int";
        // return null;
    }
}
