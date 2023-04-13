import java.util.ArrayList;
import java.util.HashMap;

public class Env
{
    public Env prev;
    public HashMap<String, Object> table;

    public Env(Env prev)
    {
        this.prev = prev;
    }
    public void Put(String name, Object value)
    {
        this.table.put(name, value);
    }

    public Object Get(String name)
    {
        // this is a fake implementation
        // For the real implementation, I recommend to return a class object
        //   since the identifier's type can be variable or function
        //   whose detailed attributes will be different
        // if(name.equals("a") == true) return "int";
        // if(name.equals("b") == true) return "bool";
        // if(name.equals("testfunc") == true) return "func()->int";

        // TODO: make an interface for Env objects to impl with a display function
        if ( table.containsKey(name) )
            return table.get(name);

        return null;
    }
}
