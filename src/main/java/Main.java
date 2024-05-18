import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.ScriptFunctionsService;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.debug.DebugExecutor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {

        String fname = "C:\\WORK\\botplatform-script\\experiments.bas";
//        String fname = "C:\\WORK\\botplatform-script\\src\\test\\resources\\sample\\for.jas";
        //String fname = "C:\\WORK\\botplatform-script\\src\\test\\resources\\sample\\ifthenbeginend.jas";

        ScriptFunctionsService.register(new DebugExecutor());

        try (InputStream inputStream = new FileInputStream(fname)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);
            botScript.interpret(new ScriptSession(ScriptRunnerContext.empty){{ setCurrentStatement(0);}});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
