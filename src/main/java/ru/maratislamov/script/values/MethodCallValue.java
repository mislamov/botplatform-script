package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptFunctionsService;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.statements.Statement;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A string value.
 */
public class MethodCallValue implements Statement, Value {

    private static final Logger logger = LoggerFactory.getLogger(MethodCallValue.class);

    //
    private String name;

    //
    private List<Expression> args;

    public MethodCallValue() {
    }

    public MethodCallValue(String call, List<Expression> args) {
        this.name = call;
        this.args = args;
    }

    @JsonIgnore(value = false)
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Expression> getArgs() {
        return args;
    }

    public void setArgs(List<Expression> args) {
        this.args = args;
    }


    public Double toNumber() {
        throw new ClassCastException("MethodCallValue can't be cast to Number");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value evaluate(ScriptSession session) {
        try {
            List<Value> args = (this.args == null) ? null : this.args.stream().map(e -> Expression.evaluate(e, session)).collect(Collectors.toList());
            return ScriptFunctionsService.execFunction(name, args, session);

        } catch (Exception | Error e) {
            logger.error("CallMethodError[" + name + "]");
            throw new Error("ERROR when call function '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public Value execute(ScriptSession session) {
        return evaluate(session);
    }

    @Override
    public String toString() {
        return "{CALL " + name + " " + args + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCallValue that = (MethodCallValue) o;
        return Objects.equals(name, that.name) && Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, args);
    }
}
