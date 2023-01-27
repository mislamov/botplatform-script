package ru.maratislamov.script.parser;

/**
 * This is a single meaningful chunk of code. It is created by the tokenizer
 * and consumed by the parser.
 */
public class Token {

    public final String text;
    public final TokenType type;
    public final boolean isSeparatedWord;

    public Token(String text, TokenType type) {
        this.text = text;
        this.type = type;
        this.isSeparatedWord = false;
    }

    public Token(String text, TokenType type, boolean isSeparatedWord) {
        assert type == TokenType.WORD;
        this.text = text;
        this.type = type;
        this.isSeparatedWord = isSeparatedWord;
    }

    public boolean isSeparatedWord() {
        return isSeparatedWord;
    }

    @Override
    public String toString() {
        return type.name() + "{" + text + "}";
    }
}
