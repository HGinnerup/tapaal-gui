package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class NumberConstantExpression extends Expression {

    private Integer number;

    public NumberConstantExpression(Integer number) {
        this.number = number;
    }

    public Integer eval(ExpressionContext context) {
        return number;
    }

    @Override
    public Expression replace(Expression object1, Expression object2) {
        return null;
    }

    @Override
    public Expression copy() {
        return null;
    }

    @Override
    public boolean containsPlaceHolder() {
        return false;
    }

    @Override
    public Expression findFirstPlaceHolder() {
        return null;
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        return exprValues;
    }

    public void getVariables(Set<Variable> variables) {

    }

    @Override
    public String toString() {
        return number.toString();
    }
}
