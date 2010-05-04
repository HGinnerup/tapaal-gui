package pipe.dataLayer.colors;

import pipe.dataLayer.NormalArc;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.gui.undo.ColoredOutputArcOutputValueEdit;
import pipe.gui.undo.UndoableEdit;

public class ColoredOutputArc extends NormalArc {
	private IntOrConstant outputValue = new IntOrConstant();
	private boolean displayValues = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8410344461976132988L;


	public ColoredOutputArc(double startPositionXInput,
			double startPositionYInput, double endPositionXInput,
			double endPositionYInput, PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput,
			boolean taggedInput) {
		super(startPositionXInput, startPositionYInput, endPositionXInput,
				endPositionYInput, sourceInput, targetInput, weightInput, idInput,
				taggedInput);
		updateWeightLabel();
	}

	public ColoredOutputArc(PlaceTransitionObject newSource) {
		super(newSource);
		updateWeightLabel();
	}


	public ColoredOutputArc(NormalArc arc) {
		super(arc);
		updateWeightLabel();
	}

	public UndoableEdit setOutputValue(IntOrConstant newOutputValue) {
		IntOrConstant old = this.outputValue;
		this.outputValue = newOutputValue;
		
		updateWeightLabel();
		
		return new ColoredOutputArcOutputValueEdit(this, old, newOutputValue);
	}

	public IntOrConstant getOutputValue() {
		if(outputValue == null){
			outputValue = new IntOrConstant();
		}
		return outputValue;
	}
	
	public String getOutputString(){
		return "val := " + getOutputValue().toString(displayValues);
	}
	
	@Override
	public void updateWeightLabel(){ 		
		weightLabel.setText(getOutputString());
		
		this.setWeightLabelPosition();
	}

	public ColoredToken generateOutputToken() {
		return new ColoredToken(getOutputValue());
	}

	public void displayValues(boolean showValues) {
		this.displayValues  = showValues;		
		updateWeightLabel();
	}
}
