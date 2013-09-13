package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie eine Project beinhaltet.
 * 
 * @author LEJAFR
 */
public interface LanguageVariantContainer {
/**
 * Liefert die Sprachvariante. 
 */
public LanguageVariant getLanguageVariant();
/**
 * Liefert die RedDot GUID der Sprachvariante. 
 */
public String getLanguageVariantGuid();
/**
 * Liefert den Namen der Sprachvariante. 
 */
public String getLanguageVariantName();
}
