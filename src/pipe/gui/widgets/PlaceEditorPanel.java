package pipe.gui.widgets;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.CustomJSpinner;
import net.tapaal.swinghelpers.GridBagHelper;
import net.tapaal.swinghelpers.WidthAdjustingComboBox;
import pipe.dataLayer.Template;
import pipe.gui.ColoredComponents.ColorComboboxPanel;
import pipe.gui.ColoredComponents.ColoredTimeInvariantDialogPanel;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.ChangedInvariantCommand;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.gui.undo.MakePlaceSharedCommand;
import dk.aau.cs.gui.undo.MakePlaceNewSharedCommand;
import dk.aau.cs.gui.undo.MakePlaceNewSharedMultiCommand;
import dk.aau.cs.gui.undo.RenameTimedPlaceCommand;
import dk.aau.cs.gui.undo.TimedPlaceMarkingEdit;
import dk.aau.cs.gui.undo.UnsharePlaceCommand;
import dk.aau.cs.model.tapn.Bound.InfBound;
import dk.aau.cs.util.RequireException;

import static net.tapaal.swinghelpers.GridBagHelper.Anchor.*;
import static net.tapaal.swinghelpers.GridBagHelper.Fill.HORIZONTAL;

public class PlaceEditorPanel extends javax.swing.JPanel {

	private final JRootPane rootPane;
	
	private JCheckBox sharedCheckBox;
	private JCheckBox makeNewSharedCheckBox;
	private WidthAdjustingComboBox<TimedPlace> sharedPlacesComboBox;

	private final TimedPlaceComponent place;
	private final Context context;
	private boolean makeNewShared = false;
	private boolean doNewEdit = true;
	private final TabContent currentTab;
	private final EscapableDialog parent;
	
	private Vector<TimedPlace> sharedPlaces;
	private final int maxNumberOfPlacesToShowAtOnce = 20;

	public PlaceEditorPanel(EscapableDialog parent,JRootPane rootPane, TimedPlaceComponent placeComponent, Context context) {
		this.rootPane = rootPane;
		currentTab = context.tabContent();
		place = placeComponent;
		this.context = context;
        this.parent = parent;
		this.colorType = place.underlyingPlace().getColorType();
		initComponents();
		hideIrrelevantInformation();
	}

	private void hideIrrelevantInformation(){
        if(!place.isTimed()) {
            timeInvariantPanel.setVisible(false);
            timeInvariantColorPanel.setVisible(false);
        }
        if(!place.isColored()){
            timeInvariantColorPanel.setVisible(false);
            tokenPanel.setVisible(false);
            colorTypePanel.setVisible(false);
        }
        if(place.isColored()){
            markingLabel.setVisible(false);
            markingSpinner.setVisible(false);
            if(place.isTimed()){
                timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Default Age Invariant"));
            }
        }
    }

