package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * This class defines the Interface between a business object and the JSP to show a progress while a long recursively scan is running in the business object (accordingly to MVC pattern).<p>
 * Should be used when a connection between a page and a multi link is changed. This could a connect or a disconnect you want to track.
 */
public interface ConnectionListener {
	/**
	 * Implementing this method you can decide what in the view will change (the visualization). 
	 */
	public void update(Page page, MultiLink multiLink) throws RQLException;
}
