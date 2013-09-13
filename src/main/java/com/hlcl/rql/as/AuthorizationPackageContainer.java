package com.hlcl.rql.as;

/**
 * Das Interface sammelt alle Methoden, die eine Klasse implementieren muss,
 * wenn sie ein Berechtigungspaket beinhaltet.
 * 
 * @author LEJAFR
 */
public interface AuthorizationPackageContainer extends ProjectContainer {
	/**
	 * Liefert die GUID des umgebenden Berechtigungspaketes.
	 */
	public String getAuthorizationPackageGuid();

	/**
	 * Liefert das umgebenden Berechtigungspaket.
	 */
	public AuthorizationPackage getAuthorizationPackage();
}
