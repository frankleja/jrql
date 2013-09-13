package com.hlcl.rql.as;

import java.util.Collections;
import java.util.Iterator;

import com.hlcl.rql.util.as.PageArrayList;

/**
 * Diese Klasse beschreibt einen RedDot MultiLink List.
 * 
 * @author LEJAFR
 */
public class List extends MultiLink {
	// null means no target container assigned
	private String targetContainerGuid;

	/**
	 * Container constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Multi-Link beinhaltet.
	 * @param name
	 *            Name des Links auf Seite page
	 * @param linkGuid
	 *            GUID des Links auf Seite page
	 * @param isReferenceSource
	 *            Link referenziert ein anderes Element
	 */
	public List(Page page, TemplateElement templateElement, String name, String linkGuid, boolean isReferenceSource, String targetContainerGuid) {
		super(page, templateElement, name, linkGuid, isReferenceSource);

		this.targetContainerGuid = targetContainerGuid;
	}

	/**
	 * Ordnet dieser Liste einen Container als RedDot Targetcontainer zu.
	 * 
	 * @param targetContainer
	 *            RedDot Container einer anderen Seite.
	 */
	public void assignTargetContainer(Container targetContainer) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="3AA30C34993B4F06AAE52C6A92EEE79C">
		 * <PROJECT sessionkey="7940512570JgQa0Oni60" > <PAGE> <ELEMENT
		 * action="save" guid="B75287BBA38E40FA97C9AAF92B2E0A7B"
		 * targetcontainerguid="939105A5CD764FD399178C522536DD47"
		 * isinternaltargetcontainer="0" usepagemainlinktargetcontainer="0"/>
		 * </PAGE> </PROJECT> </IODATA> V5 response
		 * <IODATA>B75287BBA38E40FA97C9AAF92B2E0A7B </IODATA>
		 */

		// call CMS
		String linkGuid = targetContainer.getLinkGuid();
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "' sessionkey='" + getSessionKey() + "'>" + " <PAGE>"
				+ "  <ELEMENT action='save' guid='" + getLinkGuid() + "' targetcontainerguid='" + linkGuid
				+ "' isinternaltargetcontainer='0' usepagemainlinktargetcontainer='0'/>" + " </PAGE>" + "</IODATA>";
		callCms(rqlRequest);

		// update local cache
		targetContainerGuid = linkGuid;
	}

	/**
	 * Liefert den zugewiesenen TargetContainer, falls vorhanden; sonst null;
	 */
	public Container getTargetContainer() throws RQLException {
		if (targetContainerGuid == null) {
			return null;
		}
		return getProject().getContainerByGuid(targetContainerGuid);
	}

	/**
	 * Liefert immer true, da dieser Link eine Liste ist.
	 */
	public boolean isList() {

		return true;
	}

	/**
	 * Liefert true, falls diese Liste einen Targetcontainer zugewiesen hat.
	 */
	public boolean isTargetContainerAssigned() {
		return targetContainerGuid != null;
	}

	/**
	 * Kopiert alle Kindseiten (inkl. deren Kindseiten in Containern) von sourceList an diese Liste. <p>
	 * Die Werte der content elements werden mit kopiert.
	 * 
	 * @param ignoreElementNames
	 *            Liste of template element names, deren Werte nicht kopiert werden sollen (alle Templates kombiniert!)
	 * @param separator
	 *            Trennzeichen der Namen
	 */
	public void copyChildrenWithContentFrom(com.hlcl.rql.as.List sourceList, String ignoreElementNames, String separator) throws RQLException {
		// immer container Kindseiten der Kinder von sourceList mitkopieren 
		super.copyChildrenWithContentFrom(sourceList, ignoreElementNames, separator, true);
	}
}
