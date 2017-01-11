package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn versucht wird einen PublishingJob für eine Seite zu starten,
 * für die bereits ein Job in der Queue wartet, oder deren Generierung bereits läuft.
 *
 * @see <code>PublishingJob</code>
 * @author LEJAFR
 */
public class PageAlreadyInPublishingQueueException extends RQLException {
	private static final long serialVersionUID = 5989896035512128914L;

/**
 * PageAlreadyInPublishingQueueException constructor comment.
 *
 * @param s java.lang.String
 */
public PageAlreadyInPublishingQueueException(String s) {
	super(s);
}
}
