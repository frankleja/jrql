package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie eine Project beinhaltet.
 * 
 * @author LEJAFR
 */
public interface ProjectContainer extends CmsClientContainer {
/**
 * Liefert das Projekt. 
 */
public Project getProject();
/**
 * Liefert die RedDot GUID des Projekts. 
 */
public String getProjectGuid() throws RQLException;
/**
 * Liefert den RedDot Session key.
 */
public String getSessionKey();
}
