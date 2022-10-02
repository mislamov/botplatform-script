package ru.maratislamov.script.utils;

import ru.maratislamov.script.values.MapValue;

import java.util.stream.Collectors;

public class ValueUtils {

    /**
     * форматирование отображения мапы в текст. Например: {x:1, z.y.{a:2, b:3}}
     * @param mapValue
     * @return
     */
    public static String mapToString(MapValue mapValue) {
        if (mapValue.getBody().size() == 0) return "{}";

        String body = mapValue.getAll().stream().map(entry -> {
            if (entry.getValue() instanceof MapValue) {
                return entry.getKey() + "." + entry.getValue();
            }
            return entry.getKey() + ":" + entry.getValue().toString();
        }).collect(Collectors.joining(", "));

        if (mapValue.getBody().size() > 1)
            return "{" + body + "}";
        return body;
    }

}
