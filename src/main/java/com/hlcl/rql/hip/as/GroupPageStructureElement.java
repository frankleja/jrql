package com.hlcl.rql.hip.as;

import java.text.MessageFormat;

/**
 * @author lejafr
 *
 * This class encapsulates an element with an information string only. 
 *@see PageStructureCollector
 */
public class GroupPageStructureElement extends PageStructureElement {
	
	private int size;
	
	/**
	 * Erzeugt einen neues Element.
	 */
	public GroupPageStructureElement(int deepness) {
		super(deepness);
		size = 0;
	}
	/** 
	 * Liefert true, falls dieses Element eine Seite beinhaltet.
	 */
	public boolean isGroup() {
		return true;		
	}
	/** 
	 * Liefert true, falls diese Gruppe noch nicht erhöht wurde.
	 */
	public boolean isEmpty() {
		return size() == 0;		
	}
	/** 
	 * Liefert true, falls diese Gruppe noch erhöht wurde.
	 */
	public boolean isNotEmpty() {
		return !isEmpty();		
	}
	/** 
	 * Liefert die Anzahl Elemente dieser Gruppe zurück.
	 */
	public int size() {
		return size;		
	}
	/** 
	 * Erhöht die Anzahl Elemente um 1.
	 */
	public void incrementSize() {
		size++;		
	}
	/** 
	 * Liefert den formatierten Text mit der group size als {0} zurück.
	 */
	public String getInformation(String pattern) {
		Object[] args = new Object[1];
		args[0] = new Integer(size()); 
		return MessageFormat.format(pattern, args);		
	}
}
