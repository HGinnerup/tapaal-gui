package pipe.gui.ColoredComponents;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TransportArc;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ColoredArcGuardPanel extends JPanel {
    PetriNetObject objectToBeEdited;
    boolean isTransportArc = false;
    boolean isInputArc = false;
    Context context;
    private Integer transportWeight;
    private ColorExpression transportInputExpr;
    private ColorExpression transportOutputExpr;

    public ColoredArcGuardPanel(PetriNetObject objectToBeEdited, Context context){
        this.objectToBeEdited = objectToBeEdited;
        if(objectToBeEdited instanceof TimedTransportArcComponent){
            isTransportArc = true;
            setTransportExpression();
        }
        if(((Arc)objectToBeEdited).getSource() instanceof Place){
            isInputArc = true;
            this.colorType = ((TimedPlaceComponent) ((Arc)objectToBeEdited).getSource()).underlyingPlace().getColorType();
        } else if(isTransportArc){
            this.colorType = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().source().getColorType();
        } else{
            this.colorType = ((TimedOutputArcComponent)objectToBeEdited).underlyingArc().destination().getColorType();
        }
        this.context = context;
        this.setLayout(new GridBagLayout());
        initPanels();
        initExpr();
        setTimeConstraints();
        getVariables();
        updateSelection();
        hideIrrelevantInformation();

        //TODO: implement these
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        editExprButton.setEnabled(false);

    }

    public void hideIrrelevantInformation(){
        if(!objectToBeEdited.isTimed()){
            nonDefaultArcColorIntervalPanel.setVisible(false);
        }
        if(isTransportArc){
            regularArcExprPanel.setVisible(false);
        } else{
            transportWeightPanel.setVisible(false);
        }
    }

    private void setTransportExpression() {
        ArcExpression arcExprInput = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getInputExpression();
        ArcExpression arcExprOutput = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getOutputExpression();

        if (arcExprInput instanceof NumberOfExpression) {
            transportWeight = arcExprInput.weight();
            Vector<ColorExpression> vecColorExpr = ((NumberOfExpression) arcExprInput).getColor();
            transportInputExpr = new TupleExpression(vecColorExpr);
        }

        if (arcExprOutput instanceof  NumberOfExpression) {
            Vector<ColorExpression> vecColorExpr = ((NumberOfExpression) arcExprOutput).getColor();
            transportOutputExpr = new TupleExpression(vecColorExpr);
        }
    }

    private void initPanels() {
        initRegularArcExpressionPanel();
        if(isInputArc || isTransportArc){
            initColoredTimedGuard();
        }
        initWeightPanel();
        if(isTransportArc){
            initTransportArcExpressionPanel();
        }

    }

    private void initWeightPanel(){
        transportWeightPanel = new JPanel(new GridBagLayout());
        //int current = transportWeight;
        int min = 1;
        int max = 9999;
        int step = 1;
        numberModel = new SpinnerNumberModel(1, min, max, step);
        colorExpressionWeightSpinner = new JSpinner(numberModel);
        JLabel weightLabel = new JLabel("Weight:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10 ,5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        transportWeightPanel.add(weightLabel, gbc);

        gbc.gridx = 1;
        transportWeightPanel.add(colorExpressionWeightSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(transportWeightPanel, gbc);
    }

    private void initColoredTimedGuard() {
        ColorType ct;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(initNonDefaultColorIntervalPanel(), gbc);
    }

    private JPanel initNonDefaultColorIntervalPanel() {
        nonDefaultArcColorIntervalPanel = new JPanel(new GridBagLayout());
        JPanel colorIntervalEditPanel = new JPanel(new GridBagLayout());
        nonDefaultArcColorIntervalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Color specific time intervals"));

        colorIntervalComboboxPanel = new ColorComboboxPanel(colorType, "colors") {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {
                ColoredTimeInterval timeConstraint;
                if (!(colorType instanceof ProductType)) {
                    timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR((dk.aau.cs.model.CPN.Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
                } else {
                    Vector<dk.aau.cs.model.CPN.Color> colors = new Vector<dk.aau.cs.model.CPN.Color>();
                    for (int i = 0; i < comboBoxes.length; i++) {
                        colors.add((dk.aau.cs.model.CPN.Color) comboBoxes[i].getItemAt(comboBoxes[i].getSelectedIndex()));
                    }
                    dk.aau.cs.model.CPN.Color color = new dk.aau.cs.model.CPN.Color(colorType, 0, colors);
                    timeConstraint = ColoredTimeInterval.ZERO_INF_DYN_COLOR(color);
                }
                boolean alreadyExists = false;
                for (int i = 0; i < timeConstraintListModel.size(); i++) {
                    if (timeConstraint.equalsOnlyColor(timeConstraintListModel.get(i))){
                        intervalEditorPanel.setTimeInterval((ColoredTimeInterval) timeConstraintListModel.get(i));
                        addTimeConstraintButton.setText("Modify");
                        alreadyExists = true;
                    }
                }
                if(!alreadyExists){
                    intervalEditorPanel.setTimeInterval(timeConstraint);
                    addTimeConstraintButton.setText("Add");
                }
            }
        };
        colorIntervalComboboxPanel.removeScrollPaneBorder();

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
                    ColoredTimeInterval cti = (ColoredTimeInterval) source.getModel().getElementAt(source.getSelectedIndex());
                    intervalEditorPanel.setTimeInterval(cti);
                    colorIntervalComboboxPanel.updateSelection(cti.getColor());
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
        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time interval for colors"));


        addTimeConstraintButton.addActionListener(actionEvent -> {
            ColoredTimeInterval timeConstraint = intervalEditorPanel.getInterval();
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
        colorIntervalEditPanel.add(colorIntervalComboboxPanel, gbc);

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

        ColoredTimeInterval cti = null;
        if(isInputArc && !isTransportArc){
            if(((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals().isEmpty()) {
                cti = ColoredTimeInterval.ZERO_INF_DYN_COLOR(((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().source().getColorType().getFirstColor());
            } else{
                cti = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals().get(0);
            }
        } else if(isInputArc && isTransportArc){
            if(((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals().isEmpty()) {
                cti = ColoredTimeInterval.ZERO_INF_DYN_COLOR(((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().source().getColorType().getFirstColor());
            } else{
                cti = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals().get(0);
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        intervalEditorPanel = new ColoredTimeIntervalDialogPanel(getRootPane(),context, cti);
        colorIntervalEditPanel.add(intervalEditorPanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        colorIntervalEditPanel.add(buttonPanel,gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        nonDefaultArcColorIntervalPanel.add(colorIntervalEditPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth =3;
        nonDefaultArcColorIntervalPanel.add(timeConstraintScrollPane, gbc);

        return nonDefaultArcColorIntervalPanel;
    }

    private void initTransportArcExpressionPanel(){
        transportExprTabbedPane = new JTabbedPane();
        inputPanel = new ColorExpressionDialogPanel(context, transportInputExpr, true, colorType);
        outputPanel = new ColorExpressionDialogPanel(context, transportOutputExpr, true, colorType);

        transportExprTabbedPane.add(inputPanel, "input");
        transportExprTabbedPane.add(outputPanel, "output");

        GridBagConstraints gbc = GridBagHelper.as(0, 4, GridBagHelper.Anchor.WEST, new Insets(5, 10, 5, 10));
        gbc.fill = GridBagConstraints.BOTH;
        add(transportExprTabbedPane, gbc);

    }

    private void initRegularArcExpressionPanel(){
        regularArcExprPanel = new JPanel(new GridBagLayout());
        initExprField();
        initNumberExpressionsPanel();
        initArithmeticPanel();
        initEditPanel();
        initColorExpressionButtonsPanel();

        expressionPanel = new JPanel(new GridBagLayout());
        expressionPanel.setBorder(BorderFactory.createTitledBorder("Arc Expression"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        expressionPanel.add(regularArcExprPanel, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(expressionPanel, gbc);
    }

    private void initEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        editPanel.setPreferredSize(new Dimension(260, 190));

        ButtonGroup editButtonsGroup = new ButtonGroup();
        deleteExprSelectionButton = new JButton("Delete Selection");
        resetExprButton = new JButton("Reset Expression");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        editExprButton = new JButton("Edit Expression");
        editExprButton.setEnabled(true);

        //TODO: add tooltips to buttons

        editButtonsGroup.add(deleteExprSelectionButton);
        editButtonsGroup.add(resetExprButton);
        editButtonsGroup.add(undoButton);
        editButtonsGroup.add(redoButton);
        editButtonsGroup.add(editExprButton);

        deleteExprSelectionButton.addActionListener(actionEvent -> deleteSelection());

        resetExprButton.addActionListener(actionEvent -> {
            PlaceHolderArcExpression pHExpr = new PlaceHolderArcExpression();
            arcExpression = arcExpression.replace(arcExpression, pHExpr);
            updateSelection(pHExpr);
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        editPanel.add(undoButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        editPanel.add(redoButton, gbc);

        gbc.insets = new Insets(0, 0, 5 , 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(deleteExprSelectionButton, gbc);

        gbc.gridy = 2;
        editPanel.add(resetExprButton, gbc);

        gbc.gridy = 3;
        editPanel.add(editExprButton, gbc);

        //TODO: Actionlisteners

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(editPanel, gbc);
    }

    private void initArithmeticPanel() {
        JPanel arithmeticPanel = new JPanel(new GridBagLayout());
        arithmeticPanel.setBorder(BorderFactory.createTitledBorder("Arithmetic Operators"));

        additionButton = new JButton("Addition");
        addAdditionPlaceHolderButton = new JButton("Add Placeholder");
        subtractionButton = new JButton("Subtraction");
        scalarButton = new JButton("Scalar");

        final Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelScalar = new SpinnerNumberModel(current, min, max, step);

        JSpinner scalarJSpinner = new JSpinner(numberModelScalar);

        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));

        additionButton.setPreferredSize(new Dimension(110, 30));
        additionButton.setMinimumSize(new Dimension(110, 30));
        additionButton.setMaximumSize(new Dimension(110, 30));

        subtractionButton.setPreferredSize(new Dimension(110, 30));
        subtractionButton.setMinimumSize(new Dimension(110, 30));
        subtractionButton.setMaximumSize(new Dimension(110, 30));

        scalarButton.setPreferredSize(new Dimension(110, 30));
        scalarButton.setMinimumSize(new Dimension(110, 30));
        scalarButton.setMaximumSize(new Dimension(110, 30));

        addAdditionPlaceHolderButton.setPreferredSize(new Dimension(150, 30));

        additionButton.addActionListener(actionEvent -> {
            AddExpression addExpr;
            if (currentSelection.getObject() instanceof ArcExpression) {
                Vector<ArcExpression> vExpr = new Vector();
                vExpr.add((ArcExpression) currentSelection.getObject());
                vExpr.add(new PlaceHolderArcExpression());
                addExpr = new AddExpression(vExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                updateSelection(addExpr);
            }
        });

        subtractionButton.addActionListener(actionEvent -> {
            SubtractExpression subExpr = null;
            if (currentSelection.getObject() instanceof PlaceHolderArcExpression) {
                subExpr = new SubtractExpression((PlaceHolderArcExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                updateSelection(subExpr);
            }
            else if (currentSelection.getObject() instanceof SubtractExpression) {
                subExpr = new SubtractExpression((SubtractExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                updateSelection(subExpr);
            }
            else if (currentSelection.getObject() instanceof ScalarProductExpression) {
                subExpr = new SubtractExpression((ScalarProductExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                updateSelection(subExpr);
            } else if (currentSelection.getObject() instanceof NumberOfExpression || currentSelection.getObject() instanceof AddExpression) {
                subExpr = new SubtractExpression((ArcExpression) currentSelection.getObject(), new PlaceHolderArcExpression());
                arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                updateSelection(subExpr);
            }
        });

        scalarButton.addActionListener(actionEvent -> {
            ScalarProductExpression scalarExpr = null;
            Integer value = (Integer)scalarJSpinner.getValue();
            if (currentSelection.getObject() instanceof ArcExpression) {
                scalarExpr = new ScalarProductExpression(value, (ArcExpression) currentSelection.getObject());
                arcExpression = arcExpression.replace(currentSelection.getObject(), scalarExpr);
                updateSelection(scalarExpr);
            }
        });

        addAdditionPlaceHolderButton.addActionListener(actionEvent -> {
            if (currentSelection.getObject() instanceof AddExpression) {
                AddExpression addExpr = (AddExpression) currentSelection.getObject();
                Vector<ArcExpression> vecExpr =  addExpr.getAddExpression();
                vecExpr.add(new PlaceHolderArcExpression());
                addExpr = new AddExpression(vecExpr);
                arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                updateSelection(addExpr);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0,0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        arithmeticPanel.add(additionButton, gbc);

        gbc.gridy = 1;
        arithmeticPanel.add(addAdditionPlaceHolderButton, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        arithmeticPanel.add(separator,gbc);


        gbc.gridy = 3;
        arithmeticPanel.add(subtractionButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        arithmeticPanel.add(scalarJSpinner, gbc);

        gbc.gridx = 1;
        arithmeticPanel.add(scalarButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(arithmeticPanel,gbc);

    }

    private void initNumberExpressionsPanel() {
        JPanel numberExprPanel = new JPanel(new GridBagLayout());
        numberExprPanel.setBorder(BorderFactory.createTitledBorder("Numerical Expressions"));
        colorExpressionComboBoxPanel = new ColorComboboxPanel(colorType, "colors", true) {
            @Override
            public void changedColor(JComboBox[] comboBoxes) {

            }
        };
        colorExpressionComboBoxPanel.removeScrollPaneBorder();
        Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelNumber = new SpinnerNumberModel(current, min, max, step);
        variableCombobox = new JComboBox();
        useVariableCheckBox = new JCheckBox("Use variable");
        useVariableCheckBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED){
                colorExpressionComboBoxPanel.setVisible(false);
                variableCombobox.setVisible(true);
            } else{
                colorExpressionComboBoxPanel.setVisible(true);
                variableCombobox.setVisible(false);
            }
        });
        numberExpressionJSpinner = new JSpinner(numberModelNumber);
        addExpressionButton = new JButton("Add Expression");

        numberExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMaximumSize(new Dimension(50, 27));

        addExpressionButton.setPreferredSize(new Dimension(125, 27));
        addExpressionButton.setMinimumSize(new Dimension(125, 27));
        addExpressionButton.setMaximumSize(new Dimension(125, 27));

        addExpressionButton.addActionListener(actionEvent -> addNumberExpression());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(useVariableCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(numberExpressionJSpinner, gbc);

        gbc.gridx = 1;
        numberExprPanel.add(colorExpressionComboBoxPanel, gbc);
        numberExprPanel.add(variableCombobox, gbc);
        variableCombobox.setVisible(false);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(addExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(numberExprPanel, gbc);
    }

    private void initExprField () {
        exprField = new JTextPane();

        StyledDocument doc = exprField.getStyledDocument();

        //Set alignment to be centered for all paragraphs
        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 14);
        doc.setParagraphAttributes(0, 0, standard, true);

        exprField.setBackground(java.awt.Color.white);

        exprField.setEditable(false);
        exprField.setToolTipText("Tooltip missing");

        JScrollPane exprScrollPane = new JScrollPane(exprField);
        exprScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(880, 80);
        exprScrollPane.setPreferredSize(d);
        exprScrollPane.setMinimumSize(d);

        exprField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!exprField.isEditable()) {
                    updateSelection();
                }
            }
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
                    if(currentSelection.getObject() instanceof ColorExpression){
                        editColorExpressionButton.doClick();
                    }
                }
            }
        });

        exprField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }
        });

        exprField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!exprField.isEditable()) {
                    //TODO: see line 1232 in CTLQueryDialog for impl example.
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 4;
        regularArcExprPanel.add(exprScrollPane, gbc);
    }

    private void addNumberExpression() {
        Vector<ColorExpression> exprVec = new Vector();
        if (!useVariableCheckBox.isSelected()){
            TupleExpression tupleExpression;
            if (colorType instanceof ProductType) {
                Vector<ColorExpression> tempVec = new Vector();
                for (int i = 0; i < colorExpressionComboBoxPanel.getColorTypeComboBoxesArray().length; i++) {
                    ColorExpression expr;
                    if (colorExpressionComboBoxPanel.getAllCheckBoxesArray()[i].isSelected()) {
                        expr = new AllExpression(((dk.aau.cs.model.CPN.Color) colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[i].getSelectedItem()).getColorType());
                    } else {
                        expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[i]
                            .getItemAt(colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[i].getSelectedIndex()));
                    }
                    tempVec.add(expr);
                }
                tupleExpression = new TupleExpression(tempVec);
                exprVec.add(tupleExpression);
            } else {
                ColorExpression expr;
                if (colorExpressionComboBoxPanel.getAllCheckBoxesArray()[0].isSelected()) {
                    expr = new AllExpression(((dk.aau.cs.model.CPN.Color) colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[0].getSelectedItem()).getColorType());
                } else {
                    expr = new UserOperatorExpression((dk.aau.cs.model.CPN.Color) colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[0]
                        .getItemAt(colorExpressionComboBoxPanel.getColorTypeComboBoxesArray()[0].getSelectedIndex()));
                }
                exprVec.add(expr);
            }
        } else{
            Variable var = variableCombobox.getItemAt(variableCombobox.getSelectedIndex());
            VariableExpression varExpr = new VariableExpression(var);
            exprVec.add(varExpr);
        }
        Integer value = (Integer) numberExpressionJSpinner.getValue();
        NumberOfExpression numbExpr = new NumberOfExpression(value, exprVec);
        arcExpression = arcExpression.replace(currentSelection.getObject(), numbExpr);
        updateSelection(numbExpr);
    }

    private void parseExpression(ArcExpression expressionToParse) {
        if (expressionToParse instanceof AddExpression) {
            AddExpression addExpr = new AddExpression(((AddExpression) expressionToParse).getAddExpression());
            arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
            updateSelection(addExpr);
        }
        else if(expressionToParse instanceof SubtractExpression) {
            SubtractExpression subExpr = new SubtractExpression(((SubtractExpression) expressionToParse).getLeftExpression(), ((SubtractExpression) expressionToParse).getRightExpression());
            arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
            updateSelection(subExpr);
        }
        else if (expressionToParse instanceof NumberOfExpression) {
            NumberOfExpression expessionToParse = (NumberOfExpression) expressionToParse;
            NumberOfExpression numbExpr = null;
            numbExpr = new NumberOfExpression(expessionToParse.getNumber(), expessionToParse.getNumberOfExpression());
            arcExpression = arcExpression.replace(currentSelection.getObject(), numbExpr);
            updateSelection(numbExpr);
        }
    }
    private void deleteSelection() {
        if (currentSelection != null) {
            Expression replacement = null;
            if (currentSelection.getObject() instanceof ArcExpression) {
                replacement = getSpecificChildOfProperty(1, currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof ArcExpression) {
                replacement = new PlaceHolderColorExpression();
            }
            if (replacement != null) {
                arcExpression = arcExpression.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
            }
        }
    }

    private ArcExpression getSpecificChildOfProperty(int number, Expression property) {
        ExprStringPosition[] children = property.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Expression child = children[i].getObject();
            if (child instanceof ArcExpression) {
                count++;
            }
            if (count == number) {
                return (ArcExpression) child;
            }
        }

        return new PlaceHolderArcExpression();
    }

    public void initColorExpressionButtonsPanel() {
        colorExpressionButtons = new JPanel(new GridBagLayout());
        colorExpressionButtons.setBorder(BorderFactory.createTitledBorder("Misc"));
        colorExpressionButtons.setPreferredSize(new Dimension(300 ,158 ));

        ButtonGroup expressionButtonsGroup = new ButtonGroup();
        predButton = new JButton("Add Pred");
        succButton = new JButton("Add Succ");

        predButton.setPreferredSize(new Dimension(130 , 27));
        predButton.setMinimumSize(new Dimension(130 , 27));
        predButton.setMaximumSize(new Dimension(130 , 27));
        succButton.setPreferredSize(new Dimension(130 , 27));
        succButton.setMinimumSize(new Dimension(130 , 27));
        succButton.setMaximumSize(new Dimension(130 , 27));


        expressionButtonsGroup.add(predButton);
        expressionButtonsGroup.add(succButton);

        predButton.addActionListener(actionEvent -> {
            PredecessorExpression predExpr;
            if (currentSelection.getObject() instanceof ColorExpression) {
                predExpr = new PredecessorExpression((ColorExpression) currentSelection.getObject());
                arcExpression = arcExpression.replace(currentSelection.getObject(), predExpr);
                updateSelection(predExpr);
            }
        });

        succButton.addActionListener(actionEvent -> {
            SuccessorExpression succExpr;
            if (currentSelection.getObject() instanceof  ColorExpression) {
                succExpr = new SuccessorExpression((ColorExpression) currentSelection.getObject());
                arcExpression = arcExpression.replace(currentSelection.getObject(), succExpr);
                updateSelection(succExpr);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        colorExpressionButtons.add(predButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        colorExpressionButtons.add(succButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        regularArcExprPanel.add(colorExpressionButtons, gbc);
    }

    private void initExpr() {
        if (!isTransportArc && isInputArc) {
            TimedInputArc inputArc = ((TimedInputArcComponent) objectToBeEdited).underlyingTimedInputArc();
            if (inputArc.getArcExpression() != null) {
                arcExpression = new PlaceHolderArcExpression();
                updateSelection(arcExpression);
                parseExpression(inputArc.getArcExpression());
            } else {
                arcExpression = new PlaceHolderArcExpression();
                exprField.setText(arcExpression.toString());
            }
        } else if (!isTransportArc) {
            if ((((TimedOutputArcComponent) objectToBeEdited).underlyingArc()).getExpression() != null) {
                arcExpression = new PlaceHolderArcExpression();
                updateSelection(arcExpression);
                parseExpression((((TimedOutputArcComponent) objectToBeEdited).underlyingArc()).getExpression());
            } else {
                arcExpression = new PlaceHolderArcExpression();
                exprField.setText(arcExpression.toString());
            }
        }else {
            TransportArc transportArc = ((TimedTransportArcComponent) objectToBeEdited).underlyingTransportArc();
            if (isInputArc) {
                if (transportArc.getInputExpression() != null) {
                    arcExpression = new PlaceHolderArcExpression();
                    updateSelection(arcExpression);
                    parseExpression(transportArc.getInputExpression());
                } else {
                    arcExpression = new PlaceHolderArcExpression();
                    exprField.setText(arcExpression.toString());
                }
            } else {
                if (transportArc.getOutputExpression() != null) {
                    arcExpression = new PlaceHolderArcExpression();
                    updateSelection(arcExpression);
                    parseExpression(transportArc.getOutputExpression());
                } else {
                    arcExpression = new PlaceHolderArcExpression();
                    exprField.setText(arcExpression.toString());
                }
            }
        }
    }
    private void updateSelection() {
        int index = exprField.getCaretPosition();
        ExprStringPosition position = arcExpression.objectAt(index);
        Logger.log(position.getObject().toString());

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        toggleEnabledButtons();
    }

    private void updateSelection(Expression newSelection) {
        exprField.setText(arcExpression.toString());

        ExprStringPosition position;
        if (arcExpression.containsPlaceHolder()) {
            Expression ae = arcExpression.findFirstPlaceHolder();
            position = arcExpression.indexOf(ae);
        }
        else {
            position = arcExpression.indexOf(newSelection);
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        toggleEnabledButtons();

    }
    private void toggleEnabledButtons() {
        if(currentSelection == null){
            allExpressionButton.setEnabled(false);
            addExpressionButton.setEnabled(false);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
            scalarButton.setEnabled(false);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
        }
        if (currentSelection.getObject() instanceof ColorExpression) {
            addExpressionButton.setEnabled(false);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
            scalarButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
            succButton.setEnabled(true);
            predButton.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof AddExpression) {
            addExpressionButton.setEnabled(false);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
            scalarButton.setEnabled(false);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof ArcExpression) {
            addExpressionButton.setEnabled(true);
            additionButton.setEnabled(true);
            subtractionButton.setEnabled(true);
            scalarButton.setEnabled(true);
            succButton.setEnabled(false);
            predButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
        }

    }
    public void onOkColored() {
        if (isInputArc) {
            if (isTransportArc) {
                int weight =  Integer.parseInt(colorExpressionWeightSpinner.getValue().toString());
                TransportArc transportArc = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc();
                ArcExpression inputExpression = getTransportExpression(inputPanel.getColorExpression(), weight);
                ArcExpression outputExpression = getTransportExpression(outputPanel.getColorExpression(), weight);
                transportArc.setInputExpression(inputExpression);
                transportArc.setOutputExpression(outputExpression);
                /*objectToBeEditedTransport.setUnderlyingArc(transportArc);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setInputExpression(inputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setOutputExpression(outputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setCtiList(getctiList());*/
                transportArc.setColorTimeIntervals(getctiList());
                ((TimedTransportArcComponent) objectToBeEdited).updateLabel(false);
            } else {
                TimedInputArc inputArc = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc();
                ArcExpression arcExpression = this.arcExpression;
                inputArc.setExpression(arcExpression);
                /*objectToBeEditedInput.setUnderlyingArc(inputArc);
                ((ColoredInputArc)((ColoredInputArcComponent)petriNetObject).underlyingTimedInputArc()).setExpression(arcExpression);
                ((ColoredInputArc)((ColoredInputArcComponent)petriNetObject).underlyingTimedInputArc()).setColorTimeIntervals(getctiList());*/
                inputArc.setColorTimeIntervals(getctiList());
                ((TimedInputArcComponent) objectToBeEdited).updateLabel(false);
            }
        } else {
            if(isTransportArc){
                int weight =  Integer.parseInt(colorExpressionWeightSpinner.getValue().toString());
                TransportArc transportArc = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc();
                ArcExpression inputExpression = getTransportExpression(inputPanel.getColorExpression(), weight);
                ArcExpression outputExpression = getTransportExpression(outputPanel.getColorExpression(), weight);
                transportArc.setInputExpression(inputExpression);
                transportArc.setOutputExpression(outputExpression);
                /*objectToBeEditedTransport.setUnderlyingArc(transportArc);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setInputExpression(inputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setOutputExpression(outputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setCtiList(getctiList());*/
                transportArc.setColorTimeIntervals(getctiList());
                ((TimedTransportArcComponent) objectToBeEdited).updateLabel(false);
            } else{
                ArcExpression arcExpression = this.arcExpression;
                TimedOutputArc outputArc = ((TimedOutputArcComponent)objectToBeEdited).underlyingArc();
                ((TimedOutputArcComponent) objectToBeEdited).setUnderlyingArc(outputArc);
                outputArc.setExpression(arcExpression);
            }
        }
    }

    private ArcExpression getTransportExpression(ColorExpression colorExpr, int weight) {
        ArcExpression expr;
        Vector<ColorExpression> vecColorExpr = new Vector<>();
        if (colorExpr instanceof TupleExpression) { // we have to use TupleExpression if we want the colorExpressionPanel inside arcPanel when it is transport. IF the tuple only have one element we extract it to remove an unnecessary expression and parentheses
            if (((TupleExpression) colorExpr).getColors().size() == 1) {
                colorExpr = ((TupleExpression) colorExpr).getColors().firstElement();
            }
        }
        vecColorExpr.add(colorExpr);
        expr = new NumberOfExpression(weight, vecColorExpr);
        return expr;
    }

    private void setTimeConstraints() {
        List<ColoredTimeInterval> timeIntervals;

        if (isTransportArc){
            timeIntervals = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals();
        }
        else if(isInputArc){
            timeIntervals = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals();
        } else{
            return;
        }
        for (ColoredTimeInterval timeInterval : timeIntervals) {
            timeConstraintListModel.addElement(timeInterval);
        }
    }

    public DefaultListModel getTimeConstraintModel() {return timeConstraintListModel;}
    private List<ColoredTimeInterval> getctiList() {
        List<ColoredTimeInterval> ctiList = new ArrayList<ColoredTimeInterval>();
        for (int i = 0; i < getTimeConstraintModel().size(); i++) {
            ctiList.add((ColoredTimeInterval) getTimeConstraintModel().get(i));
        }
        return ctiList;
    }

    private void getVariables(){
        for (Variable element : context.network().variables()) {
            if (element.getColorType().getName().equals(colorType.getName())) {
                variableCombobox.addItem(element);
            }
        }
    }

    private ColorType colorType;
    private JPanel regularArcExprPanel;
    JPanel nonDefaultArcColorIntervalPanel;
    JPanel transportWeightPanel;
    JTabbedPane transportExprTabbedPane;
    DefaultListModel timeConstraintListModel;
    JList timeConstraintList;
    private ExprStringPosition currentSelection = null;
    JSpinner numberExpressionJSpinner;
    JSpinner allExpressionJSpinner;
    private ArcExpression arcExpression;
    private JTextPane exprField;
    JButton allExpressionButton;
    JButton editColorExpressionButton;
    JButton addTimeConstraintButton;
    JButton removeTimeConstraintButton;
    JButton editTimeConstraintButton;
    JButton deleteExprSelectionButton;
    JButton resetExprButton;
    JButton undoButton;
    JButton redoButton;
    JButton editExprButton;
    JButton additionButton;
    JButton addAdditionPlaceHolderButton;
    JButton subtractionButton;
    JButton scalarButton;
    JButton addExpressionButton;
    SpinnerNumberModel numberModel;
    ColorExpressionDialogPanel inputPanel;
    ColorExpressionDialogPanel outputPanel;
    JSpinner colorExpressionWeightSpinner;
    ColorComboboxPanel colorIntervalComboboxPanel;
    ColoredTimeIntervalDialogPanel intervalEditorPanel;
    JPanel expressionPanel;


    JPanel colorExpressionButtons;
    JButton predButton;
    JButton succButton;
    ColorComboboxPanel colorExpressionComboBoxPanel;

    JCheckBox useVariableCheckBox;
    JComboBox<Variable> variableCombobox;

}
