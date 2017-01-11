package com.hlcl.rql.as;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Exception bei einer RQL-Anfrage an das CMS.
 * 
 * Die RWLException kann eine genauere Ursache in Form eines Throwables
 * enthalten. In einer Ausbaustufe muß die RQL- Exception evtl. auch noch die
 * Error-Codes des CMS liefern können.
 * 
 * @author BURMEBJ
 */
public class RQLException extends Exception {
	private static final long serialVersionUID = -9007889746606754536L;
	/**
	 * Dieses Feld enthält die Ursache dieser Exception.
	 */
	private Date created = new Date();

	/**
	 * Konstruktor.
	 * 
	 * @param msg
	 *            String: Beschreibung der Exception.
	 */
	public RQLException(String msg) {
		super(msg);
	}

	/**
	 * Konstruktor.
	 * 
	 * @param msg		Beschreibung der Exception.
	 * @param reason	Ursache des Problems.
	 */
	public RQLException(String msg, Throwable reason) {
		super(msg, reason);
	}

	/**
	 * Overwrite to add the created timestamp.
	 */
	public String getMessage() {
		// format date to 20061127 18:35
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm");
		return df.format(created) +" "+ super.getMessage();
	}

	/**
	 * Getter für das Feld "reason".
	 */
	public Throwable getReason() {
		return getCause();
	}
}
