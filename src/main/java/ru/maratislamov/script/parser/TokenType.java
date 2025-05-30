package ru.maratislamov.script.parser;

/**
 * This defines the different kinds of tokens or meaningful chunks of code
 * that the parser knows how to consume. These let us distinguish, for
 * example, between a string "foo" and a variable named "foo".
 * <p>
 * HACK: A typical tokenizer would actually have unique token types for
 * each keyword (print, goto, etc.) so that the parser doesn't have to look
 * at the names, but Jasic is a little more crude.
 */
public enum TokenType {
    WORD, DIGITS, STRING, STRING_FRAME, LABEL, LINE, COMMAND_SEP, /* SEP - разделитель команд в одной строке */
    OPERATOR, LEFT_PAREN, RIGHT_PAREN, BEGIN_LIST, END_LIST, BEGIN_MAP, END_MAP, EOF, COMMA, DOT,
    MAP_SEP;
}
