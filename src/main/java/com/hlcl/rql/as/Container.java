package com.hlcl.rql.as;

/**
 * Diese Klasse beschreibt einen RedDot MultiLink Container.
 * 
 * @author LEJAFR
 */
public class Container extends MultiLink {

	/**
	 * Liefert immer true, da dieser Link ein Container ist.
	 */
	public boolean isContainer() {

		return true;
	}

	/**
	 * Container constructor comment.
	 *
	 * @param	page	Seite, die diesen Multi-Link beinhaltet.
	 * @param	name	Name des Links auf Seite page
	 * @param	linkGuid	GUID des Links auf Seite page
	 * @param	isReferenceSource	Link referenziert ein anderes Element
	 */
	public Container(Page page, TemplateElement templateElement, String name, String linkGuid, boolean isReferenceSource) {
		super(page, templateElement, name, linkGuid, isReferenceSource);
	}
	/**
	 * Kopiert alle Kindseiten (inkl. deren Kindseiten in Containern) von sourceContainer an diesen Container. <p>
	 * Die Werte der content elements werden mit kopiert.
	 * 
	 * @param ignoreElementNames
	 *            Liste of template element names, deren Werte nicht kopiert werden sollen (alle Templates kombiniert!)
	 * @param separator
	 *            Trennzeichen der Namen
	 */
	public void copyChildrenWithContentFrom(Container sourceContainer, String ignoreElementNames, String separator) throws RQLException {
		// niemals container auf den kindseiten berücksichtigen
		super.copyChildrenWithContentFrom(sourceContainer, ignoreElementNames, separator, false);
	}
}
