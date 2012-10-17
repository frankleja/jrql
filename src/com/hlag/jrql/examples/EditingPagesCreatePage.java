package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;

/**
 * @author lejafr
 * 
 */
public class EditingPagesCreatePage {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid="4D522ED5406E49838F2A77F8F4E6AA97";
		String sessionKey="84289ADEBB3C4E0EB3BE5A15DBE6145E";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

//		// create with template bottom
//		Page demoPg = project.getPageById("26629");
//		Template cntPgTmplt = project.getTemplateByName("templates_content_all_areas", "content_page");
//		Page cntPg = demoPg.createAndConnectPageAtList("internal_list", cntPgTmplt, "jrql test created content page", true);
//		System.out.println(cntPg.getInfoText());
//		
//		// create container page
//		Template textBlockTmplt = project.getTemplateByGuid("2722AAEFF2644C859666D19A66984BCE");
//		Page textBlockPg = cntPg.createAndConnectPageAtContainer("blocks", textBlockTmplt, "text_block");
//		System.out.println(textBlockPg.getInfoText());
//		textBlockPg.setTextValue("text", "<h2>Überschrift</h2><p>ein text, programmatisch gesetzt</p>");
//		
//		// submit both pages
//		cntPg.submitToWorkflow();
//		textBlockPg.submitToWorkflow();

		// create without template top
		Page prListPg = project.getPageById("26630");
		Page prPg = prListPg.createAndConnectPageAtList("navi_list", "new pr details page", false);
		prPg.setTextValue("issue_teaser_text", "hier der teaser ascii text rein");
		prPg.setStandardFieldDateValueToToday("issue_date_sfd");
		prPg.setTextValue("text", "<p>der Text der Pressemitteilung</p>");
		prPg.submitToWorkflow();
	}
}
