package com.hlcl.rql.as;

/**
 * TextAnchor structural element, not a dynamic text anchor.
 * 
 * @author lejafr
 */
public class TextAnchor extends Anchor {
	/**
	 * Container constructor comment.
	 * 
	 * @param page
	 *            Seite, die diesen Link beinhaltet.
	 * @param name
	 *            Name des Links auf Seite page
	 * @param anchorGuid
	 *            GUID des Ankers auf Seite page
	 */
	public TextAnchor(Page page, String name, String anchorGuid) {
		super(page, name, anchorGuid);
	}
}
