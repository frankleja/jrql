package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.List;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.User;
import com.hlcl.rql.as.UserGroup;

/**
 * @author lejafr
 * 
 */
public class ServerManagerAccessUserGroupsAndUsers {

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

		UserGroup userGroup1 = project.getUserGroupByGuid("userGroupGuid");
		UserGroup userGroup2 = project.getUserGroupByName("userGroupName");

		List<UserGroup> userGroups1 = project.getUserGroups();
		List<UserGroup> userGroups2 = project.getUserGroupsNameStartsWith("area-");

		boolean contains = userGroup1.contains("user name");
		User user = client.getUserByName("userName");
		boolean contains2 = userGroup1.contains(user);
		userGroup2.addUser(user);

		List<User> users = userGroup1.getUsers();
		for (User user2 : users) {
			System.out.println(user2.getName());
			System.out.println(user2.getFullname());
			System.out.println(user2.getDescription());
			System.out.println(user2.getEmailAddress());
		}

		user.isAdministratorInCurrentProject();
		user.isAuthorInCurrentProject();
		user.isSiteBuilderInCurrentProject();
		user.isVisitorInCurrentProject();

		user.isActive();
		user.isDirectEditCtlAndMouse();
		user.isDirectEditMouseOnly();
		user.getPublishableProjectVariants();

	}
}
