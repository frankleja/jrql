/**
 * 
 */
package com.hlcl.rql.hip.as;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.as.StringHelper;

/**
 * @author lejafr
 * 
 * Representiert einen Eintrag im ChangeLog.
 */
public class ChangeLogEntry {

	private ChangeLogPage parent;
	private String userName;
	private Date timestamp;

	private String comment;

	private SimpleDateFormat formatter;

	/**
	 * Constructor zur Erzeugung eines neuen Eintrags.
	 */
	public ChangeLogEntry(ChangeLogPage parent, String userName) {
		super();

		this.parent = parent;
		this.userName = userName;
		this.timestamp = new Date();
	}

	/**
	 * Constructor zur Erzeugung eines Objektes für den page created Eintrag.
	 * 
	 * @throws RQLException
	 */
	private ChangeLogEntry(ChangeLogPage parent, String userName, String comment, Date timestamp) throws RQLException {
		super();

		this.parent = parent;
		this.userName = userName;
		this.comment = comment;
		this.timestamp = timestamp;
	}

	/**
	 * Constructor zur Erzeugung eines Objektes für einen bereits gespeicherten Eintrag.
	 * 
	 * @throws RQLException
	 */
	public ChangeLogEntry(ChangeLogPage parent, String userName, String comment, String dateStr) throws RQLException {
		super();

		this.parent = parent;
		this.userName = userName;
		this.comment = comment;
		this.timestamp = parseTimestamp(dateStr);
	}

	/**
	 * Liefert das xml change tag zurück.
	 * @throws RQLException 
	 */
	public String getChangeXmlTag() throws RQLException {
		StringBuffer result = new StringBuffer();
		result.append("<change>");
		result.append("<user>");
		result.append(userName);
		result.append("</user>");
		result.append("<date>");
		result.append(getTimestampFormattedForXml());
		result.append("</date>");
		result.append("<comment>");
		result.append("<![CDATA[" + getComment() + "]]>");
		result.append("</comment>");
		result.append("</change>");
		return result.toString();
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return the comment with escaped characters, replaces " with ""
	 */
	public String getCommentEscaped(){
	    return getComment().replace("\"", "\"\"");
	}
	
	/**
	 * @return the formatter for the xml file
	 */
	private SimpleDateFormat getFormatterXml() throws RQLException {
		if (formatter == null) {
			formatter = new SimpleDateFormat(getXmlDateFormatPattern());
		}
		return formatter;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the timestamp formatted for user display, Format 27 Aug 2006 6:15 PM
	 */
	public String getTimestampFormattedForDisplay() {
		return ReddotDate.formatAsddMMMyyyyhmma(timestamp);
	}

	/**
	 * @return the timestamp formatted as yyyyMMdd (20100119).
	 */
	public String getTimestampAsyyyyMMdd() {
		return ReddotDate.formatAsyyyyMMdd(timestamp);
	}

	/**
	 * @return the timestamp formatted for xml using format: 2009-0324|8:50:47
	 * @throws RQLException 
	 */
	public String getTimestampFormattedForXml() throws RQLException {
		return getFormatterXml().format(getTimestamp());
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Liefert das date format pattern für die Ausgabe in die structure xml.
	 */
	private String getXmlDateFormatPattern() throws RQLException {
		return parent.getXmlDateFormatPattern();
	}

	/**
	 * Parst das als String gegebene Datum in ein Objekt.
	 */
	private Date parseTimestamp(String timestampStr) throws RQLException {
		try {
			return getFormatterXml().parse(timestampStr);
		} catch (ParseException pe) {
			throw new RQLException("Could not parse date " + timestampStr + " into pattern " + getXmlDateFormatPattern() + ".", pe);
		}
	}

	/**
	 * Ändert den Kommentar. Der Kommentar wird auf {@link #getCommentMaxLength()} gekürzt. <p>
	 * Vorherige Prüfung der Textlänge ist mit {@link ChangeLogPage#isCommentValid(String)} möglich. 
	 * @throws RQLException 
	 */
	public void setComment(String comment) throws RQLException {
		this.comment = StringHelper.ensureLength(comment, getCommentMaxLength());
	}
    /**
     * Returns the maximal length of the comment field. 
     */
    public int getCommentMaxLength() throws RQLException {
    	return parent.getCommentMaxLength();
    }
}
