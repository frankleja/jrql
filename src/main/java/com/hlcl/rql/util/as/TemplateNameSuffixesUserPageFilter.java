package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;
/**
 * Diese Klasse beschreibt einen Seitenfilter, der Prüfungen gegen das Template macht.
 * 
 * @author lejafr
 */
public class TemplateNameSuffixesUserPageFilter extends UserPageFilter {

	private String[] nameSuffixes;
/**
 * TemplatePageFilter constructor comment.
 *
 *@param	username	name of user
 *@param	suffixes	list of suffixes; e.g. page,fragment
 *@param	separator	the , to split suffixes accordingly
 */
public TemplateNameSuffixesUserPageFilter(String username, String suffixes, String separator) {
	super(username);

	nameSuffixes = StringHelper.split(suffixes, separator);	
}
/**
 * Prüft die gegebenen Seite auf Gültigkeit dieses und des übergeordneten Filters.
 *
 * Liefert true, falls der Templatename der gegebenen Seite auf einen der Suffixe endet.
 */
public boolean check(Page page) throws RQLException {

	// check user name
	if (!super.check(page)) {
		return false;
	}

	return StringHelper.endsWithOneOf(page.getTemplateName(), nameSuffixes);
}
}
