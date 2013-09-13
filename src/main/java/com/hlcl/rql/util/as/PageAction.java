/*
 * Created on 20.05.2005
 *
 */
package com.hlcl.rql.util.as;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;


/**
 * @author lejafr
 *
 * Diese Klasse definiert ein Interface für auszuführenden Code, der für eine Page z.B. im TreeWalker aufgerufen wird.
 */
public abstract class PageAction {
	/**
	 * Führt die zu implementierende Methode für die übergebene Klasse aus.
	 */
	public abstract void invoke(Page page) throws RQLException;
}
