package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.undo.UndoManager;
import pipe.gui.widgets.EscapableDialog;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.util.Require;

public class SharedPlacesAndTransitionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final String TRANSITIONS = "Transitions";
	private static final String PLACES = "Places";

	private JList list;
	private SharedPlacesListModel sharedPlacesListModel;
	private SharedTransitionsListModel sharedTransitionsListModel;
	private JComboBox placesTransitionsComboBox;
	private UndoManager undoManager;

	public SharedPlacesAndTransitionsPanel(TimedArcPetriNetNetwork network, UndoManager undoManager){
		Require.that(network != null, "network cannot be null");
		Require.that(undoManager != null, "undoManager cannot be null");
		this.undoManager = undoManager;

		sharedPlacesListModel = new SharedPlacesListModel(network);
		sharedTransitionsListModel = new SharedTransitionsListModel(network);

		setLayout(new BorderLayout());
		initComponents();

		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Shared"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)
		));		
	}

	private void initComponents() {
		list = new JList();
		JScrollPane scrollPane = new JScrollPane(list);

		placesTransitionsComboBox = new JComboBox(new String[]{ PLACES, TRANSITIONS });
		placesTransitionsComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox source = (JComboBox)e.getSource();
				String selectedItem = (String)source.getSelectedItem();
				if(selectedItem.equals(PLACES)){
					list.setModel(sharedPlacesListModel);
				}else if(selectedItem.equals(TRANSITIONS)){
					list.setModel(sharedTransitionsListModel);
				}
			}		
		});
		placesTransitionsComboBox.setSelectedIndex(0); // Sets up the proper list model

		JPanel buttonPanel = new JPanel();
		JButton renameButton = new JButton("Rename");
		JButton removeButton = new JButton("Remove");
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				showSharedTransitionNameDialog();
			}		
		});

		buttonPanel.add(renameButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(addButton);

		add(placesTransitionsComboBox, BorderLayout.PAGE_START);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	private boolean isDisplayingTransitions(){
		return placesTransitionsComboBox.getSelectedItem().equals(TRANSITIONS);
	}

	private void showSharedTransitionNameDialog() {
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);
		Container contentPane = guiDialog.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel panel = isDisplayingTransitions() ? new SharedTransitionNamePanel(guiDialog.getRootPane(), sharedTransitionsListModel, undoManager) : new SharedPlaceNamePanel(guiDialog.getRootPane(), sharedPlacesListModel, undoManager);
		contentPane.add(panel);

		guiDialog.setResizable(false);
		guiDialog.pack();
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);
	}

	public class SharedPlacesListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedPlacesListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
		}

		public Object getElementAt(int index) {
			return network.getSharedPlaceByIndex(index);
		}

		public int getSize() {
			return network.numberOfSharedPlaces();
		}

		public void addElement(SharedPlace place){
			network.add(place);
			fireIntervalAdded(this, network.numberOfSharedPlaces()-1, network.numberOfSharedPlaces());
		}

		public void removeElement(SharedPlace place) {
			network.remove(place);
			fireContentsChanged(this, 0, getSize()+1);
		}
	}

	public class SharedTransitionsListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;
		private TimedArcPetriNetNetwork network;

		public SharedTransitionsListModel(TimedArcPetriNetNetwork network){
			Require.that(network != null, "network must not be null");
			this.network = network;
		}

		public Object getElementAt(int index) {
			return network.getSharedTransitionByIndex(index);
		}

		public int getSize() {
			return network.numberOfSharedTransitions();
		}

		public void addElement(SharedTransition transition){
			network.add(transition);
			fireIntervalAdded(this, network.numberOfSharedTransitions()-1, network.numberOfSharedTransitions());
		}

		public void removeElement(SharedTransition transition) {
			network.remove(transition);
			fireContentsChanged(this, 0, getSize()+1);
		}
	}
}
