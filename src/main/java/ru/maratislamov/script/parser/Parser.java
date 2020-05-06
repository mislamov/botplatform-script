package ru.maratislamov.script.parser;

import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.OperatorExpression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.statements.*;
import ru.maratislamov.script.values.*;

import java.util.*;

/**
 * This defines the Jasic parser. The parser takes in a sequence of tokens
 * and generates an abstract syntax tree. This is the nested data structure
 * that represents the series of statements, and the expressions (which can
 * nest arbitrarily deeply) that they evaluate. In technical terms, what we
 * have is a recursive descent parser, the simplest kind to hand-write.
 * <p>
 * As a side-effect, this phase also stores off the line numbers for each
 * label in the program. It's a bit gross, but it works.
 */
public class Parser {
    private final BotScript botScript;

    private final List<Token> tokens;
    private int position;
    private int positionLine;

    public Parser(BotScript botScript, List<Token> tokens) {
        this.botScript = botScript;
        this.tokens = tokens;
        position = 0;
        positionLine = 0;
    }

    public String debugCurrentPosition() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; ++i) {
            result.append(get(i).text);
            result.append(" ");
        }
        return result.toString();
    }


    /**
     * The top-level function to start parsing. This will keep consuming
     * tokens and routing to the other parse functions for the different
     * grammar syntax until we run out of code to parse.
     *
     * @param labels A map of label names to statement indexes. The
     *               parser will fill this in as it scans the code.
     * @return The list of parsed statements.
     */
    public List<Statement> parse(Map<String, Integer> labels) {
        List<Statement> statements = new ArrayList<Statement>();

        while (true) {
            // Counting empty lines.
            while (match(TokenType.LINE)) {
                positionLine++;
            }

            if (match(TokenType.LABEL)) {
                // Mark the index of the statement after the label.
                labels.put(last(1).text, statements.size());

            } else if (match(TokenType.WORD, TokenType.EQUALS)) {
                // присваивание
                String name = last(2).text;
                Expression value = expression();
                statements.add(new AssignStatement(name, value));

            } else if (match("print")) {
                statements.add(new MethodCallValue("print", Collections.singletonList(expression())));

            } else if (match("inline")) {
                statements.add(new MethodCallValue("inline", Collections.singletonList(expression())));

            } else if (match("button")) {
                statements.add(new MethodCallValue("button", Collections.singletonList(expression())));

            } else if (match("input")) {
                statements.add(new MethodCallValue("input", Collections.singletonList(new TermValue(consume(TokenType.WORD).text))));

            } else if (match("goto")) {
                statements.add(new GotoStatement(botScript, consume(TokenType.WORD).text));

            } else if (match("push")) {
                String varMap = consume(TokenType.WORD).text;
                Expression value = expression();
                statements.add(new PushStatement(varMap, value));

                //} else if (match("EXEC")) {

            } else if (match("if")) {
                Expression condition = expression();
                consume("then");
                String label = consume(TokenType.WORD).text;
                statements.add(new IfThenStatement(botScript, condition, label));

            } else {
                if (match(TokenType.EOF))
                    break;

                throw new RuntimeException("ERROR [line " + (positionLine + 1) + "]: near '" + get(0).text + "'");
                //break; // Unexpected token (likely EOF), so end.
            }
        }

        return statements;
    }

    // The following functions each represent one grammatical part of the
    // language. If this parsed English, these functions would be named like
    // noun() and verb().

    /**
     * Parses a single expression. Recursive descent parsers start with the
     * lowest-precedent term and moves towards higher precedence. For Jasic,
     * binary operators (+, -, etc.) are the lowest.
     *
     * @return The parsed expression.
     */
    private Expression expression() {
        // список
        if (match(TokenType.BEGIN_LIST)){
            List<Expression> expressionList = new ArrayList<>();

            while (!match(TokenType.END_LIST)){
                expressionList.add(expression());
                if (!match(TokenType.SEP_LIST) && !peek(TokenType.END_LIST)){
                    throw new Error("List parse error here: " + debugCurrentPosition());
                }
            }
            return new ListValue(expressionList);
        }

        // оператор
        return operator();
    }

    /**
     * Parses a series of binary operator expressions into a single
     * expression. In Jasic, all operators have the same predecence and
     * associate left-to-right. That means it will interpret:
     * 1 + 2 * 3 - 4 / 5
     * like:
     * ((((1 + 2) * 3) - 4) / 5)
     * <p>
     * It works by building the expression tree one at a time. So, given
     * this code: 1 + 2 * 3, this will:
     * <p>
     * 1. Parse (1) as an atomic expression.
     * 2. See the (+) and start a new operator expression.
     * 3. Parse (2) as an atomic expression.
     * 4. Build a (1 + 2) expression and replace (1) with it.
     * 5. See the (*) and start a new operator expression.
     * 6. Parse (3) as an atomic expression.
     * 7. Build a ((1 + 2) * 3) expression and replace (1 + 2) with it.
     * 8. Return the last expression built.
     *
     * @return The parsed expression.
     */
    private Expression operator() {
        Expression expression = atomic();

        // Keep building operator expressions as long as we have operators.
        while (match(TokenType.OPERATOR) || match(TokenType.EQUALS) || (match(TokenType.NOEQUALS))) {
            String operator = last(1).text;
//            if (operator == '=' && last(2).text.charAt(0) == '!')
//                operator = '!';
            Expression right = atomic();
            expression = new OperatorExpression(expression, operator, right);
        }

        return expression;
    }

    /**
     * Parses an "atomic" expression. This is the highest level of
     * precedence and contains single literal tokens like 123 and "foo", as
     * well as parenthesized expressions.
     *
     * @return The parsed expression.
     */
    private Expression atomic() {

        if (match(TokenType.WORD)) {
            // A word is a reference to a variable.
            String prev = last(1).text;

            if ("NULL".equals(prev)) {
                return new NULLValue();
            }

            if ("EXEC".equals(prev)) {
                // вызов внешней функции
                String call = get(0).text;
                assert get(0).type == TokenType.STRING;
                ++position;

                List<Expression> argList = new ArrayList<>();

                while (get(0).type != TokenType.LINE) {
                    Expression expression = expression();
                    argList.add(expression);
                }
                return new MethodCallValue(call, argList);
            }
            return new VariableExpression(prev);

        } else if (match(TokenType.NUMBER)) {
            return new NumberValue(last(1).text);

        } else if (match(TokenType.STRING)) {
            return new StringValue(last(1).text);

        } else if (match(TokenType.LEFT_PAREN)) {
            // The contents of a parenthesized expression can be any
            // expression. This lets us "restart" the precedence cascade
            // so that you can have a lower precedence expression inside
            // the parentheses.
            Expression expression = expression();
            consume(TokenType.RIGHT_PAREN);
            return expression;
        }

        throw new Error("Can't parse line " + (positionLine + 1) + ": " + debugCurrentPosition());
    }

    // The following functions are the core low-level operations that the
    // grammar parser is built in terms of. They match and consume tokens in
    // the token stream.

    /**
     * Consumes the next two tokens if they are the given type (in order).
     * Consumes no tokens if either check fais.
     *
     * @param type1 Expected type of the next token.
     * @param type2 Expected type of the subsequent token.
     * @return True if tokens were consumed.
     */
    private boolean match(TokenType type1, TokenType type2) {
        if (get(0).type != type1) return false;
        if (get(1).type != type2) return false;
        position += 2;
        return true;
    }

    private boolean match(TokenType type1, TokenType type2, TokenType type3) {
        if (get(0).type != type1) return false;
        if (get(1).type != type2) return false;
        if (get(2).type != type3) return false;
        position += 3;
        return true;
    }

    /**
     * Consumes the next token if it's the given type.
     *
     * @param type Expected type of the next token.
     * @return True if the token was consumed.
     */
    private boolean match(TokenType type) {
        if (get(0).type != type) return false;
        position++;
        return true;
    }

    private boolean peek(TokenType type) {
        if (get(0).type != type) return false;
        return true;
    }

    /**
     * Consumes the next token if it's a word token with the given name.
     *
     * @param name Expected name of the next word token.
     * @return True if the token was consumed.
     */
    private boolean match(String name) {
        if (get(0).type != TokenType.WORD) return false;
        if (!get(0).text.equals(name)) return false;
        position++;
        return true;
    }

    /**
     * Consumes the next token if it's the given type. If not, throws an
     * exception. This is for cases where the parser demands a token of a
     * certain type in a certain position, for example a matching ) after
     * an opening (.
     *
     * @param type Expected type of the next token.
     * @return The consumed token.
     */
    private Token consume(TokenType type) {
        if (get(0).type != type) throw new Error("Expected " + type + ".");
        return tokens.get(position++);
    }

    /**
     * Consumes the next token if it's a word with the given name. If not,
     * throws an exception.
     *
     * @param name Expected name of the next word token.
     * @return The consumed token.
     */
    private Token consume(String name) {
        if (!match(name))
            throw new Error("Expected '" + name + "' in line " + (positionLine + 1) + " but: " + debugCurrentPosition());
        return last(1);
    }

    /**
     * Gets a previously consumed token, indexing backwards. last(1) will
     * be the token just consumed, last(2) the one before that, etc.
     *
     * @param offset How far back in the token stream to look.
     * @return The consumed token.
     */
    private Token last(int offset) {
        return tokens.get(position - offset);
    }

    /**
     * Gets an unconsumed token, indexing forward. get(0) will be the next
     * token to be consumed, get(1) the one after that, etc.
     *
     * @param offset How far forward in the token stream to look.
     * @return The yet-to-be-consumed token.
     */
    private Token get(int offset) {
        if (position + offset >= tokens.size()) {
            return new Token("", TokenType.EOF);
        }
        return tokens.get(position + offset);
    }

}
