package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PageSearch;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.ReddotDate;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 */
public class BatchProgrammingUsingPageSearch {

	/**
	 * @param args
	 * @throws RQLException
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid="4D522ED5406E49838F2A77F8F4E6AA97";
		String sessionKey="84289ADEBB3C4E0EB3BE5A15DBE6145E";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// find all instances of a template
		Template cntPgTmplt = project.getTemplateByName("templates_content_all_areas", "content_page");
//		PageArrayList cntPages = cntPgTmplt.getAllPages(10000);
//		for (int i = 0; i < cntPages.size(); i++) {
//			Page p = cntPages.getPage(i);
//			System.out.println(p.getHeadlineAndId());
//		}
		
		// define a seach
		PageSearch search = project.definePageSearch();
		search.addContentClassCriteriaEqual(cntPgTmplt);
		search.addTypeCriteriaOnlyLinkedPages();
//		search.addTypeCriteriaAllPages();
//		search.addTypeCriteriaLinkedAndUnlinkedPages();
//		search.addTypeCriteriaOnlyUnlinkedPages();
//		search.addTypeCriteriaOnlyPagesInRecycleBin();
		search.addLastChangedOnCriteriaLowerEqual(ReddotDate.build("20120101"));
		PageArrayList pages = search.getPages();
		for (int i = 0; i < pages.size(); i++) {
			Page p = pages.getPage(i);
			System.out.println(p.getInfoText());
		}
	}
}
