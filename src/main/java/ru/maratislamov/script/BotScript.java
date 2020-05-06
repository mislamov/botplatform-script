package ru.maratislamov.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.parser.*;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.Value;

import java.io.InputStream;
import java.util.*;

import static ru.maratislamov.script.values.Value.SUSPEND;

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
 * @author Marat Islamov, Bob Nystrom
 */
public class BotScript {
    private static final Logger logger = LoggerFactory.getLogger(BotScript.class);

    // Loaded program as tokens
    List<Statement> statements;

    ScriptFunctionsImplemntator functionsImplemntator;

    public final Map<String, Integer> labels;

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
    public BotScript(ScriptFunctionsImplemntator context) {
        this.labels = new HashMap<>();
        this.functionsImplemntator = context;
    }

    /**
     * @param source A string containing the source code of a .jas script to interpret.
     */
    public void load(InputStream source) {

        // Tokenize.
        List<Token> tokens = Tokenizer.tokenize(source);

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
    public <TSession extends ScriptSession> void interpret(TSession session) {
        if (statements == null) throw new RuntimeException("script is not loaded");

        // Interpret until we're done.
        while (session.getCurrentStatement() < statements.size()) {
            int thisStatement = session.getCurrentStatement();
            Statement statementEntity = statements.get(thisStatement);
            session.incCurrentStatement();
            Value value = statementEntity.execute(session, functionsImplemntator);

            if (value == SUSPEND) {
                session.decCurrentStatement();
                return;
            }


        }

    }
}
