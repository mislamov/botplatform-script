package ru.maratislamov.script.statements;

import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.NumberValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * An "input" statement reads input from the user and stores it in a
 * variable.
 */
public class InputStatement implements Statement {

    private final BotScript botScript;
    private final String name;
    private final BufferedReader lineIn;
    private final int positionLine;

    public InputStatement(BotScript botScript, String name, int positionLine) {
        this.botScript = botScript;
        this.name = name;
        this.positionLine = positionLine;

        InputStreamReader converter = new InputStreamReader(System.in);
        lineIn = new BufferedReader(converter);
    }

    public void execute(ScriptSession session) {
        try {
            String input = lineIn.readLine();

            // Store it as a number if possible, otherwise use a string.
            try {
                double value = Double.parseDouble(input);
                session.getVariables().put(name, new NumberValue(value));

            } catch (NumberFormatException e) {
                session.getVariables().put(name, new StringValue(input));
            }
        } catch (IOException e1) {
            // HACK: Just ignore the problem.
        }
    }

}
