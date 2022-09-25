package ru.maratislamov.script;

import ru.maratislamov.script.values.Value;

import java.util.List;

/**
 * Сервис исполнения встроенных функций для скриптов
 */
public class ScriptFunctionsService {

    /**
     * вычислитель встроенных функций
     */
    private static ScriptFunctionsExecutor scriptFunctionsExecutor = null;

    /**
     * регистрация вычислителя функций
     */
    public static void register(ScriptFunctionsExecutor impl) {
        scriptFunctionsExecutor = impl;
    }

    /**
     * Найти библиотеку с функцией @call и выполнить её
     *
     * @param call
     * @param args
     * @param session
     * @return
     * @throws Exception
     */
    public static Value execFunction(String call, List<Value> args, ScriptSession session) throws Exception {
        Value result = scriptFunctionsExecutor.onExec(call, args, session);
        if (result != Value.NotFound) return result;
        throw new Error("Unknown function: " + call);
    }
}
