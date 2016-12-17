package oops.model;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Data Model for ontological pitfalls.
 */
public class Pitfall {
    private PitfallImportanceLevel importanceLevel;
    private String pitfallID;
    private String description;
    
    public Pitfall(PitfallImportanceLevel importanceLevel, String pitfallID, String description) {
        this.importanceLevel = importanceLevel;
        this.pitfallID = pitfallID;
        this.description = description;
    }

    /**
     * @return the importanceLevel
     */
    public PitfallImportanceLevel getImportanceLevel() {
        return importanceLevel;
    }

    /**
     * @param importanceLevel the importanceLevel to set
     */
    public void setImportanceLevel(PitfallImportanceLevel importanceLevel) {
        this.importanceLevel = importanceLevel;
    }

    /**
     * @return the pitfallID
     */
    public String getPitfallID() {
        return pitfallID;
    }

    /**
     * @param pitfallID the pitfallID to set
     */
    public void setPitfallID(String pitfallID) {
        this.pitfallID = pitfallID;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return String.format("%s[%s] - %s", pitfallID, importanceLevel, description);
    }
}