	private void initComponents() {
		setLayout(new java.awt.GridBagLayout());

		initBasicPropertiesPanel();
		GridBagConstraints gridBagConstraints = GridBagHelper.as(0,0, WEST, HORIZONTAL, new Insets(5, 8, 0, 8));
		add(basicPropertiesPanel, gridBagConstraints);

		initTimeInvariantPanel();

		gridBagConstraints = GridBagHelper.as(0,2, WEST, HORIZONTAL, new Insets(0, 8, 0, 8));
		add(timeInvariantPanel, gridBagConstraints);


		initButtonPanel();

		gridBagConstraints = GridBagHelper.as(0,5, new Insets(0, 8, 5, 8));
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gridBagConstraints);
		initColorTypePanel();
        initColorInvariantPanel();
		initTokensPanel();
        setInitialComboBoxValue();
        writeTokensToList();
		setColoredTimeInvariants();
		if (currentTab.getEditorMode() == Pipe.ElementType.ADDTOKEN) {
            basicPropertiesPanel.setVisible(false);
            timeInvariantPanel.setVisible(false);
            colorTypePanel.setVisible(false);
            timeInvariantColorPanel.setVisible(false);
        } else {
            basicPropertiesPanel.setVisible(true);
            timeInvariantPanel.setVisible(true);
            colorTypePanel.setVisible(true);
            timeInvariantColorPanel.setVisible(true);
        }
	}

	private void initButtonPanel() {
		java.awt.GridBagConstraints gridBagConstraints;
		buttonPanel = new javax.swing.JPanel();
		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton = new javax.swing.JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));

		okButton.addActionListener(evt -> {
			if(doOKColored() && doOK()){
				exit();
			}
		});
		rootPane.setDefaultButton(okButton);

		cancelButton = new javax.swing.JButton();
		cancelButton.setText("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
		cancelButton.addActionListener(evt -> exit());

		gridBagConstraints = GridBagHelper.as(0,0,EAST, new Insets(5, 5, 5, 5));
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;

		buttonPanel.add(cancelButton, gridBagConstraints);



		gridBagConstraints = GridBagHelper.as(1,0, WEST, new Insets(5, 5, 5, 5));
		buttonPanel.add(okButton, gridBagConstraints);

		setupInitialState();
		if(place.underlyingPlace().isShared()){
			switchToNameDropDown();
		}else{
			switchToNameTextField();
		}
	}

	private void setupInitialState() {
		sharedPlaces = new Vector<TimedPlace>(context.network().sharedPlaces());

		Collection<TimedPlace> usedPlaces = context.activeModel().places();

		sharedPlaces.removeAll(usedPlaces);
		if (place.underlyingPlace().isShared()){
			sharedPlaces.add(place.underlyingPlace());
		}

		sharedPlaces.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
		sharedPlacesComboBox.setModel(new DefaultComboBoxModel<>(sharedPlaces));
		if(place.underlyingPlace().isShared()) {

			sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
		}

		sharedCheckBox.setEnabled(sharedPlaces.size() > 0 && !hasArcsToSharedTransitions(place.underlyingPlace()));
		sharedCheckBox.setSelected(place.underlyingPlace().isShared());
		
		makeSharedButton.setEnabled(!sharedCheckBox.isSelected() && !hasArcsToSharedTransitions(place.underlyingPlace()));

		nameTextField.setText(place.underlyingPlace().name());
		nameTextField.selectAll();
		attributesCheckBox.setSelected(place.getAttributesVisible());

		setMarking(place.underlyingPlace().numberOfTokens());
		setInvariantControlsBasedOn(place.underlyingPlace().invariant());		
	}

	private boolean hasArcsToSharedTransitions(TimedPlace underlyingPlace) {
		for(TimedInputArc arc : context.activeModel().inputArcs()){
			if(arc.source().equals(underlyingPlace) && arc.destination().isShared()) return true;
		}

		for(TimedOutputArc arc : context.activeModel().outputArcs()){
			if(arc.destination().equals(underlyingPlace) && arc.source().isShared()) return true;
		}

		for(TransportArc arc : context.activeModel().transportArcs()){
			if(arc.source().equals(underlyingPlace) && arc.transition().isShared()) return true;
			if(arc.destination().equals(underlyingPlace) && arc.transition().isShared()) return true;
		}

		for(TimedInhibitorArc arc : context.activeModel().inhibitorArcs()){
			if(arc.source().equals(underlyingPlace) && arc.destination().isShared()) return true;
		}

		return false;
	}

	private void initBasicPropertiesPanel() {
		basicPropertiesPanel = new JPanel();
		basicPropertiesPanel.setLayout(new java.awt.GridBagLayout());
		basicPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Place"));

		sharedCheckBox = new JCheckBox("Shared");
		sharedCheckBox.addActionListener(arg0 -> {
			JCheckBox box = (JCheckBox)arg0.getSource();
			if(box.isSelected()){
				switchToNameDropDown();
				makeSharedButton.setEnabled(false);
			}else{
				switchToNameTextField();
				nameTextField.setText(place.underlyingPlace().isShared()? CreateGui.getDrawingSurface().getNameGenerator().getNewPlaceName(context.activeModel()) : place.getName());
				makeSharedButton.setEnabled(true);
			}
		});

		GridBagConstraints gridBagConstraints = GridBagHelper.as(2,1, WEST, new Insets(0, 3, 3, 3));
		basicPropertiesPanel.add(sharedCheckBox, gridBagConstraints);

		makeSharedButton = new javax.swing.JButton();
		makeSharedButton.setText("Make shared");
		makeSharedButton.setMaximumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setMinimumSize(new java.awt.Dimension(110, 25));
		makeSharedButton.setPreferredSize(new java.awt.Dimension(110, 25));
		
		makeSharedButton.addActionListener(evt -> {
			makeNewShared = true;
			if(doOK()){
				setupInitialState();
				makeSharedButton.setEnabled(false);
				sharedCheckBox.setEnabled(true);
				sharedCheckBox.setSelected(true);
				switchToNameDropDown();
				sharedPlacesComboBox.setSelectedItem(place.underlyingPlace());
			}
			makeNewShared = false;
		});
		
		gridBagConstraints = GridBagHelper.as(3,1, WEST, new Insets(5, 5, 5, 5));
		basicPropertiesPanel.add(makeSharedButton, gridBagConstraints);
		
		nameLabel = new javax.swing.JLabel("Name:");
		gridBagConstraints = GridBagHelper.as(0,1, EAST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(nameLabel, gridBagConstraints);

		nameTextField = new javax.swing.JTextField();
		nameTextField.setPreferredSize(new Dimension(290,27));

		sharedPlacesComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);

		sharedPlacesComboBox.setPreferredSize(new Dimension(290,27));

		sharedPlacesComboBox.addItemListener(e -> {
			SharedPlace place = (SharedPlace)e.getItem();
			if(place.getComponentsUsingThisPlace().size() > 0){
				setMarking(place.numberOfTokens());
			}
			setInvariantControlsBasedOn(place);
		});

		markingLabel = new javax.swing.JLabel("Marking:");
		gridBagConstraints = GridBagHelper.as(0,2, EAST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(markingLabel, gridBagConstraints);

		markingSpinner = new CustomJSpinner(0, okButton);
		gridBagConstraints = GridBagHelper.as(1,2, WEST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(markingSpinner, gridBagConstraints);

		attributesCheckBox = new javax.swing.JCheckBox("Show place name");
		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new Insets(0, 0, 0, 0));

		gridBagConstraints = GridBagHelper.as(1,3,WEST, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(attributesCheckBox, gridBagConstraints);
	}

	private boolean isUrgencyOK(){
		for(TransportArc arc : CreateGui.getCurrentTab().currentTemplate().model().transportArcs()){
			if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
				JOptionPane.showMessageDialog(rootPane, "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if(place.underlyingPlace().isShared()){
			for(Template t : CreateGui.getCurrentTab().allTemplates()){
				for(TransportArc arc : t.model().transportArcs()){
					if(arc.destination().equals(place.underlyingPlace()) && arc.transition().isUrgent()){
						JOptionPane.showMessageDialog(rootPane, "Transport arcs going through urgent transitions cannot have an invariant at the destination.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}
		return true;
	}

	private void initTimeInvariantPanel() {
		timeInvariantPanel = new JPanel();
		timeInvariantPanel.setLayout(new java.awt.GridBagLayout());
		timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Age Invariant"));

		invariantGroup = new JPanel(new GridBagLayout());
		invRelationNormal = new JComboBox<>(new String[] { "<=", "<" });
		invRelationConstant = new JComboBox<>(new String[] { "<=", "<" });
		//invariantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		invariantSpinner = new CustomJSpinner(0, okButton);
		invariantSpinner.addChangeListener(e -> {
			if(!invariantInf.isSelected()){
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
					invRelationNormal.setSelectedItem("<=");
				} else if (invRelationNormal.getModel().getSize() == 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
			}
		});

		GridBagConstraints gbc = GridBagHelper.as(1,0, HORIZONTAL, new Insets(3, 3, 3, 3));
		invariantGroup.add(invRelationNormal, gbc);

		gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
        invariantGroup.add(invRelationConstant, gbc);

		gbc = GridBagHelper.as(2,0, new Insets(3, 3, 3, 3));
		invariantGroup.add(invariantSpinner, gbc);

		invariantInf = new JCheckBox("inf");
		invariantInf.addActionListener(arg0 -> {
			if(!isUrgencyOK()){
				invariantInf.setSelected(true);
				return;
			}
			if (!invariantInf.isSelected()) {
				invRelationNormal.setEnabled(true);
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem("<=");
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				} else {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
			} else {
				invRelationNormal.setEnabled(false);
				invariantSpinner.setEnabled(false);
				invRelationNormal.setSelectedItem("<");
				invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
			}

		});
		gbc = GridBagHelper.as(3,0);
		invariantGroup.add(invariantInf, gbc);

		Set<String> constants = context.network().getConstantNames();
		String[] constantArray = constants.toArray(new String[constants.size()]);
		Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);

		invConstantsComboBox = new WidthAdjustingComboBox<>(maxNumberOfPlacesToShowAtOnce);
		invConstantsComboBox.setModel(new DefaultComboBoxModel<>(constantArray));
		//	invConstantsComboBox = new JComboBox(constants.toArray());
		invConstantsComboBox.setMaximumRowCount(20);
		//	invConstantsComboBox.setMinimumSize(new Dimension(100, 30));
		invConstantsComboBox.setPreferredSize(new Dimension(230, 30));
		invConstantsComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				setRelationModelForConstants();
			}
		});

		gbc = GridBagHelper.as(2,1, WEST);
		invariantGroup.add(invConstantsComboBox, gbc);

		normalInvRadioButton = new JRadioButton("Normal");
		normalInvRadioButton.addActionListener(e -> {
			disableInvariantComponents();
			enableNormalInvariantComponents();
		});

		constantInvRadioButton = new JRadioButton("Constant");
		constantInvRadioButton.addActionListener(e -> {
			disableInvariantComponents();
			enableConstantInvariantComponents();
		});
		if (constants.isEmpty()){
			constantInvRadioButton.setEnabled(false);
		}
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(normalInvRadioButton);
		btnGroup.add(constantInvRadioButton);

		gbc = GridBagHelper.as(0,0, WEST);
		invariantGroup.add(normalInvRadioButton, gbc);

		gbc = GridBagHelper.as(0,1, WEST);
		invariantGroup.add(constantInvRadioButton, gbc);

		TimeInvariant invariantToSet = place.getInvariant();

		if (invariantToSet.isUpperNonstrict()) {
			invRelationNormal.setSelectedItem("<=");
		} else {
			invRelationNormal.setSelectedItem("<");
		}

		if (invariantToSet.upperBound() instanceof InfBound) {
			invariantSpinner.setEnabled(false);
			invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
			invariantInf.setSelected(true);
			invRelationNormal.setSelectedItem("<");
		}

		disableInvariantComponents();
		if (invariantToSet.upperBound() instanceof ConstantBound) {
			enableConstantInvariantComponents();
			constantInvRadioButton.setSelected(true);
			invConstantsComboBox.setSelectedItem(((ConstantBound) invariantToSet.upperBound()).name());
			invRelationConstant.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
		} else {
			enableNormalInvariantComponents();
			normalInvRadioButton.setSelected(true);
			if (invariantToSet.upperBound() instanceof IntBound) {
				if ((Integer) invariantSpinner.getValue() < 1) {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				} else {
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
				}
				invariantSpinner.setValue(invariantToSet.upperBound().value());
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
				invariantInf.setSelected(false);
			}
		}

		GridBagConstraints gridBagConstraints = GridBagHelper.as(1,4,2, WEST, new Insets(3, 3, 3, 3));
		timeInvariantPanel.add(invariantGroup, gridBagConstraints);
	}

	private void setRelationModelForConstants() {
		int value = CreateGui.getCurrentTab().network().getConstantValue(invConstantsComboBox.getSelectedItem().toString());

		String selected = invRelationConstant.getSelectedItem().toString();
		if (value == 0) {
			invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
		} else {
			invRelationConstant.setModel(new DefaultComboBoxModel<>(new String[] { "<=", "<" }));
		}
		invRelationConstant.setSelectedItem(selected);
	}

	protected void enableConstantInvariantComponents() {
		invRelationConstant.setEnabled(true);
		invConstantsComboBox.setEnabled(true);
		setRelationModelForConstants();
	}

	protected void enableNormalInvariantComponents() {
		invRelationNormal.setEnabled(true);
		invariantInf.setEnabled(true);
		invariantSpinner.setValue(0);
		invariantInf.setSelected(true);
		invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
	}

	protected void disableInvariantComponents() {
		invRelationNormal.setEnabled(false);
		invRelationConstant.setEnabled(false);
		invariantSpinner.setEnabled(false);
		invConstantsComboBox.setEnabled(false);
		invariantInf.setEnabled(false);
	}

	private void switchToNameTextField() {
		basicPropertiesPanel.remove(sharedPlacesComboBox);
		GridBagConstraints gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(nameTextField, gbc);

		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();
	}

	private void switchToNameDropDown() {
		basicPropertiesPanel.remove(nameTextField);
		GridBagConstraints gbc = GridBagHelper.as(1,1, HORIZONTAL, new Insets(3, 3, 3, 3));
		basicPropertiesPanel.add(sharedPlacesComboBox, gbc);

		basicPropertiesPanel.validate();
		basicPropertiesPanel.repaint();

		SharedPlace selected = (SharedPlace)sharedPlacesComboBox.getSelectedItem();
		setInvariantControlsBasedOn(selected);
		if(selected.getComponentsUsingThisPlace().size() > 0){
			setMarking(selected.numberOfTokens());
		}
	}

	private void setMarking(int numberOfTokens) {
		markingSpinner.setValue(numberOfTokens);
	}

	private void setInvariantControlsBasedOn(TimedPlace place) {
		if(place instanceof SharedPlace && ((SharedPlace) place).getComponentsUsingThisPlace().size() > 0){
			setInvariantControlsBasedOn(place.invariant());
		}
	}
	
	private void setInvariantControlsBasedOn(TimeInvariant invariant) {
		if(invariant.upperBound() instanceof ConstantBound){
			constantInvRadioButton.setSelected(true);
			invRelationConstant.setModel(new DefaultComboBoxModel<>(invariant.upperBound().value() == 0 ? new String[] { "<=" } : new String[] { "<", "<=" }));
			invRelationConstant.setSelectedItem(invariant.isUpperNonstrict() ? "<=" : "<");
			invRelationConstant.setEnabled(true);
			invConstantsComboBox.setEnabled(true);
			invConstantsComboBox.setSelectedItem(((ConstantBound)invariant.upperBound()).constant());
		}else{
			normalInvRadioButton.setSelected(true);
			if(invariant.upperBound() instanceof InfBound){
				invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<" }));
				invariantSpinner.setValue(0);
				invRelationNormal.setEnabled(false);
				invariantSpinner.setEnabled(false);
				invariantInf.setSelected(true);
			}else{
				if(invariant.upperBound().value() == 0 && !invariantInf.isSelected()){
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<=" }));
				}else{
					invRelationNormal.setModel(new DefaultComboBoxModel<>(new String[] { "<", "<=" }));
				}
				invRelationNormal.setSelectedItem(invariant.isUpperNonstrict() ? "<=" : "<");
				invariantSpinner.setValue(invariant.upperBound().value());
				invRelationNormal.setEnabled(true);
				invariantSpinner.setEnabled(true);
				invariantInf.setSelected(false);
			}
		}
	}

	private boolean doOK() {
		// Check urgent constrain
		if(!invariantInf.isSelected() && !isUrgencyOK()){
			return false;
		}

		int newMarking = (Integer)markingSpinner.getValue();
		if (newMarking > Pipe.MAX_NUMBER_OF_TOKENS_ALLOWED) {
			JOptionPane.showMessageDialog(this,"It is allowed to have at most " + Pipe.MAX_NUMBER_OF_TOKENS_ALLOWED + " tokens in a place.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//Only make new edit if it has not already been done
		if(doNewEdit) {
			context.undoManager().newEdit(); // new "transaction""
			doNewEdit = false;
		}
		TimedPlace underlyingPlace = place.underlyingPlace();

		SharedPlace selectedPlace = (SharedPlace)sharedPlacesComboBox.getSelectedItem();
		if(sharedCheckBox.isSelected() && !selectedPlace.equals(underlyingPlace)){
			Command command = new MakePlaceSharedCommand(context.activeModel(), selectedPlace, place.underlyingPlace(), place, context.tabContent());
			context.undoManager().addEdit(command);
			try{
				command.redo();
			}catch(RequireException e){
				context.undoManager().undo();
				JOptionPane.showMessageDialog(this,"Another place in the same component is already shared under that name", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}		
		}else if(!sharedCheckBox.isSelected()){
			if(underlyingPlace.isShared()){
				String uniqueName = context.nameGenerator().getNewPlaceName(context.activeModel());
				Command unshareCmd = new UnsharePlaceCommand(context.activeModel(), (SharedPlace)underlyingPlace, new LocalTimedPlace(uniqueName), place);
				unshareCmd.redo();
				context.undoManager().addEdit(unshareCmd);
			}

			String newName = nameTextField.getText();
			String oldName = place.underlyingPlace().name();
			if(context.activeModel().isNameUsed(newName) && !oldName.equalsIgnoreCase(newName)){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this, "The specified name is already used by another place or transition.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}   	
			
			Command renameCommand = new RenameTimedPlaceCommand(context.tabContent(), (LocalTimedPlace)place.underlyingPlace(), oldName, newName);
			context.undoManager().addEdit(renameCommand);
			try{ // set name
				renameCommand.redo();
			}catch(RequireException e){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this, "Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			context.nameGenerator().updateIndices(context.activeModel(), newName);
		
			if(makeNewShared){
				Command command = new MakePlaceNewSharedCommand(context.activeModel(), newName, place.underlyingPlace(), place, context.tabContent(), false);
				context.undoManager().addEdit(command);
				try{
					command.redo();
				}catch(RequireException e){
					context.undoManager().undo();
					//This is checked as a place cannot be shared if there exists a transition with the same name
					if(context.activeModel().parentNetwork().isNameUsedForPlacesOnly(newName)) {
						int dialogResult = JOptionPane.showConfirmDialog(this, "A place with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords. \n\nThis place name will be changed into shared one also in all other components.", "Error", JOptionPane.OK_CANCEL_OPTION);
						if(dialogResult == JOptionPane.OK_OPTION) {
							Command cmd = new MakePlaceNewSharedMultiCommand(context, newName, place);	
							cmd.redo();
							context.undoManager().addNewEdit(cmd);
						} else {
							return false;
						}
					} else {
						JOptionPane.showMessageDialog(this, "A transition with the specified name already exists in one or more components, or the specified name is invalid.\n\nAcceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*\n\nNote that \"true\" and \"false\" are reserved keywords.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}	
			}
		}
		//TODO: Look at this
        if(place.isTimed() && !place.isColored()){
            if(newMarking != place.underlyingPlace().numberOfTokens()){
                Command command = new TimedPlaceMarkingEdit(place, newMarking - place.underlyingPlace().numberOfTokens());
                command.redo();
                context.undoManager().addEdit(command);
            }
        }

		TimeInvariant newInvariant = constructInvariant();
		TimeInvariant oldInvariant = place.underlyingPlace().invariant();
		if(!newInvariant.equals(oldInvariant)){
			context.undoManager().addEdit(new ChangedInvariantCommand(place.underlyingPlace(), oldInvariant, newInvariant));
			place.underlyingPlace().setInvariant(newInvariant);
		}

		if ((place.getAttributesVisible() && !attributesCheckBox.isSelected()) || (!place.getAttributesVisible() && attributesCheckBox.isSelected())) {
			place.toggleAttributesVisible();
		}
		place.update(true);
		place.repaint();

		context.network().buildConstraints();
		
		return true;
	}

	private TimeInvariant constructInvariant() {
		if(normalInvRadioButton.isSelected()){
			if(invariantInf.isSelected()){
				return TimeInvariant.LESS_THAN_INFINITY;
			}else{
				int bound = (Integer)invariantSpinner.getValue();
				boolean nonStrict = "<=".equals(invRelationNormal.getSelectedItem());
				return new TimeInvariant(nonStrict, new IntBound(bound));
			}
		}else{
			boolean nonStrict = "<=".equals(invRelationConstant.getSelectedItem());
			Constant constant = context.network().getConstant((String)invConstantsComboBox.getSelectedItem());
			return new TimeInvariant(nonStrict, new ConstantBound(constant));
		}
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}
    private void initTokensPanel() {
        tokenPanel = new JPanel();
        tokenButtonPanel = new JPanel(new GridBagLayout());
        tokenPanel.setLayout(new GridBagLayout());
        tokenPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tokens"));

        tokenColorComboboxPanel = new ColorComboboxPanel(colorType, "colors") {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                updateSpinnerValue(comboBoxes);
            }
        };
        tokenColorComboboxPanel.removeScrollPaneBorder();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        tokenPanel.add(tokenColorComboboxPanel, gbc);
        //Logger.log(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getItemAt(0).toString());


        coloredTokenListModel = new DefaultListModel();
        tokenList = new JList(coloredTokenListModel);
        tableModel = new DefaultTableModel(0, new String[]{"Number of Token", "Token type"}.length);
        tableModel.setColumnIdentifiers(new String[]{"Number of Token", "Token type"});
        tokenTable = new JTable(tableModel);
        tokenTable.setCellSelectionEnabled(false);
        tokenTable.setRowSelectionAllowed(true);
        tokenTable.setColumnSelectionAllowed(false);
        tokenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tokenTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            if(tokenTable.getSelectedRow() < 0){
                removeColoredTokenButton.setEnabled(false);
            } else{
                updateSpinnerValue();
                tokenColorComboboxPanel.updateSelection(((TimedToken)tokenTable.getValueAt(tokenTable.getSelectedRow(), 1)).color());
                removeColoredTokenButton.setEnabled(true);
            }
        });
        tokenTable.setDefaultRenderer(Object.class, new TokenTableCellRenderer());


        JScrollPane tokenListScrollPane = new JScrollPane(tokenTable);
        tokenListScrollPane.setViewportView(tokenTable);
        tokenListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension tokenScrollPaneDim = new Dimension(750, 200);
        tokenListScrollPane.setBorder(BorderFactory.createTitledBorder( "Tokens"));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(3, 3, 3,3);
        //tokenPanel.add(tokenListScrollPane, gbc);
        tokenListScrollPane.setPreferredSize(tokenScrollPaneDim);
        tokenPanel.add(tokenListScrollPane, gbc);

        addColoredTokenButton = new JButton("Set");
        Dimension buttonSize = new Dimension(100, 30);
        addColoredTokenButton.setPreferredSize(buttonSize);
        addColoredTokenButton.setMinimumSize(buttonSize);
        addColoredTokenButton.setMaximumSize(buttonSize);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(3, 3, 3,3);
        tokenButtonPanel.add(addColoredTokenButton, gbc);

        addColoredTokenButton.addActionListener(actionEvent -> {
            addColoredTokens((int) addTokenSpinner.getValue());
        });
        SpinnerModel addTokenSpinnerModel = new SpinnerNumberModel(1,1,999,1);
        addTokenSpinner = new JSpinner(addTokenSpinnerModel);
        addTokenSpinner.setPreferredSize(buttonSize);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(3, 3, 3,3);
        tokenPanel.add(addTokenSpinner, gbc);

        removeColoredTokenButton = new JButton("Remove");


        removeColoredTokenButton.setPreferredSize(buttonSize);
        removeColoredTokenButton.setMinimumSize(buttonSize);
        removeColoredTokenButton.setMaximumSize(buttonSize);

        removeColoredTokenButton.addActionListener(actionEvent -> {
            if(tokenTable.getSelectedRow() > -1){
                tableModel.removeRow(tokenTable.getSelectedRow());
            }

        });
        removeColoredTokenButton.setEnabled(false);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        tokenButtonPanel.add(removeColoredTokenButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tokenPanel.add(tokenButtonPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        add(tokenPanel, gbc);
    }

    public void initColorInvariantPanel(){
	    timeInvariantColorPanel = new JPanel(new GridBagLayout());
        timeInvariantColorPanel.setBorder(BorderFactory.createTitledBorder("Time invariants for specific colors"));

        GridBagConstraints gbc = GridBagHelper.as(0,0);
        timeInvariantColorPanel.add(initNonDefaultColorInvariantPanel(), gbc);

        gbc = GridBagHelper.as(0,3, WEST, HORIZONTAL, new Insets(3, 3, 3, 3));
        add(timeInvariantColorPanel, gbc);

    }

    private JPanel initNonDefaultColorInvariantPanel() {
	    //This panel holds the edit panel and the scrollpane
        JPanel nonDefaultColorInvariantPanel = new JPanel(new GridBagLayout());
        //this panel holds the buttons, the invariant editor panel and the color combobox
        JPanel colorInvariantEditPanel = new JPanel(new GridBagLayout());
        //colorInvariantEditPanel.setBorder(BorderFactory.createTitledBorder("Edit color specific invariant"));

        colorInvariantComboboxPanel = new ColorComboboxPanel(colorType, "colors") {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                ColoredTimeInvariant timeConstraint;
                if (!(colorType instanceof ProductType)) {
                    timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR((Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                } else {
                    Vector<Color> colors = new Vector<Color>();
                    for (int i = 0; i < comboBoxes.length; i++) {
                        colors.add((Color) comboBoxes[i].getItemAt(comboBoxes[i].getSelectedIndex()));
                    }
                    Color color = new Color(colorType, 0, colors);
                    timeConstraint = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR(color);
                }
                boolean alreadyExists = false;
                for (int i = 0; i < timeConstraintListModel.size(); i++) {
                    if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                        invariantEditorPanel.setInvariant((ColoredTimeInvariant) timeConstraintListModel.get(i));
                        addTimeConstraintButton.setText("Modify");
                        alreadyExists = true;
                    }
                }
                if(!alreadyExists){
                    invariantEditorPanel.setInvariant(timeConstraint);
                    addTimeConstraintButton.setText("Add");
                }
            }
        };
        colorInvariantComboboxPanel.removeScrollPaneBorder();
        addTimeConstraintButton = new JButton("Add");
        removeTimeConstraintButton = new JButton("Remove");

        Dimension buttonSize = new Dimension(80, 27);

        addTimeConstraintButton.setPreferredSize(buttonSize);
        removeTimeConstraintButton.setPreferredSize(buttonSize);

        timeConstraintListModel = new DefaultListModel();
        timeConstraintList = new JList(timeConstraintListModel);
        timeConstraintList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        timeConstraintListModel.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent arg0) {
            }

            public void intervalAdded(ListDataEvent arg0) {
                timeConstraintList.setSelectedIndex(arg0.getIndex0());
                timeConstraintList.ensureIndexIsVisible(arg0.getIndex0());
            }

            public void intervalRemoved(ListDataEvent arg0) {
                int index = (arg0.getIndex0() == 0) ? 0 : (arg0.getIndex0() - 1);
                timeConstraintList.setSelectedIndex(index);
                timeConstraintList.ensureIndexIsVisible(index);
            }

        });
        timeConstraintList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                JList source = (JList) listSelectionEvent.getSource();
                if(source.getSelectedIndex() >= 0){
                    ColoredTimeInvariant cti = (ColoredTimeInvariant) source.getModel().getElementAt(source.getSelectedIndex());
                    invariantEditorPanel.setInvariant(cti);
                    colorInvariantComboboxPanel.updateSelection(cti.getColor());
                    addTimeConstraintButton.setText("Modify");
                }
                if(timeConstraintList.isSelectionEmpty()){
                    removeTimeConstraintButton.setEnabled(false);
                } else{
                    removeTimeConstraintButton.setEnabled(true);
                }
            }
        });
        JScrollPane timeConstraintScrollPane = new JScrollPane(timeConstraintList);
        timeConstraintScrollPane.setViewportView(timeConstraintList);
        timeConstraintScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time invariant for colors"));

        addTimeConstraintButton.addActionListener(actionEvent -> {
            JComboBox[] comboBoxes = colorInvariantComboboxPanel.getColorTypeComboBoxesArray();
            ColoredTimeInvariant timeConstraint = invariantEditorPanel.getInvariant();
            boolean alreadyExists = false;

            for (int i = 0; i < timeConstraintListModel.size(); i++) {
                if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                    alreadyExists = true;
                    timeConstraintListModel.setElementAt(timeConstraint, i);
                }
            }
            if (!alreadyExists){
                timeConstraintListModel.addElement(timeConstraint);
            }
        });

        removeTimeConstraintButton.addActionListener(actionEvent -> {
            timeConstraintListModel.removeElementAt(timeConstraintList.getSelectedIndex());
            if(timeConstraintListModel.isEmpty()){
                addTimeConstraintButton.setText("Add");
            } else{
                timeConstraintList.setSelectedIndex(0);
            }

        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = gbc.BOTH;
        colorInvariantEditPanel.add(colorInvariantComboboxPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3,3);
        buttonPanel.add(addTimeConstraintButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(removeTimeConstraintButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        ColoredTimeInvariant cti;
        if(place.underlyingPlace().getCtiList().isEmpty()) {
            cti = ColoredTimeInvariant.LESS_THAN_INFINITY_DYN_COLOR(place.underlyingPlace().getColorType().getFirstColor());
        } else{
            cti = place.underlyingPlace().getCtiList().get(0);
        }
        invariantEditorPanel = new ColoredTimeInvariantDialogPanel(rootPane,context,
            cti, place) {
            @Override
            public void placeHolder() {

            }
        };
        //invariantEditorPanel.removeBorder();
        colorInvariantEditPanel.add(invariantEditorPanel,gbc);


        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        colorInvariantEditPanel.add(buttonPanel,gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        nonDefaultColorInvariantPanel.add(colorInvariantEditPanel,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        //gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        timeConstraintScrollPane.setPreferredSize(new Dimension(300, 150));
        nonDefaultColorInvariantPanel.add(timeConstraintScrollPane, gbc);

        return nonDefaultColorInvariantPanel;
    }

    private void initColorTypePanel() {
	    colorTypePanel = new JPanel();
        colorTypePanel.setLayout(new GridBagLayout());
        colorTypePanel.setBorder(new TitledBorder("Color Type"));

        JLabel colortypeLabel = new JLabel();
        colortypeLabel.setText("Color Type:");

        colorTypeComboBox = new JComboBox();
        List<ColorType> colorTypes = context.network().colorTypes();

        for (ColorType element : colorTypes) {
            colorTypeComboBox.addItem(element);
        }

        colorTypeComboBox.addActionListener(actionEvent -> {
            if (!(tokenTable.getRowCount() < 1) || !timeConstraintListModel.isEmpty()) {
                int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to change the color type for this place?\n" +
                    "All tokens and time invariants for colors will be deleted.","alert", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    setNewColorType(colorTypeComboBox.getItemAt(colorTypeComboBox.getSelectedIndex()));
                }
                else { // NO.OPTION - we set the color type to the previous selected one
                    for (int i = 0; i <  colorTypeComboBox.getItemCount(); i++) {
                        if (colorType.getName().equals(colorTypeComboBox.getItemAt(i).getName())) {
                            colorTypeComboBox.setSelectedIndex(i);
                        }
                    }
                }
            } else {
                setNewColorType(colorTypeComboBox.getItemAt(colorTypeComboBox.getSelectedIndex()));
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);
        colorTypePanel.add(colortypeLabel, gbc);

        Dimension colorTypeComboBoxSize = new Dimension(500, 30);
        colorTypeComboBox.setPreferredSize(colorTypeComboBoxSize);
        colorTypeComboBox.setMinimumSize(colorTypeComboBoxSize);
        colorTypeComboBox.setPreferredSize(colorTypeComboBoxSize);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3,3 );
        colorTypePanel.add(colorTypeComboBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        add(colorTypePanel, gbc);
    }

    private void addColoredTokens(int numberOfTokens) {
        TimedToken ct;
        if (colorType instanceof ProductType) {
            Vector<Color> colorVector = new Vector();
            for (int i = 0; i < tokenColorComboboxPanel.getColorTypeComboBoxesArray().length ; i++) {
                colorVector.add((Color)tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i].getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[i].getSelectedIndex()));
            }
            Color tempColor = new Color(colorType, 0, colorVector);
            ct = new TimedToken(place.underlyingPlace(), tempColor);
        }
        else {
            ct = new TimedToken(place.underlyingPlace(), (dk.aau.cs.model.CPN.Color) tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getItemAt(tokenColorComboboxPanel.getColorTypeComboBoxesArray()[0].getSelectedIndex()));
        }
        boolean existsAlready = false;
        if(tableModel.getRowCount() > 0){
            int currSize = tableModel.getRowCount();
            for(int i = 0; i < currSize; i++){
                if(((TimedToken) tokenTable.getValueAt(i,1)).color().toString().equals(ct.color().toString())){
                    tableModel.setValueAt(numberOfTokens,i,0);
                    existsAlready = true;
                }
            }
            if(!existsAlready){
                tableModel.addRow(new Object[]{numberOfTokens, ct});
            }
        }else{
            tableModel.addRow(new Object[]{numberOfTokens, ct});
        }
	}
    private void addColoredToken(TimedToken tokenToAdd) {
        boolean existsAlready = false;
        if(tableModel.getRowCount() > 0){
            int currSize = tableModel.getRowCount();
            for(int i = 0; i < currSize; i++){
                if(((TimedToken) tokenTable.getValueAt(i,1)).color().toString().equals(tokenToAdd.color().toString())){
                    tableModel.setValueAt((int)tokenTable.getValueAt(i,0) +1,i,0);
                    existsAlready = true;
                }
            }
            if(!existsAlready){
                tableModel.addRow(new Object[]{1, tokenToAdd});
            }
        }else{
            tableModel.addRow(new Object[]{1, tokenToAdd});
        }
    }


    public boolean doOKColored() {
        ArrayList<TimedToken> tokenList = new ArrayList(context.activeModel().marking().getTokensFor(place.underlyingPlace()));

        for (TimedToken token : tokenList) {
            context.activeModel().marking().remove(token);
        }
        for(int row = 0; row < tableModel.getRowCount(); row++){
            int numberToAdd = (int)tableModel.getValueAt(row,0);
            TimedToken tokenTypeToAdd = (TimedToken) tableModel.getValueAt(row,1);
            for (int i = 0; i < numberToAdd; i++) {
                context.activeModel().marking().add(tokenTypeToAdd);
            }
        }

        List<ColoredTimeInvariant> ctiList = new ArrayList<ColoredTimeInvariant>();
        for (int i = 0; i < timeConstraintListModel.size(); i++) {
            ctiList.add((ColoredTimeInvariant) timeConstraintListModel.get(i));
        }

        place.underlyingPlace().setCtiList(ctiList);
        place.underlyingPlace().setColorType(colorType);

        return true;
    }

    private void writeTokensToList() {
        coloredTokenListModel.clear();
        for (TimedToken element : context.network().marking().getTokensFor(place.underlyingPlace())) {
            addColoredToken(element);
        }
        updateSpinnerValue();
    }

    private void updateSpinnerValue(){
	    updateSpinnerValue(tokenColorComboboxPanel.getColorTypeComboBoxesArray());
    }

    private void updateSpinnerValue(JComboBox[] comboBoxes){
        boolean existsAlready = false;
        if(tableModel.getRowCount() > 0){
            int currSize = tableModel.getRowCount();
            for(int i = 0; i < currSize; i++){
                if(((TimedToken) tokenTable.getValueAt(i,1)).color().toString().equals(((Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex())).toString())){
                    addTokenSpinner.setValue(tokenTable.getValueAt(i,0));
                    existsAlready = true;
                }
            }
            if(!existsAlready){
                addTokenSpinner.setValue(1);
            }
        }else{
            addTokenSpinner.setValue(1);
        }
    }

    private void setInitialComboBoxValue() {
        List<ColorType> colorTypes = context.network().colorTypes();
        if (colorType != null) {
            colorTypeComboBox.setSelectedIndex(colorTypes.indexOf(colorType));
        }
        else if (colorTypes.size() != 0) {
            colorTypeComboBox.setSelectedIndex(0);
        }
    }

    private void setNewColorType(ColorType colorType) {
        this.colorType = colorType;
        tableModel.setRowCount(0);
        timeConstraintListModel.clear();
        tokenColorComboboxPanel.updateColorType(colorType);
        colorInvariantComboboxPanel.updateColorType(colorType);
        addTokenSpinner.setValue(1);
        parent.pack();
    }

    private void setColoredTimeInvariants() {
        for (ColoredTimeInvariant timeInvariant : place.underlyingPlace().getCtiList()) {
            timeConstraintListModel.addElement(timeInvariant);
        }
        timeConstraintList.setSelectedIndex(0);
    }


    private javax.swing.JCheckBox attributesCheckBox;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel markingLabel;
	private javax.swing.JSpinner markingSpinner;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JButton makeSharedButton;
	private javax.swing.JPanel basicPropertiesPanel;
	private javax.swing.JPanel timeInvariantPanel;
	private JPanel invariantGroup;
	private JComboBox<String> invRelationNormal;
	private JComboBox<String> invRelationConstant;
	private JSpinner invariantSpinner;
	private JCheckBox invariantInf;
	private JComboBox<String> invConstantsComboBox;
	private JRadioButton normalInvRadioButton;
	private JRadioButton constantInvRadioButton;
    private JPanel tokenPanel;
    private DefaultListModel<TimedToken> coloredTokenListModel;
    private JList tokenList;
    private JPanel tokenButtonPanel;
    private JButton addColoredTokenButton;
    private JButton removeColoredTokenButton;
    private ColorComboboxPanel tokenColorComboboxPanel;
    private ColorType colorType;
    JPanel timeInvariantColorPanel;
    DefaultListModel timeConstraintListModel;
    JList timeConstraintList;
    JComboBox<ColorType>  colorTypeComboBox;
    JPanel colorTypePanel;
    JSpinner addTokenSpinner;
    DefaultTableModel tableModel;
    JTable tokenTable;
    ColoredTimeInvariantDialogPanel invariantEditorPanel;
    JButton addTimeConstraintButton;
    JButton removeTimeConstraintButton;
    ColorComboboxPanel colorInvariantComboboxPanel;

}

