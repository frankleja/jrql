package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie eine Project beinhaltet.
 * 
 * @author LEJAFR
 */
public interface ProjectVariantContainer {
/**
 * Liefert die Projektvariante. 
 */
public ProjectVariant getProjectVariant();
/**
 * Liefert die RedDot GUID der Projektvariante. 
 */
public String getProjectVariantGuid();
/**
 * Liefert den Namen der Projektvariante. 
 */
public String getProjectVariantName();
}
