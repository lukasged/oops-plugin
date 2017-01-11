package oops.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
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
 * The view component with the controls for the OOPS! plugin
 */
public class OOPSControlViewComponent extends AbstractOWLViewComponent implements EvaluationListener{
	private static final Logger logger = LoggerFactory.getLogger(OOPSControlViewComponent.class);
	
	private static final String EVALUATE_BTN_LABEL = "Evaluate";
	private static final String EVALUATE_BTN_TOOLTIP = "Evaluates the ontology and marks its elements with pitfalls";
	private static final String LIST_PITFALLS_BTN_LABEL = "Show all pitfalls";
	private static final String LIST_PITFALLS_BTN_TOOLTIP = "Lists all the detected pitfalls for the active ontology "
			+ "in a popup window";
	private static final String LIST_PITFALLS_DIALOG_TITLE = "All detected pitfalls";
	private static final String CONFIG_OPTIONS_BTN_LABEL = "Configure";
	private static final String CONFIG_OPTIONS_BTN_TOOLTIP = "Configure the options for the evaluation";
	
	private static final String EVALUATION_ERROR_MSG = "There has been an error while contacting OOPS! Web Service" +
			"\n\nThe error may be caused by your internet connectivity or the OOPS! service is unavailable.";
	private static final String EVALUATION_ERROR_TITLE = "Ontology evaluation error";
	
	private static final String OOPS_RESULTS_REFERENCES_TEXT = "<ul><li>[1]	Aguado-De Cea, G., Montiel-Ponsoda, "
			+ "E., Poveda-Villalón, M., and Giraldo-Pasmin, O.X. (2015). Lexicalizing Ontologies: The issues behind "
			+ "the labels. In Multimodal communication in the 21st century: Professional and academic challenges. "
			+ "33rd Conference of the Spanish Association of Applied Linguistics (AESLA), XXXIII AESLA.</li>"
			+ "<li>[2]	Noy, N. F., McGuinness, D. L., et al. (2001). Ontology development 101: A guide to creating "
			+ "your first ontology.</li>"
			+ "<li>[3]	Gómez-Pérez, A. (1999). Evaluation of Taxonomic Knowledge in Ontologies and Knowledge Bases. "
			+ "Proceedings of the Banff Knowledge Acquisition for Knowledge-Based Systems Workshop. Alberta, Canada."
			+ "</li><li>[4]	Montiel-Ponsoda, E., Vila Suero, D., Villazón-Terrazas, B., Dunsire, G., Escolano "
			+ "Rodríguez, E., Gómez-Pérez, A. (2011). Style guidelines for naming and labeling ontologies in the "
			+ "multilingual web.</li>"
			+ "<li>[5]	Vrandecic, D. (2010). Ontology Evaluation. PhD thesis.</li>"
			+ "<li>[6]	Gómez-Pérez, A. (2004). Ontology evaluation. In Handbook on ontologies, pages 251-273. "
			+ "Springer.</li>"
			+ "<li>[7]	Rector, A., Drummond, N., Horridge, M., Rogers, J., Knublauch, H., Stevens, R., "
			+ "Wang, H., and Wroe, C. (2004). Owl pizzas: Practical experience of teaching owl-dl: Common errors "
			+ "& common patterns. In Engineering Knowledge in the Age of the Semantic Web, pages 63-81. Springer.</li>"
			+ "<li>[8]	Hogan, A., Harth, A., Passant, A., Decker, S., and Polleres, A. (2010). Weaving the pedantic "
			+ "web. In Proceedings of the WWW2010 Workshop on Linked Data on the Web, LDOW 2010, Raleigh, USA, April "
			+ "27, 2010.</li>"
			+ "<li>[9]	Archer, P., Goedertier, S., and Loutas, N. (2012). D7. 1.3-study on persistent URIs, with iden"
			+ "tification of best practices and recommendations on the topic for the Mss and the EC. PwC EU Services."
			+ "</li><li>[10] Bernes-Lee Tim. (2006). “Linked Data - Design issues”. http://www.w3.org/DesignIssues/"
			+ "LinkedData.html</li>"
			+ "<li>[11] Heath, T. and Bizer, C. (2011). Linked Data: Evolving the Web into a Global Data Space. Morgan"
			+ " & Claypool, 1st edition.</li>"
			+ "<li>[12] Vatant, B. (2012). Is your linked data vocabulary 5-star?. http://bvatant.blogspot.fr/2012/02/"
			+ "is-your-linked-data-vocabulary-5-star_9588.html</li></ul>";
	
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
		
		btnEvaluate = new JButton(EVALUATE_BTN_LABEL);
		btnEvaluate.setToolTipText(EVALUATE_BTN_TOOLTIP);
		
		btnListAllPitfalls = new JButton(LIST_PITFALLS_BTN_LABEL);
		btnListAllPitfalls.setToolTipText(LIST_PITFALLS_BTN_TOOLTIP);
		btnListAllPitfalls.setEnabled(false); // disable until the evaluation is done
		
		btnConfigEval = new JButton(CONFIG_OPTIONS_BTN_LABEL);
		btnConfigEval.setToolTipText(CONFIG_OPTIONS_BTN_TOOLTIP);
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
			pitfallsListDialog.setSize(new Dimension(800,250));
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
					
