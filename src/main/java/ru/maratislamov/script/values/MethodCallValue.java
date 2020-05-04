package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A string value.
 */
public class MethodCallValue implements Value {

    private final String call;

    private final List<Expression> args;

    public MethodCallValue(String call, List<Expression> args) {
        this.call = call;
        this.args = args;
    }

    @Override
    public String toString() {
        return "$" + call;
    }

    public double toNumber() {
        throw new ClassCastException("MethodCallValue can't be cast to Number");
    }

    public Value evaluate(ScriptSession session) {
        //TODO: асинхронный вывод по аналогии с input
        List<Value> argValues = args.stream().map(e -> e.evaluate(session)).collect(Collectors.toList());

        //return new NULLValue();
        MapValue result = new MapValue(new LinkedHashMap<>());
        //result.put("error", new StringValue("Сообщение от @BotFather не содержит имени созданного бота и токена"));
        result.put("domain", new StringValue("https://domain.bitrix.ru"));
        return result;
    }
}
