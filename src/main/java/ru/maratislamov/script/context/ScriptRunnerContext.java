package ru.maratislamov.script.context;

/**
 * Интерфейс передачи внешнего контекста выполнения скрипта, который передается через сессию во все функции,
 * включая кастомные.
 */
public interface ScriptRunnerContext {
    ScriptRunnerContext empty = new ScriptRunnerContext() {
    };
}
