package oops.model;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Data Model for ontological pitfalls.
 */
public class Pitfall implements Comparable<Pitfall> {
	private PitfallImportanceLevel importanceLevel;
	private String pitfallID;
	private String name;
	private String description;
	private int numAffectedElements;

	public Pitfall(PitfallImportanceLevel importanceLevel, String pitfallID, String name, String description,
			int numAffectedElements) {
		this.importanceLevel = importanceLevel;
		this.pitfallID = pitfallID;
		this.name = name;
		this.description = description;
		this.numAffectedElements = numAffectedElements;
	}

	/**
	 * @return the importanceLevel
	 */
	public PitfallImportanceLevel getImportanceLevel() {
		return importanceLevel;
	}

	/**
	 * @param importanceLevel
	 *            the importanceLevel to set
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
	 * @param pitfallID
	 *            the pitfallID to set
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
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return String.format("%s[%s] - %s", pitfallID, importanceLevel, description);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the numAffectedElements
	 */
	public int getNumAffectedElements() {
		return numAffectedElements;
	}

	/**
	 * @param numAffectedElements the numAffectedElements to set
	 */
	public void setNumAffectedElements(int numAffectedElements) {
		this.numAffectedElements = numAffectedElements;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Pitfall)) return false;
	    Pitfall otherPitfall = (Pitfall)other;
		return this.importanceLevel == otherPitfall.importanceLevel && 
				this.description.equals(otherPitfall.description) &&
				this.name.equals(otherPitfall.name) &&
				this.pitfallID.equals(otherPitfall.pitfallID);
	}
	
	@Override
	public int hashCode() {
		return description.hashCode();
	}
	
	@Override
	public int compareTo(Pitfall otherPitfall) {
		return this.pitfallID.compareTo(otherPitfall.pitfallID);
	}
}