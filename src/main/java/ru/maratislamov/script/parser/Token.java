package ru.maratislamov.script.parser;

/**
 * This is a single meaningful chunk of code. It is created by the tokenizer
 * and consumed by the parser.
 */
public class Token {
    public Token(String text, TokenType type) {
        this.text = text;
        this.type = type;
    }

    public final String text;
    public final TokenType type;
}
