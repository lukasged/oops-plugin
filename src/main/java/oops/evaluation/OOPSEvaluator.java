package oops.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static OOPSEvaluator instance = null;
    
    private static EvaluationResult evaluationResults = null;
	
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
	 * Resets the evaluation results to prepare for a new evaluation
	 */
	public void resetEvaluationResults() {
		evaluationResults = null;
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
	public synchronized EvaluationResult evaluate(OWLOntology ontology) throws InterruptedException {
		if (evaluationResults != null) { // return the results directly, if available
			return evaluationResults;
		} else { // when the results are not available, generate them
			HashMap<String, ArrayList<Pitfall>> detectedPitfalls = new HashMap<String, ArrayList<Pitfall>>();
			
			Thread.sleep(7000); // simulation of a long-running web service call
			
			detectedPitfalls.put("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza",
					new ArrayList<Pitfall>(
							Arrays.asList(new Pitfall(PitfallImportanceLevel.IMPORTANT, "P1", "P1 is about bla bla"),
									new Pitfall(PitfallImportanceLevel.CRITICAL, "P3", "P3 must be avoided!"),
									new Pitfall(PitfallImportanceLevel.CRITICAL, "P9", "P9 is unacceptable!"))));

			detectedPitfalls.put("http://www.co-ode.org/ontologies/pizza/pizza.owl#Spiciness",
					new ArrayList<Pitfall>(
							Arrays.asList(new Pitfall(PitfallImportanceLevel.MINOR, "P2", "Missing annotations..."),
									new Pitfall(PitfallImportanceLevel.IMPORTANT, "P8",
											"The ontology lacks information about equivalent properties "
													+ "(owl:equivalentProperty) in the cases of duplicated "
													+ "relationships and/or attributes."))));
	        
	        evaluationResults = new EvaluationResult(detectedPitfalls);
			
			return evaluationResults;
		}
	}

}
