package ru.maratislamov.script;

import ru.maratislamov.script.parser.Parser;
import ru.maratislamov.script.parser.Token;
import ru.maratislamov.script.parser.TokenType;
import ru.maratislamov.script.parser.TokenizeState;
import ru.maratislamov.script.statements.Statement;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This defines a single class that contains an entire interpreter for a
 * language very similar to the original BASIC. Everything is here (albeit in
 * very simplified form): tokenizing, parsing, and interpretation. The file is
 * organized in phases, with each appearing roughly in the order that they
 * occur when a program is run. You should be able to read this top-down to walk
 * through the entire process of loading and running a program.
 * <p>
 * Jasic language syntax
 * ---------------------
 * <p>
 * Comments start with ' and proceed to the end of the line:
 * <p>
 * print "hi there" ' this is a comment
 * <p>
 * Numbers and strings are supported. Strings should be in "double quotes", and
 * only positive integers can be parsed (though numbers are double internally).
 * <p>
 * Variables are identified by name which must start with a letter and can
 * contain letters or numbers. Case is significant for names and keywords.
 * <p>
 * Each statement is on its own line. Optionally, a line may have a label before
 * the statement. A label is a name that ends with a colon:
 * <p>
 * foo:
 * <p>
 * <p>
 * The following statements are supported:
 *
 * <name> = <expression>
 * Evaluates the expression and assigns the result to the given named
 * variable. All variables are globally scoped.
 * <p>
 * pi = (314159 / 10000)
 * <p>
 * print <expression>
 * Evaluates the expression and prints the result.
 * <p>
 * print "hello, " + "world"
 * <p>
 * input <name>
 * Reads in a line of input from the user and stores it in the variable with
 * the given name.
 * <p>
 * input guess
 * <p>
 * goto <label>
 * Jumps to the statement after the label with the given name.
 * <p>
 * goto loop
 * <p>
 * if <expression> then <label>
 * Evaluates the expression. If it evaluates to a non-zero number, then
 * jumps to the statement after the given label.
 * <p>
 * if a < b then dosomething
 * <p>
 * <p>
 * The following expressions are supported:
 *
 * <expression> = <expression>
 * Evaluates to 1 if the two expressions are equal, 0 otherwise.
 *
 * <expression> + <expression>
 * If the left-hand expression is a number, then adds the two expressions,
 * otherwise concatenates the two strings.
 *
 * <expression> - <expression>
 * <expression> * <expression>
 * <expression> / <expression>
 * <expression> < <expression>
 * <expression> > <expression>
 * You can figure it out.
 *
 * <name>
 * A name in an expression simply returns the value of the variable with
 * that name. If the variable was never set, it defaults to 0.
 * <p>
 * All binary operators have the same precedence. Sorry, I had to cut corners
 * somewhere.
 * <p>
 * To keep things simple, I've omitted some stuff or hacked things a bit. When
 * possible, I'll leave a "HACK" note there explaining what and why. If you
 * make your own interpreter, you'll want to address those.
 *
 * @author Bob Nystrom
 */
public class BotScript {

    // Loaded program as tokens
    List<Statement> statements;

    public final Map<String, Integer> labels;

    // Tokenizing (lexing) -----------------------------------------------------

