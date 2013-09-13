package com.hlcl.rql.hip.as;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.User;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * This class represents a page in the HIP project containing a change log for HIP subscription; in fact every physical page.
 */
public class ChangeLogPage extends ProjectPage {

	private LinkedList<ChangeLogEntry> changes;
	private final String XML_HEADER = "<?xml version=\"1.0\"?>";

	private ChangeLogEntry newEntryCache;
	private ChangeLogEntry latestChange; // cache latest change access
	private boolean isLatestChangeRead = false;

	/**
	 * Construct a change log page wrapping the given general page.
	 */
	public ChangeLogPage(Page page) {
		super(page);
	}

	/**
	 * Returns the text element hip_change_log contents.
	 */
	private String getChangeLogText() throws RQLException {
		return getTextValue(getParameter("changeLogTmpltElemName"));
	}

	/**
	 * Liefert true, falls diese seite noch keinen change kommentar hat.
	 */
	private boolean isChangeLogTextEmpty() throws RQLException {
		return isTextEmpty(getParameter("changeLogTmpltElemName"));
	}

	/**
	 * Liefert true, falls diese Seite das element hip_change_log besitzt, sonst false.
	 */
	private boolean hasChangeLogText() throws RQLException {
		return contains(getParameter("changeLogTmpltElemName"));
	}

	/**
	 * Liefert true, falls diese seite abonniert werden darf. Falls nicht, darf kein change comment erfasst werden.
	 */
	public boolean isSubscribeable() throws RQLException {
		return StringHelper.convertToBoolean(getOptionListValue(getParameter("isSubscribeableTmpltElemName")));
	}

	/**
	 * Ändert die Seiteneigenschaft isSubscribeable auf den gegebenen Wert.
	 */
	public void setIsSubscribeable(boolean isSubscribeable) throws RQLException {
		setOptionListValue(getParameter("isSubscribeableTmpltElemName"), StringHelper.convertToString(isSubscribeable));
	}

	/**
	 * Erzeugt aus allen changes die XML tags.
	 */
	private String buildXml() throws RQLException {
		// open
		StringBuffer result = new StringBuffer();
		result.append("<changes>");
		for (Iterator iterator = getChanges().iterator(); iterator.hasNext();) {
			ChangeLogEntry entry = (ChangeLogEntry) iterator.next();
			result.append(entry.getChangeXmlTag());
		}
		// close
		result.append("</changes>");
		return result.toString();
	}

	/**
	 * Speichert das xml mit den change comments im ascii text der seite.
	 */
	private void setChangeLogText(String changesXml) throws RQLException {
		setTextValue(getParameter("changeLogTmpltElemName"), changesXml);
	}

	/**
	 * Adds a new change log entry for given user and comment with current date and time.
	 */
	public ChangeLogEntry addEntry(User user, String comment) throws RQLException {
		ChangeLogEntry result = getNewEntry(user);
		saveNewEntry(comment);
		return result;
	}

	/**
	 * Returns the new entry initialized with user and timestamp, but empty comment.
	 */
	public ChangeLogEntry getNewEntry(User user) throws RQLException {
		newEntryCache = new ChangeLogEntry(this, user.getName());
		return newEntryCache;
	}

	/**
	 * Returns the maximal length of the comment field.
	 */
	public int getCommentMaxLength() throws RQLException {
		return Integer.parseInt(getParameter("commentMaxLength"));
	}

	/**
	 * Returns the timestamp at which time the new entry was requested.
	 */
	public Date getNewEntryTimestamp() throws RQLException {
		return newEntryCache.getTimestamp();
	}

	/**
	 * Speichert den Kommtar comment im neuen Entry und speichert alle changes wieder in der seite.
	 */
	public void saveNewEntry(String comment) throws RQLException {
		newEntryCache.setComment(comment);

		// force creation of changes list
		getChanges();

		// determine ordering in xml: newest on top
		changes.addFirst(newEntryCache);
		setChangeLogText(buildXml());

		// reset latest changes cache
		resetLatestChangeCache();
	}

