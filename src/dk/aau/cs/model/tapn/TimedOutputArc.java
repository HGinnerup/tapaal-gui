package dk.aau.cs.model.tapn;

import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import dk.aau.cs.util.Require;

public class TimedOutputArc extends TAPNElement {
	private Weight weight;
	private final TimedTransition source;
	private TimedPlace destination;
    private ArcExpression expression;
	
	public TimedOutputArc(TimedTransition source, TimedPlace destination){
		this(source, destination, new IntWeight(1), null);
	}

    public TimedOutputArc(TimedTransition source, TimedPlace destination, Weight weight){
        this(source, destination, weight, null);
    }

	public TimedOutputArc(TimedTransition source, TimedPlace destination, Weight weight, ArcExpression expression) {
		Require.that(source != null, "An arc must have a non-null source transition");
		Require.that(destination != null, "An arc must have a non-null destination place");
		Require.that(!source.isShared() || !destination.isShared(), "You cannot draw an arc between a shared transition and shared place.");
		this.source = source;
		this.destination = destination;
		this.weight = weight;
		this.expression = expression;
	}
	
	public Weight getWeight(){
		return weight;
	}
        
        public Weight getWeightValue(){
                return new IntWeight(weight.value());
	}
	
	public void setWeight(Weight weight){
		this.weight = weight;
	}

	public TimedTransition source() {
		return source;
	}

	public TimedPlace destination() {
		return destination;
	}

	@Override
	public void delete() {
		model().remove(this);
	}

	public TimedOutputArc copy(TimedArcPetriNet tapn) {
		return new TimedOutputArc(tapn.getTransitionByName(source.name()), tapn.getPlaceByName(destination.name()), weight, expression.copy());
	}

	public void setDestination(TimedPlace place) {
		Require.that(place != null, "place cannot be null");
		destination = place;		
	}
	
	@Override
	public String toString() {
		return "From " + source.name() + " to " + destination.name();
	}

    public void setExpression(ArcExpression expression) {this.expression = expression;}

    public ArcExpression getExpression(){return this.expression;}
}
