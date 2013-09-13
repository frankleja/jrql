package com.hlcl.rql.util.as;

import java.util.Comparator;

import com.hlcl.rql.as.*;

/**
 * This class collect comparators for rql framework classes (except for pages, there is a separate hierarchy).
 */
public class Comparators {

	/**
	 * Comparator to sort Users by name (lejafr) ascending.
	 */
	public static Comparator getUserNameComparator() {

		class UserNameComparator implements Comparator {
			public int compare(Object o1, Object o2) {
				User u1 = (User) o1;
				User u2 = (User) o2;
				String name1;
				String name2;
				try {
					name1 = u1.getName();
					name2 = u2.getName();
				} catch (RQLException ex) {
					// wrap the rql exception into a valid class cast exeption
					String msg = "RQLException: " + ex.getMessage();
					Throwable re = ex.getReason();
					if (re != null) {
						msg += re.getClass().getName() + ": " + re.getMessage();
					}
					throw new ClassCastException(msg);
				}
				return name1.compareTo(name2);
			}
		}
		return new UserNameComparator();
	}

	/**
	 * Comparator to sort Templates by name (content_page) ascending.
	 */
	public static Comparator<Template> getTemplateNameComparator() {

		class TemplateNameComparator implements Comparator<Template> {
			public int compare(Template t1, Template t2) {
				return t1.getName().compareTo(t2.getName());
			}
		}
		return new TemplateNameComparator();
	}
}
