package at.kaindorf.dienstplan.getVariableFromJS;
import javax.script.*;
import java.io.FileNotFoundException;

public class getVariableFromJS {
        public static void main(String[] args) throws ScriptException, FileNotFoundException {
            // Create a new ScriptEngine instance
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            // Load the external .js file
            engine.eval(new java.io.FileReader("main.js"));

            // Retrieve the value of the variable
            String myVar = (String) engine.get("myVar");
            System.out.println(myVar); // Output: Hello, World!
        }
}