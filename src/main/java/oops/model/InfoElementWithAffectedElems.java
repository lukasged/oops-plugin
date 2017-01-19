package oops.model;

import java.util.List;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br>
 * <br>
 *
 * Data Model for OOPS! warnings and suggestions that hold a list of affected
 * elements.
 */
public class InfoElementWithAffectedElems extends InfoElement {
	private List<String> affectedElements;

	public InfoElementWithAffectedElems(String name, String description, int numAffectedElements, List<String> affectedElements) {
		super(name, description, numAffectedElements);
		
		this.affectedElements = affectedElements;
	}

	/**
	 * @return the affectedElements
	 */
	public List<String> getAffectedElements() {
		return affectedElements;
	}

	/**
	 * @param affectedElements the affectedElements to set
	 */
	public void setAffectedElements(List<String> affectedElements) {
		this.affectedElements = affectedElements;
	}
}
