package oops.model;

/**
 * Author: Lukas Gedvilas<br>
 * Universidad Politécnica de Madrid<br><br>
 *
 * Data Model to represent pairs of OWL Elements.
 */
public class ElementPair {
	private String elementA;
	private String elementB;
	
	public ElementPair(String elementA, String elementB) {
		this.elementA = elementA;
		this.elementB = elementB;
	}

	/**
	 * @return the elementA
	 */
	public String getElementA() {
		return elementA;
	}

	/**
	 * @param elementA
	 *            the elementA to set
	 */
	public void setElementA(String elementA) {
		this.elementA = elementA;
	}

	/**
	 * @return the elementB
	 */
	public String getElementB() {
		return elementB;
	}

	/**
	 * @param elementB
	 *            the elementB to set
	 */
	public void setElementB(String elementB) {
		this.elementB = elementB;
	}
}
