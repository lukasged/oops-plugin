package oops.ui;

import java.awt.FlowLayout;
import javax.swing.JButton;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * The view component with the controls for the OOPS! plugin
 */
public class OOPSControlViewComponent extends AbstractOWLViewComponent implements EvaluationListener{
	private static final Logger logger = LoggerFactory.getLogger(OOPSControlViewComponent.class);
	
	private static final String EVALUATE_BUTTON_LABEL = "Evaluate";
	private static final String EVALUATE_BUTTON_TOOLTIP = "Evaluates the ontology and marks its elements with pitfalls";
	private static final String LIST_PITFALLS_BUTTON_LABEL = "List all pitfalls";
	private static final String LIST_PITFALLS_BUTTON_TOOLTIP = "Lists all the detected pitfalls for the active ontology in a popup window";
	private static final String CONFIG_OPTIONS_BUTTON_LABEL = "Configure";
	private static final String CONFIG_OPTIONS_BUTTON_TOOLTIP = "Configure the options for the evaluation";
	
	private OOPSEvaluator evaluator;
	
	private EvaluationDialog evaluatingDialog;

	@Override
	protected void initialiseOWLView() throws Exception {
		setLayout(new FlowLayout());
		
		evaluator = OOPSEvaluator.getInstance();
		
		evaluatingDialog = new EvaluationDialog();
		
		JButton btnEvaluate = new JButton(EVALUATE_BUTTON_LABEL);
		btnEvaluate.setToolTipText(EVALUATE_BUTTON_TOOLTIP);
		
		JButton btnListAllPitfalls = new JButton(LIST_PITFALLS_BUTTON_LABEL);
		btnListAllPitfalls.setToolTipText(LIST_PITFALLS_BUTTON_TOOLTIP);
		btnListAllPitfalls.setEnabled(false); // disable until it's implemented
		
		JButton btnConfigEval = new JButton(CONFIG_OPTIONS_BUTTON_LABEL);
		btnConfigEval.setToolTipText(CONFIG_OPTIONS_BUTTON_TOOLTIP);
		btnConfigEval.setEnabled(false); // disable until it's implemented
		
		add(btnConfigEval);
		add(btnEvaluate);
		
		evaluator.addListener(this);
		
		btnEvaluate.addActionListener(event -> {
			logger.info("Evaluation button was clicked.");
			
			try {
				logger.info("Calling to evaluate from the OOPSControl");
				evaluator.evaluate(getOWLEditorKit().getOWLModelManager().getActiveOntology());
				logger.info("Returned to OOPSControl after evaluation");
			} catch (InterruptedException ie) {
				logger.error(ie.getLocalizedMessage());
			}
		});
		
		getView().setShowViewBar(false); // disable view label bar
	}
	
	@Override
	protected void disposeOWLView() {
		evaluator.removeListener(this);
	}

	@Override
	public void onEvaluationStarted() {
		evaluatingDialog.setVisible(true);
	}

	@Override
	public void onEvaluationDone(EvaluationResult result) {
		evaluatingDialog.setVisible(false);
	}
}
