package oops.model;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Data Model for ontological pitfalls.
 */
public class Pitfall extends InfoElement implements Comparable<Pitfall> {
	private PitfallImportanceLevel importanceLevel;
	private String pitfallID;

	public Pitfall(PitfallImportanceLevel importanceLevel, String pitfallID, String name, String description,
			int numAffectedElements) {
		super(name, description, numAffectedElements);
		
		this.importanceLevel = importanceLevel;
		this.pitfallID = pitfallID;
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

	@Override
	public String toString() {
		return String.format("%s[%s] - %s", pitfallID, importanceLevel, super.getDescription());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Pitfall)) return false;
	    Pitfall otherPitfall = (Pitfall)other;
		return this.importanceLevel == otherPitfall.importanceLevel && 
				super.getDescription().equals(otherPitfall.getDescription()) &&
				super.getName().equals(otherPitfall.getName()) &&
				this.pitfallID.equals(otherPitfall.pitfallID);
	}
	
	@Override
	public int hashCode() {
		return super.getDescription().hashCode();
	}
	
	@Override
	public int compareTo(Pitfall otherPitfall) {
		return this.pitfallID.compareTo(otherPitfall.pitfallID);
	}
}