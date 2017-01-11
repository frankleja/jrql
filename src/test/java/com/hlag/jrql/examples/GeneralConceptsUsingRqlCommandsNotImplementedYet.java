package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.PublicationPackage;
import com.hlcl.rql.as.RQLException;

/**
 * @author lejafr
 * 
 */
public class GeneralConceptsUsingRqlCommandsNotImplementedYet {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// get session info
		client.getLogonGuid();
		project.getSessionKey();
		project.getProjectGuid();

		// example; create a new publication package by copying from existing one
		PublicationPackage oldPckg = project.getPublicationPackageByName("publ_pages_to_career");
		String newPckgName = "publ_pages_to_test";
		String rqlRequest = "<IODATA loginguid='" + logonGuid + "' sessionkey='" + sessionKey + "'>"
				+ "<PROJECT><EXPORTPACKET action='copy' guid='" + oldPckg.getPublicationPackageGuid() + "' name='" + newPckgName
				+ " '/>" + "</PROJECT></IODATA>";

		// call cms and ignore response (no parsing)
		client.callCmsWithoutParsing(rqlRequest);

		// get newly created package
		PublicationPackage newPckg = project.getPublicationPackageByName(newPckgName);
		// further actions on newPckg
	}
}
