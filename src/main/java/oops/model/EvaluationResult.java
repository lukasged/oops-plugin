package oops.model;

import java.util.ArrayList;
import java.util.HashMap;

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

}
