package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public abstract class ColorExpression extends Expression {

    protected ColorExpression parent;

    public ColorExpression() {

    }

    public abstract ColorExpression findFirstPlaceHolder();

    public abstract boolean containsPlaceHolder();

    public abstract ColorExpression replace(Expression object1, Expression object2);

    public void setParent(ColorExpression parent) {this.parent = parent;}

    public void getVariables(Set<Variable> variables) {
    }
    // This function might only be needed in the derived classes
    public abstract Color eval(ExpressionContext context);
}
