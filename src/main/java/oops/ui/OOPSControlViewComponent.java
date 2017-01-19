package oops.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oops.evaluation.EvaluationListener;
import oops.evaluation.OOPSEvaluator;
import oops.model.ElementPair;
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
	private static final Dimension LIST_PITFALLS_DIMENSION = new Dimension(800, 450);
	
	private static final Color COLOR_PITFALL_ID_BACKGROUND = new Color(233, 231, 231);
	private static final Color COLOR_PITFALL_ID_BACKGROUND_HOVER = new Color(227, 226, 226);
	private static final Color COLOR_PITFALL_BACKGROUND = new Color(249, 249, 249);

	private static final String CONFIG_DIALOG_TITLE = "OOPS! evaluation options";
	private static final Border CONFIG_DIALOG_CATEGORY_TOTAL_BORDER = BorderFactory.createEmptyBorder(0, 10, 10, 10);
	private static final Border CONFIG_DIALOG_CATEGORY_INNER_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 0);
	private static final Border CONFIG_DIALOG_CATEGORY_HEAD_BORDER =  BorderFactory.createEmptyBorder(10, 0, 10, 0);
	private static final Dimension CONFIG_DIALOG_PITFALLS_DIMENSION = new Dimension(400, 150);
	private static final Dimension CONFIG_DIALOG_CATEGORY_DIMENSION = new Dimension(750, 500);
	private static final Dimension CONFIG_DIALOG_CATEGORY_LABEL_DIM = new Dimension(370,35);
	private static final int CONFIG_DIALOG_CHECKS_PER_ROW = 8;
	private static final String SELECT_PITFALLS_BTN_TEXT = "Select Pitfalls for Evaluation";
	private static final String SELECT_CATEGORY_BTN_TEXT = "Select Category for Evaluation";
	private static final String BTN_SELECT_ALL_TEXT = "Select all";
	private static final String BTN_CLEAR_SELECTION_TEXT = "Clear all";
	
	private static final String LABEL_CLASSIFICATION_BY_DIMENSION = "Classification by Dimension";
	private static final String LABEL_CLASSIFICATION_BY_EVAL_CRIT = "Classification by Evaluation Criteria";
	private static final String LABEL_STRUCTURAL_DIMENSION = "Structural Dimension";
	private static final String LABEL_MODELLING_DECISIONS = "Modelling Decisions";
	private static final String LABEL_WRONG_INFERENCE = "Wrong Inference";
	private static final String LABEL_NO_INFERENCE = "No Inference";
	private static final String LABEL_ONTO_LANGUAGE = "Ontology language";
	private static final String LABEL_FUNCTIONAL_DIMENSION = "Functional Dimension";
	private static final String LABEL_RWM_COMMON_SENSE = "Real World Modelling or Common Sense";
	private static final String LABEL_REQS_COMPLETENESS = "Requirements Completeness";
	private static final String LABEL_APP_CONTEXT = "Application context";
	private static final String LABEL_USABILITY_PROFILING_DIM = "Usability-Profiling Dimension";
	private static final String LABEL_ONTO_CLARITY = "Ontology Clarity";
	private static final String LABEL_ONTO_UNDERSTANDING = "Ontology Understanding";
	private static final String LABEL_ONTO_METADATA = "Ontology Metadata";
	private static final String LABEL_CONSISTENCY = "Consistency";
	private static final String LABEL_COMPLETENESS = "Completeness";
	private static final String LABEL_CONCISENESS = "Consciseness";
	
	private static final String allPitfalls[][] = {
			{"P02", "P02: Creating synonyms as classes"},
			{"P03", "P03: Creating the relationship \"is\" instead of using \"rdfs:subClassOf\", \"rdf:type\" or "
					+ "\"owl:sameAs\""},
			{"P04", "P04: Creating unconnected ontology elements"},
			{"P05", "P05: Defining wrong inverse relationships"},
			{"P06", "P06: Including cycles in a class hierarchy"},
			{"P07", "P07: Merging different concepts in the same class"},
			{"P08", "P08: Missing annotations"},
			{"P10", "P10: Missing disjointness"},
			{"P11", "P11: Missing domain or range in properties"},
			{"P12", "P12: Equivalent properties not explicitly declared"},
			{"P13", "P13: Inverse relationships not explicitly declared"},
			{"P19", "P19: Defining multiple domains or ranges in properties"},
			{"P20", "P20: Misusing ontology annotations"},
			{"P21", "P21: Using a miscellaneous class"},
			{"P22", "P22: Using different naming conventions in the ontology"},
			{"P24", "P24: Using recursive definitions"},
			{"P25", "P25: Defining a relationship as inverse to itself"},
			{"P26", "P26: Defining inverse relationships for a symmetric one"},
			{"P27", "P27: Defining wrong equivalent properties"},
			{"P28", "P28: Defining wrong symmetric relationships"},
			{"P29", "P29: Defining wrong transitive relationships"},
			{"P30", "P30: Equivalent classes not explicitly declared"},
			{"P31", "P31: Defining wrong equivalent classes"},
			{"P32", "P32: Several classes with the same label"},
			{"P33", "P33: Creating a property chain with just one property"},
			{"P34", "P34: Untyped class"},
			{"P35", "P35: Untyped property"},
			{"P38", "P38: No OWL ontology declaration"},
			{"P39", "P39: Ambiguous namespace"},
			{"P40", "P40: Namespace hijacking"},
			{"P41", "P41: No license declared"}
	};
	
	private static final HashMap<String, String[]> pitfallCategories = new HashMap<String, String[]>() {{
		put(LABEL_MODELLING_DECISIONS, new String[]{"P02", "P03", "P03", "P07", "P21", "P24", "P25", "P26", "P33"});
		put(LABEL_WRONG_INFERENCE, new String[]{"P05", "P06", "P19", "P27", "P28", "P29", "P31"});
		put(LABEL_NO_INFERENCE, new String[]{"P11", "P12", "P13", "P30"});
		put(LABEL_ONTO_LANGUAGE, new String[]{"P34", "P35", "P38"});
		put(LABEL_STRUCTURAL_DIMENSION, Stream.of(
				get(LABEL_MODELLING_DECISIONS),
				get(LABEL_WRONG_INFERENCE),
				get(LABEL_NO_INFERENCE),
				get(LABEL_ONTO_LANGUAGE)).flatMap(Stream::of).distinct().sorted().toArray(String[]::new));
		
		put(LABEL_RWM_COMMON_SENSE, new String[]{"P04", "P10"});
		put(LABEL_REQS_COMPLETENESS, new String[]{"P04", "P09"});
		put(LABEL_APP_CONTEXT, new String[]{"P36", "P37", "P38", "P39", "P40"});
		put(LABEL_FUNCTIONAL_DIMENSION,  Stream.of(
				get(LABEL_RWM_COMMON_SENSE),
				get(LABEL_REQS_COMPLETENESS),
				get(LABEL_APP_CONTEXT)).flatMap(Stream::of).distinct().sorted().toArray(String[]::new));
		
		put(LABEL_ONTO_CLARITY, new String[]{"P08", "P22"});
		put(LABEL_ONTO_UNDERSTANDING, new String[]{"P02", "P07", "P08", "P11", "P12", "P13", "P20", "P32", "P37"});
		put(LABEL_ONTO_METADATA, new String[]{"P38", "P41"});
		put(LABEL_USABILITY_PROFILING_DIM,  Stream.of(
				get(LABEL_ONTO_CLARITY),
				get(LABEL_ONTO_UNDERSTANDING),
				get(LABEL_ONTO_METADATA)).flatMap(Stream::of).distinct().sorted().toArray(String[]::new));
		
		put(LABEL_CONSISTENCY, new String[]{"P05", "P06", "P07", "P19", "P24"});
		put(LABEL_COMPLETENESS, new String[]{"P04", "P10", "P11", "P12", "P13"});
		put(LABEL_CONCISENESS, new String[]{"P02", "P03", "P21"});
	}};
	
	private JPanel cards, pitfallsSelectionCard, categorySelectionCard;
	private CardLayout cardLayout;
	
	private JDialog configDialog;
	
	private JButton btnEvaluate, btnListAllPitfalls, btnConfigEval;

	private List<JCheckBox> pitfallCheckBoxes;
	
	private JButton btnSelectAll, btnClearSelection;
	
	private ButtonGroup btnGroup;
	
	private boolean configurationDone; // flag that indicates that the config dialog has been used
	
	private String selectedFilter; // selected CardLayout card
	
	private String selectedCategory; // selected category in the config dialog 
	
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
		
		add(btnListAllPitfalls);
		add(btnConfigEval);
		add(btnEvaluate);
		
		evaluator.addListener(this); // listen to evaluation events to change the UI
		
		btnEvaluate.addActionListener(event -> {
			try {
				List<String> pitfallsSubset = null;
				if (configurationDone) {
					if (selectedFilter.equals(SELECT_PITFALLS_BTN_TEXT)) {
						pitfallsSubset = pitfallCheckBoxes.stream()
								.filter(JCheckBox::isSelected)
								.map(JCheckBox::getText)
								.collect(Collectors.toList());
					} else if (selectedFilter.equals(SELECT_CATEGORY_BTN_TEXT)) {
						pitfallsSubset = new ArrayList<String>(Arrays.asList(pitfallCategories.get(selectedCategory)));
					}
					
				} else {
					pitfallsSubset = new ArrayList<String>();
				}
				
				evaluator.evaluate(getOWLEditorKit().getOWLModelManager().getActiveOntology(), pitfallsSubset);
			} catch (InterruptedException ie) {
				logger.error(ie.getLocalizedMessage());
			}
		});
		
		btnConfigEval.addActionListener(event -> {
			configDialog = new JDialog();
			configDialog.setTitle(CONFIG_DIALOG_TITLE);
			configDialog.setModalityType((JDialog.ModalityType.APPLICATION_MODAL));
			configDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			configDialog.setLocationRelativeTo(null);
			
			JPanel contentPane = new JPanel();
			contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			
			JPanel radioButtons = new JPanel();
			
			btnGroup = new ButtonGroup();
			
			JRadioButton selectPitfallsBtn = new JRadioButton(SELECT_PITFALLS_BTN_TEXT);
			selectPitfallsBtn.setSelected(true); // selected by default
			selectPitfallsBtn.setActionCommand(SELECT_PITFALLS_BTN_TEXT);
			selectedFilter = SELECT_PITFALLS_BTN_TEXT;
			radioButtons.add(selectPitfallsBtn);
			
			JRadioButton selectCategoryBtn = new JRadioButton(SELECT_CATEGORY_BTN_TEXT);
			selectCategoryBtn.setActionCommand(SELECT_CATEGORY_BTN_TEXT);
			radioButtons.add(selectCategoryBtn);
			
			btnGroup.add(selectPitfallsBtn);
			btnGroup.add(selectCategoryBtn);
			
			selectPitfallsBtn.addActionListener(this::cardsSwitchActionListener);
			selectCategoryBtn.addActionListener(this::cardsSwitchActionListener);
			
			cardLayout = new AdaptiveCardLayout();
			cards = new JPanel(cardLayout);
			
			pitfallsSelectionCard = getPitfallsSelectionPanel();
			
			categorySelectionCard = getCategorySelectionPanel();
			categorySelectionCard.setPreferredSize(CONFIG_DIALOG_CATEGORY_DIMENSION);
			
			cards.add(pitfallsSelectionCard, SELECT_PITFALLS_BTN_TEXT);
			cards.add(categorySelectionCard, SELECT_CATEGORY_BTN_TEXT);
			
			contentPane.add(radioButtons);
			contentPane.add(cards);
			
			configDialog.setContentPane(contentPane);
			configDialog.pack();
			configDialog.setLocationRelativeTo(null); // set panel's location to the center
			configDialog.setVisible(true);
			
			configurationDone = true; // set this flag to true, so the state of the configuration will be restored
		});
		
		btnListAllPitfalls.addActionListener(event -> {
			JDialog pitfallsListDialog = new JDialog();
			pitfallsListDialog.setTitle(LIST_PITFALLS_DIALOG_TITLE);
			pitfallsListDialog.setModalityType((JDialog.ModalityType.APPLICATION_MODAL));
			pitfallsListDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			pitfallsListDialog.setSize(LIST_PITFALLS_DIMENSION);
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
					String numCasesOrOntology = OOPSEvaluator.isGeneralPitfall(p.getPitfallID()) ? "ontology*" : cases;
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
					
					String pitfallText = getPitfallText(p, elements, evaluationResult);
					
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
	 * Changes the card showed in the card layout when radio buttons are pressed
	 */
	private void cardsSwitchActionListener(ActionEvent event) {
		selectedFilter = event.getActionCommand();
		
		cardLayout.show(cards, selectedFilter);
		
		configDialog.pack();
		configDialog.setLocationRelativeTo(null); // set panel's location to the center
	}
	
	/**
	 * Generates the panel for the pitfalls filtering by explicit pitfalls list
	 * 
	 * @return the panel for the pitfalls filtering by explicit pitfalls list
	 */
	private JPanel getPitfallsSelectionPanel() {
		pitfallsSelectionCard = new JPanel();
		pitfallsSelectionCard.setLayout(new BoxLayout(pitfallsSelectionCard, BoxLayout.Y_AXIS));
		pitfallsSelectionCard.setPreferredSize(CONFIG_DIALOG_PITFALLS_DIMENSION);
		
		if (!configurationDone) {
			pitfallCheckBoxes = new ArrayList<JCheckBox>();
		}
		
		JPanel checkBoxesPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints checkBoxesConstraints = new GridBagConstraints();
		checkBoxesConstraints.weightx = 1;
		checkBoxesConstraints.weighty = 1;
		
		for (int pitfallIndex = 0; pitfallIndex < allPitfalls.length; pitfallIndex++) {
			String pitfall[] = allPitfalls[pitfallIndex];
			
			JCheckBox checkBox = null;
			
			if (!configurationDone) {
				checkBox = new JCheckBox(pitfall[0]);
				checkBox.setToolTipText(pitfall[1]);
				
				pitfallCheckBoxes.add(checkBox);
			} else {
				checkBox = pitfallCheckBoxes.get(pitfallIndex);
			}
			
			checkBoxesConstraints.gridy = pitfallIndex / CONFIG_DIALOG_CHECKS_PER_ROW;
			checkBoxesConstraints.gridx = pitfallIndex % CONFIG_DIALOG_CHECKS_PER_ROW;
			checkBoxesPanel.add(checkBox, checkBoxesConstraints);
		}
		
		JPanel buttonsPanel = new JPanel();
		
		// "Select all" button
		btnSelectAll = new JButton(BTN_SELECT_ALL_TEXT);
		buttonsPanel.add(btnSelectAll);
		btnSelectAll.addActionListener(e -> pitfallCheckBoxes.forEach(check -> check.setSelected(true)));
		
		// "Clear all" button
		btnClearSelection = new JButton(BTN_CLEAR_SELECTION_TEXT);
		buttonsPanel.add(btnClearSelection);
		btnClearSelection.addActionListener(e -> pitfallCheckBoxes.forEach(check -> check.setSelected(false)));
		
		pitfallsSelectionCard.add(checkBoxesPanel);
		pitfallsSelectionCard.add(buttonsPanel);
		
		return pitfallsSelectionCard;
	}
	
	/**
	 * Generates the panel for the pitfalls filtering by category
	 * 
	 * @return the panel for the pitfalls filtering by category
	 */
	private JPanel getCategorySelectionPanel() {
		JPanel categorySelectionPanel = new JPanel(new GridLayout(1,2,10,0));
		categorySelectionPanel.setBorder(CONFIG_DIALOG_CATEGORY_TOTAL_BORDER);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setAlignmentX(LEFT_ALIGNMENT);
		leftPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		ButtonGroup allButtonsGroup = new ButtonGroup();
		
		ActionListener listener = new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            JRadioButton btn = (JRadioButton) e.getSource();
	            logger.debug("Selected Button = " + btn.getActionCommand());
	            selectedCategory = btn.getActionCommand(); // save the selected category
	        }
	    };
		
		JPanel classByDimPanel = new JPanel(new BorderLayout());
		classByDimPanel.setAlignmentX(LEFT_ALIGNMENT);
		classByDimPanel.setBorder(CONFIG_DIALOG_CATEGORY_HEAD_BORDER);
		classByDimPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
		JLabel classByDimLabel = new JLabel(LABEL_CLASSIFICATION_BY_DIMENSION, SwingConstants.CENTER);
		classByDimLabel.setFont(new Font("serif", Font.BOLD, 14));
		classByDimPanel.add(classByDimLabel, BorderLayout.CENTER);
		leftPanel.add(classByDimPanel);
		
		// Structural Dimension
		JPanel structDimHeaderPanel = new JPanel(new BorderLayout());
		structDimHeaderPanel.setBackground(Color.LIGHT_GRAY);
		structDimHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
		structDimHeaderPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
		String pitfalls = Stream.of(pitfallCategories.get(LABEL_STRUCTURAL_DIMENSION))
				.collect(Collectors.joining(", "));
		JRadioButton structDimHeaderBtn = new JRadioButton("<html><b>" + LABEL_STRUCTURAL_DIMENSION + "</b></html>");
		structDimHeaderBtn.setToolTipText("Checks for pitfalls " + pitfalls);
		structDimHeaderBtn.setOpaque(false);
		structDimHeaderBtn.setActionCommand(LABEL_STRUCTURAL_DIMENSION);
		structDimHeaderBtn.addActionListener(listener);
		
		structDimHeaderPanel.add(structDimHeaderBtn, BorderLayout.WEST);
		allButtonsGroup.add(structDimHeaderBtn);
		leftPanel.add(structDimHeaderPanel);
		
		JPanel structDimPanel = new JPanel();
		structDimPanel.setLayout(new BoxLayout(structDimPanel, BoxLayout.Y_AXIS));
		structDimPanel.setBorder(CONFIG_DIALOG_CATEGORY_INNER_BORDER);
		structDimPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		for (String label : Arrays.asList(LABEL_MODELLING_DECISIONS, LABEL_WRONG_INFERENCE, LABEL_NO_INFERENCE, LABEL_ONTO_LANGUAGE)) {
			pitfalls = Stream.of(pitfallCategories.get(label)).collect(Collectors.joining(", "));
			JRadioButton radioBtn = new JRadioButton("<html><b>" + label + ":</b> Checks for pitfalls " + pitfalls + ".</html>");
			radioBtn.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
			radioBtn.addActionListener(listener);
			radioBtn.setActionCommand(label);
			allButtonsGroup.add(radioBtn);
			structDimPanel.add(radioBtn);
		}
		
		leftPanel.add(structDimPanel);
		
		// Functional Dimension
		JPanel funcDimHeaderPanel = new JPanel(new BorderLayout());
		funcDimHeaderPanel.setBackground(Color.LIGHT_GRAY);
		funcDimHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
		funcDimHeaderPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
		pitfalls = Stream.of(pitfallCategories.get(LABEL_FUNCTIONAL_DIMENSION))
				.collect(Collectors.joining(", "));
		JRadioButton funcDimHeaderBtn = new JRadioButton("<html><b>" + LABEL_FUNCTIONAL_DIMENSION + "</b></html>");
		funcDimHeaderBtn.setToolTipText("Checks for pitfalls " + pitfalls);
		funcDimHeaderBtn.setOpaque(false);
		funcDimHeaderBtn.addActionListener(listener);
		funcDimHeaderBtn.setActionCommand(LABEL_FUNCTIONAL_DIMENSION);
		
		allButtonsGroup.add(funcDimHeaderBtn);
		funcDimHeaderPanel.add(funcDimHeaderBtn, BorderLayout.WEST);
		leftPanel.add(funcDimHeaderPanel);
		
		JPanel funcDimPanel = new JPanel();
		funcDimPanel.setLayout(new BoxLayout(funcDimPanel, BoxLayout.Y_AXIS));
		funcDimPanel.setBorder(CONFIG_DIALOG_CATEGORY_INNER_BORDER);
		funcDimPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		for (String label : Arrays.asList(LABEL_RWM_COMMON_SENSE, LABEL_REQS_COMPLETENESS, LABEL_APP_CONTEXT)) {
			pitfalls = Stream.of(pitfallCategories.get(label)).collect(Collectors.joining(", "));
			JRadioButton radioBtn = new JRadioButton("<html><b>" + label + ":</b> Checks for pitfalls " + pitfalls + ".</html>");
			radioBtn.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
			radioBtn.addActionListener(listener);
			radioBtn.setActionCommand(label);
			allButtonsGroup.add(radioBtn);
			funcDimPanel.add(radioBtn);
		}
		
		leftPanel.add(funcDimPanel);
		
		// Usability-Profiling Dimension
		JPanel usabilityDimHeaderPanel = new JPanel(new BorderLayout());
		usabilityDimHeaderPanel.setBackground(Color.LIGHT_GRAY);
		usabilityDimHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
		usabilityDimHeaderPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
		pitfalls = Stream.of(pitfallCategories.get(LABEL_USABILITY_PROFILING_DIM))
				.collect(Collectors.joining(", "));
		JRadioButton usabilityDimHeaderBtn = new JRadioButton("<html><b>" + LABEL_USABILITY_PROFILING_DIM + "</b></html>");
		usabilityDimHeaderBtn.setToolTipText("Checks for pitfalls " + pitfalls);
		usabilityDimHeaderBtn.setOpaque(false);
		usabilityDimHeaderBtn.addActionListener(listener);
		usabilityDimHeaderBtn.setActionCommand(LABEL_USABILITY_PROFILING_DIM);
		
		allButtonsGroup.add(usabilityDimHeaderBtn);
		usabilityDimHeaderPanel.add(usabilityDimHeaderBtn, BorderLayout.WEST);
		leftPanel.add(usabilityDimHeaderPanel);
		
		JPanel usabilityDimPanel = new JPanel();
		usabilityDimPanel.setLayout(new BoxLayout(usabilityDimPanel, BoxLayout.Y_AXIS));
		usabilityDimPanel.setBorder(CONFIG_DIALOG_CATEGORY_INNER_BORDER);
		usabilityDimPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		for (String label : Arrays.asList(LABEL_ONTO_CLARITY, LABEL_ONTO_UNDERSTANDING, LABEL_ONTO_METADATA)) {
			pitfalls = Stream.of(pitfallCategories.get(label)).collect(Collectors.joining(", "));
			JRadioButton radioBtn = new JRadioButton("<html><b>" + label + ":</b> Checks for pitfalls " + pitfalls + ".</html>");
			radioBtn.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
			radioBtn.addActionListener(listener);
			radioBtn.setActionCommand(label);
			allButtonsGroup.add(radioBtn);
			usabilityDimPanel.add(radioBtn);
		}
		
		leftPanel.add(usabilityDimPanel);
		
		// RIGHT PANEL
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		JPanel classByEvalCritPanel = new JPanel(new BorderLayout());
		classByEvalCritPanel.setAlignmentX(LEFT_ALIGNMENT);
		classByEvalCritPanel.setBorder(CONFIG_DIALOG_CATEGORY_HEAD_BORDER);
		classByEvalCritPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
		JLabel classByEvalCritLabel = new JLabel(LABEL_CLASSIFICATION_BY_EVAL_CRIT, SwingConstants.CENTER);
		classByEvalCritLabel.setFont(new Font("serif", Font.BOLD, 14));
		classByEvalCritPanel.add(classByEvalCritLabel, BorderLayout.CENTER);
		
		rightPanel.add(classByEvalCritPanel);
		
		// loop for the 3 categories on the right panel
		for (String label : Arrays.asList(LABEL_CONSISTENCY, LABEL_COMPLETENESS, LABEL_CONCISENESS)) {
			pitfalls = Stream.of(pitfallCategories.get(label)).collect(Collectors.joining(", "));
			
			JPanel headerPanel = new JPanel(new BorderLayout());
			headerPanel.setBackground(Color.LIGHT_GRAY);
			headerPanel.setAlignmentX(LEFT_ALIGNMENT);
			headerPanel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
			JRadioButton radioBtn = new JRadioButton("<html><b>" + label + "</b></html>");
			radioBtn.setToolTipText("Checks for pitfalls : " + pitfalls);
			radioBtn.setOpaque(false);
			radioBtn.addActionListener(listener);
			radioBtn.setActionCommand(label);
			
			allButtonsGroup.add(radioBtn);
			headerPanel.add(radioBtn, BorderLayout.LINE_START);
			rightPanel.add(headerPanel);
			
			JPanel bodyPanel = new JPanel();
			bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
			bodyPanel.setBorder(CONFIG_DIALOG_CATEGORY_INNER_BORDER);
			bodyPanel.setAlignmentX(LEFT_ALIGNMENT);
			
			JLabel pitfallsLabel = new JLabel("<html>For this evaluation criteria the following pitfalls "
					+ "will be checked: " + pitfalls + ".</html>");
			pitfallsLabel.setMaximumSize(CONFIG_DIALOG_CATEGORY_LABEL_DIM);
			bodyPanel.add(pitfallsLabel);
			
			rightPanel.add(bodyPanel);
		}
		
		categorySelectionPanel.add(leftPanel);
		categorySelectionPanel.add(rightPanel);
		
		return categorySelectionPanel;
	}
	
	/**
	 * Obtains a detailed text for the given pitfall
	 * 
	 * @param pitfall
	 *            the specified pitfall
	 * @param affectedElements
	 *            the elements affected by the pitfall
	 * @param evaluationResult
	 *            the current evaluation results
	 * @return a detailed text for the given pitfall
	 */
	private String getPitfallText(Pitfall pitfall, ArrayList<String> affectedElements, EvaluationResult evaluationResult) {
		String pitfallText = "<html><br><p>" + pitfall.getDescription() + "</p><br>";
		
		switch (pitfall.getPitfallID()) {
		case OOPSEvaluator.PITFALL_EQUIVALENT_CLASSES_ID:
			List<ElementPair> equivalentClasses = evaluationResult.getEquivalentClasses();

			pitfallText += "<p>The following classes might be equivalent:</p>";

			for (ElementPair pair : equivalentClasses) {
				pitfallText += "<p>> " + pair.getElementA() + ", " + pair.getElementB() + "</p>";
			}
			
			break;
		case OOPSEvaluator.PITFALL_MIGHT_BE_EQUIVALENT_ID:
			List<ElementPair> equivalentProperties = evaluationResult.getEquivalentRelations();
			List<ElementPair> equivalentAttributes = evaluationResult.getEquivalentAttributes();
			
			if (equivalentProperties.size() > 0) {
				pitfallText += "<p>The following relations could be defined as equivalent:</p>";
				
				for (ElementPair pair : equivalentProperties) {
					pitfallText += "<p>> " + pair.getElementA() + ", " + pair.getElementB() + "</p>";
				}
			}
			
			if (equivalentAttributes.size() > 0) {
				pitfallText += "<br><p>The following attributes could be defined as equivalent:</p>";
				
				for (ElementPair pair : equivalentAttributes) {
					pitfallText += "<p>> " + pair.getElementA() + ", " + pair.getElementB() + "</p>";
				}
			}

			break;
		case OOPSEvaluator.PITFALL_MIGHT_BE_INVERSE_ID:
			List<ElementPair> mightBeInverseRelations = evaluationResult.getMightBeInverseRelations();
			
			if (mightBeInverseRelations != null && mightBeInverseRelations.size() > 0) {
				pitfallText += "<p>OOPS! has the following suggestions for the relationships without inverse:</p>";
			}
			
			for (ElementPair pair : mightBeInverseRelations) {
				pitfallText += "<p>> " + pair.getElementA() + " could be inverse of " + pair.getElementB() + "</p>";
			}
			
			List<String> relationsWithoutInverse = evaluationResult.getRelationsWithoutInverse();
			
			if (relationsWithoutInverse != null && relationsWithoutInverse.size() > 0) {
				pitfallText += "<p>Sorry, OOPS! has no suggestions for the following relationships without inverse:</p>";
			}
			
			for (String relationWithoutInverse : relationsWithoutInverse) {
				pitfallText += "<p>> " + relationWithoutInverse + "</p>";
			}
			
			break;
		case OOPSEvaluator.PITFALL_SAME_LABEL:
			List<ElementPair> elementsWithSameLabel = evaluationResult.getElementsWithSameLabel();
			
			if (elementsWithSameLabel.size() > 0) {
				pitfallText += "<p>The following elements have the same label:</p>";
			}
			
			for (ElementPair pair : elementsWithSameLabel) {
				pitfallText += "<p>> " + pair.getElementA() + ", " + pair.getElementB() + "</p>";
			}
			
			break;
		case OOPSEvaluator.PITFALL_WRONG_INVERSE_ID:
			List<ElementPair> wrongInverseRelations = evaluationResult.getWrongInverseRelations();
			
			if (wrongInverseRelations.size() > 0) {
				pitfallText += "<p>OOPS! has the following suggestions for the relationships without inverse:</p>";
			}
			
			for (ElementPair pair : wrongInverseRelations) {
				pitfallText += "<p>> " + pair.getElementA() + " may not be inverse of " + pair.getElementB() + "</p>";
			}
			
			break;
		default:
			if (OOPSEvaluator.isGeneralPitfall(pitfall.getPitfallID())) {
				pitfallText += "<p>*This pitfall applies to the ontology in general instead of "
						+ "specific elements.</p>";
			} else {
				pitfallText += "<p>This pitfall appears in the following elements:</p>";

				for (String element : affectedElements) {
					pitfallText += "<p>> " + element + "</p>";
				}
			}
		}
		
		pitfallText += "</html>";
		
		return pitfallText;
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
		btnListAllPitfalls.setEnabled(true); // re-enable after the evaluation is done
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
	
	/**
	 * Custom CardLayout implementation that adapts the layout to each card's preferred size
	 */
    private class AdaptiveCardLayout extends CardLayout {
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Component current = findCurrentComponent(parent);
            if (current != null) {
                Insets insets = parent.getInsets();
                Dimension pref = current.getPreferredSize();
                pref.width += insets.left + insets.right;
                pref.height += insets.top + insets.bottom;
                return pref;
            }
            return super.preferredLayoutSize(parent);
        }

        public Component findCurrentComponent(Container parent) {
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()) {
                    return comp;
                }
            }
            return null;
        }
    }
}
