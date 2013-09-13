package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn versucht wird eine Seite für 
 * eine Kombination von Projektvariante und Sprachvariante zu generieren, 
 * für die kein Exportsetting im Exportpaket vorliegt. 
 *
 * @see <code>PublishingJob</code>
 * @author LEJAFR
 */
public class InvalidPublishingRequestException extends RQLException {
	private static final long serialVersionUID = -3376084590018280317L;

/**
 * InvalidPublishingRequestException constructor comment.
 *
 * @param s java.lang.String
 */
public InvalidPublishingRequestException(String s) {
	super(s);
}
}
