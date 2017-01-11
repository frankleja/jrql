package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie eine Page beinhaltet.
 * 
 * @author LEJAFR
 */
public interface PageContainer extends ProjectContainer {
/**
 * Liefert die RedDot GUID der Seite.
 */
public String getPageGuid();

/**
 * Liefert die Seite zu der das Element gehoert.
 */
public Page getPage();
}
