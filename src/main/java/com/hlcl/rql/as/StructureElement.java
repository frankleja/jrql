package com.hlcl.rql.as;

/**
 * An Element that is used to construct the Page/Reference/Link/URL-structure of the tree.
 * Super-Interface for everything that somehow deals with containment: MultiLinks and Anchors. 
 * Specific semantics of the various methods may differ.
 *
 */
public interface StructureElement {

	boolean isContainer();
	boolean isList();
	boolean isAnchor();
	
	boolean isReferenceSource();
	boolean isMultiLink();

	java.util.List<Page> getChildPages() throws RQLException;

	String getGuid();
	
	String getName();
	
	void disconnectAllChilds() throws RQLException;
	
	void connectToExistingPage(String targetGuid, boolean atBottom, boolean setMainLink) throws RQLException;
	void connectToExistingPage(Page target, boolean atBottom, boolean setMainLink) throws RQLException;
	void connectToRedirectUrl(String url, String target, String headline) throws RQLException;

}
