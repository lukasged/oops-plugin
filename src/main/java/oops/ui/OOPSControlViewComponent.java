package oops.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.EvaluationResult;
import oops.model.Pitfall;

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
	private static final String LIST_PITFALLS_BUTTON_LABEL = "Show all pitfalls";
	private static final String LIST_PITFALLS_BUTTON_TOOLTIP = "Lists all the detected pitfalls for the active ontology in a popup window";
	private static final String LIST_PITFALLS_DIALOG_TITLE = "All detected pitfalls";
	private static final String CONFIG_OPTIONS_BUTTON_LABEL = "Configure";
	private static final String CONFIG_OPTIONS_BUTTON_TOOLTIP = "Configure the options for the evaluation";
	
	private static final String EVALUATION_ERROR_MSG = "There has been an error while contacting OOPS! Web Service\n\n" +
			"The error may be caused by your internet connectivity or the OOPS! service is unavailable.";
	private static final String EVALUATION_ERROR_TITLE = "Ontology evaluation error";
	
	private static final int LIST_PITFALLS_BORDER_MARGIN = 30;
	
	private static final Color COLOR_PITFALL_ID_BACKGROUND = new Color(233, 231, 231);
	private static final Color COLOR_PITFALL_ID_BACKGROUND_HOVER = new Color(227, 226, 226);
	private static final Color COLOR_PITFALL_BACKGROUND = new Color(249, 249, 249);
	
	JButton btnEvaluate, btnListAllPitfalls, btnConfigEval;
	
	private OOPSEvaluator evaluator;
	
	private EvaluationDialog evaluatingDialog;
	
	private EvaluationResult evaluationResult;

	@Override
	protected void initialiseOWLView() throws Exception {
		setLayout(new FlowLayout());
		
		evaluator = OOPSEvaluator.getInstance();
		
		evaluatingDialog = new EvaluationDialog();
		
		btnEvaluate = new JButton(EVALUATE_BUTTON_LABEL);
		btnEvaluate.setToolTipText(EVALUATE_BUTTON_TOOLTIP);
		
		btnListAllPitfalls = new JButton(LIST_PITFALLS_BUTTON_LABEL);
		btnListAllPitfalls.setToolTipText(LIST_PITFALLS_BUTTON_TOOLTIP);
		btnListAllPitfalls.setEnabled(false); // disable until the evaluation is done
		
		btnConfigEval = new JButton(CONFIG_OPTIONS_BUTTON_LABEL);
		btnConfigEval.setToolTipText(CONFIG_OPTIONS_BUTTON_TOOLTIP);
		btnConfigEval.setEnabled(false); // disable until it's implemented
		
		add(btnListAllPitfalls);
		add(btnConfigEval);
		add(btnEvaluate);
		
		evaluator.addListener(this); // listen to evaluation events to change the UI
		
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
		
		btnListAllPitfalls.addActionListener(event -> {
			logger.info("Evaluation button was clicked.");
			
			JDialog pitfallsListDialog = new JDialog();
			pitfallsListDialog.setTitle(LIST_PITFALLS_DIALOG_TITLE);
			pitfallsListDialog.setModalityType((JDialog.ModalityType.APPLICATION_MODAL));
			pitfallsListDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pitfallsListDialog.setSize(new Dimension(600,250));
			pitfallsListDialog.setLocationRelativeTo(null);
			
			JPanel contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			contentPane.setBorder(BorderFactory.createEmptyBorder(
					LIST_PITFALLS_BORDER_MARGIN, LIST_PITFALLS_BORDER_MARGIN,
					LIST_PITFALLS_BORDER_MARGIN, LIST_PITFALLS_BORDER_MARGIN));
			contentPane.setBackground(COLOR_PITFALL_BACKGROUND);
			
			TreeMap<Pitfall, ArrayList<String>> pitfalls = evaluationResult.pitfallsWithAffectedElements();
			
			if (pitfalls.size() > 0) {
				pitfalls.forEach((p, elements) -> {
					JPanel pitfallLabelHolder = new JPanel();
					pitfallLabelHolder.setLayout(new BorderLayout());
					pitfallLabelHolder.setCursor(new Cursor(Cursor.HAND_CURSOR));
					pitfallLabelHolder.setBackground(COLOR_PITFALL_ID_BACKGROUND); // same as in OOPS! website
					pitfallLabelHolder.setAlignmentX(LEFT_ALIGNMENT);
					pitfallLabelHolder.setMaximumSize(new Dimension(
							(int) pitfallsListDialog.getSize().getWidth() - LIST_PITFALLS_BORDER_MARGIN * 3, 1000));
					
					pitfallLabelHolder.addMouseListener(new MouseAdapter() {
						public void mouseEntered(MouseEvent me) {
							pitfallLabelHolder.setBackground(COLOR_PITFALL_ID_BACKGROUND_HOVER);
						}

						public void mouseExited(MouseEvent me) {
							pitfallLabelHolder.setBackground(COLOR_PITFALL_ID_BACKGROUND);
						}
					});
					
					JLabel pitfallNameLabel = new JLabel("Results for " + p.getPitfallID() + ": " + p.getName());
					pitfallNameLabel.setFont(new Font("serif", Font.BOLD, 14));
					
					String cases = " case" + (elements.size() != 1 ? "s" : "");
					String importanceLevel = p.getImportanceLevel().toString();
					String capitalizedImportance = importanceLevel.charAt(0) +
							importanceLevel.toLowerCase().substring(1, importanceLevel.length());
					JLabel pitfallNumCasesLabel = new JLabel(
							p.getNumAffectedElements() + cases + " | " + capitalizedImportance);
					pitfallNumCasesLabel.setFont(new Font("serif", Font.BOLD, 14));
					
					pitfallLabelHolder.add(pitfallNameLabel, BorderLayout.WEST);
					pitfallLabelHolder.add(pitfallNumCasesLabel, BorderLayout.EAST);
					
					String pitfallText = "<html><br><p>" + p.getDescription() + "</p><br>";
					pitfallText += "<p>This pitfall appears in the following elements:</p>";

					for (String element : elements) {
						pitfallText += "<p>> <a href=" + element + ">" + element + "</a></p>";
					}
					
					pitfallText += "</html>";
					
					JLabel pitfallDescriptionLabel = new JLabel(pitfallText);
					pitfallDescriptionLabel.setVisible(false);
					pitfallDescriptionLabel.setOpaque(true);
					pitfallDescriptionLabel.setBackground(COLOR_PITFALL_BACKGROUND);
					pitfallDescriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
					pitfallDescriptionLabel.setMaximumSize(new Dimension(
							(int) pitfallsListDialog.getSize().getWidth() - LIST_PITFALLS_BORDER_MARGIN * 3, 1000));
					pitfallDescriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
					
					pitfallLabelHolder.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent me) {
							pitfallDescriptionLabel.setVisible(!pitfallDescriptionLabel.isVisible());
						}
					});
					
					contentPane.add(pitfallLabelHolder);
					contentPane.add(pitfallDescriptionLabel);
				});			
			} else {
				String noPitfallsText = "<html>" +
											"<b>We haven't detected any pitfalls for your ontology</b><br><br>" +
											"<p>Congratulations for applying the best practices. Keep it going!</p>" +
										"</html>";
				JLabel noPitfallsLabel = new JLabel(noPitfallsText);
				
				contentPane.add(noPitfallsLabel);
			}
			
			JScrollPane scrollPane = new JScrollPane(contentPane);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			pitfallsListDialog.setContentPane(scrollPane);
			pitfallsListDialog.setVisible(true);
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
		this.evaluationResult = result;
		evaluatingDialog.setVisible(false);
		btnListAllPitfalls.setEnabled(true);
	}

	@Override
	public void OnEvaluationException(Throwable exception) {
		evaluatingDialog.setVisible(false);
		
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null,
					EVALUATION_ERROR_MSG,
				    EVALUATION_ERROR_TITLE,
				    JOptionPane.ERROR_MESSAGE);
		});
	}
}
