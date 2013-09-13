package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn versucht wird einen PublishingJob zu starten,
 * für den keine Sprach- oder Projektvariante vorgegeben wurde.
 *
 * @see <code>PublishingJob</code>
 * @author LEJAFR
 */
public class IncompletePublishingJobException extends RQLException {
	private static final long serialVersionUID = -656852979774348115L;

/**
 * IncompletePublishingJobException constructor comment.
 *
 * @param s java.lang.String
 */
public IncompletePublishingJobException(String s) {
	super(s);
}
}
