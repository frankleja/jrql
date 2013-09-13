package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.User;
import com.hlcl.rql.util.as.PageArrayList;

/**
 * @author lejafr
 * 
 */
public class WorkflowAndPageStateGetPagesResidingInAWorkflowState {

	/**
	 * @param args
	 * @throws RQLException
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		PageArrayList allPagesSavedAsDraft = project.getAllPagesSavedAsDraft();
		PageArrayList allPagesWaitingForCorrection = project.getAllPagesWaitingForCorrection();
		PageArrayList allPagesWaitingForRelease = project.getAllPagesWaitingForRelease();
		
		User connectedUser = client.getConnectedUser();
		connectedUser.getPagesSavedAsDraft(project);
		connectedUser.getPagesWaitingForCorrection(project);
		connectedUser.getPagesWaitingForRelease(project);
		connectedUser.getMyPagesInWorkflow(project);
	}
}
