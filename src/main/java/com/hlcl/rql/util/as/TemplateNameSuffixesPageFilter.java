package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;
/**
 * Diese Klasse beschreibt einen Seitenfilter, der Prüfungen gegen den Suffix des Templatenamens macht.
 * 
 * @author lejafr
 */
public class TemplateNameSuffixesPageFilter extends PageFilterImpl {

	private String[] nameSuffixes;
/**
 * TemplatePageFilter constructor comment.
 *
 *@param	suffixes	list of suffixes; e.g. page,fragment
 *@param	separator	the , to split suffixes accordingly
 */
public TemplateNameSuffixesPageFilter(String suffixes, String separator) {

	nameSuffixes = StringHelper.split(suffixes, separator);	
}
/**
 * Prüft die gegebenen Seite auf Gültigkeit dieses und des übergeordneten Filters.
 *
 * Liefert true, falls der Templatename der gegebenen Seite auf einen der Suffixe endet.
 */
public boolean check(Page page) throws RQLException {

	return StringHelper.endsWithOneOf(page.getTemplateName(), nameSuffixes);
}
}
