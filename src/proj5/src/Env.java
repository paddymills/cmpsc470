import java.util.ArrayList;
import java.util.HashMap;

public class Env
{
    public Env prev;
    private HashMap<String, Object> entries;
    public Env(Env prev)
    {
        this.prev = prev;
    }
    public void Put(String name, Object value)
    {
        this.entries.put(name, value);
    }
    public Object Get(String name)
    {
        if ( this.entries.containsKey(name) )
            return this.entries.get(name);

        // this is a fake implementation
        // For the real implementation, I recommend to return a class object
        //   since the identifier's type can be variable or function
        //   whose detailed attributes will be different
        // if(name.equals("a") == true) return "int";
        // if(name.equals("b") == true) return "bool";
        // if(name.equals("testfunc") == true) return "func()->int";
        return null;
    }
}
