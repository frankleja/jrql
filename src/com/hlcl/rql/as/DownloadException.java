package com.hlcl.rql.as;

/**
 * Diese Ausnahme wird geworfen, wenn eine eine Datei nicht vom RD CMS Server heruntergeladen werden konnte. 
 * 
 * @author LEJAFR
 */
public class DownloadException extends RQLException {
	private static final long serialVersionUID = 3082449146053295280L;
/**
 * DownloadException constructor comment.
 *
 * @param s java.lang.String
 */
public DownloadException(String s) {
	super(s);
}
/**
 * DownloadException constructor comment.
 *
 * @param s java.lang.String
 */
public DownloadException(String s, Throwable t) {
	super(s, t);
}
}
