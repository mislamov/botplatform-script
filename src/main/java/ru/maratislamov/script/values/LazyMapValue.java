package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import org.apache.commons.lang3.StringUtils;


import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LazyMapValue implements Value, MapValueInterface {

    final Map<String, Expression> body;

    public LazyMapValue(Map<String, Expression> body) {
        this.body = body;
    }

    @Override
    public MapValue evaluate(ScriptSession session) {
        MapValue result = new MapValue(new LinkedHashMap<>());
        body.forEach((k, v) -> result.put(k, v.evaluate(session)));
        return result;
    }

    @Override
    public Double toNumber() {
        throw new ClassCastException("LazyMapValue to Number");
    }

    @Override
    public Value copy() {
        HashMap<String, Expression> map = new HashMap<>();
        map.putAll(body);
        return new LazyMapValue(map);
    }

    @Override
    public String toString() {
        return "{" + StringUtils.join(body.keySet(), ",") + "}";
    }

    @Override
    public boolean containsKey(String name) {
        return body.containsKey(name);
    }

    @Override
    public Value get(String name, ScriptSession session/*, ScriptFunctionsImplemntator context*/) {
        Expression expression = body.get(name);
        return expression == null ? Value.NULL : expression.evaluate(session/*, context*/);
    }

}
