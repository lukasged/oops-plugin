package oops.model;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br>
 * <br>
 *
 * Data Model for elements that hold OOPS! information like pitfalls, warnings
 * and suggestions.
 */
public class InfoElement {
	private String name;
	private String description;
	private int numAffectedElements;

	public InfoElement(String name, String description, int numAffectedElements) {
		this.name = name;
		this.description = description;
		this.numAffectedElements = numAffectedElements;
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
		return String.format("%s - %s", name, description);
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
}
