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
public class EditingPagesStandardFields {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page currentPg = project.getPageById("1234");

		System.out.println(currentPg.getStandardFieldTextValue("column_1_header"));
		currentPg.setStandardFieldTextValue("column_1_header", "new header value");

		currentPg.getStandardFieldDateValue("column_updated");
		currentPg.getStandardFieldNumericValue("column_width");
		currentPg.getStandardFieldUserDefinedValue("column_email");

		currentPg.setStandardFieldDateValue("template element name", new ReddotDate());
		currentPg.setStandardFieldNumericValue("template element name", 60);
		currentPg.setStandardFieldUserDefinedValue("template element name", "my-email@test.test");
	}

}
