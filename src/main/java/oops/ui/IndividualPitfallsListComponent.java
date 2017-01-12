package oops.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.protege.editor.core.ui.util.Resettable;
import org.protege.editor.core.util.HandlerRegistration;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.selection.SelectionPlane;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.ElementPair;
import oops.model.EvaluationResult;
import oops.model.Pitfall;
import oops.model.PitfallImportanceLevel;


/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * A custom Protégé view component for the OOPS! plugin that shows the detected pitfalls for the selected element.
 */
public class IndividualPitfallsListComponent extends AbstractOWLViewComponent
		implements Resettable, SelectionPlane, OWLSelectionModelListener, EvaluationListener {

    public static final String ID = "org.protege.editor.owl.SelectedEntityView";

    private final CardLayout cardLayout = new CardLayout();

    private final JPanel cardPanel = new JPanel();

    private static final String BLANK_PANEL_ID = "Blank";
    private static final String BLANK_PANEL_LABEL = "Select an element to see its pitfalls";
    
    private static final String EVALUATION_PENDING_PANEL_ID = "Evaluation_pending";
    private static final String EVALUATION_PENDING_PANEL_LABEL = "Evaluate the ontology to see its pitfalls";
    
    private static final String PITFALLS_PANEL_ID = "Pitfalls";
    
    private static final String TEXT_SELECT_PITFALL = "Please select a pitfall to see its details";

    private static final Logger logger = LoggerFactory.getLogger(IndividualPitfallsListComponent.class);

    private JLabel pitfallsListLabel;
    
    private JPanel selectedItemPitfallsCard;
    
    private JTree pitfallsTree;
    
    private JPanel pitfallDetails;
    
    private JTextArea pitfallDetailsTextArea;
    
    private JScrollPane pitfallsTreeView, pitfallDetailsView;
    
    private OOPSEvaluator evaluator;
    
    private EvaluationResult evaluationResult;
    
    private OWLEntity selEntity;
    private String selectedEntityIRI;
    
    List<Pitfall> detectedPitfalls;

    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        
        pitfallsListLabel = new JLabel();
        pitfallsListLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 3, 0));
        add(pitfallsListLabel, BorderLayout.NORTH);
        
        selectedItemPitfallsCard = new JPanel();
        selectedItemPitfallsCard.setLayout(new GridLayout(1, 2));
        
        pitfallsTree = new JTree();
        pitfallsTree.setRootVisible(false); // hide the root node
        pitfallsTree.setCellRenderer(new TreeCellRendererWithTooltip()); // enable custom CellRenderer
        pitfallsTree.addTreeSelectionListener(event -> {
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode) pitfallsTree.getLastSelectedPathComponent();
        	
        	// if nothing is selected
        	if (node == null) {
        		pitfallDetailsTextArea.setText(TEXT_SELECT_PITFALL);
        	} else {
            	String nodeText = node.getUserObject().toString();
            	
            	// if one of the parent nodes is selected
            	if (nodeText.matches("(Minor|Important|Critical).*")) {
            		pitfallDetailsTextArea.setText(TEXT_SELECT_PITFALL);
            	} else { // a pitfall is selected
                	Pitfall selectedPitfall = getSelectedPitfall(nodeText);
                	
                	String pitfallDetailsText = getPitfallDetails(selectedPitfall, selEntity);
                	
                	pitfallDetailsTextArea.setText(pitfallDetailsText);
                	pitfallDetailsTextArea.setCaretPosition(0); // scroll back to top
            	}
        	}
        });
        pitfallsTreeView = new JScrollPane(pitfallsTree);
        pitfallsTreeView.getVerticalScrollBar().setUnitIncrement(16); // increase scrolling speed to a more usual pace
        
        pitfallDetails = new JPanel();
        pitfallDetails.setLayout(new BorderLayout());
        pitfallDetails.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        pitfallDetailsTextArea = new JTextArea();
        pitfallDetailsTextArea.setEditable(false);
        pitfallDetailsTextArea.setFont(new Font("Serif", Font.PLAIN, 14));
        pitfallDetailsTextArea.setLineWrap(true);
        pitfallDetailsTextArea.setWrapStyleWord(true);
        pitfallDetailsTextArea.setText("Please select a pitfall to see its details");
        pitfallDetails.add(pitfallDetailsTextArea, BorderLayout.CENTER);
        pitfallDetails.setBackground(Color.WHITE);
        
        pitfallDetailsView = new JScrollPane(pitfallDetails);
        pitfallDetailsView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pitfallDetailsView.getVerticalScrollBar().setUnitIncrement(16); // increase scrolling speed to a more usual pace
        
        selectedItemPitfallsCard.add(pitfallsTreeView);
        selectedItemPitfallsCard.add(pitfallDetailsView);
        
        add(cardPanel, BorderLayout.CENTER);
        cardPanel.setLayout(cardLayout);
        
        cardPanel.add(new CenteredMessagePanel(EVALUATION_PENDING_PANEL_LABEL), EVALUATION_PENDING_PANEL_ID);
        cardPanel.add(new CenteredMessagePanel(BLANK_PANEL_LABEL), BLANK_PANEL_ID);
        
        cardPanel.add(selectedItemPitfallsCard, PITFALLS_PANEL_ID);
        
        evaluator = OOPSEvaluator.getInstance();
        
        evaluator.addListener(this);
        
        getOWLWorkspace().getOWLSelectionModel().addListener(this);
        getView().setShowViewBar(false); // disable view label bar
        selectionChanged();
    }

	/**
	 * Returns a detailed description for the selected Pitfall
	 * 
	 * @param selectedPitfall
	 *            the selected pitfall in the selected element's pitfalls view
	 * @param selEntity
	 *            the selected entity in the hierarchy view
	 * @return a detailed description for the selected Pitfall
	 */
	private String getPitfallDetails(Pitfall selectedPitfall, OWLEntity selEntity) {
    	String pitfallDetailsText = selectedPitfall.getDescription();
    	
    	List<ElementPair> additionalInfoElements = null;
    	
    	switch (selectedPitfall.getPitfallID()) {
    	case OOPSEvaluator.PITFALL_EQUIVALENT_CLASSES_ID:
    		additionalInfoElements = evaluationResult.getEquivalentClasses();
    		pitfallDetailsText += "\n\nThis class might be equivalent to the following classes:\n";
    		break;
    	case OOPSEvaluator.PITFALL_MIGHT_BE_EQUIVALENT_ID:
    		if (selEntity.getEntityType() == EntityType.OBJECT_PROPERTY) {
    			additionalInfoElements = evaluationResult.getEquivalentRelations();
    			pitfallDetailsText += "\n\nThis relation might be equivalent to the following elements:\n";
    			
    		} else if (selEntity.getEntityType() == EntityType.DATA_PROPERTY) {
    			additionalInfoElements = evaluationResult.getEquivalentAttributes();
    			pitfallDetailsText += "\n\nThis attribute might be equivalent to the following elements:\n";
    		}
    		break;
    	case OOPSEvaluator.PITFALL_MIGHT_BE_INVERSE_ID:
    		additionalInfoElements = evaluationResult.getMightBeInverseRelations();
    		
    		if (additionalInfoElements != null && additionalInfoElements.size() > 0) {
    			pitfallDetailsText += "\n\nThis relation could be inverse of:\n";
    		}
    		
    		break;
    	case OOPSEvaluator.PITFALL_SAME_LABEL:
    		additionalInfoElements = evaluationResult.getElementsWithSameLabel();
    		pitfallDetailsText += "\n\nThis element has the same label as:\n";
    		break;
    	case OOPSEvaluator.PITFALL_WRONG_INVERSE_ID:
    		additionalInfoElements = evaluationResult.getWrongInverseRelations();
    		pitfallDetailsText += "\n\nThis relation may not be inverse of:\n";
    		break;
    	}
    	
    	if (additionalInfoElements != null && additionalInfoElements.size() > 0) {
    		List<ElementPair> relatedPairs = additionalInfoElements.stream()
    				.filter(pair -> 
    					pair.getElementA().equals(selectedEntityIRI) || 
    					pair.getElementB().equals(selectedEntityIRI))
    				.collect(Collectors.toList());
    		
    		for (ElementPair relatedPair : relatedPairs) {
    			String equivalentClass = relatedPair.getElementA().equals(selectedEntityIRI) ?
    					relatedPair.getElementB() : relatedPair.getElementA();
    			pitfallDetailsText += ">   " + equivalentClass + "\n";
    		}    		
    	}
		
    	// if it's the pitfall P13, check if it has suggestions for elements without inverse relationships
    	if (selectedPitfall.getPitfallID().equals(OOPSEvaluator.PITFALL_MIGHT_BE_INVERSE_ID)) {
    		List<String> noInverseSuggestions = evaluationResult.getRelationsWithoutInverse();
    		
    		if (noInverseSuggestions != null && noInverseSuggestions.size() > 0) {
    			Optional<String> noInverseSuggestion = noInverseSuggestions.stream()
    				.filter(element -> element.equals(selectedEntityIRI))
    				.findFirst();
    			
    			if (noInverseSuggestion.isPresent()) {
    				pitfallDetailsText += "\n\nSorry, OOPS! has no suggestion for this relationship without inverse.";
    			}
    		}
    	}
    	
    	return pitfallDetailsText;
	}

	/**
	 * Gets the selected pitfall in the individual pitfalls list
	 * 
	 * @param nodeText
	 *            the text of the selected node in the pitfalls tree
	 * @return the selected pitfall in the individual pitfalls list
	 */
    private Pitfall getSelectedPitfall(String nodeText) {
    	return detectedPitfalls.stream()
    		.filter(p -> nodeText.startsWith(p.getPitfallID()))
    		.findFirst()
    		.get();
	}


	public void reset() {
        pitfallsTree.removeAll();
        pitfallsListLabel.setText("");
        validate();
        
        logger.info("IndividualPitfallsListComponent received reset event!!");
    }

    private void selectPanel(String name) {
        cardLayout.show(cardPanel, name);
    }

    protected void disposeOWLView() {
        evaluator.removeListener(this);
    }

    @Override
    public HandlerRegistration registerSelectionDriver(SelectionDriver driver) {
        return () -> {};
    }

    @Override
    public void transmitSelection(SelectionDriver driver, OWLObject selection) {
        // Since we display the current selection we don't initiate selection changes.  If a user drops a navigation
        // driving view on to this card view then it's probably an error.
        logger.debug("[SelectedEntityCardView] Ignoring request to transmit selection");
    }

	@Override
	public void selectionChanged() {
        OWLObject selectedObject = getOWLWorkspace().getOWLSelectionModel().getSelectedObject();
        if(selectedObject == null) {
            pitfallsListLabel.setIcon(null);
            pitfallsListLabel.setText("");
            pitfallsListLabel.setBackground(null);
            selectPanel((evaluationResult != null) ? BLANK_PANEL_ID : EVALUATION_PENDING_PANEL_ID);
            return;
        }
        
        if(!(selectedObject instanceof OWLEntity)) {
            return;
        }
        
        selectPanel((evaluationResult != null) ? PITFALLS_PANEL_ID : EVALUATION_PENDING_PANEL_ID);
        
        selEntity = (OWLEntity) selectedObject;
        
        if (evaluationResult != null) {
        	selectedEntityIRI = selEntity.getIRI().toString();
            
            DefaultMutableTreeNode top = new DefaultMutableTreeNode();
            DefaultMutableTreeNode minor = new DefaultMutableTreeNode("Minor (0 items)");
            DefaultMutableTreeNode important = new DefaultMutableTreeNode("Important (0 items)");
            DefaultMutableTreeNode critical = new DefaultMutableTreeNode("Critical (0 items)");
            
            top.add(minor);
            top.add(important);
            top.add(critical);
            
            detectedPitfalls = evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI);
            
    		if (detectedPitfalls != null) {
    			for (Pitfall pitfall : detectedPitfalls) {
    				logger.info("Adding pitfall: " + pitfall + " for the entity " + selectedEntityIRI);
    				switch (pitfall.getImportanceLevel()) {
    				case MINOR:
    					minor.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getName())));
    					break;
    				case IMPORTANT:
    					important.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getName())));
    					break;
    				case CRITICAL:
    					critical.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getName())));
    					break;
    				}
    			}
    			
    			long numMinorPitfalls = detectedPitfalls.stream()
    					.filter(p -> p.getImportanceLevel() == PitfallImportanceLevel.MINOR).count();
    			long numImportantPitfalls = detectedPitfalls.stream()
    					.filter(p -> p.getImportanceLevel() == PitfallImportanceLevel.IMPORTANT).count();
    			long numCriticalPitfalls = detectedPitfalls.stream()
    					.filter(p -> p.getImportanceLevel() == PitfallImportanceLevel.CRITICAL).count();

    			minor.setUserObject(String.format("Minor (%d item%s)", numMinorPitfalls, numMinorPitfalls != 1 ? "s" : ""));
    			important.setUserObject(String.format("Important (%d item%s)", numImportantPitfalls, numImportantPitfalls != 1 ? "s" : ""));
    			critical.setUserObject(String.format("Critical (%d item%s)", numCriticalPitfalls, numCriticalPitfalls != 1 ? "s" : ""));
    		}
            
            pitfallsTree.setModel(new DefaultTreeModel(top));
        }
        
	}
	
    private static class TreeCellRendererWithTooltip extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            setToolTipText("foobar" + row);
            return super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
        }
    }

	@Override
	public void onEvaluationStarted() {
		logger.info("IndividualPitfallsList received evaluation start event!!");
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				pitfallsTree.setEnabled(false); // disable the pitfalls tree during evaluation
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void onEvaluationDone(EvaluationResult result) {
		logger.info("IndividualPitfallsList received evaluation results!!");
		
		evaluationResult = result;
		
        int minorPitfalls = evaluationResult.getNumberOfPitfalls(PitfallImportanceLevel.MINOR);
        int importantPitfalls = evaluationResult.getNumberOfPitfalls(PitfallImportanceLevel.IMPORTANT);
        int criticalPitfalls = evaluationResult.getNumberOfPitfalls(PitfallImportanceLevel.CRITICAL);
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				pitfallsTree.setEnabled(true); // re-enable the pitfalls tree
				selectionChanged(); // update view with the selected element
				
				pitfallsListLabel.setText(String.format("Detected pitfalls (total %d critical, %d important, %d minor)",
	            		criticalPitfalls, importantPitfalls, minorPitfalls));
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void OnEvaluationException(Throwable exception) {
		logger.info("IndividualPitfallsList received evaluation exception!!");
		try {
			SwingUtilities.invokeAndWait(() -> {
				pitfallsTree.setEnabled(true); // re-enable the pitfalls tree
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

}
