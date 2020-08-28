package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class AllExpression extends ColorExpression {

    private ColorType sort;

    public ColorType getColorType(){
        return this.sort;
    }

    public AllExpression(ColorType sort) {
        this.sort = sort;
    }

    //TODO unsure about this
    public Color eval(ExpressionContext context) {
        return null;
    }

    public Integer size() {
        return sort.size();
    }

    @Override
    public AllExpression replace(Expression object1, Expression object2) {
        if (this == object1 && object2 instanceof AllExpression) {
            AllExpression obj2 = (AllExpression) object2;
            obj2.setParent(parent);
            return obj2;
        } else {
            return this;
        }
    }

    public ColorType getSort() {return sort;}

    @Override
    public AllExpression copy() {
        return new AllExpression(sort);
    }

    @Override
    public boolean containsPlaceHolder() {
        return false;
    }

    @Override
    public AllExpression findFirstPlaceHolder() {
        return null;
    }

    @Override
    public ExprValues getValues(ExprValues exprValues) {
        exprValues.addColorType(sort);
        return exprValues;
    }

    @Override
    public void getVariables(Set<Variable> variables) {

    }

    public String toString() {
        return sort.getName() + ".all";
    }

    @Override
    public boolean isSimpleProperty() {return false; }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[0];
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AllExpression) {
            AllExpression expr = (AllExpression) o;
            return expr.sort.equals(this.sort);
        }
        return false;
    }
}
