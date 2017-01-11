package com.hlcl.rql.hip.as;

/**
 * @author lejafr
 *
 * This class hierarchy describe an element of a page structure. 
 *@see PageStructureCollector
 */
public abstract class PageStructureElement {

	private int deepness;

	/**
	 * Erzeugt einen neues Element.
	 */
	public PageStructureElement(int deepness) {
		super();
		this.deepness = deepness;
	}
	/** 
	 * Liefert die Tiefe dieses Elementes in der Structure (<=0).
	 */
	public int getDeepness() {
		return deepness;
	}
	public boolean isMultiLink() {
		return false;		
	}
	/** 
	 * Liefert true, falls dieses Element eine Seite beinhaltet.
	 */
	public boolean isPage() {
		return false;
	}
	/** 
	 * Liefert true, falls dieses Element ein Infoelement ist.
	 */
	public boolean isGroup() {
		return false;
	}
}