					String cases = p.getNumAffectedElements() + " case" + (elements.size() != 1 ? "s" : "");
					String importanceLevel = p.getImportanceLevel().toString();
					String capitalizedImportance = importanceLevel.charAt(0) +
							importanceLevel.toLowerCase().substring(1, importanceLevel.length());
					String numCasesOrOntology = isGeneralPitfall(p.getPitfallID()) ? "ontology*" : cases;
					JLabel pitfallNumCasesLabel = new JLabel(numCasesOrOntology + " | " + capitalizedImportance + " ");
					pitfallNumCasesLabel.setOpaque(false);
					pitfallNumCasesLabel.setFont(new Font("serif", Font.BOLD, 14));
					
					IconComponent iconComponent = new IconComponent();
					iconComponent.setOpaque(false);
					URL iconURL = this.getClass().getResource("/" + 
							p.getImportanceLevel().toString().toLowerCase() + ".png");
					Image scaledImage = new ImageIcon(iconURL).getImage().getScaledInstance(16, 16, 
							Image.SCALE_DEFAULT);
					iconComponent.setIcon(new ImageIcon(scaledImage));
					
					JPanel rightSidePanel = new JPanel();
					rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.X_AXIS));
					rightSidePanel.add(pitfallNumCasesLabel);
					rightSidePanel.add(iconComponent);
					rightSidePanel.setOpaque(false);
					
					pitfallLabelHolder.add(pitfallNameLabel, BorderLayout.WEST);
					pitfallLabelHolder.add(rightSidePanel, BorderLayout.EAST);
					
					String pitfallText = "<html><br><p>" + p.getDescription() + "</p><br>";
					
					switch (p.getPitfallID()) {
					case OOPSEvaluator.PITFALL_EQUIVALENT_CLASSES_ID:
						List<ElementPair> equivalentClasses = evaluationResult.getEquivalentClasses();

						pitfallText += "<p>The following classes might be equivalent:</p>";

						for (ElementPair pair : equivalentClasses) {
							pitfallText += "<p>> <a href=" + pair.getElementA() + ">" + pair.getElementA() + "</a>, "
									+ "<a href=" + pair.getElementB() + ">" + pair.getElementB() + "</a>" + "</p>";
						}
						break;
					case OOPSEvaluator.PITFALL_MIGHT_BE_EQUIVALENT_ID:
						List<ElementPair> equivalentProperties = evaluationResult.getEquivalentRelations();
						List<ElementPair> equivalentAttributes = evaluationResult.getEquivalentAttributes();
						
						if (equivalentProperties.size() > 0) {
							pitfallText += "<p>The following relations could be defined as equivalent:</p>";
							
							for (ElementPair pair : equivalentProperties) {
								pitfallText += "<p>> <a href=" + pair.getElementA() + ">" + pair.getElementA() +
										"</a>, <a href=" + pair.getElementB() + ">" + pair.getElementB() + "</a></p>";
							}
						}
						
						if (equivalentAttributes.size() > 0) {
							pitfallText += "<br><p>The following attributes could be defined as equivalent:</p>";
							
							for (ElementPair pair : equivalentAttributes) {
								pitfallText += "<p>> <a href=" + pair.getElementA() + ">" + pair.getElementA() +
										"</a>, <a href=" + pair.getElementB() + ">" + pair.getElementB() + "</a></p>";
							}
						}

						break;
					case OOPSEvaluator.PITFALL_MIGHT_BE_INVERSE_ID:
						List<ElementPair> mightBeInverseRelations = evaluationResult.getMightBeInverseRelations();
						
						for (ElementPair pair : mightBeInverseRelations) {
							pitfallText += "<p>> <a href=" + pair.getElementA() + ">" + pair.getElementA() + 
									"</a> could be inverse of <a href=" + pair.getElementB() + ">" +
									pair.getElementB() + "</a>" + "</p>";
						}
						
						break;
					case OOPSEvaluator.PITFALL_WRONG_INVERSE_ID:
						List<ElementPair> wrongInverseRelations = evaluationResult.getWrongInverseRelations();
						
						for (ElementPair pair : wrongInverseRelations) {
							pitfallText += "<p>> <a href=" + pair.getElementA() + ">" + pair.getElementA() + 
									"</a> may not be inverse of <a href=" + pair.getElementB() + ">" + 
									pair.getElementB() + "</a>" + "</p>";
						}
						
						break;
					default:
						pitfallText += "<p>This pitfall appears in the following elements:</p>";

						for (String element : elements) {
							pitfallText += "<p>> <a href=" + element + ">" + element + "</a></p>";
						}
					}
					
					pitfallText += "<br><p>References:</p>";
					pitfallText += OOPS_RESULTS_REFERENCES_TEXT;
					
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
				
				JLabel referencesLabel = new JLabel(
						"<html><p>References:</p>" + OOPS_RESULTS_REFERENCES_TEXT + "</html>");
				referencesLabel.setMaximumSize(new Dimension(
						(int) pitfallsListDialog.getSize().getWidth() - LIST_PITFALLS_BORDER_MARGIN * 3, 1000));
				referencesLabel.setAlignmentX(LEFT_ALIGNMENT);
				
				contentPane.add(referencesLabel);
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
			scrollPane.getVerticalScrollBar().setUnitIncrement(16); // increase scrolling speed to a more usual pace
			
			pitfallsListDialog.setContentPane(scrollPane);
			pitfallsListDialog.setVisible(true);
		});
		
		getView().setShowViewBar(false); // disable view label bar
	}
	
	/**
	 * Returns true if the specified pitfall affects the ontology itself
	 * 
	 * @param pitfallCode
	 *            the specified pitfall code
	 * @return true if the specified pitfall affects the ontology itself
	 */
	private boolean isGeneralPitfall(String pitfallCode) {
		return pitfallCode.equals(OOPSEvaluator.PITFALL_DIFF_NAMING_CONVENTIONS_ID);
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
