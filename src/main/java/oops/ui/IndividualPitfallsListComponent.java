package oops.ui;

import org.protege.editor.core.ui.util.Resettable;
import org.protege.editor.core.ui.view.ViewsPane;
import org.protege.editor.core.ui.view.ViewsPaneMemento;
import org.protege.editor.core.util.HandlerRegistration;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.model.selection.SelectionDriver;
import org.protege.editor.owl.model.selection.SelectionPlane;
import org.protege.editor.owl.ui.renderer.OWLSystemColors;
import org.protege.editor.owl.ui.util.NothingSelectedPanel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.editor.owl.ui.view.EntityBannerFormatter;
import org.protege.editor.owl.ui.view.EntityBannerFormatterImpl;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;
import oops.model.Pitfall;
import oops.model.PitfallImportanceLevel;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


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

    private final List<ViewsPane> viewsPanes = new ArrayList<>();

    private static final String CLASSES_PANEL = "Classes";

    private static final String OBJECT_PROPERTIES_PANEL = "ObjectProperties";

    private static final String DATA_PROPERTIES_PANEL = "DataProperties";

    private static final String ANNOTATION_PROPERTIES_PANEL = "AnnotationProperties";

    private static final String INDIVIDUALS_PANEL = "Individual";

    private static final String DATATYPES_PANEL = "Datatypes";

    private static final String BLANK_PANEL = "Blank";
    
    private static final String INITIAL_ROOT_NOTE_TEXT = "Pitfalls (evaluate the ontology to see its pitfalls)";

    private static final Logger logger = LoggerFactory.getLogger(IndividualPitfallsListComponent.class);

    private JLabel entityIRILabel;

    private EntityBannerFormatter entityBannerFormatter;
    
    private JTree pitfallsTree;
    
    private JScrollPane pitfallsTreeView;
    
    private OOPSEvaluator evaluator;
    
    private EvaluationResult evaluationResult;
    
    private Runnable evaluationTask = () -> {
    	Instant startInstant = Instant.now();
    	logger.info(String.format("evaluationTask[PitfallsListComp] in thread %s", Thread.currentThread().getName()));
    	
    	pitfallsTree.setEnabled(false); // disable the pitfalls tree while evaluating
    	
    	try {
			evaluator.evaluate(getOWLEditorKit().getOWLModelManager().getActiveOntology());
			
			logger.info(String.format("evaluationTask[PitfallsListComp] finished in %d seconds", 
					Duration.between(startInstant, Instant.now()).getSeconds()));
			logger.info("DetectedPitfallsListComponent came back from evaluation!!");
		} catch (InterruptedException e) {
			logger.error("There has been an error while trying to evaluate the ontology");
			logger.error(e.getLocalizedMessage());
		} finally {
			pitfallsTree.setEnabled(true); // re-enable the pitfalls tree after evaluation
		}
    };    

    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());
        entityBannerFormatter = new EntityBannerFormatterImpl();
        entityIRILabel = new JLabel();
        entityIRILabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 3, 0));
        add(entityIRILabel, BorderLayout.NORTH);
        
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(INITIAL_ROOT_NOTE_TEXT);
        
        pitfallsTree = new JTree(top);
        pitfallsTreeView = new JScrollPane(pitfallsTree);
        
        add(pitfallsTreeView);
        
        //add(cardPanel);
        //cardPanel.setLayout(cardLayout);
        //cardPanel.add(new NothingSelectedPanel(), BLANK_PANEL);
        //createViewPanes(false);
        //logger.info("DetectedPitfallsListComponent adding listener!!");
        //OOPSEvaluator.addListener(this);
        
        evaluator = OOPSEvaluator.getInstance();
        
        evaluator.addListener(this);
        
        getOWLWorkspace().getOWLSelectionModel().addListener(this);
        getView().setShowViewBar(false); // disable view label bar
        selectionChanged();
    }

    private void createViewPanes(boolean reset) {
        addPane(CLASSES_PANEL,
                "/selected-entity-view-class-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.classes",
                reset);


        addPane(OBJECT_PROPERTIES_PANEL,
                "/selected-entity-view-objectproperty-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.objectproperties",
                reset);


        addPane(DATA_PROPERTIES_PANEL,
                "/selected-entity-view-dataproperty-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.dataproperties",
                reset);


        addPane(ANNOTATION_PROPERTIES_PANEL,
                "/selected-entity-view-annotationproperty-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.annotproperties",
                reset);


        addPane(INDIVIDUALS_PANEL,
                "/selected-entity-view-individual-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.individuals",
                reset);


        addPane(DATATYPES_PANEL,
                "/selected-entity-view-datatype-panel.xml",
                "org.protege.editor.owl.ui.view.selectedentityview.datatypes",
                reset);
    }


    private void addPane(String panelId, String configFile, String viewPaneId, boolean reset) {
        URL clsURL = getClass().getResource(configFile);
        ViewsPane pane = new ViewsPane(getOWLWorkspace(), new ViewsPaneMemento(clsURL, viewPaneId, reset));
        cardPanel.add(pane, panelId);
        viewsPanes.add(pane);
    }


    public void reset() {
        for (ViewsPane pane : viewsPanes){
            cardPanel.remove(pane);
            pane.dispose();
        }

        viewsPanes.clear();
        pitfallsTree.removeAll();
        entityIRILabel.setText("");
        createViewPanes(true);
        validate();

        for (ViewsPane pane : viewsPanes){
            pane.saveViews();
        }
        
        logger.info("IndividualPitfallsListComponent received reset event!!");
    }

    private void selectPanel(String name) {
        cardLayout.show(cardPanel, name);
    }

    protected void disposeOWLView() {
        for (ViewsPane pane : viewsPanes){
            pane.saveViews();
            pane.dispose();
        }
        
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
            entityIRILabel.setIcon(null);
            entityIRILabel.setText("");
            entityIRILabel.setBackground(null);
            //selectPanel(BLANK_PANEL);
            return;
        }
        if(!(selectedObject instanceof OWLEntity)) {
            return;
        }
        OWLEntity selEntity = (OWLEntity) selectedObject;
        String selectedEntityIRI = selEntity.getIRI().toString();
        String banner = entityBannerFormatter.formatBanner(selEntity, getOWLEditorKit());
        entityIRILabel.setIcon(getOWLWorkspace().getOWLIconProvider().getIcon(selEntity));
        entityIRILabel.setText(banner);
        
        if (evaluationResult != null) {
            DefaultMutableTreeNode top = new DefaultMutableTreeNode("Pitfalls");
            DefaultMutableTreeNode minor = new DefaultMutableTreeNode("Minor (0 items)");
            DefaultMutableTreeNode important = new DefaultMutableTreeNode("Important (0 items)");
            DefaultMutableTreeNode critical = new DefaultMutableTreeNode("Critical (0 items)");
            
            top.add(minor);
            top.add(important);
            top.add(critical);
            
            logger.info(String.format("In processSelections now ...\n evalResult %s null!", evaluationResult == null ? "is" : "isn't"));
            logger.info("selectedEntityIRI -> " + selectedEntityIRI);
            logger.info("evaluationResult -> " + evaluationResult.toString());
            if (evaluationResult != null && evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI) != null) {
                logger.info("getPitfallsForOWLEntity -> " + evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI).toString());
                logger.info("number of pitfalls -> " + evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI).size());
            }
            
            ArrayList<Pitfall> detectedPitfalls = evaluationResult.getPitfallsForOWLEntity(selectedEntityIRI);
            
    		if (detectedPitfalls != null) {
    			for (Pitfall pitfall : detectedPitfalls) {
    				logger.info("Adding pitfall: " + pitfall + " for the entity " + selectedEntityIRI);
    				switch (pitfall.getImportanceLevel()) {
    				case MINOR:
    					minor.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getDescription())));
    					break;
    				case IMPORTANT:
    					important.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getDescription())));
    					break;
    				case CRITICAL:
    					critical.add(new DefaultMutableTreeNode(
    							String.format("%s - %s", pitfall.getPitfallID(), pitfall.getDescription())));
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
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				pitfallsTree.setEnabled(true); // re-enable the pitfalls tree
				selectionChanged(); // update view with the selected element
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
