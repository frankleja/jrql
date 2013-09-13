package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 */
public class ExportAndImportAccessingTwoProjects {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuidOld = "EF103F2989C94BCEBBC189427F9E6729";
		String sessionKeyOld = "6F4929F495D04A779F07587E1B294A6D";
		String projectGuidOld = "5256C671655D4CE696F663C73CE3E526";

		CmsClient clientOld = new CmsClient(logonGuidOld);
		Project projectOld = clientOld.getProject(sessionKeyOld, projectGuidOld);

		String logonGuid = "D714CF042F3749DFA13298E489552468";
		String sessionKey = "C463987D1AAA4F1E9C7B394CD485B7AE";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		Page sourcePg = projectOld.getPageById("old page id");
		PageArrayList listChildren = sourcePg.getListChildPages("fields_list");

		Page targetPg = project.getPageById("new page id");

		for (int i = 0; i < listChildren.size(); i++) {
			Page childPage = listChildren.getPage(i);

		}
	}
}
