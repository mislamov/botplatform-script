package ru.maratislamov.script.parser;

/**
 * This defines the different states the tokenizer can be in while it's
 * scanning through the source code. Tokenizers are state machines, which
 * means the only data they need to store is where they are in the source
 * code and this one "state" or mode value.
 * <p>
 * One of the main differences between tokenizing and parsing is this
 * regularity. Because the tokenizer stores only this one state value, it
 * can't handle nesting (which would require also storing a number to
 * identify how deeply nested you are). The parser is able to handle that.
 */
public enum TokenizeState {
    DEFAULT, WORD, NUMBER, STRING, COMMENT
}
