package checkers.typestate;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public enum TransitionElement {
	AFTER("after"),
	AFTER_TRUE("afterTrue"),
	AFTER_FALSE("afterFalse");

	private final String elementName;

	TransitionElement(String elementName) {
		this.elementName = elementName;
	}

	public String getElementName() {
		return elementName;
	}
}
