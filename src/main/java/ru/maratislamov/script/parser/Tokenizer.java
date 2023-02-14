package ru.maratislamov.script.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

    // Tokenizing (lexing) -----------------------------------------------------

    // Many tokens are a single character, like operators and ().
    private static final String charTokens = "\n=!+-*/<>()[],.;{}:&|";
    private static final TokenType[] tokenTypes = {TokenType.LINE, TokenType.OPERATOR, TokenType.OPERATOR,
            TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
            TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
            TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN, TokenType.BEGIN_LIST, TokenType.END_LIST,
            TokenType.COMMA, TokenType.DOT, TokenType.COMMAND_SEP, TokenType.BEGIN_MAP, TokenType.END_MAP, TokenType.MAP_SEP,
            TokenType.OPERATOR, TokenType.OPERATOR
    };


    /**
     * This function takes a script as a string of characters and chunks it into
     * a sequence of tokens. Each token is a meaningful unit of program, like a
     * variable name, a number, a string, or an operator.
     */
    public static List<Token> tokenize(InputStream inputStream) {
        List<Token> tokens = new ArrayList<>();

        String token = "";
        TokenizeState state = TokenizeState.DEFAULT;

        // Scan through the code one character at a time, building up the list
        // of tokens.

        try (InputStreamReader source = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            int iC = source.read();
            char c = 0;

            while (true) {
                c = (char) iC;

                switch (state) {

                    case DEFAULT:
                        if (iC == -1)
                            return tokens;

  /*                      if (c == '=') {
                            tokens.add(new Token("=", TokenType.OPERATOR));
                            token = "";

                            iC = source.read();
                            c = (char) iC;
                            *//*if (c == '=') {
                                break;
                            }*//*
                            continue;
  */
/*                        } else if ((c == '+' || c == '-' || c == '*' || c == '\\')) {
                            token += c;
                            iC = source.read();
                            token = "";

                            if (iC == '=') {
                                tokens.add(new Token(c + "=", TokenType.OPERATOR));
                                break;
                            } else {
                                tokens.add(new Token("" + c, TokenType.OPERATOR));
                                c = (char) iC;
                                continue;
                            }

                        } else*/

                        if (c == '\n') {
                            tokens.add(new Token("\n", TokenType.LINE));
                            // схлопываем все смежные отступы и переносы в этот один токен
                            while (Character.isWhitespace(iC = source.read())) ;
                            continue;

                        } else if (charTokens.indexOf(c) != -1) {
                            tokens.add(new Token(Character.toString(c), tokenTypes[charTokens.indexOf(c)]));

                        } else if (Character.isLetter(c) || c == '_' || c == '$') {
                            token += c;
                            state = TokenizeState.WORD;

                        } else if (Character.isDigit(c)) {
                            token += c;
                            state = TokenizeState.NUMBER;

                        } else if (c == '"') {
                            // начало строки или фрейм-строки

                            iC = source.read();
                            c = (char) iC;
                            if (c != '"') {  //   одна двойная кавычка - это начало строки
                                state = TokenizeState.STRING;
                                continue;
                            }

                            // две двойных кавычки
                            iC = source.read();
                            c = (char) iC;
                            if (c != '"') {  //   всего две двойных кавычки - пустая строка
                                tokens.add(new Token("", TokenType.STRING));
                                token = "";
                                continue;

                            } else {
                                // три двойных кавычки
                                token = "";
                                state = TokenizeState.STRING_FRAME;
                            }
                            break;


                        } else if (c == '\'') {
                            state = TokenizeState.COMMENT;
                        }
                        break;

                    case WORD:
                        if (Character.isLetterOrDigit(c) || c == '_') {
                            token += c;
                        } else if (c == ':') {
                            tokens.add(new Token(token, TokenType.LABEL));
                            token = "";
                            state = TokenizeState.DEFAULT;
                        } else {
                            tokens.add(new Token(token, TokenType.WORD, Character.isWhitespace(c)));
                            token = "";
                            state = TokenizeState.DEFAULT;
                            ////i--; // Reprocess this character in the default state.
                            continue;
                        }
                        break;

                    case NUMBER:
                        // HACK: Negative numbers and floating points aren't supported.
                        // To get a negative number, just do 0 - <your number>.
                        // To get a floating point, divide. todo fix description
                        if (Character.isDigit(c) /*|| c == '.'*/) {
                            token += c;
                        } else {
                            tokens.add(new Token(token, TokenType.DIGITS));
                            token = "";
                            state = TokenizeState.DEFAULT;
                            ////i--; // Reprocess this character in the default state.
                            continue;
                        }
                        break;

                    case STRING:
                        if (c == '\\') { // экранирование
                            iC = source.read();
                            c = (char) iC;
                            //token += esc(c);  - в StringValue применяется StringEscapeUtils.unescapeJava для этого
                            token += "\\" + c;
                            break;

                        } else if (c == '"') {
                            iC = source.read();
                            c = (char) iC;
                            if (c == '"') {  //   двойные кавычки внутри текста = экранирование
                                token += '"';
                                break;
                            }

                            tokens.add(new Token(token, TokenType.STRING));
                            token = "";
                            state = TokenizeState.DEFAULT;
                            continue;
                        } else {
                            token += c;
                        }
                        break;

                    case STRING_FRAME:
                        if (c == '"') {
                            iC = source.read();
                            c = (char) iC;
                            if (c == '"') {
                                iC = source.read();
                                c = (char) iC;
                                if (c == '"') {
                                    tokens.add(new Token(token.trim(), TokenType.STRING_FRAME));
                                    token = "";
                                    state = TokenizeState.DEFAULT;
                                    break;
                                } else {
                                    token += "\"\"";
                                }
                            } else {
                                token += "\"";
                            }
                        } else {
                            token += c;
                        }
                        break;

                    case COMMENT:
                        if (c == '\n' || c == (char) -1) {
                            state = TokenizeState.DEFAULT;
                            ////i--;
                            continue;
                        }
                        break;

                    default:
                        throw new Error("Unexpected token here: " + debugString(iC, source));
                }

                if (iC == -1) break;
                iC = source.read();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (state != TokenizeState.DEFAULT) throw new Error("Unexpected end of script. Closing " + state + " expected");

        return tokens;
    }

    // escape codes
    private static String esc(char c) {
        switch (c) {
            case 'n':
                return "\n";
            case 't':
                return "\t";
            case '\\':
                return "\\";
            default:
                return String.valueOf(c);
        }
    }


    private static String debugString(int iC, InputStreamReader source) {
        StringBuilder substring = new StringBuilder();

        while (iC != -1) {
            if (iC == '\n') break;
            substring.append((char) iC);
            try {
                iC = source.read();

            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return substring.toString();
    }

}
