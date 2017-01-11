package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.MultiLink;
import com.hlcl.rql.as.Page;

/**
 * @author lejafr
 *
 * This class encapsulates a element a page structure. It contains either a page or a multilink object.
 *@see PageStructureCollector
 */
public class ObjectPageStructureElement extends PageStructureElement {

	private Object element;

	/**
	 * Erzeugt einen neues Element.
	 */
	public ObjectPageStructureElement(int deepness, Object element) {
		super(deepness);
		this.element = element;
	}
	/** 
	 * Liefert den MultiLink.
	 */
	public MultiLink getMultiLink() {
		return (MultiLink) element;
	}
	/** 
	 * Liefert die Seite.
	 */
	public Page getPage() {
		return (Page) element;
	}
	/** 
	 * Liefert true, falls dieses Element eine Seite beinhaltet.
	 */
	public boolean isMultiLink() {
		return element instanceof MultiLink;		
	}
	/** 
	 * Liefert true, falls dieses Element eine Seite beinhaltet.
	 */
	public boolean isPage() {
		return element instanceof Page;
	}
}
