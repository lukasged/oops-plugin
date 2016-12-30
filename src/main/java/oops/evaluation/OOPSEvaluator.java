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
import java.util.HashMap;
import java.util.stream.Collectors;

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
    		+ "    <Pitfalls></Pitfalls>"
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

    private static OWLOntology activeOntology;
    
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
			
			String oopsRequestBody = String.format(OOPS_WS_REQUEST_TEMPLATE, rdfFormattedOntology);
			
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
				Node pitfallNumberAffectedElemsNode = pitfall.getElementsByTagName(OOPS_TAG_NUMBER_AFFECTED_ELEMS).item(0);
				Element pitfallAffectsElement = (Element) pitfall.getElementsByTagName(OOPS_TAG_AFFECTS).item(0);

				String pitfallDescription = pitfallDescriptionNode.getTextContent();
				String pitfallCode = pitfallCodeNode.getTextContent();
				String pitfallName = pitfallNameNode.getTextContent();
				String pitfallImportance = pitfallImportanceNode.getTextContent();
				
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
									pitfallDescription));
				}
			}
		}
        
        evaluationResults = new EvaluationResult(detectedPitfalls);
        
        return evaluationResults;
	}

	/**
	 * Resets the evaluation results to prepare for a new evaluation
	 */
	public void resetEvaluationResults() {
		evaluationResults = null;
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
	public void evaluate(OWLOntology ontology) throws InterruptedException {
		activeOntology = ontology;
		
		Thread thread = new Thread(evaluationTask);
		thread.start();
	}

}
