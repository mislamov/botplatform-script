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
    public static String mapToString(MapValue mapValue) {
        if (mapValue.getBody().size() == 0) return "{}";

        Set<Object> markers = threadMarkers.get();

        String body = mapValue.getAll().stream().map(entry -> {
            final Value value = entry.getValue();
            final String key = entry.getKey();

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
        }).collect(Collectors.joining(", "));

        if (mapValue.getBody().size() > 1)
            return "{" + body + "}";
        return body;
    }


}
