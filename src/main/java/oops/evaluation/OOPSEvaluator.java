package oops.evaluation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oops.model.ElementPair;
import oops.model.EvaluationResult;
import oops.model.Pitfall;
import oops.model.PitfallImportanceLevel;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Evaluation service for the OOPS! plugin that uses the OOPS!(oops.linkeddata.es) web service
 * for the evaluation logic.
 */
public class OOPSEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(OOPSEvaluator.class);
    
    private static final String OOPS_WS_ENDPOINT = "http://oops-ws.oeg-upm.net/rest";
    
    private static final String OOPS_WS_REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    		+ "<OOPSRequest>"
    		+ "    <OntologyURI></OntologyURI>"
    		+ "    <OntologyContent><![CDATA[ %s ]]></OntologyContent>"
    		+ "    <Pitfalls>%s</Pitfalls>"
    		+ "    <OutputFormat>XML</OutputFormat>"
    		+ "</OOPSRequest>";
    
    private static final int OOPS_WS_TIMEOUT = 30 * 1000; // set OOPS! WS timeout to 30s
    
	private static final String OOPS_XML_PREFIX = "oops:";
	private static final String OOPS_TAG_RESPONSE = OOPS_XML_PREFIX + "OOPSResponse";
	private static final String OOPS_TAG_PITFALL = OOPS_XML_PREFIX + "Pitfall";
	private static final String OOPS_TAG_NAME = OOPS_XML_PREFIX + "Name";
	private static final String OOPS_TAG_CODE = OOPS_XML_PREFIX + "Code";
	private static final String OOPS_TAG_DESCRIPTION = OOPS_XML_PREFIX + "Description";
	private static final String OOPS_TAG_IMPORTANCE = OOPS_XML_PREFIX + "Importance";
	private static final String OOPS_TAG_NUMBER_AFFECTED_ELEMS = OOPS_XML_PREFIX + "NumberAffectedElements";
	private static final String OOPS_TAG_AFFECTS = OOPS_XML_PREFIX + "Affects";
	private static final String OOPS_TAG_AFFECTED_ELEM = OOPS_XML_PREFIX + "AffectedElement";
	private static final String OOPS_TAG_WRONG_INVERSE = OOPS_XML_PREFIX + "MightNotBeInverseOf";
	private static final String OOPS_TAG_MIGHT_BE_INVERSE = OOPS_XML_PREFIX + "MightBeInverse";
	private static final String OOPS_TAG_EQUIVALENT_PROPERTY = OOPS_XML_PREFIX + "MightBeEquivalentProperty";
	private static final String OOPS_TAG_EQUIVALENT_ATTRIBUTE = OOPS_XML_PREFIX + "MightBeEquivalentAttribute";
	private static final String OOPS_TAG_EQUIVALENT_CLASSES = OOPS_XML_PREFIX + "MightBeEquivalentClass";
	private static final String OOPS_TAG_NO_INVERSE_SUGGESTION = OOPS_XML_PREFIX + "NoInverseSuggestion";
	private static final String OOPS_TAG_SAME_LABEL = OOPS_XML_PREFIX + "HaveSameLabel";
	
	public static final String PITFALL_WRONG_INVERSE_ID = "P05";
	public static final String PITFALL_MIGHT_BE_INVERSE_ID = "P13";
	public static final String PITFALL_MIGHT_BE_EQUIVALENT_ID = "P12";
	public static final String PITFALL_EQUIVALENT_CLASSES_ID = "P30";
	public static final String PITFALL_SAME_LABEL = "P32";
	public static final String PITFALL_DIFF_NAMING_CONVENTIONS_ID = "P22";
	
	// pitfalls that apply to the ontology in general
	private static final String generalPitfalls[] = { "P22", "P38", "P39", "P41" };
	
	private static final String OWL_THING_IRI = "http://www.w3.org/2002/07/owl#Thing";

    private static OWLOntology activeOntology;
    
    private static List<String> pitfallsSubset;
    
    private static OOPSEvaluator instance = null;
    
    private static ArrayList<EvaluationListener> listeners = new ArrayList<EvaluationListener>();
    
    private static EvaluationResult evaluationResults = null;

	/**
     * A runnable task that completes the ontology evaluation process using the OOPS! Web Service
     */
    private static Runnable evaluationTask = () -> {
    	listeners.forEach(l -> l.onEvaluationStarted()); // notify all listeners about evaluation start
    	
    	Instant startInstant = Instant.now();
    	logger.info(String.format("evaluationTask[OOPSEvaluator] in thread %s", Thread.currentThread().getName()));
		
		activeOntology.getOWLOntologyManager();
		
		StringWriter rdfWriter = new StringWriter();
		RDFXMLRenderer rdfRenderer = new RDFXMLRenderer(activeOntology, rdfWriter);
		
		try {
			rdfRenderer.render();
			
			String rdfFormattedOntology = rdfWriter.toString();
			
			String pitfallsField = pitfallsSubset.stream().collect(Collectors.joining(","));
			String oopsRequestBody = String.format(OOPS_WS_REQUEST_TEMPLATE, rdfFormattedOntology, pitfallsField);
			
			String oopsResponse = sendOOPSRequest(oopsRequestBody);
			
			logger.info("The oopsResponse is -> " + oopsResponse);
			
			evaluationResults = getResultsFromResponse(oopsResponse);
	        
	        logger.info(String.format("evaluationTask[OOPSEvaluator] finished in %d seconds", 
					Duration.between(startInstant, Instant.now()).getSeconds()));
	        
	        listeners.forEach(l -> l.onEvaluationDone(evaluationResults)); // send results to each listener
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			listeners.forEach(l -> l.OnEvaluationException(e));
		}
    };
	
	/**
	 * Returns an OOPSEvaluator singleton instance
	 * 
	 * @return OOPSEvaluator singleton instance
	 */
	public static OOPSEvaluator getInstance() {
		if (instance == null) {
			instance = new OOPSEvaluator();
		}

		return instance;
	}
	
	/**
	 * Send the required message to OOPS! WS endpoint and returns its response
	 * 
	 * @param oopsRequestBody
	 *            the message to send
	 * @return the response text
	 * @throws Exception
	 */
	private static String sendOOPSRequest(String oopsRequestBody) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) new URL(OOPS_WS_ENDPOINT).openConnection();
		connection.setRequestMethod("POST");
		connection.setReadTimeout(OOPS_WS_TIMEOUT);
		
		logger.info("Preparing for OOPS! WS post request...");
		
		// Send POST request
		connection.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(oopsRequestBody);
		wr.flush();
		wr.close();
		
		int responseCode = connection.getResponseCode();
		
		logger.info("The response code is -> " + responseCode);
		
		if (responseCode == 200) {
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(connection.getInputStream()));
			String response = in.lines().collect(Collectors.joining("\n"));
			
			return response;
		} else {
			throw new Exception("The OOPS! web service request has failed with status code " + responseCode);
		}
	}
	
	/**
	 * Parses the OOPS! WS response and returns the organised results
	 * 
	 * @param oopsResponse
	 *            the response from the OOPS! WebService
	 * @return OOPS! WS results
	 * @throws Exception
	 */
	private static EvaluationResult getResultsFromResponse(String oopsResponse) throws Exception {
		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		Document doc = dBuilder.parse(new ByteArrayInputStream(oopsResponse.getBytes()));
		doc.getDocumentElement().normalize();
		
		Element parsedResponse = (Element) doc.getElementsByTagName(OOPS_TAG_RESPONSE).item(0);
		
		NodeList pitfallsList = parsedResponse.getElementsByTagName(OOPS_TAG_PITFALL);
		
		HashMap<String, ArrayList<Pitfall>> detectedPitfalls = new HashMap<String, ArrayList<Pitfall>>();
		
		evaluationResults = new EvaluationResult();
		
		if (pitfallsList.getLength() == 0) {
			logger.info("There are no pitfalls!");
		} else {
			logger.info(String.format("There are %d pitfalls!  -->>", pitfallsList.getLength()));
			for (int i = 0; i < pitfallsList.getLength(); i++) {
				Element pitfall = (Element) pitfallsList.item(i);
				Node pitfallDescriptionNode = pitfall.getElementsByTagName(OOPS_TAG_DESCRIPTION).item(0);
				Node pitfallCodeNode = pitfall.getElementsByTagName(OOPS_TAG_CODE).item(0);
				Node pitfallNameNode = pitfall.getElementsByTagName(OOPS_TAG_NAME).item(0);
				Node pitfallImportanceNode = pitfall.getElementsByTagName(OOPS_TAG_IMPORTANCE).item(0);
				Node pitfallNumAffectedElemsNode = pitfall.getElementsByTagName(OOPS_TAG_NUMBER_AFFECTED_ELEMS).item(0);
				Element pitfallAffectsElement = (Element) pitfall.getElementsByTagName(OOPS_TAG_AFFECTS).item(0);

				String pitfallDescription = pitfallDescriptionNode.getTextContent();
				String pitfallCode = pitfallCodeNode.getTextContent();
				String pitfallName = pitfallNameNode.getTextContent();
				String pitfallImportance = pitfallImportanceNode.getTextContent();
				int pitfallNumAffectedElems = Integer.parseInt(pitfallNumAffectedElemsNode.getTextContent());
				
				switch (pitfallCode) {
				case PITFALL_EQUIVALENT_CLASSES_ID:
					NodeList mightBeEquivalentClassList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_EQUIVALENT_CLASSES);
					
					ArrayList<ElementPair> equivalentClasses = new ArrayList<ElementPair>();
					
					for (int j = 0; j < mightBeEquivalentClassList.getLength(); j++) {
						Element mightBeEquivalentNode = (Element) mightBeEquivalentClassList.item(j);
						
						NodeList equivalentClassesNodes = mightBeEquivalentNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = equivalentClassesNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = equivalentClassesNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						equivalentClasses.add(new ElementPair(equivalent1, equivalent2));
					}
					
					evaluationResults.setEquivalentClasses(equivalentClasses);
					break;
				case PITFALL_MIGHT_BE_EQUIVALENT_ID:
					NodeList mightBeEquivalentPropertyList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_EQUIVALENT_PROPERTY);
					NodeList mightBeEquivalentAttributeList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_EQUIVALENT_ATTRIBUTE);
					
					ArrayList<ElementPair> equivalentProperties = new ArrayList<ElementPair>();
					ArrayList<ElementPair> equivalentAttributes = new ArrayList<ElementPair>();
					
					for (int j = 0; j < mightBeEquivalentPropertyList.getLength(); j++) { // eq properties loop
						Element mightBeEquivalentPropertyNode = (Element) mightBeEquivalentPropertyList.item(j);
						
						NodeList equivalentPropertyNodes = mightBeEquivalentPropertyNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = equivalentPropertyNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = equivalentPropertyNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						equivalentProperties.add(new ElementPair(equivalent1, equivalent2));
					}
					
					for (int j = 0; j < mightBeEquivalentAttributeList.getLength(); j++) { // eq attributes loop
						Element mightBeEquivalentAttributeNode = (Element) mightBeEquivalentAttributeList.item(j);
						
						NodeList equivalentAttributesNodes = mightBeEquivalentAttributeNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = equivalentAttributesNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = equivalentAttributesNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						equivalentAttributes.add(new ElementPair(equivalent1, equivalent2));
					}
					
					if (equivalentProperties.size() > 0) {
						evaluationResults.setEquivalentRelations(equivalentProperties);
					}
					
					if (equivalentAttributes.size() > 0) {
						evaluationResults.setEquivalentAttributes(equivalentAttributes);
					}
					
					break;
				case PITFALL_MIGHT_BE_INVERSE_ID:
					// MIGHT BE INVERSE
					NodeList mightBeInverseList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_MIGHT_BE_INVERSE);
					
					List<ElementPair> inverseRelations = new ArrayList<ElementPair>();
					
					for (int j = 0; j < mightBeInverseList.getLength(); j++) {
						Element mightBeInverseNode = (Element) mightBeInverseList.item(j);
						
						NodeList inverseNodes = mightBeInverseNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = inverseNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = inverseNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						inverseRelations.add(new ElementPair(equivalent1, equivalent2));
					}
					
					evaluationResults.setMightBeInverseRelations(inverseRelations);
					
					// NO INVERSE SUGGESTION
					NodeList noInverseSuggestionNodes = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_NO_INVERSE_SUGGESTION);
					
					List<String> noInverseSuggestions = new ArrayList<String>();
					
					for (int j = 0; j < noInverseSuggestionNodes.getLength(); j++) {
						Element noInverseNode = (Element) noInverseSuggestionNodes.item(j);
						
						NodeList inverseNodes = noInverseNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						
						for (int nodeIndex = 0; nodeIndex < inverseNodes.getLength(); nodeIndex++) {
							Node affectedElement = inverseNodes.item(nodeIndex);
							String affectedElementIRI = affectedElement.getTextContent();
							if (!detectedPitfalls.containsKey(affectedElementIRI)) {
								detectedPitfalls.put(affectedElementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(affectedElementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
							
							noInverseSuggestions.add(affectedElementIRI);
						}
					}
					
					evaluationResults.setRelationsWithoutInverse(noInverseSuggestions);
					
					break;
				case PITFALL_WRONG_INVERSE_ID:
					NodeList wrongInverseList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_WRONG_INVERSE);
					
					ArrayList<ElementPair> wrongInverseRelations = new ArrayList<ElementPair>();
					
					for (int j = 0; j < wrongInverseList.getLength(); j++) {
						Element wrongInverseNode = (Element) wrongInverseList.item(j);
						
						NodeList wrongInverseNodes = wrongInverseNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = wrongInverseNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = wrongInverseNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						wrongInverseRelations.add(new ElementPair(equivalent1, equivalent2));
					}
					
					evaluationResults.setWrongInverseRelations(wrongInverseRelations);
					
					break;
				case PITFALL_SAME_LABEL:
					NodeList haveSameLabelNodeList = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_SAME_LABEL);
					
					ArrayList<ElementPair> elementsWithSameLabel = new ArrayList<ElementPair>();
					
					for (int j = 0; j < haveSameLabelNodeList.getLength(); j++) {
						Element hasSameLabelNode = (Element) haveSameLabelNodeList.item(j);
						
						NodeList sameLabelNodes = hasSameLabelNode.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
						String equivalent1 = sameLabelNodes.item(0).getFirstChild().getNodeValue();
						String equivalent2 = sameLabelNodes.item(1).getFirstChild().getNodeValue();
						
						for (String elementIRI : new String[]{equivalent1, equivalent2}) {
							if (!detectedPitfalls.containsKey(elementIRI)) {
								detectedPitfalls.put(elementIRI, new ArrayList<Pitfall>());
							}
							
							detectedPitfalls.get(elementIRI).add(
									new Pitfall(
											PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
											pitfallCode,
											pitfallName,
											pitfallDescription,
											pitfallNumAffectedElems));
						}
						
						elementsWithSameLabel.add(new ElementPair(equivalent1, equivalent2));
					}
					
					evaluationResults.setElementsWithSameLabel(elementsWithSameLabel);
					
					break;
				case PITFALL_DIFF_NAMING_CONVENTIONS_ID:
					detectedPitfalls.put(OWL_THING_IRI, new ArrayList<Pitfall>(Arrays.asList(new Pitfall(
							PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
							pitfallCode,
							pitfallName,
							pitfallDescription,
							pitfallNumAffectedElems))));
					
					break;
				default:
					NodeList affectedElements = pitfallAffectsElement.getElementsByTagName(OOPS_TAG_AFFECTED_ELEM);
					for (int j = 0; j < affectedElements.getLength(); j++) {
						Node affectedElement = affectedElements.item(j);
						String affectedElementIRI = affectedElement.getTextContent();
						logger.info(String.format("The pitfall for elem <%s> : [%s][%s] - (%s) -> Description : %s",
								affectedElementIRI, pitfallCode, pitfallImportance, pitfallName, pitfallDescription));
						if (!detectedPitfalls.containsKey(affectedElementIRI)) {
							detectedPitfalls.put(affectedElementIRI, new ArrayList<Pitfall>());
						}
						
						detectedPitfalls.get(affectedElementIRI).add(
								new Pitfall(
										PitfallImportanceLevel.valueOf(pitfallImportance.toUpperCase()),
										pitfallCode,
										pitfallName,
										pitfallDescription,
										pitfallNumAffectedElems));
					}
				}
			}
		}
        
		evaluationResults.setDetectedPitfalls(detectedPitfalls);
        
        return evaluationResults;
	}
	
	/**
	 * Returns true if the specified pitfall affects the ontology itself
	 * 
	 * @param pitfallCode
	 *            the specified pitfall code
	 * @return true if the specified pitfall affects the ontology itself
	 */
	public static boolean isGeneralPitfall(String pitfallCode) {
		return Stream.of(generalPitfalls).anyMatch(p -> p.equals(pitfallCode));
	}
	
    /**
	 * @return the evaluationResults
	 */
	public static EvaluationResult getEvaluationResults() {
		return evaluationResults;
	}
	
	/**
	 * Add a listener for evaluation events
	 * 
	 * @param listener
	 *            the evaluation events listener to add
	 */
	public void addListener(EvaluationListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove a listener for evaluation events
	 * 
	 * @param listener
	 *            the evaluation events listener to remove
	 */
	public void removeListener(EvaluationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method is synchronized, only lets one thread to execute it and
	 * checks if the evaluation is already done. If there are results already
	 * available, it returns them directly. Otherwise, it evaluates the
	 * ontology, saves the results and returns them.
	 * 
	 * @param ontology
	 *            the ontology to evaluate
	 * @return the results after evaluating the given ontology
	 * @throws InterruptedException
	 */
	public void evaluate(OWLOntology ontology, List<String> pitfallsSubset) throws InterruptedException {
		OOPSEvaluator.activeOntology = ontology;
		OOPSEvaluator.pitfallsSubset = pitfallsSubset;
		
		Thread thread = new Thread(evaluationTask);
		thread.start();
	}

}
