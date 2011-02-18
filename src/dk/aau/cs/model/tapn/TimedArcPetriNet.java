package dk.aau.cs.model.tapn;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.util.Require;

public class TimedArcPetriNet {
	private String name;

	private List<TimedPlace> places;
	private List<TimedTransition> transitions;
	private List<TimedInputArc> inputArcs;
	private List<TimedOutputArc> outputArcs;
	private List<TimedInhibitorArc> inhibitorArcs;
	private List<TransportArc> transportArcs;

	private TimedMarking currentMarking;

	public TimedArcPetriNet(String name) {
		Require.that(name != null && !name.isEmpty(), "Error: name cannot be empty or null");
		
		this.name = name;
		places = new ArrayList<TimedPlace>();
		transitions = new ArrayList<TimedTransition>();
		inputArcs = new ArrayList<TimedInputArc>();
		outputArcs = new ArrayList<TimedOutputArc>();
		inhibitorArcs = new ArrayList<TimedInhibitorArc>();
		transportArcs = new ArrayList<TransportArc>();

		setMarking(new TimedMarking());
	}

	public void add(TimedPlace place) {
		Require.that(place != null, "Argument must be a non-null place");
		Require.that(!isNameUsed(place.name()),	"A place or transition with the specified name already exists in the petri net.");

		place.setModel(this);
		places.add(place);
		place.setCurrentMarking(currentMarking);
	}

	public void add(TimedTransition transition) {
		Require.that(transition != null, "Argument must be a non-null transition");
		Require.that(!isNameUsed(transition.name()), "A place or transition with the specified name already exists in the petri net.");

		transition.setModel(this);
		transitions.add(transition);
	}

