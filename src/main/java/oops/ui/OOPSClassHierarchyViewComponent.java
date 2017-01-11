package oops.ui;

import org.protege.editor.owl.ui.view.cls.ToldOWLClassHierarchyViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;


/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Customized ClassHierarchyViewComponent for OOPS! plugin. It evaluates the active ontology and sets the custom
 * TreeCellRenderer to let the users find elements with pitfalls quickly and intuitively.
 */
public class OOPSClassHierarchyViewComponent extends ToldOWLClassHierarchyViewComponent implements EvaluationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(OOPSClassHierarchyViewComponent.class);
    
    private OOPSEvaluator evaluator;
    
    private EvaluationResult evaluationResult;

    @Override
    public void performExtraInitialisation() throws Exception {
    	super.performExtraInitialisation();
        
        evaluator = OOPSEvaluator.getInstance();
        
        evaluator.addListener(this);
    }

    @Override
    public void disposeView() {
        super.disposeView();
        
        evaluator.removeListener(this);
    }

	@Override
	public void onEvaluationStarted() {
		logger.info("OOPSClassHierarchy received evaluation start event!!");
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				getTree().setEnabled(false); // disable the tree view during evaluation
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void onEvaluationDone(EvaluationResult result) {
		evaluationResult = result;

		logger.info("OOPSClassHierarchy received evaluation results!!");
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				getTree().setCellRenderer(new OOPSTreeCellRenderer(getOWLEditorKit(), evaluationResult));
				getTree().setEnabled(true); // re-enable the tree view after evaluation
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void OnEvaluationException(Throwable exception) {
		logger.info("OOPSClassHierarchy received evaluation exception!!");
		
		try {
			SwingUtilities.invokeAndWait(() -> {
				getTree().setEnabled(true); // re-enable the tree view after evaluation
			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}
}