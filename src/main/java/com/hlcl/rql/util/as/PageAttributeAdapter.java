/**
 * 
 */
package com.hlcl.rql.util.as;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;

/**
 * This class adapts a specific page attribute. Uses reflection API.
 * 
 * @author lejafr
 */
public class PageAttributeAdapter {

	private Page adaptee;
	private String methodName;
	private String parameterValue;

	private Method method;

	/**
	 * Constructor for a page attribute of given page adaptee. Page method has no parameter and has to return a string.
	 */
	public PageAttributeAdapter(Page adaptee, String methodName) throws RQLException {
		this.adaptee = adaptee;
		this.methodName = methodName;
		this.parameterValue = null;

		buildMethod();
	}

	/**
	 * Constructor for a arbitrary page attribute of given page adaptee. Page method uses the given parameter and has to return a string.
	 */
	public PageAttributeAdapter(Page adaptee, String methodName, String parameterValue) throws RQLException {
		this.adaptee = adaptee;
		this.methodName = methodName;
		this.parameterValue = parameterValue;

		buildMethod();
	}

	/**
	 * Erzeugt aus den gegebenen Werte eine methode des reflection api.
	 */
	private void buildMethod() throws RQLException {
		try {
			if (parameterValue == null) {
				this.method = adaptee.getClass().getMethod(methodName);
			} else {
				Class<?>[] parms = new Class[1];
				parms[0] = parameterValue.getClass();
				this.method = adaptee.getClass().getMethod(methodName, parms);
			}
		} catch (SecurityException ex) {
			throw new RQLException("Security exception getting method with name " + methodName + " for parameter value " + parameterValue
					+ " on page " + adaptee.getInfoText(), ex);
		} catch (NoSuchMethodException ex) {
			throw new RQLException("Missing method with name " + methodName + " for parameter value " + parameterValue + " on page "
					+ adaptee.getInfoText(), ex);
		}

		// check method's return type: fix string
		if (!method.getReturnType().equals("".getClass())) {
			throw new RQLException("Adapter misconfiguration exception: method with name " + methodName + " for parameter value " + parameterValue
					+ " doesn't return a string object");
		}
	}

	/**
	 * Returns the configured attribute value from the page; the return value of the method call.
	 */
	public String getValue() throws RQLException {
		String result = null;
		try {
			if (parameterValue == null) {
				// simple getter
				result = (String) method.invoke(adaptee);
			} else {
				// one string parameter value
				result = (String) method.invoke(adaptee, parameterValue);
			}
		} catch (IllegalArgumentException ex) {
			throw new RQLException("Illegal argument exception invoking method with name " + method.getName() + " for parameter value "
					+ parameterValue + " on page " + adaptee.getInfoText(), ex);
		} catch (IllegalAccessException ex) {
			throw new RQLException("Illegal access exception invoking method with name " + method.getName() + " for parameter value "
					+ parameterValue + " on page " + adaptee.getInfoText(), ex);
		} catch (InvocationTargetException ex) {
			throw new RQLException("Invocation target exception invoking method with name " + method.getName() + " for parameter value "
					+ parameterValue + " on page " + adaptee.getInfoText(), ex);
		}
		return result;
	}
}
