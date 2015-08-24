package com.hlcl.rql.as;

import java.util.Map;
import java.util.Set;

/**
 * Instanzen dieser Klasse beschreiben ein Bestandteil einer RQL-Antwort
 * vom CMS. Dabei kann es sich um ein Element (d.h. ein Tag) oder um einen
 * Text innerhalb eines Tags handeln.
 * 
 * @author BURMEBJ
 */

public abstract class RQLNode {
	/**
	 * Wenn dieser RQLNode Bestandteil eines Baumes ist, enthält
	 * diese Feld den Parent.
	 */
	private RQLTagNode parent = null;

	/**
	 *  Setter für das Feld "parent".
	 * 
	 * @param parent RQLTagNode: s.o.
	 */
	void setParent(RQLTagNode parent) {
		this.parent = parent;
	}

	/**
	 * Getter für das Feld "parent".
	 * 
	 * @return RQLTagNode: s.o.
	 */
	RQLTagNode getParent() {
		return parent;
	}

	/**
	 * Diese Methode liefert true, wenn es sich bei diesem RQLNode um ein 
	 * Tag handelt, ansonsten false.
	 * 
	 * @return boolean: s.o.
	 */
	public abstract boolean isTagNode();

	/**
	 * Diese Methode liefert true, wenn es sich bei diesem RQLNode um einen
	 * Text handelt, ansonsten false.
	 * 
	 * @return boolean: s.o.
	 */
	public abstract boolean isTextNode();

	/**
	 * Diese Methode liefert den Wert des übergebenen Attributes. Falls
	 * dieser RQLNode ein Text ist, liefert die Methode null. Das ist auch
	 * dann der Fall, wenn es sich bei diesem RQLNode um ein Tag handelt,
	 * das das angegebene Attribut nicht enthält.
	 * 
	 * @param key String: s.o.
	 * @return String: s.o.
	 */
	public abstract String getAttribute(String key);

	/**
	 * Diese Methode liefert die children dieses RQLNodes in Form einer
	 * RQLNodeList.
	 * 
	 * @return RQLNodeList: s.o.
	 */
	public abstract RQLNodeList getChildren();

	/**
	 *  Diese Methode liefert den Namen dieses RQLNodes. Das ist im Falle
	 * eines Tags dessen Name. Im Falle eines texted liefert die Methode
	 * null.
	 * 
	 * @return String: s.o.
	 */
	public abstract String getName();

	/**
	 * Diese Methode liefert den Text dieses Nodes. Handelt es sich um einen
	 * TextNode, so wird einfach dieser Text geliefert. Handelt es sich um
	 * einen Tag-Node, so liefert die Methode:
	 * - null, wenn das Tag keine children hat,
	 * - den enthaltenen Text, wenn das Tag genau ein child vom Typ "Text" hat.
	 * - andernfalls kommt es zu einer RuntimeException.
	 * 
	 * @return String: s.o.
	 */
	public abstract String getText() throws RQLException;

	/**
	 * Diese Methode sucht diesen RQLNode und alle Kinder, Enkel, ... darauf
	 * ab, ob es Tags gibt, deren Name gleich dem übergebenen nodeName ist.
	 * Alle gefundenen RQLNodes werden der übergebenen RQLNodeList hinzu-
	 * gefügt.
	 *
	 * @param nodeName String: s.o.
	 * @param list RQLNodeList: s.o.
	 */
	private void addNodesToList(String nodeName, RQLNodeList list) {
		if (this instanceof RQLTagNode) // TextNodes haben keine children.
		{
			if (nodeName.equals(getName())) {
				list.add(this);
			}
			RQLNodeList children = getChildren();
			for (int i = 0; i < (children != null ? children.size() : 0); i++) {
				children.get(i).addNodesToList(nodeName, list);
			}
		}
	}

	/**
	 * Diese Methode durchsucht diesen RQLNode und alle Nachkommen (children, ...)
	 * nach Tags mit dem Namen nodeName. Wenn alle gefundenen Tags den gleichen parent
	 * haben, werden diese Tags in Form einer RQLNodeList zurückgegeben. Wenn Tags mit
	 * unterschiedlichen Parents gefunden werden, kommt es zu einer RuntimeException.
	 * Wenn keine Tags gefunden werden, liefert die Methode null zurück.
	 * 
	 * @param nodeName String: s.o.
	 * @return RQLNodeList: s.o.
	 * @throws RQLException 
	 */
	public RQLNodeList getNodes(String nodeName) throws RQLException {
		RQLNodeList returnList = new RQLNodeList();
		addNodesToList(nodeName, returnList);

		// Haben alle gefundenen Tags den gleichen Parent?

		RQLNode parentNode = null;
		boolean isFirstNode = true;

		for (int i = 0; i < returnList.size(); i++) {
			RQLNode node = returnList.get(i);
			if (isFirstNode) {
				parentNode = node.getParent();
				isFirstNode = false;
			} else if (node.getParent() != parentNode) {
				throw new RQLException("RQLHelper.getNodes(): parent not unique!");
			}
		}

		if (returnList.size() == 0)
			return null;

		return returnList;
	}

	
	/**
	 * Diese Methode durchsucht diesen RQLNode und alle Nachkommen (children, ...)
	 * nach Tags mit dem Namen nodeName. 
	 * 
	 * @param nodeName element to look for
	 * @return RQLNodeList a possibly empty list.
	 */
	public RQLNodeList getNodesRecursive(String nodeName) {
		RQLNodeList returnList = new RQLNodeList();
		addNodesToList(nodeName, returnList);
		return returnList;
	}

		
	/**
	 * Diese Methode durchsucht diesen RQLNode und dessen Nachkommen (children, ...)
	 * darauf, ob es ein Tag mit dem Namen nodeName gibt. Wenn genau ein solches Tag 
	 * gefunden wird, wird der entsprechende RQLNode zurückgegeben. Wenn kein Tag
	 * gefunden wird, wird null zurückgegeben. Wenn mehrere Tags des gesuchten Namens
	 * gefunden werden, kommt es zu einer RuntimeException.
	 * 
	 * @param nodeName String: s.o.
	 * @return RQLNode: s.o.
	 * @throws RQLException 
	 */
	public RQLNode getNode(String nodeName) throws RQLException {
		RQLNodeList l = getNodes(nodeName);

		if (l == null) {
			return null;
		} else if (l.size() == 1) {
			return (RQLNode) l.get(0);
		} else {
			throw new RQLException("RQLHelper.getNode(): too many (" + l.size() +") nodes found: " + nodeName);
		}
	}

	/**
	 * Returns a new instance of this node with same data.
	 */
	public abstract RQLNode copy();

	/**
	 * Returns a new instance with same data as this node except the given attributes names skipAttributeNames. Children are copied as well.
	 */
	public abstract RQLNode copy(String skipAttributeNames, String delimiter);

	/**
	 * Deliver all attribute key values of this tag.
	 */
	public abstract Set<String> getAttributeKeys();

	/**
	 * Deliver all attributes of this tag as an key value map.
	 */
	public abstract Map<String, String> getAttributes();
	

	/**
	 * Test if this is an XML element of the given name.
	 * 
	 * @param elementName the "tag" to look for.
	 */
	public boolean isTag(String elementName)
	{
		return isTagNode() && elementName.equals(getName());
	}

}