	/**
	 * Reset the latest change entry cache and force a new read.
	 */
	private void resetLatestChangeCache() {
		latestChange = null;
		isLatestChangeRead = false;
	}

	/**
	 * Returns true only, if the given comment text is valid. Currently only the length is checked.
	 */
	public boolean isCommentValid(String comment) throws RQLException {
		return comment.length() <= getCommentMaxLength();
	}

	/**
	 * Returns the timestamp of the latest change formatted 20100119 or ifNotAvailable, if no change comment at all.
	 */
	public String getLatestChangeTimestampAsyyyyMMdd(String ifNotAvailable) throws RQLException {
		if (!hasChangeLogText()) {
			return ifNotAvailable;
		}
        return hasChanges() ? getLatestChange().getTimestampAsyyyyMMdd() : ifNotAvailable;
    }

	/**
	 * Returns the user name of the latest change entry or ifNotAvailable, if no change comment at all.
	 */
	public String getLatestChangeUserName(String ifNotAvailable) throws RQLException {
		if (!hasChangeLogText()) {
			return ifNotAvailable;
		}
        return hasChanges() ? getLatestChange().getUserName() : ifNotAvailable;
    }

	/**
	 * Returns the comment of the latest change entry or ifNotAvailable, if no change comment at all.
	 */
	public String getLatestChangeComment(String ifNotAvailable) throws RQLException {
		if (!hasChangeLogText()) {
			return ifNotAvailable;
		}
        return hasChanges() ? getLatestChange().getComment() : ifNotAvailable;
    }

	/**
	 * Returns true, if this page didn't has at least one change log entry.
	 */
	public boolean hasChanges() throws RQLException {
		return getNumberOfChanges() != 0;
	}

	/**
	 * Returns the number of changes of page. Treat the created entry not as change.
	 */
	public int getNumberOfChanges() throws RQLException {
		return getChanges().size();
	}

	/**
	 * Returns the latest entry of the list of changes
	 * 
	 * @return
	 * @throws RQLException
	 */
	public ChangeLogEntry getLatestChange() throws RQLException {
		if (latestChange == null && !isLatestChangeRead) {
			if (contains(getParameter("changeLogTmpltElemName"))) {
				java.util.List<ChangeLogEntry> changes = getChanges();
				isLatestChangeRead = true;
				if (!changes.isEmpty()) {
					latestChange = changes.get(0);
				}
			}
		}
		return latestChange;
	}

	/**
	 * Returns the list of changes sorted by date descending. Saved in xml in this order.
	 */
	public java.util.List<ChangeLogEntry> getChanges() throws RQLException {
		if (changes == null) {
			changes = new LinkedList<ChangeLogEntry>();

			try {
				// parse only, if changes are existing
				if (!isChangeLogTextEmpty()) {
					// add header to parse as xml
					String toParse = XML_HEADER + getChangeLogText();
					SAXBuilder builder = new SAXBuilder();
					Document doc = builder.build(new StringReader(toParse));
					Element root = doc.getRootElement();

					// convert into change log entries
					for (Iterator iterator = root.getChildren().iterator(); iterator.hasNext();) {
						Element elem = (Element) iterator.next();
						changes.add(new ChangeLogEntry(this, elem.getChildTextTrim("user"), elem.getChildTextTrim("comment"), elem
								.getChildTextTrim("date")));
					}
				}
			} catch (JDOMException ex) {
				throw new RQLException("JDOM exception parsing of changes (hip_change_log) in page " + getPageInfoText() + " not possible.", ex);
			} catch (IOException ex) {
				throw new RQLException("IO exception parsing of changes (hip_change_log) in page " + getPageInfoText() + " not possible.", ex);
			}
		}
		return changes;
	}

	/**
	 * Liefert das date format pattern für die Ausgabe in die structure xml.
	 */
	String getXmlDateFormatPattern() throws RQLException {
		return getParameter("xmlDateFormatPattern");
	}

}
