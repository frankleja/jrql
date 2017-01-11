package com.hlcl.rql.as;

/**
 * One particular authorization together with its allowed/denied state.
 */
public class AuthorizationSet {

	public Authorization authorization;
	public boolean allowed;
	public boolean denied;
	
	public String toString() {
		return authorization.getPrefix() + "." + authorization.toString() + ": " + (allowed?"A":"_") + (denied?"D":"_");
	}
	
	
	/**
	 * Inverse of the toString() result
	 * 
	 * @param spec format "Prefix.Name: AD" (or A_ or _D or __)
	 * @return null on parse error
	 */
	public static AuthorizationSet parse(String spec) {
		int colon = spec.indexOf(':');
		Authorization a = Authorization.Fun.parse(spec.substring(0, colon));
		if (a == null)
			return null;
		
		AuthorizationSet out = new AuthorizationSet();
		out.authorization = a;
		try {
			out.allowed = spec.charAt(colon + 2) == 'A';
			out.denied = spec.charAt(colon + 3) == 'D';
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		}
		return out;
	}
}
