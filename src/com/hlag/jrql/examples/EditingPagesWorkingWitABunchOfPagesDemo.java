package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.List;
import java.util.Set;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 */
public class EditingPagesWorkingWitABunchOfPagesDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid="D68A3DF135AE41FFBC341C773BF5800C";
		String sessionKey="39FDF72C0D0C48F09E9143FA46CB55B2";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// child pages from list 
		// get all pr details pages
		Page prListPg = project.getPageById("237");
		PageArrayList prPages = prListPg.getListChildPages("navi_list");
		for (int i = 0; i < prPages.size(); i++) {
			Page page = prPages.getPage(i);
			// echo details
			System.out.println(page.getHeadlineAndId());
//			System.out.println("  " + page.getStandardFieldDateValueFormatted("issue_date_sfd"));
//			System.out.println("  " + page.getTextValue("issue_teaser_text")); // ascii text
		}
		
		
//		// find child page by template from container
//		// office hamburg page
//		System.out.println("\nfind container block page");
//		Page officePg = project.getPageById("6070");
//		PageArrayList blocks = officePg.getContainerChildPages("content_right_ctr", "textlinks_right_block");
//		for (int i = 0; i < blocks.size(); i++) {
//			Page page = blocks.getPage(i);
//			System.out.println(page.getInfoText());
//		}
//		
//		// find child page
//		System.out.println("\nfind page by date value");
//		Page page = prPages.findByStandardFieldDateValue("issue_date_sfd", ReddotDate.build("20120817"));
//		System.out.println(page.getInfoText());
//		
//		// sort all children
//		Page sortPg = project.getPageById("26630");
//		sortPg.getList("navi_list").sortChildsByDateDesc("issue_date_sfd");
	}
}
