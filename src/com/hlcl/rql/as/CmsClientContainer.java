package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie einen CmsServer beinhaltet.
 * 
 * @author LEJAFR
 */
public interface CmsClientContainer {
/**
 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
 */
public RQLNode callCms(String rqlRequest) throws RQLException;
/**
 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck.
 * Erforderlich für die Ermittlung des Werts eines Textelements.
 */
public String callCmsWithoutParsing(String rqlRequest) throws RQLException;
/**
 * Liefert den CmsClient.
 */
public CmsClient getCmsClient();
/**
 * Liefert die RedDot logon GUID.
 */
public String getLogonGuid();
}
