package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn Namen von Template-Elementen benötigt werden, aber diese nicht gegeben wurden. 
 * 
 * @author LEJAFR
 */
public class MissingTemplateNamesException extends RQLException {
	private static final long serialVersionUID = 3446887530286931132L;

/**
 * MissingTemplateNamesException constructor comment.
 *
 * @param s java.lang.String
 */
public MissingTemplateNamesException(String s) {
	super(s);
}
}
