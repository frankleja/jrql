package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie ein Template beinhaltet.
 * 
 * @author LEJAFR
 */
public interface TemplateContainer extends TemplateFolderContainer {
/**
 * Liefert die GUID des Templates vom Container.
 */
public String getTemplateGuid();

/**
 * Liefert das Template vom Container.
 */
public Template getTemplate();
}
