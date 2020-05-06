package ru.maratislamov.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.values.NULLValue;
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
public class ScriptConsoleImplementator<T> extends ScriptFunctionsImplemntator<T> {

    Logger logger = LoggerFactory.getLogger(ScriptConsoleImplementator.class);

    @Override
    public Value onExec(String fname, List<Value> args, ScriptSession<T> session) throws Exception {
        String function = fname.toLowerCase();

        switch (function) {
            case "print":
            case "inline":
            case "button":
                System.out.println(args.stream().map(Value::toString).collect(Collectors.joining(" ")));
                return null;

            case "input":
                InputStreamReader converter = new InputStreamReader(System.in);
                BufferedReader lineIn = new BufferedReader(converter);
                String input = lineIn.readLine();
                Value result;
                try {
                    double value = Double.parseDouble(input);
                    result = new NumberValue(value);

                    if (args.size() != 1) throw new Error("1 arg expected for input");
                    session.getVariables().put(args.get(0).toString(), result);

                } catch (NumberFormatException e) {
                    result = new StringValue(input);
                    session.getVariables().put(args.get(0).toString(), result);
                }
                return result;

            case "verifybitrix":
            case "verifybot":
                return new NULLValue();

            default:
                throw new Error("Unknown function: " + fname);
        }
    }

}
