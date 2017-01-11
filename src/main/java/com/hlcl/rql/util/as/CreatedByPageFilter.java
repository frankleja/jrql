package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;

/**
 * @author lejafr
 *
 * Filtert alle Seiten, dessen Ersteller in der gegebenen Usergruppe sind.
 */
public class CreatedByPageFilter extends PageFilterImpl {

	private UserGroup userGroup;

	/**
	 * constructor comment.
	 */
	public CreatedByPageFilter(UserGroup userGroup) {
		super();
		
		this.userGroup = userGroup;
	}
	/** 
	 * Liefert true, falls die Seite von einem Benutzer erstellt wurde, der in der angegebenen Gruppe ist.
	 */
	public boolean check(Page page) throws RQLException {
		return userGroup.contains(page.getCreatedByUser());
	}

}
