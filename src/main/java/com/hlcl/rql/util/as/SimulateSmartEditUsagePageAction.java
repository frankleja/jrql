package com.hlcl.rql.util.as;

import java.util.ResourceBundle;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 * Diese Klasse simuliert die Anzeige einer Seite im SmartEdit.
 * <p>
 * Dabei werden alle neuen Templateelemente in der Seite aktiviert und der Seitencache befüllt.
 */
public class SimulateSmartEditUsagePageAction extends PageAction {

	/**
	 * Für jede übergebene Seite wird der SmartEdit HTML code angefordert.
	 * <p>
	 * Für komplexe Seite auftretende IOExceptions durch ASP timeouts werden ignoriert.
	 * 
	 * @see com.hlcl.rql.util.as.PageAction#invoke(com.hlcl.rql.as.Page)
	 */
	@Override
	public void invoke(Page page) throws RQLException {
		try {
			page.simulateSmartEditUsage();
		} catch (RQLException ex) {
			// get value to compare with
			ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
			String serverMsg = b.getString("iisHttp500ErrorText");

			// check for the Error 500 message
			String message = ex.getMessage();
			if (message.indexOf(serverMsg) > 0) {
				// ignore this page cache refresh
			} else {
				// throw this exception
				throw ex;
			}
		}
	}
}
