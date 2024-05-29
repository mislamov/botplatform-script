package ru.maratislamov.script.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptFunctionsExecutor;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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

            case "throw":
                throw new RuntimeException("throw called: " + args.stream().map(Value::toString).collect(Collectors.joining(" ")));

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

            case "sort_maps_by":
                assert args.size() == 2;

                assert args.get(0) instanceof StringValue;
                assert args.get(1) instanceof ListValue;

                String path = ((StringValue) args.get(0)).getValue();
                ListValue maps = (ListValue) args.get(1);

                maps.sort((v1, v2) -> {
                    if (v1 instanceof MapOrListValueInterface && v2 instanceof MapOrListValueInterface) {
                        Value w1 = ((MapOrListValueInterface) v1).get(path);
                        Value w2 = ((MapOrListValueInterface) v2).get(path);

                        if (w1 instanceof NumberValue) return Double.compare(w1.toNumber(), w2.toNumber());
                        return w1.toString().compareTo(w2.toString());
                    } else {
                        throw new RuntimeException("Unexpected items type on map_or_list: " + v1.getClass().getSimpleName() + " and " + v2.getClass().getSimpleName());
                    }
                });

                return maps;

            case "regexp": {
                assert args.size() == 2;

                Pattern pattern = Pattern.compile(args.get(0).toString());
                Value arg1 = args.get(1);
                if (arg1 instanceof MapValue mv) {
                    return new ListValue(mv.getBody().entrySet().stream().filter(v -> pattern.matcher(v.getKey()).find()).map(Map.Entry::getValue).collect(Collectors.toList()));
                } else if (arg1 instanceof ListValue lv) {
                    return new ListValue(lv.stream().filter(v -> pattern.matcher(v.toString()).find()).collect(Collectors.toList()));
                } else {
                    throw new RuntimeException("NYR");
                }
            }

            default:
                //throw new Error("Unknown function: " + fname);
                return NotFoundValue.NOT_FOUND_VALUE;
        }
    }

}
