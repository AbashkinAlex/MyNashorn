import org.junit.Test;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by alex on 10.10.16.
 */
public class RestAPIControllerTest {
    @Test
    public void processingInputScript() throws Exception {

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        assertNotNull(engine);
        Object result = engine.eval("var v = { field1: 'someValue', field2: 3e3 } ; v;");
        Object JSON = engine.get("JSON");
        Invocable i = (Invocable) engine;
        String s = (String) i.invokeMethod(JSON, "stringify", result);
        System.out.print(s);
        assertEquals("Must match to stringified ", "{\"field1\":\"someValue\",\"field2\":3000}", s);


    }

}