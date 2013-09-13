package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie ein Exportpaket beinhaltet.
 * 
 * @author LEJAFR
 */
public interface PublicationPackageContainer extends ProjectContainer {
/**
 * Liefert die GUID des umgebenden Exportpaketes.
 */
public String getPublicationPackageGuid();
}
