package dk.aau.cs.model;

import dk.aau.cs.model.tapn.NetworkMarking;

public interface TapaalTraceStep {
	NetworkMarking performStepFrom(NetworkMarking marking); // TODO: We should introduce an interface for NetworkMarking, that way this trace stuff is independent of a specific model
}
