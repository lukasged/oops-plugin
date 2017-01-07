package oops.ui;

import org.protege.editor.core.ui.util.Resettable;
import org.protege.editor.core.util.HandlerRegistration;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.selection.SelectionPlane;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;
import oops.model.Pitfall;
import oops.model.PitfallImportanceLevel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


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
    
    private static final String INITIAL_ROOT_NOTE_TEXT = "Evaluate the ontology to see its pitfalls";

    private static final Logger logger = LoggerFactory.getLogger(IndividualPitfallsListComponent.class);

    private JLabel pitfallsListLabel;
    
    private JPanel selectedItemPitfallsCard;
    
    private JTree pitfallsTree;
    
    private JScrollPane pitfallsTreeView;
    
    private OOPSEvaluator evaluator;
    
    private EvaluationResult evaluationResult;   

    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        
        pitfallsListLabel = new JLabel();
        pitfallsListLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 3, 0));
        add(pitfallsListLabel, BorderLayout.NORTH);
        
        selectedItemPitfallsCard = new JPanel();
        selectedItemPitfallsCard.setLayout(new BorderLayout());
        
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        DefaultMutableTreeNode evalMessage = new DefaultMutableTreeNode(INITIAL_ROOT_NOTE_TEXT);
        top.add(evalMessage);
        
        pitfallsTree = new JTree(top);
        pitfallsTree.setRootVisible(false); // hide the root node
        pitfallsTree.setCellRenderer(new TreeCellRendererWithTooltip()); // enable custom CellRenderer
        pitfallsTreeView = new JScrollPane(pitfallsTree);
        
        selectedItemPitfallsCard.add(pitfallsTreeView, BorderLayout.CENTER);
        
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
        
        OWLEntity selEntity = (OWLEntity) selectedObject;
        
        if (evaluationResult != null) {
        	String selectedEntityIRI = selEntity.getIRI().toString();
            
            DefaultMutableTreeNode top = new DefaultMutableTreeNode();
            DefaultMutableTreeNode minor = new DefaultMutableTreeNode("Minor (0 items)");
            DefaultMutableTreeNode important = new DefaultMutableTreeNode("Important (0 items)");
            DefaultMutableTreeNode critical = new DefaultMutableTreeNode("Critical (0 items)");
            
            top.add(minor);
            top.add(important);
            top.add(critical);
            
            ArrayList<Pitfall> detectedPitfalls = evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI);
            
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
