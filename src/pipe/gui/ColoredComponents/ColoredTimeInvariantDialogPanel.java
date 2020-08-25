package pipe.gui.ColoredComponents;

import dk.aau.cs.gui.Context;
import dk.aau.cs.model.CPN.ColoredTimeInvariant;
import dk.aau.cs.model.tapn.*;
import net.tapaal.swinghelpers.CustomJSpinner;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import pipe.gui.widgets.WidthAdjustingComboBox;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

public abstract class ColoredTimeInvariantDialogPanel extends JPanel {

    private JPanel timeInvariantPanel;
    private JPanel invariantGroup;
    private JComboBox invRelationNormal;
    private JComboBox invRelationConstant;
    private CustomJSpinner invariantSpinner;
    private JCheckBox invariantInf;
    private JComboBox invConstantsComboBox;
    private JRadioButton normalInvRadioButton;
    private JRadioButton constantInvRadioButton;

    private JPanel buttonPanel;
    private JButton okButton;
    private JButton cancelButton;

    private int maxNumberOfPlacesToShowAtOnce = 20;

    private JRootPane rootPane;
    private Context context;
    private ColoredTimeInvariant oldTimeInvariant;
    private ColoredTimeInvariant newTimeInvariant;
    private TimedPlaceComponent place;
    public boolean didEdit;

    public ColoredTimeInvariantDialogPanel(JRootPane rootPane, Context context, ColoredTimeInvariant cti, TimedPlaceComponent place) {
        this.context = context;
        this.oldTimeInvariant = cti;
        this.place = place;
        this.rootPane = rootPane;
        initPanel();
    }

    private void initPanel() {
        initInvariantPanel();
        setTimeInvariantVariables();
    }

    public ColoredTimeInvariant getNewTimeInvariant() {return newTimeInvariant;}

    private void setTimeInvariantVariables() {
        if (oldTimeInvariant.upperBound().value() == -1) {
            invariantInf.setSelected(true);
            invariantCheckedEvent();
        } else {
            invariantInf.setSelected(false);
            invariantSpinner.setValue(oldTimeInvariant.upperBound().value());
            invariantCheckedEvent();
        }
    }

    private void setRelationModelForConstants() {
        int value = CreateGui.getCurrentTab().network().getConstantValue(invConstantsComboBox.getSelectedItem().toString());

        String selected = invRelationConstant.getSelectedItem().toString();
        if (value == 0) {
            invRelationConstant.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
        } else {
            invRelationConstant.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
        }
        invRelationConstant.setSelectedItem(selected);
    }

    private void enableConstantInvariantComponents() {
        invRelationConstant.setEnabled(true);
        invConstantsComboBox.setEnabled(true);
        setRelationModelForConstants();
    }

    private void enableNormalInvariantComponents() {
        invRelationNormal.setEnabled(true);
        invariantInf.setEnabled(true);
        invariantSpinner.setValue(0);
        invariantInf.setSelected(true);
        invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
    }

    private void disableInvariantComponents() {
        invRelationNormal.setEnabled(false);
        invRelationConstant.setEnabled(false);
        invariantSpinner.setEnabled(false);
        invConstantsComboBox.setEnabled(false);
        invariantInf.setEnabled(false);
    }

