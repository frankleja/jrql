package com.hlag.jrql.examples;

/**
 * 
 */

import java.util.ArrayList;
import java.util.List;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.User;
import com.hlcl.rql.as.UserGroup;

/**
 * @author lejafr TODO needs post
 */
public class ServerManagerEditingUserGroups {

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

		// add users to user group
		User user = client.getUserByName("lejafr");
		userGroup1.addUser(user);
		userGroup1.addUsers(userGroup2);

		// add given users
		List<User> users = new ArrayList<User>();
		users.add(client.getUserByName("lejafr2"));
		users.add(client.getUserByName("lejafr3"));
		userGroup1.addUsers(users);
		
		// remove users
		userGroup1.removeUser(user);
		userGroup1.addUsers(userGroup2);
		userGroup1.removeUsers(users);
		
		// find users from group1 not in group2
		userGroup1.difference(userGroup2);

	}
}
