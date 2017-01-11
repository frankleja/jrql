package com.hlcl.rql.as;

/**
 * Insert the type's description here.
 * 
 * @author lejafr
 */
public interface ElementContainer extends PageContainer {
/**
 * Liefert die RedDot GUID des Elements.
 */
public String getElementGuid();

/**
 * Liefert das Element..
 */
public Element getElement();
}
