package ru.maratislamov.script.utils;

import ru.maratislamov.script.values.MapOrListValueInterface;
import ru.maratislamov.script.values.MapValue;
import ru.maratislamov.script.values.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ValueUtils {

    private static ThreadLocal<Set<Object>> threadMarkers = ThreadLocal.withInitial(HashSet::new);

    /**
     * форматирование отображения мапы в текст. Например: {x:1, z.y.{a:2, b:3}}
     *
     * @param mapValue
     * @return
     */
    public static String mapToDebugString(MapValue mapValue) {
        if (mapValue.getBody().size() == 0) return "{}";

        Set<Object> markers = threadMarkers.get();

        final String[] body = {""};

        mapValue.getAll().forEach(entry -> {
            final Value value = entry.getValue();
            final String key = entry.getKey();
            body[0] += ("".equals(body[0])?"":",") + mapKeyValueToText(key, value, markers);
        });

        if (mapValue.getBody().size() > 1)
            return "{" + body[0] + "}";
        return body[0];
    }

    private static String mapKeyValueToText(String key, Value value, Set<Object> markers) {
        if (value != null && value != Value.NULL && markers.contains(value)) {
            // loop
            return key + ".$$loop$$";
        }

        if (value instanceof MapOrListValueInterface) {
            markers.add(value);
            threadMarkers.set(markers);

            final String text = key + "." + value;

            markers.remove(value);
            threadMarkers.set(markers);
            return text;
        }


        return key + ":" + value;
    }


}
