package com.hlcl.rql.as;

/**
 * One particular authorization together with its allowed/denied state.
 */
public class AuthorizationSet {

	public Authorization authorization;
	public boolean allowed;
	public boolean denied;
	
	public String toString() {
		return authorization.toString() + ": " + (allowed?"A":"_") + (denied?"D":"_");
	}	
}
