package ru.maratislamov.script;

import ru.maratislamov.script.values.Value;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * фабрика библиотек функций
 */
public class ScriptFunctionsImplemntatorFactory {

    /**
     * все зарегестрированные библиотеки функций
     */
    private static Set<ScriptFunctionsImplemntator> implemntators = new LinkedHashSet<>();

    /**
     * регистрация библиотеки функций
     */
    public static void registrate(ScriptFunctionsImplemntator impl) {
        implemntators.add(impl);
    }

    /**
     * Найти библиотеку с функцией @call и выполнить её
     * @param call
     * @param args
     * @param session
     * @return
     * @throws Exception
     */
    public static Value onExec(String call, List<Value> args, ScriptSession session) throws Exception {
        for (ScriptFunctionsImplemntator implemntator : implemntators) {
            Value result = implemntator.onExec(call, args, session);
            if (result != Value.NotFound) return result;
        }

        throw new Error("Unknown function: " + call);
    }
}
