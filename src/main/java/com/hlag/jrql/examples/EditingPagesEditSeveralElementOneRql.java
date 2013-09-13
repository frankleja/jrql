package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;

/**
 * @author lejafr
 * 
 */
public class EditingPagesEditSeveralElementOneRql {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);
		
		// 1. step - prepare the page
		Page currentPg = project.getPageById("4711");
		currentPg.startSetElementValues();
		
		// 2. step
		// remember new page element values only 
		currentPg.addSetOptionListValue("templateElementName", "value");
		currentPg.addSetStandardFieldDateValue("templateElementName", new ReddotDate());
		currentPg.addSetStandardFieldNumericValue("templateElementName", 20);
		currentPg.addSetStandardFieldTextValue("templateElementName", "text value");
		currentPg.addSetStandardFieldUserDefinedValue("templateElementName", "value");
		
		// 2. step
		// remember copied values
		//TODO needs post
		Page sourcePage = project.getPageById("4712");
		currentPg.addCopyOptionListValueFrom("templateElementName", sourcePage);
		currentPg.addCopyStandardFieldDateValueFrom("templateElementName", sourcePage);
		currentPg.addCopyStandardFieldNumericValueFrom("templateElementName", sourcePage);
		currentPg.addCopyStandardFieldTextValueFrom("templateElementName", sourcePage);
		currentPg.addCopyStandardFieldUserDefinedValueFrom("templateElementName", sourcePage);
		
		// 3. step - build a combined update RQL command and trigger it
		currentPg.endSetElementValues();
		
		
	}
}
