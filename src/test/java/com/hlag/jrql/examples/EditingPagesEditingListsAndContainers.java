package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.List;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.MultiLink;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class EditingPagesEditingListsAndContainers {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page currentPg = project.getPageById("4711");

		MultiLink multiLink = currentPg.getMultiLink("multiLinkTemplateElementName");
		List multiLinks = currentPg.getMultiLinks();
		boolean includeReferences = false;
		List<MultiLink> multiLinks2 = currentPg.getMultiLinks(includeReferences);

		List<MultiLink> linksToThisPage = currentPg.getMultiLinksToThisPage();
		MultiLink mainMultiLink = currentPg.getMainMultiLink();
	}
}
