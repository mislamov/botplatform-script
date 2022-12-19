package ru.maratislamov.script.expressions;

// переменная - элемент списка-массива
public class ArrayVariableExpression extends VariableExpression {

    Expression itemIdxExpr;

    public ArrayVariableExpression(String name, Expression itemIdxExpr) {
        super(name);
        this.itemIdxExpr = itemIdxExpr;
    }

}
