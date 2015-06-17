package com.hlcl.rql.as;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Diese Klasse beschreibt einen speziellen RQLNode vom "Tag"-Typ.
 * 
 * @author BURMEBJ
 */

class RQLTagNode extends RQLNode {
	/**
	 * Zurodnung key -> value für die Attribute dieses Tags.
	 */
	private Map<String, String> attributes = null;

	/**
	 * Liste der Kinder dieses RQLTagNodes.
	 */
	private RQLNodeList children = null;

	/**
	 * Name dieses Tags.
	 */
	private String name = null;

	/**
	 * Konstruktor.
	 * 
	 * @param name
	 *            String: Name dieses Tags.
	 */
	RQLTagNode(String name) {
		this.name = name;
	}

	/**
	 * Diese Methode fügt diesem RQLTagNode ein weiteres Attribut hinzu.
	 * 
	 * @param key
	 *            String. s.o.
	 * @param value
	 *            String s.o.
	 */
	void addAttribute(String key, String value) {
		if (attributes == null)
			attributes = new HashMap<String, String>();
		attributes.put(key, value);
	}

	/**
	 * Diese Methode fügt diesem RQLTagNode ein Kind hinzu und setzt gleichzeitig den parent des übergebenen child.
	 * 
	 * @param child
	 *            RQLNode: s.o.
	 */
	void addChild(RQLNode child) {
		if (children == null)
			children = new RQLNodeList();
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Diese Methode liefert den value des übergebenen Attributes. Falls das Attribut nicht existiert, wird null zurückgegeben.
	 * 
	 * @param key
	 *            String : s.o.
	 * @return String: s.o.
	 */
	public String getAttribute(String key) {
		if (attributes == null)
			return null;
		return (String) attributes.get(key);
	}

	/**
	 * Deliver all attribute key values of this tag.
	 */
	public Set<String> getAttributeKeys() {
		return attributes.keySet();
	}

	/**
	 * Deliver all attributes of this tag as an key value map.
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Diese Methode liefert die RQLNodeList der children, evtl. auch null.
	 */
	public RQLNodeList getChildren() {
		return children;
	}

	/**
	 * Diese Methode liefert den Namen dieses Tags.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a new instance with the same data as this node. Child nodes are copied too.
	 */
	public RQLNode copy() {
		RQLTagNode result = new RQLTagNode(getName());
		// copy all attributes
		for (String attributeKey : getAttributeKeys()) {
			result.addAttribute(attributeKey, getAttribute(attributeKey));
		}
		// copy children
		RQLNodeList children = getChildren();
		for (int i = 0; i < children.size(); i++) {
			RQLNode child = children.get(i);
			result.addChild(child.copy());
		}
 		return result;
	}

	/**
	 * Returns a new instance with the same attributes as this node except the given attributes names skipAttributeNames, but always without children.
	 */
	public RQLNode copy(String skipAttributeNames, String delimiter) {
		RQLTagNode result = new RQLTagNode(getName());
		// copy all attributes
		for (String attributeKey : getAttributeKeys()) {
			// copy attribute if not in skip list
			if (!StringHelper.contains(skipAttributeNames, attributeKey, false)) {
				result.addAttribute(attributeKey, getAttribute(attributeKey));
			}
		}
		// skip children copy
		return result;
	}

	/**
	 * Wenn dieses Tag genau ein Kind vom Typ "Text" enthält, liefert diese MEthode diesen Text zurück. Falls dieses Tag keine Kinder hat, liefert die
	 * Methode null. In allen anderen Fällen kommt es zu einer RQLException.
	 * 
	 * @return String: s.o.
	 * @throws RQLException
	 */
	public String getText() throws RQLException {
		if (children == null) {
			return null;
		} else if (children.size() > 1) {
			throw new RQLException("RQLTagNode.getText(): child not unique!");
		} else {
			RQLNode uniqueChild = (RQLNode) children.get(0);
			if (!(uniqueChild instanceof RQLTextNode))
				throw new RQLException("RQLTagNode.getText(): child no text element!");
			else
				return ((RQLTextNode) uniqueChild).getText();
		}
	}

	/**
	 * Diese Methode liefert false.
	 * 
	 * @return boolean: s.o.
	 */
	public boolean isTextNode() {
		return false;
	}

	/**
	 * Diese Methode liefert true.
	 * 
	 * @return boolean: s.o.
	 */
	public boolean isTagNode() {
		return true;
	}
	
	
}
