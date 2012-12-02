package com.hlcl.rql.util.as;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;

/**
 * @author lejafr
 * 
 * Liefert alle Seiten, deren StandardFieldDate Wert vor (<, nicht =) dem gegebenen Datum geändert wurden.
 * <p>
 * Achtung, alle Seiten in der Liste müssen dieses StandardFieldDate Element besitzen! Auch wenn sie gar nicht zurückgegeben werden sollen.
 */
public class StandardFieldDateBeforePageFilter extends PageFilterImpl {

	private ReddotDate marginDate;
	private String standardFieldDateTmpltElemName;

	/**
	 * constructor comment.
	 */
	public StandardFieldDateBeforePageFilter(ReddotDate marginDate, String standardFieldDateTmpltElemName) {
		super();

		this.marginDate = marginDate;
		this.standardFieldDateTmpltElemName = standardFieldDateTmpltElemName;
	}

	/**
	 * Liefert true, falls der Wert im Feld standardFieldDateTmpltElemName vor marginDate liegt.
	 */
	public boolean check(Page page) throws RQLException {
		return page.getStandardFieldDateValue(standardFieldDateTmpltElemName).before(marginDate);
	}
}
