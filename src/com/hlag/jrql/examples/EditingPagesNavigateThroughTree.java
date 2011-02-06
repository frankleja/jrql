package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.Iterator;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 */
public class EditingPagesNavigateThroughTree {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page currentPg = project.getPageById("34009");
		PageArrayList listChildren = currentPg.getListChildPages("fields_list");

		for (int i = 0; i < listChildren.size(); i++) {
			Page page = listChildren.getPage(i);
			System.out.println(page.getInfoText());
			System.out.println(page.getLastChangedByUserName());
			System.out.println(page.getLastChangedOnAsddMMyyyyHmma());
		}
		if (currentPg.hasContainerChildPages("blocks_bottom")) {
			Page child = currentPg.getContainerChildPages("blocks_bottom").first();
			System.out.println("page=" + child.getHeadlineAndId());
			System.out.println("released=" + child.isInStateSavedAsDraft());
		}	
	}
}