    protected boolean isUrgencyOK(){
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

    private void invariantCheckedEvent() {
        if(!isUrgencyOK()){
            invariantInf.setSelected(true);
            return;
        }
        if (!invariantInf.isSelected()) {
            invRelationNormal.setEnabled(true);
            invariantSpinner.setEnabled(true);
            invRelationNormal.setSelectedItem("<=");
            if ((Integer) invariantSpinner.getValue() < 1) {
                invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
            } else {
                invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
            }
        } else {
            invRelationNormal.setEnabled(false);
            invariantSpinner.setEnabled(false);
            invRelationNormal.setSelectedItem("<");
            invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
        }

    }

    private void initInvariantPanel() {
        timeInvariantPanel = new JPanel();
        timeInvariantPanel.setLayout(new GridBagLayout());
        timeInvariantPanel.setBorder(BorderFactory.createTitledBorder("Age Invariant"));

        invariantGroup = new JPanel(new GridBagLayout());
        invRelationNormal = new JComboBox(new String[] { "<=", "<" });
        invRelationConstant = new JComboBox(new String[] { "<=", "<" });
        //invariantSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        invariantSpinner = new CustomJSpinner(0); //TODO: missing okButton as parameter, mby?
        invariantSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(!invariantInf.isSelected()){
                    if ((Integer) invariantSpinner.getValue() < 1) {
                        invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
                        invRelationNormal.setSelectedItem("<=");
                    } else if (invRelationNormal.getModel().getSize() == 1) {
                        invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
                    }
                }
            }

        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        invariantGroup.add(invRelationNormal, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        invariantGroup.add(invRelationConstant, gbc);

        //	invariantSpinner.setMaximumSize(new Dimension(100, 30));
        //  invariantSpinner.setMinimumSize(new Dimension(230, 30));
        //invariantSpinner.setPreferredSize(new Dimension(230, 30));

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);
        invariantGroup.add(invariantSpinner, gbc);

        invariantInf = new JCheckBox("inf");
        invariantInf.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                invariantCheckedEvent();
            }

        });
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        invariantGroup.add(invariantInf, gbc);

        Set<String> constants = context.network().getConstantNames();
        String[] constantArray = constants.toArray(new String[constants.size()]);
        Arrays.sort(constantArray, String.CASE_INSENSITIVE_ORDER);

        invConstantsComboBox = new WidthAdjustingComboBox(maxNumberOfPlacesToShowAtOnce);
        invConstantsComboBox.setModel(new DefaultComboBoxModel(constantArray));
        //	invConstantsComboBox = new JComboBox(constants.toArray());
        invConstantsComboBox.setMaximumRowCount(20);
        //	invConstantsComboBox.setMinimumSize(new Dimension(100, 30));
        invConstantsComboBox.setPreferredSize(new Dimension(230, 30));
        invConstantsComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setRelationModelForConstants();
                }
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        invariantGroup.add(invConstantsComboBox, gbc);

        normalInvRadioButton = new JRadioButton("Normal");
        normalInvRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disableInvariantComponents();
                enableNormalInvariantComponents();
            }
        });

        constantInvRadioButton = new JRadioButton("Constant");
        constantInvRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disableInvariantComponents();
                enableConstantInvariantComponents();
            }
        });
        if (constants.isEmpty()){
            constantInvRadioButton.setEnabled(false);
        }
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(normalInvRadioButton);
        btnGroup.add(constantInvRadioButton);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        invariantGroup.add(normalInvRadioButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        invariantGroup.add(constantInvRadioButton, gbc);

        setInvariant(oldTimeInvariant);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        timeInvariantPanel.add(invariantGroup, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(timeInvariantPanel, gridBagConstraints);
    }

    public abstract void placeHolder();

    public ColoredTimeInvariant getInvariant(){
        ColoredTimeInvariant cti;
        double spinnerValue = (Integer) invariantSpinner.getValue();
        Bound bound = new IntBound((int)spinnerValue);
        if(constantInvRadioButton.isSelected()) {
            boolean nonStrict = "<=".equals(invRelationConstant.getSelectedItem());
            Constant constant = context.network().getConstant((String)invConstantsComboBox.getSelectedItem());
            cti = new ColoredTimeInvariant(nonStrict, new ConstantBound(constant), oldTimeInvariant.getColor());
        }else {
            if (invariantInf.isSelected())
                cti = new ColoredTimeInvariant(false, Bound.Infinity, oldTimeInvariant.getColor());
            else {
                if ("<=".equals(invRelationNormal.getSelectedItem()))
                    cti = new ColoredTimeInvariant(true, bound, oldTimeInvariant.getColor());
                else
                    cti = new ColoredTimeInvariant(false, bound, oldTimeInvariant.getColor());
            }

        }
        return cti;
    }

    public void setInvariant(ColoredTimeInvariant invariantToSet){
        oldTimeInvariant = invariantToSet;
        if (invariantToSet.isUpperNonstrict()) {
            invRelationNormal.setSelectedItem("<=");
        } else {
            invRelationNormal.setSelectedItem("<");
        }

        if (invariantToSet.upperBound() instanceof Bound.InfBound) {
            invariantSpinner.setEnabled(false);
            invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<" }));
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
                    invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=" }));
                } else {
                    invRelationNormal.setModel(new DefaultComboBoxModel(new String[] { "<=", "<" }));
                }
                invariantSpinner.setValue(invariantToSet.upperBound().value());
                invariantSpinner.setEnabled(true);
                invRelationNormal.setSelectedItem(invariantToSet.isUpperNonstrict() ? "<=" : "<");
                invariantInf.setSelected(false);
            }
        }
    }
}