	public void add(TimedInputArc arc) {
		Require.that(arc != null, "Argument must be a non-null input arc.");
		Require.that(places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inputArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
		inputArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}

	public void add(TimedOutputArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(transitions.contains(arc.source()), "The source transition must be part of the petri net");
		Require.that(!outputArcs.contains(arc),	"The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromTransitionToPlace(arc.source(), arc.destination()),	"Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		outputArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.destination().addToPreset(arc);
	}

	public void add(TimedInhibitorArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()),	"The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.destination()), "The destination transition must be part of the petri net");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.destination()), "Cannot have two arcs between the same place and transition");

		arc.setModel(this);
		inhibitorArcs.add(arc);
		arc.destination().addInhibitorArc(arc);
	}

	public void add(TransportArc arc) {
		Require.that(arc != null, "Argument must be a non-null output arc.");
		Require.that(places.contains(arc.source()), "The source place must be part of the petri net.");
		Require.that(transitions.contains(arc.transition()), "The transition must be part of the petri net");
		Require.that(places.contains(arc.destination()), "The destination place must be part of the petri net.");
		Require.that(!inhibitorArcs.contains(arc), "The specified arc is already a part of the petri net.");
		Require.that(!hasArcFromPlaceToTransition(arc.source(), arc.transition()), "Cannot have two arcs between the same place and transition");
		Require.that(!hasArcFromTransitionToPlace(arc.transition(), arc.destination()),	"Cannot have two arcs between the same transition and place");

		arc.setModel(this);
		transportArcs.add(arc);
		arc.source().addToPostset(arc);
		arc.transition().addTransportArcGoingThrough(arc);
		arc.destination().addToPreset(arc);
	}

	public void addToken(TimedToken token) {
		currentMarking.add(token);
	}

	public void removeToken(TimedToken token) {
		currentMarking.remove(token);
	}

	public void remove(TimedPlace place) {
		boolean removed = places.remove(place);
		if (removed)
			place.setModel(null);
	}

	public void remove(TimedTransition transition) { // TODO: These methods must clean up arcs also
		boolean removed = transitions.remove(transition);
		if (removed)
			transition.setModel(null);
	}

	public void remove(TimedInputArc arc) {
		boolean removed = inputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TransportArc arc) {
		boolean removed = transportArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.transition().removeTransportArcGoingThrough(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TimedOutputArc arc) {
		boolean removed = outputArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.source().removeFromPostset(arc);
			arc.destination().removeFromPreset(arc);
		}
	}

	public void remove(TimedInhibitorArc arc) {
		boolean removed = inhibitorArcs.remove(arc);
		if (removed) {
			arc.setModel(null);
			arc.destination().removeInhibitorArc(arc);
		}
	}

	private boolean hasArcFromPlaceToTransition(TimedPlace source,
			TimedTransition destination) {
		for (TimedInputArc arc : inputArcs)
			if (arc.source().equals(source)
					&& arc.destination().equals(destination))
				return true;
		for (TimedInhibitorArc arc : inhibitorArcs)
			if (arc.source().equals(source)
					&& arc.destination().equals(destination))
				return true;
		for (TransportArc arc : transportArcs)
			if (arc.source().equals(source)
					&& arc.transition().equals(destination))
				return true;

		return false;
	}

	private boolean hasArcFromTransitionToPlace(TimedTransition source, TimedPlace destination) {
		for (TimedOutputArc arc : outputArcs){
			if (arc.source().equals(source) && arc.destination().equals(destination))
				return true;
		}
		for (TransportArc arc : transportArcs){
			if (arc.transition().equals(source) && arc.destination().equals(destination))
				return true;
		}
		return false;
	}

	public boolean isNameUsed(String name) {
		for (TimedPlace place : places){
			if (place.name().equalsIgnoreCase(name))
				return true;
		}
		for (TimedTransition transition : transitions){
			if (transition.name().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public TimedMarking marking() {
		return currentMarking;
	}

	public void setName(String newName) {
		if (name != null && name != "")
			name = newName;
	}

	public TimedPlace getPlaceByName(String placeName) {
		for (TimedPlace p : places) {
			if (p.name() == placeName) {
				return p;
			}
		}
		return null;
	}

	public TimedTransition getTransitionByName(String transitionName) {
		for (TimedTransition t : transitions) {
			if (t.name() == transitionName) {
				return t;
			}
		}
		return null;
	}

	public void setMarking(TimedMarking marking) {
		this.currentMarking = marking;

		for (TimedPlace p : places) {
			p.setCurrentMarking(marking);
		}
	}

	public Iterable<TimedPlace> places() {
		return places;
	}

	public Iterable<TimedTransition> transitions() {
		return transitions;
	}

	public Iterable<TimedInputArc> inputArcs() {
		return inputArcs;
	}

	public Iterable<TimedOutputArc> outputArcs() {
		return outputArcs;
	}

	public Iterable<TransportArc> transportArcs() {
		return transportArcs;
	}

	public Iterable<TimedInhibitorArc> inhibitorArcs() {
		return inhibitorArcs;
	}
	
	public TimedArcPetriNet copy() {
		TimedArcPetriNet tapn = new TimedArcPetriNet(this.name);
		
		for(TimedPlace p : this.places)
			tapn.add(p.copy());
		
		for(TimedTransition t : this.transitions)
			tapn.add(t.copy());
		
		for(TimedInputArc inputArc : this.inputArcs)
			tapn.add(inputArc.copy(tapn));
		
		for(TimedOutputArc outputArc : this.outputArcs)
			tapn.add(outputArc.copy(tapn));
		
		for(TransportArc transArc : this.transportArcs)
			tapn.add(transArc.copy(tapn));
		
		for(TimedInhibitorArc inhibArc : this.inhibitorArcs)
			tapn.add(inhibArc.copy(tapn));
		
		tapn.setMarking(this.currentMarking.clone());
		
		return tapn;
	}

	public TimedInputArc getInputArcFromPlaceToTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInputArc inputArc : inputArcs) {
			if(inputArc.source().equals(place) && inputArc.destination().equals(transition))
				return inputArc;
		}
		return null;
	}

	public TimedOutputArc getOutputArcFromTransitionAndPlace(TimedTransition transition, TimedPlace place) {
		for(TimedOutputArc outputArc : outputArcs) {
			if(outputArc.source().equals(transition) && outputArc.destination().equals(place))
				return outputArc;
		}
		return null;
	}

	public TransportArc getTransportArcFromPlaceTransitionAndPlace(TimedPlace sourcePlace, TimedTransition transition, TimedPlace destinationPlace) {
		for(TransportArc transArc : transportArcs) {
			if(transArc.source().equals(sourcePlace) && transArc.transition().equals(transition) && transArc.destination().equals(destinationPlace))
				return transArc;
		}
		return null;
	}

	public TimedInhibitorArc getInhibitorArcFromPlaceAndTransition(TimedPlace place, TimedTransition transition) {
		for(TimedInhibitorArc inhibArc : inhibitorArcs) {
			if(inhibArc.source().equals(place) && inhibArc.destination().equals(transition))
				return inhibArc;
		}
		
		return null;
	}
}
