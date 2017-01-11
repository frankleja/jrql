package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 *
 * This class defines the Interface between a business object and the JSP to show a progress while a long recursively scan is running in the business object.
 */
public interface PageListener {
	/**
	 * Implementing this method you can decide what in the view will change (the visualization). 
	 */
	public void update(Page currentPage) throws RQLException ;
}
