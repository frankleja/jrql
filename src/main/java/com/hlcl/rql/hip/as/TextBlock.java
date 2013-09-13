package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.ProjectPage;

/**
 * @author lejafr
 * 
 * Represents a text block page in hip.
 */
public class TextBlock extends ProjectPage {

	/**
	 * Creates a hip text block.
	 */
	public TextBlock(Page page) {
		super(page);
	}

	/**
	 * Returns the html code from this text block.
	 */
	public String getTextValue() throws RQLException {
		return getTextValue(getParameter("textTmpltElemName"));
	}
}
