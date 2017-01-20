package oops.ui;

import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.ui.view.dataproperty.OWLDataPropertyHierarchyViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

import java.lang.reflect.InvocationTargetException;


/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Customized ObjectPropertyHierarchyViewComponent for OOPS! plugin. It sets the custom
 * TreeCellRenderer to let the users find elements with pitfalls quickly and intuitively.
 */
public class OOPSDataPropertyHierarchyViewComponent extends OWLDataPropertyHierarchyViewComponent implements EvaluationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(OOPSDataPropertyHierarchyViewComponent.class);
    
    private OOPSEvaluator evaluator;
    
    private EvaluationResult evaluationResult;
    
    private TreeCellRenderer defaultRenderer;

    @Override
    public void performExtraInitialisation() throws Exception {
    	super.performExtraInitialisation();
        
        evaluator = OOPSEvaluator.getInstance();
        
        evaluator.addListener(this);
        
        defaultRenderer = getTree().getCellRenderer();
        
        // if there already are existent results, update the UI with them
        EvaluationResult existentResults = OOPSEvaluator.getEvaluationResults();
        if (existentResults != null) {
        	onEvaluationDone(existentResults);
        }
        
        getOWLModelManager().addListener(event -> {
            if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
            	reset();
            }
        });
    }
    
	public void reset() {
		evaluationResult = null;
		
		// set the default cell renderer
		if (SwingUtilities.isEventDispatchThread()) {
			getTree().setCellRenderer(defaultRenderer);
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> {
					getTree().setCellRenderer(defaultRenderer);
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
    }

    @Override
    public void disposeView() {
        super.disposeView();
        
        evaluator.removeListener(this);
        evaluationResult = null;
    }

	@Override
	public void onEvaluationStarted() {
		logger.debug("OOPSObjectPropertyHierarchy received evaluation start event!!");
	}

	@Override
	public void onEvaluationDone(EvaluationResult result) {
		evaluationResult = result;

		logger.debug("OOPSObjectPropertyHierarchy received evaluation results!!");
		
		if (SwingUtilities.isEventDispatchThread()) {
			getTree().setCellRenderer(new OOPSTreeCellRenderer(getOWLEditorKit(), evaluationResult));
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> {
					getTree().setCellRenderer(new OOPSTreeCellRenderer(getOWLEditorKit(), evaluationResult));
				});
			} catch (InvocationTargetException | InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public void OnEvaluationException(Throwable exception) {
		logger.debug("OOPSObjectPropertyHierarchy received evaluation exception!!");
	}
}