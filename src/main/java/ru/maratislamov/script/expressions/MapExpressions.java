package ru.maratislamov.script.expressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.MapValue;
import ru.maratislamov.script.values.Value;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapExpressions implements Expression {

    public static final Logger logger = LoggerFactory.getLogger(MapExpressions.class);

    private Map<String, Expression> map;

    public MapExpressions(Map<String, Expression> expressionMap) {
        this.map = expressionMap;
    }

    @Override
    public Value evaluate(ScriptSession session) {
        final LinkedHashMap<String, Value> collect = map.entrySet().stream().map(e -> {

            logger.debug("evalute: {}", e.getValue());

            return new AbstractMap.SimpleEntry<>(e.getKey(), Expression.evaluate(e.getValue(), session));

        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value, value2) -> value2, LinkedHashMap::new));
        return new MapValue(collect);
    }

    public int size() {
        return map == null ? 0 : map.size();
    }

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    @Override
    public String getName() {
        return toString();
    }
}
