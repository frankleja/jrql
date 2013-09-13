package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn kein Projekt gefundenen werden konnte (falscher Name oder GUID) oder dem User nicht zugeordnet ist.
 * 
 * @author LEJAFR
 */
public class ProjectNotFoundException extends RQLException {
	private static final long serialVersionUID = -8141078691165554272L;

/**
 * ProjectNotFoundException constructor comment.
 * @param s java.lang.String
 */
public ProjectNotFoundException(String s) {
	super(s);
}
}
