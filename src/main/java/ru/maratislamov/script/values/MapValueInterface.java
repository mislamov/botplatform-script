package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;

public interface MapValueInterface {

    boolean containsKey(String name);

    Value get(String name, ScriptSession session);

}
