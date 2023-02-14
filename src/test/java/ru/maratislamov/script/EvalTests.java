package ru.maratislamov.script;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class EvalTests {

    @Test
    public void testConcatArrays(){
        String code= "base_menu = [1,2]\n tail_menu=[3,4]\n current_menu = base_menu + [tail_menu]";

        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream((code).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        System.out.println(sess.getSessionScope());
    }


//    @Test
//    public void testLn0(){
//        String x = "0";
//        String y = "1";
//        System.out.println(x=-y);
//    }

    @Test
    public void testLn(){
        assertEval("\"head\"\n + \"tail\"", "headtail");
        assertEval("\"head\" + \"tail\"", "headtail");
        assertEval("\"head\" + \n\"tail\"", "headtail");
        assertEval("\n[\n\t1,\n\t2\n]", ListValue.of(new NumberValue(1), new NumberValue(2)));
    }


    public void assertEval(String exp, String result){
        ScriptEngine scriptEngine = new ScriptEngine();
        System.out.printf("E=" + exp);
        scriptEngine.load(new ByteArrayInputStream(("E=" + exp).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        Assertions.assertEquals(result, String.valueOf(sess.getSessionScope().get("E")), "Ошибка арифметического вычисления: " + exp);
    }

    public void assertEval(String exp, Value result){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=" + exp).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        Assertions.assertEquals(result, sess.getSessionScope().get("E"), "Ошибка арифметического вычисления: " + exp);
    }

    @Test
    public void testEval(){
        assertEval("-55", "-55");
        assertEval("6 - (5 + 5)", "-4");
        assertEval("6 * 5 - 5", "25");

        assertEval("6 - 5 + 5", "6");
        assertEval("(6 - 5) + 5", "6");
        assertEval("5 - 6 + 6", "5");
        assertEval("-55.1", "-55,1");


        assertEval("6 - 5 * 5", "-19");
        assertEval("6 * (5 + 5)", "60");
        assertEval("-6 * (5 + 5)", "-60");

        assertEval("6 * (5 + (3 + 1 * 3) - 5)", "36");
        assertEval("6 * (5 + ((3 + 1) * 3) - 5)", "72");
    }

    @Test
    public void assertIfThen() {
        assertIfThen("if 1 then result = \"yes\"");
        assertIfThen("if (1 + 2 * 3) == 7 then result = \"yes\"");

        assertIfThen("E = (7 + (1+1 == 2+0) * 3) == 10 ");
    }


    public void assertIfThen(String code){

        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});
    }



    @Test
    public void testCollection(){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=[];E.1=1; E.2=2; E.3=3").getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        ListValue listValue = (ListValue) sess.getSessionScope().get("E");

        System.out.println(listValue);

        AtomicInteger i = new AtomicInteger();
        listValue.forEach(l -> {
            System.out.println(l);
            i.incrementAndGet();
        });
        assert i.get() == 4;  // [null, 1, 2, 3]

        AtomicInteger ii = new AtomicInteger();
        listValue.getIterator().forEachRemaining(l -> {
            System.out.println(l);
            ii.incrementAndGet();
        });
        assert ii.get() == 4;  // [null, 1, 2, 3]
    }


}
