package ru.maratislamov.script.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptFunctionsExecutor;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.NotFoundValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.Value;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контекст выполнения скрипта
 */
public class DebugExecutor extends ScriptFunctionsExecutor {

    Logger logger = LoggerFactory.getLogger(DebugExecutor.class);

    public DebugExecutor() {
    }

    /**
     * выполняет библиотечную функцию. Если функция @fname неизвестна, метод должен вернуть NOT_FOUND_VALUE
     *
     * @param fname
     * @param args
     * @param session
     * @return
     * @throws Exception
     */
    @Override
    public Value onExec(String fname, List<Value> args, ScriptSession session) throws Exception {
        logger.debug("onExec: {} {}", fname, args);
        String function = fname.toLowerCase();

        switch (function) {
            case "print":
            case "inline":
            case "keyboard":
            case "getcontact":
                System.out.println(args.stream().map(Value::toString).collect(Collectors.joining(" ")));
                return args.isEmpty() ? Value.NULL : args.get(0);

            case "debug":
                System.out.println("$" + args.stream().map(Value::getClass).map(Class::getSimpleName).collect(Collectors.joining(" $")));
                return args.isEmpty() ? Value.NULL : args.get(0);

            case "input":
                try (InputStreamReader converter = new InputStreamReader(System.in)) {
                    BufferedReader lineIn = new BufferedReader(converter);
                    String input = lineIn.readLine();
                    Value result;

                    try {
                        double value = Double.parseDouble(input);
                        result = new NumberValue(value);

                    } catch (NumberFormatException e) {
                        result = new StringValue(input);
                    }
                    return result;
                }

            default:
                //throw new Error("Unknown function: " + fname);
                return NotFoundValue.NOT_FOUND_VALUE;
        }
    }

}
