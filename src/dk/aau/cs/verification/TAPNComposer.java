package dk.aau.cs.verification;

import java.util.HashSet;

import dk.aau.cs.Messenger;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Tuple;

public class TAPNComposer {
	private static final String PLACE_FORMAT = "P%1$d";
	private static final String TRANSITION_FORMAT = "T%1$d";

	private Messenger messenger;
	private boolean hasShownMessage = false;
	
	private int nextPlaceIndex;
	private int nextTransitionIndex;

	private HashSet<String> processedSharedObjects;

	public TAPNComposer(Messenger messenger){
		this.messenger = messenger;
	}
	
	public Tuple<TimedArcPetriNet, NameMapping> transformModel(TimedArcPetriNetNetwork model) {
		nextPlaceIndex = -1;
		nextTransitionIndex = -1;
		processedSharedObjects = new HashSet<String>();
		TimedArcPetriNet tapn = new TimedArcPetriNet("ComposedModel");
		NameMapping mapping = new NameMapping();
		hasShownMessage = false;

		createSharedPlaces(model, tapn, mapping);
		createPlaces(model, tapn, mapping);
		createTransitions(model, tapn, mapping);
		createInputArcs(model, tapn, mapping);
		createOutputArcs(model, tapn, mapping);
		createTransportArcs(model, tapn, mapping);
		createInhibitorArcs(model, tapn, mapping);

		//dumpToConsole(tapn, mapping);

		return new Tuple<TimedArcPetriNet, NameMapping>(tapn, mapping);
	}
	
	private void createSharedPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for(SharedPlace place : model.sharedPlaces()){
			String uniquePlaceName = getUniquePlaceName();
			
			LocalTimedPlace constructedPlace = new LocalTimedPlace(uniquePlaceName, place.invariant());
			constructedModel.add(constructedPlace);
			mapping.addMappingForShared(place.name(), uniquePlaceName);

			if(model.isSharedPlaceUsedInTemplates(place)){
				for (TimedToken token : place.tokens()) {
					constructedPlace.addToken(new TimedToken(constructedPlace, token.age()));
				}
			}
		}
	}

	private void createPlaces(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedPlace timedPlace : tapn.places()) {
				if(!timedPlace.isShared()){
					String uniquePlaceName = getUniquePlaceName();

					LocalTimedPlace place = new LocalTimedPlace(uniquePlaceName, timedPlace.invariant());
					constructedModel.add(place);
					mapping.addMapping(tapn.name(), timedPlace.name(), uniquePlaceName);

					for (TimedToken token : timedPlace.tokens()) {
						place.addToken(new TimedToken(place, token.age()));
					}
				}
			}
		}
	}

	private String getUniquePlaceName() {
		nextPlaceIndex++;
		return String.format(PLACE_FORMAT, nextPlaceIndex);
	}

	private void createTransitions(TimedArcPetriNetNetwork model, TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedTransition timedTransition : tapn.transitions()) {
				if(!processedSharedObjects.contains(timedTransition.name())){
					
					// CAUTION: This if statement removes orphan transitions.
					//   This changes answers for e.g. DEADLOCK queries if
					//   support for such queries are added later.
					// ONLY THE IF SENTENCE SHOULD BE REMOVED. REST OF CODE SHOULD BE LEFT INTACT
					if(!timedTransition.isOrphan()){
						String uniqueTransitionName = getUniqueTransitionName();
	
						constructedModel.add(new TimedTransition(uniqueTransitionName, timedTransition.isUrgent()));
						if(timedTransition.isShared()){
							String name = timedTransition.sharedTransition().name();
							processedSharedObjects.add(name);
							mapping.addMappingForShared(name, uniqueTransitionName);
						}else{
							mapping.addMapping(tapn.name(), timedTransition.name(), uniqueTransitionName);
						}
					}else{
						if(!hasShownMessage){
							messenger.displayInfoMessage("There are orphan transitions (no incoming and no outgoing arcs) in the model."
									+ System.getProperty("line.separator") + "They will be removed (together with any connected inhibitor arcs) before the verification.");
							hasShownMessage = true;
						}
					}
				}
			}
		}
	}

	private String getUniqueTransitionName() {
		nextTransitionIndex++;
		return String.format(TRANSITION_FORMAT, nextTransitionIndex);
	}

	private void createInputArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedInputArc arc : tapn.inputArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));
				
				template = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedInputArc(source, target, arc.interval(), arc.getWeight()));
			}
		}
	}

	private void createOutputArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedOutputArc arc : tapn.outputArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedTransition source = constructedModel.getTransitionByName(mapping.map(template, arc.source().name()));

				template = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedOutputArc(source, target, arc.getWeight()));
			}
		}
	}

	private void createTransportArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TransportArc arc : tapn.transportArcs()) {
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));
				
				template = arc.transition().isShared() ? "" : tapn.name();
				TimedTransition transition = constructedModel.getTransitionByName(mapping.map(template, arc.transition().name()));
				
				template = arc.destination().isShared() ? "" : tapn.name();
				TimedPlace target = constructedModel.getPlaceByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TransportArc(source, transition,target, arc.interval(), arc.getWeight()));
			}
		}
	}

	private void createInhibitorArcs(TimedArcPetriNetNetwork model,
			TimedArcPetriNet constructedModel, NameMapping mapping) {
		for (TimedArcPetriNet tapn : model.activeTemplates()) {
			for (TimedInhibitorArc arc : tapn.inhibitorArcs()) {
				if(arc.destination().isOrphan())	continue;
				
				String template = arc.source().isShared() ? "" : tapn.name();
				TimedPlace source = constructedModel.getPlaceByName(mapping.map(template, arc.source().name()));

				template = arc.destination().isShared() ? "" : tapn.name();
				TimedTransition target = constructedModel.getTransitionByName(mapping.map(template, arc.destination().name()));

				constructedModel.add(new TimedInhibitorArc(source, target, arc.interval(), arc.getWeight()));
			}
		}
	}
}
