package com.hlcl.rql.util.as;

import com.hlcl.rql.as.*;
/**
 * Diese Klasse filtert Seiten eines gegebenen Users.
 * 
 * @author lejafr
 */
public class UserPageFilter extends PageFilterImpl {

	private String username;
/**
 * UserPageFilter constructor comment.
 */
public UserPageFilter(User user) throws RQLException {
	super();

	this.username = user.getName();
}
/**
 * UserPageFilter constructor comment.
 *
 *@param	username	name of user
 */
public UserPageFilter(String username) {
	super();

	this.username = username;
}
/**
 * Prüft die gegebenen Seite auf Gültigkeit dieses Filters.
 * Liefert true, falls der Erstellungsuser der Seite gleich dem gegebenen Usernamen ist.
 */
public boolean check(Page page) throws RQLException {
	
	return page.getCreatedByUserName().equals(username);
}
}
