package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
/**
 * Diese Klasse beschreibt einen Seitenfilter, der Prüfungen gegen das Template macht.
 * 
 * @author lejafr
 */
public class MainLinkChildUserPageFilter extends PageFilterImpl {

	private Page parentPage;
/**
 * MainLinkChildUserPageFilter constructor comment.
 */
public MainLinkChildUserPageFilter(Page parentPage) {
	super();

	this.parentPage = parentPage;	
}
/**
 * Prüft die gegebenen Seite auf Gültigkeit dieses Filters.
 *
 * Liefert true, falls die gegebene Seite Kind (über Hauptlink) der parentPage ist.
 */
public boolean check(Page blockPage) throws RQLException {

	return blockPage.getMainLinkParentPage().equals(parentPage);
}
}
