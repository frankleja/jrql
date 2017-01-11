package com.hlcl.rql.as;

import java.util.*;
/**
 * Diese Klasse beschreibt ein Media-Element einer Seite.
 * 
 * @author LEJAFR
 */
public class MediaElement extends FileElement {

	// null if media element has no suffix restriction
	private Set allowedSuffixes;
	public static final String DELIMITER = ";";
/**
 * Optionale Prüfmöglichkeit für Subklassen.
 * Prüft auf korrekten Extender der Datei.
 */
protected void checkBeforeChange(String filename) throws RQLException {

	// check suffix
	if (!isAllowed(filename)) {
		throw new WrongSuffixException("The file with filename "+filename+" has an unallowed suffix " +".");
	}
}
/**
 * Liefert eine Liste der erlaubten Suffixe (mit ; getrennt).
 */
public String getAllowedSuffixes() {

	StringBuilder buffer = new StringBuilder();

	if (allowedSuffixes != null) {
		Iterator iterator = allowedSuffixes.iterator();
		while (iterator.hasNext()) {
			buffer.append((String) iterator.next()).append(DELIMITER);
		}
	}
	return buffer.toString();
}
/**
 * Liefert true genau dann, wenn der Extender des Dateinames zu den erlaubten Suffixes gehört.
 */
private boolean isAllowed(String filename) {

	return allowedSuffixes == null ? true : allowedSuffixes.contains(getExtender(filename));
}
/**
 * Konvertiert die Liste der Suffixes in ein Set.
 */
private void setAllowedSuffixes(String suffixes) {

	// convert suffixes string to set
	if (suffixes != null) {
		allowedSuffixes = new HashSet();
		StringTokenizer tokenizer = new StringTokenizer(suffixes, DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			allowedSuffixes.add(tokenizer.nextToken());
		}
	}
}

/**
 * MediaElement constructor comment.
 *
 * @param	page	Seite, die diesen Container Link beinhaltet.
 * @param	templateElement		TemplateElement auf dem dieses Element basiert
 * @param	name	Name des Elements
 * @param	elementGuid	GUID dieses Elements
 * @param	value Dateiname des Files
 */
public MediaElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value, String folderGuid, String suffixes) {
	
	super(page, templateElement, name, elementGuid, value, folderGuid);

	setAllowedSuffixes(suffixes);
}
}
