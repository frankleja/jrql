package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn am PublishingTarget der Type nicht passt. <p>
 * Z.B. kann ein LiverServer PublishingTarget keinen Pfad zurückliern.
 * 
 * @author LEJAFR
 */
public class WrongPublishingTargetTypeException extends RQLException {
	private static final long serialVersionUID = -4447126278894083481L;

/**
 * WrongPublishingTargetTypeException constructor comment.
 * @param s java.lang.String
 */
public WrongPublishingTargetTypeException(String s) {
	super(s);
}
}
