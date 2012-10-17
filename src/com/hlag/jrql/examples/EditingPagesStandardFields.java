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

		String logonGuid="D68A3DF135AE41FFBC341C773BF5800C";
		String sessionKey="39FDF72C0D0C48F09E9143FA46CB55B2";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// press release details page
		// standard field text and date
		Page page = project.getPageById("26380");
		System.out.println("page: " + page.getHeadline());
		System.out.println(page.getStandardFieldTextValue("renderCallback_sft"));
		System.out.println(page.getStandardFieldDateValue("issue_date_sfd").getAsddMMyyyy());

//		// tariff records page
//		// standard filed numeric, option list 
//		page = project.getPageById("4613");
//		System.out.println("page: " + page.getHeadline());
//		System.out.println(page.getStandardFieldNumericValue("col_tariff_number_width"));
//		// consider template element default
//		System.out.println(page.getStandardFieldTextValue("stopNavigation"));
//		// option list value
//		System.out.println(page.getOptionListValue("publishWithFollowingPages"));
//		
//		// office details row page, nicosia
//		// standard field user defined
//		page = project.getPageById("5786");
//		System.out.println("page: " + page.getHeadline());
//		System.out.println(page.getStandardFieldUserDefinedValue("e_mail"));
//		System.out.println(page.getStandardFieldUserDefinedValue("phone"));
//		
//		
//		// update pr details page
//		page = project.getPageById("26631");
//		page.setStandardFieldTextValue("meta_description", "eine Beispielseite");
//		page.setStandardFieldDateValue("issue_date_sfd", new ReddotDate());
//		page.setOptionListValue("publishWithFollowingPages", "ask");
//		
//		// update office details row
//		page = project.getPageById("26633");
//		page.setStandardFieldTextValue("name", "Frank Leja");
//		page.setStandardFieldUserDefinedValue("phone", "+49 40 3001 0");
		
		
	}

}
