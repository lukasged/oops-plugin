package oops.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Data Model to represent ontology evaluation results with the detected pitfalls.
 */
public class EvaluationResult {
    private HashMap<String, ArrayList<Pitfall>> detectedPitfalls;
    
    public EvaluationResult(HashMap<String, ArrayList<Pitfall>> detectedPitfalls) {
        this.detectedPitfalls = detectedPitfalls;
    }
    
    public PitfallImportanceLevel getHighestImportanceLevelForEntity(String entityURI) {
        ArrayList<Pitfall> pitfalls = detectedPitfalls.get(entityURI);
        
        if (pitfalls != null) {
            return PitfallImportanceLevel.IMPORTANT;
        } else {
            return null;
        }
    }
    
    public ArrayList<Pitfall> getPitfallsForOWLEntity(String entityURI) {
        return detectedPitfalls.get(entityURI);
    }
    
    public int getNumberOfPitfalls(PitfallImportanceLevel importance) {   	
    	long pitfallsCount = detectedPitfalls.values().stream()
    		.flatMap(x -> x.stream())
    		.filter(p -> p.getImportanceLevel() == importance)
    		.count();
    	
    	return (int)pitfallsCount;
    }
    
    public HashMap<String, ArrayList<Pitfall>> detectedPitfalls() {
    	return detectedPitfalls;
    }
    
    public TreeMap<Pitfall, ArrayList<String>> pitfallsWithAffectedElements() {
    	TreeMap<Pitfall, ArrayList<String>> pitfallsWithAffectedElements = new TreeMap<Pitfall, ArrayList<String>>();
    	
    	detectedPitfalls.forEach((element,pitfalls) -> {
    		pitfalls.forEach(p -> {
    			if (pitfallsWithAffectedElements.containsKey(p)) {
    				pitfallsWithAffectedElements.get(p).add(element); // merge with the elements list
    			} else {
    				pitfallsWithAffectedElements.put(p, new ArrayList<>(Arrays.asList(element))); // add first element
    			}
    		});
    	});
    	
    	return pitfallsWithAffectedElements;
    }
}
