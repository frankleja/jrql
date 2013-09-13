package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse beschreibt eine Liste von RQLNodes.
 * 
 * @author BURMEBJ
 */
public class RQLNodeList {
	/**
	 * Die Liste für die interne Verwaltung.
	 */
	private List<RQLNode> internalList = new ArrayList<RQLNode>();

	/**
	 * Diese Methode fügt dieser RQLNodeList einen RQLNode hinzu.
	 */
	void add(RQLNode node) {
		internalList.add(node);
	}

	/**
	 * Liefert den ersten Node.
	 */
	public RQLNode first() {
		return (RQLNode) internalList.get(0);
	}

	/**
	 * Diese Methode liefert das Element der Liste an der Stelle des
	 * übergebenen index.
	 */
	public RQLNode get(int index) {
		return (RQLNode) internalList.get(index);
	}

	/**
	 * Liefert den letzten Node.
	 */
	public RQLNode last() {
		return (RQLNode) internalList.get(size() - 1);
	}

	/**
	 * Diese Methode liefert die Anzahl der Elemente in dieser Liste.
	 * 
	 * @return int: s.o.
	 */
	public int size() {
		return internalList.size();
	}
}
