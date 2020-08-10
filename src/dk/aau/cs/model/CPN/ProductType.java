package dk.aau.cs.model.CPN;

import java.util.HashMap;
import java.util.Vector;

public class ProductType extends ColorType {

    private Vector<ColorType> constituents = new Vector<ColorType>();
    private String name;
    private String id;
    private HashMap<Vector<Color>, Color> colorCache = new HashMap<Vector<Color>, Color>();


    public ProductType(String name) {
        super(name);
        this.name = name;
    }
    public ProductType(String name, String id) {
        super(name, id);
        this.name = name;
        this.id = id;
    }

    public Vector<ColorType> getColorTypes() {return constituents; }

    public void addType(ColorType colortype) {
        constituents.add(colortype);
    }

    //Adding colors to product-types no longer makes sense.
    public void addColor(String colorName) {
        assert(false);
    }

    public String toString() {
        String out = "Domain: " + name + " is <";
        for (ColorType element : constituents) {
            out += element.getName() + ", ";
        }
        out = out.substring(0, out.length() -2);
        out += ">";
        return out;
    }


    public boolean containsTypes(Vector<ColorType> colorTypes) {
        return constituents.equals(colorTypes);
    }

    public Color getColor(Vector<Color> colors) {
        Color result = colorCache.get(colors);
        if (result == null) {
            //TODO: Figure out id
            result = new Color(this, 0, colors);
            colorCache.put(colors, result);
        }
        return result;
    }

    @Override
    public Color getFirstColor() {
        Vector<Color> colors = new Vector<Color>();
        for (ColorType ct : constituents) {
            colors.add(ct.getFirstColor());
        }
        return getColor(colors);
    }
}