    /**
     * This function takes a script as a string of characters and chunks it into
     * a sequence of tokens. Each token is a meaningful unit of program, like a
     * variable name, a number, a string, or an operator.
     */
    private List<Token> tokenize(InputStream inputStream) {
        List<Token> tokens = new ArrayList<Token>();

        String token = "";
        TokenizeState state = TokenizeState.DEFAULT;

        // Many tokens are a single character, like operators and ().
        String charTokens = "\n=!+-*/<>()";
        TokenType[] tokenTypes = {TokenType.LINE, TokenType.EQUALS, TokenType.NOT,
                TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
                TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN
        };

        // Scan through the code one character at a time, building up the list
        // of tokens.



        try (InputStreamReader source = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            
            int iC = source.read();

            while (iC != -1) {
                char c = (char) iC;

                switch (state) {
                    case DEFAULT:
                        if (charTokens.indexOf(c) != -1) {
                            tokens.add(new Token(Character.toString(c),
                                    tokenTypes[charTokens.indexOf(c)]));
                        } else if (Character.isLetter(c)) {
                            token += c;
                            state = TokenizeState.WORD;
                        } else if (Character.isDigit(c)) {
                            token += c;
                            state = TokenizeState.NUMBER;
                        } else if (c == '"') {
                            state = TokenizeState.STRING;
                        } else if (c == '\'') {
                            state = TokenizeState.COMMENT;
                        }
                        break;

                    case WORD:
                        if (Character.isLetterOrDigit(c) || c == '.') {
                            token += c;
                        } else if (c == ':') {
                            tokens.add(new Token(token, TokenType.LABEL));
                            token = "";
                            state = TokenizeState.DEFAULT;
                        } else {
                            tokens.add(new Token(token, TokenType.WORD));
                            token = "";
                            state = TokenizeState.DEFAULT;
                            ////i--; // Reprocess this character in the default state.
                            continue;
                        }
                        break;

                    case NUMBER:
                        // HACK: Negative numbers and floating points aren't supported.
                        // To get a negative number, just do 0 - <your number>.
                        // To get a floating point, divide.
                        if (Character.isDigit(c)) {
                            token += c;
                        } else {
                            tokens.add(new Token(token, TokenType.NUMBER));
                            token = "";
                            state = TokenizeState.DEFAULT;
                            ////i--; // Reprocess this character in the default state.
                            continue;
                        }
                        break;

                    case STRING:
                        if (c == '"') {
                            tokens.add(new Token(token, TokenType.STRING));
                            token = "";
                            state = TokenizeState.DEFAULT;
                        } else {
                            token += c;
                        }
                        break;

                    case COMMENT:
                        if (c == '\n') {
                            state = TokenizeState.DEFAULT;
                            ////i--;
                            continue;
                        }
                        break;

                    default:
                        String substring = "" + c;
                        while (c != -1) {
                            if (c == '\n') break;
                            substring += c;
                            c = (char) source.read();
                        }
                        throw new Error("Unexpected token here: " + substring);
                }

                iC = source.read();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // HACK: Silently ignore any in-progress token when we run out of
        // characters. This means that, for example, if a script has a string
        // that's missing the closing ", it will just ditch it.
        return tokens;
    }

    // Token data --------------------------------------------------------------

    // Parsing -----------------------------------------------------------------

    // Abstract syntax tree (AST) ----------------------------------------------

    // These classes define the syntax tree data structures. This is how code is
    // represented internally in a way that's easy for the interpreter to
    // understand.
    //
    // HACK: Unlike most real compilers or interpreters, the logic to execute
    // the code is baked directly into these classes. Typically, it would be
    // separated out so that the AST us just a static data structure.

    // Value types -------------------------------------------------------------

    // Interpreter -------------------------------------------------------------

    /**
     * Constructs a new Jasic instance. The instance stores the global state of
     * the interpreter such as the values of all of the variables and the
     * current statement.
     */
    public BotScript() {
        labels = new HashMap<String, Integer>();
    }

    /**
     * @param source A string containing the source code of a .jas script to interpret.
     */
    public void load(InputStream source) {

        // Tokenize.
        List<Token> tokens = tokenize(source);

        // Parse.
        Parser parser = new Parser(this, tokens);
        statements = parser.parse(labels);

    }


    /**
     * This is where the magic happens. This runs the code through the parsing
     * pipeline to generate the AST. Then it executes each statement. It keeps
     * track of the current line in a member variable that the statement objects
     * have access to. This lets "goto" and "if then" do flow control by simply
     * setting the index of the current statement.
     * <p>
     * In an interpreter that didn't mix the interpretation logic in with the
     * AST node classes, this would be doing a lot more work.
     */
    public void interpret(ScriptSession session) {
        if (statements == null) throw new RuntimeException("script is not loaded");

        // Interpret until we're done.
        while (session.getCurrentStatement() < statements.size()) {
            int thisStatement = session.getCurrentStatement();
            Statement statementEntity = statements.get(thisStatement);
            session.incCurrentStatement();
            statementEntity.execute(session);
        }

    }
}
