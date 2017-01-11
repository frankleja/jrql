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
public class EditingPagesWorkingWitABunchOfPages {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page currentPg = project.getPageById("228766");
		PageArrayList listChildren1 = currentPg.getListChildPages("content_pages_list");
		PageArrayList listChildren2 = currentPg.getListChildPages("content_pages_list", "content_page");

		PageArrayList containerChildren1 = currentPg.getContainerChildPages("blocks_bottom");
		PageArrayList containerChildren2 = currentPg.getContainerChildPages("containerTemplateElementName", "link_table_block");

		listChildren1.findByPageId("pageId");

		listChildren1.findByHeadlineStartsWith("headlinePrefix");
		listChildren1.findByHeadline("headline");

		listChildren1.findByFilename("filename");
		listChildren1.findByFilenameEndsWith("filenameSuffix");

		listChildren1.findByStandardFieldTextValue("templateElementName", "search value");
		listChildren1.findByStandardFieldDateValue("templateElementName", new ReddotDate());

		List<String> collectHeadlines = listChildren1.collectHeadlines();
		List<String> collectHeadlinesAndIds = listChildren1.collectHeadlinesAndIds();
		Set collectLastChangedByUsers = listChildren1.collectLastChangedByUsers();
		List<String> collectTemplateNames = listChildren1.collectTemplateNames();

		// by page attribute
		listChildren1.selectAllCreatedBy(project.getUserGroupByName("user group name"));
		listChildren1.selectAllLastChangedBy("user name");
		listChildren1.selectAllLastChangedOnBefore(10);
		listChildren1.selectAllLastChangedOnBefore(new ReddotDate());

		listChildren1.selectAllPagesBasedOn("content class name");

		listChildren1.selectAllPagesContaining("templateElementName");
		listChildren1.selectAllPagesNotContaining("templateElementName");
		listChildren1.selectAllPagesHeadlineStartsWith("prefix");

		// by workflow state
		listChildren1.selectAllChangeablePages();
		listChildren1.selectAllPagesInStateDraft();
		listChildren1.selectAllPagesInStateDraftChanged();
		listChildren1.selectAllPagesInStateDraftNew();
		listChildren1.selectAllPagesInStateWaitingForCorrection();
		listChildren1.selectAllPagesInStateWaitingForRelease();
		// listChildren1.selectAllPagesInStates(draft, draftNew, draftChanged, waitingForRelease, waitingForCorrection, released);

	}
}
