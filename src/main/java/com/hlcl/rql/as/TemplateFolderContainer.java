package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie einen TemplateFolder beinhaltet.
 * 
 * @author LEJAFR
 */
public interface TemplateFolderContainer extends ProjectContainer {
/**
 * Liefert die GUID des Template-Folder, in dem dieses Template enthalten ist.
 * 
 * @return GUID des TemplateFolder
 */
public String getTemplateFolderGuid();

/**
 * Liefert den Template-Folder, in dem dieses Template enthalten ist.
 * 
 * @return TemplateFolder
 */
public TemplateFolder getTemplateFolder();
}
