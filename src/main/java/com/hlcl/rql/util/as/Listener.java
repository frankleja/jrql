package com.hlcl.rql.util.as;

/**
 * @author lejafr
 * 
 * This class defines the Interface between a business object and the JSP to show a progress while a long recursively scan is running in the business
 * object.
 */
public interface Listener {
	/**
	 * Implementing this method you can decide what in the view will change (the visualization).
	 */
	public void update(String information);
}
