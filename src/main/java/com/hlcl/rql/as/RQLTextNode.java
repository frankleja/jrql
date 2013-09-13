package com.hlcl.rql.as;

import java.util.Map;
import java.util.Set;

/**
 * Diese Methode beschreibt einen RQLNode vom Typ "Text".
 * 
 * @author BURMEBJ
 */

class RQLTextNode extends RQLNode {
	/**
	 * Der Text dieses RQLNodes.
	 */
	private String text = null;

	/**
	 * Konstruktor.
	 * 
	 * @param text
	 *            String: Der Text dieses RQLTextNodes.
	 */
	RQLTextNode(String text) {
		this.text = text;
	}

	/**
	 * Diese MEthode liefert den enthaltenen Text.
	 * 
	 * @return String: s.o.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Diese MEthode liefert false.
	 * 
	 * @return boolean: s.o.
	 */
	public boolean isTagNode() {
		return false;
	}

	/**
	 * Diese MEthode liefert true.
	 * 
	 * @return boolean: s.o.
	 */
	public boolean isTextNode() {
		return true;
	}

	/**
	 * Diese Methode liefert null.
	 * 
	 * @return String: s.o.
	 */
	public String getAttribute(String key) {
		return null;
	}

	/**
	 * Diese Methode liefert null.
	 * 
	 * @return String: s.o.
	 */
	public String getName() {
		return null;
	}

	/**
	 * Diese Methode liefert null.
	 * 
	 * @return String: s.o.
	 */
	public RQLNodeList getChildren() {
		return null;
	}

	/**
	 * Returns a new instance with same text as this node.
	 */
	public RQLNode copy() {
		RQLTextNode result = new RQLTextNode(getText());
		// skip children copy
		return result;
	}

	/**
	 * Returns a new instance with the same text as this node; given attributes names are ignored.
	 */
	public RQLNode copy(String skipAttributeNames, String delimiter) {
		return copy();
	}

	/**
	 * Deliver all attribute key values of this tag - returns always null
	 */
	public Set<String> getAttributeKeys() {
		return null;
	}

	/**
	 * Deliver all attributes of this tag as an key value map - returns always null
	 */
	public Map<String, String> getAttributes() {
		return null;
	}
}
