package com.hlcl.rql.util.as;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hlcl.rql.as.Container;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.TemplateFolder;

/**
 * Diese Klasse erleichtert den Zugriff auf Parameter eines Scripts. Diese werden in einer Seite vom Template rql_script gespeichert.
 * 
 * @author lejafr
 */
public class ScriptParameters {

	private static final String PARAMETERS_TMPLT_ELEM_NAME = "parameters";
	private static final String USER_GROUP_CHECK_TEMPLATE_NAME_PREFIX = "UserGroupCheck_";
	private static final String USER_GROUP_CHECK_TEMPLATE_FOLDER_NAME = "rql_templates";
	// constants
	// delimits the parameter name from parameter value
	private static final String DELIMITER = "=";

	private static final String VALUE_TMPLT_ELEM_NAME = "value";
	// cache
	private Map<String, String> parameters;

	private Page scriptPage;

	/**
	 * ScriptParameters constructor comment.
	 */
	public ScriptParameters(Page scriptPage) {
		super();

		this.scriptPage = scriptPage;
	}

	/**
	 * Liefert den Wert des Parameters konvertiert nach int mit dem gegebenen Namen zurück.
	 */
	public int getInt(String parameterName) throws RQLException {

		return Integer.parseInt(get(parameterName));
	}

	/**
	 * Returns the page ID of the start page for this parameter set.
	 */
	public String getPageId() throws RQLException {
		return scriptPage.getPageId();
	}

	/**
	 * Liefert die Liste alle Templates für die GUIDs im gegebenen parameterName.
	 */
	public java.util.List<Template> getTemplatesByGuid(String parameterName, String separator) throws RQLException {
		java.util.List<Template> result = new ArrayList<Template>();
		String[] guids = StringHelper.split(get(parameterName), separator);
		for (int i = 0; i < guids.length; i++) {
			String guid = guids[i];
			result.add(getProject().getTemplateByGuid(guid));
		}
		return result;
	}

	/**
	 * Liefert den Wert des Parameters (nur true oder false) konvertiert nach boolean mit dem gegebenen Namen zurück.
	 */
	public boolean getBoolean(String parameterName) throws RQLException {

		return StringHelper.convertToBoolean(get(parameterName));
	}

	/**
	 * Liefert den Wert des Parameters mit dem gegebenen Namen zurück.
	 */
	public String get(String parameterName) throws RQLException {

		return (String) getParameters().get(parameterName);
	}

	/**
	 * Returns all parameter names starting with the given prefix.
	 */
	public Set<String> getKeySet(String prefix) throws RQLException {
		Set<String> result = new HashSet<String>();
		// filter
		for (String parmName : getKeySet()) {
			if (parmName.startsWith(prefix)) {
				result.add(parmName);
			}
		}
		return result;
	}
	/**
	 * Liefert das Set mit allen Keynamen des Parametermappings zurück.
	 */
	public Set<String> getKeySet() throws RQLException {

		Set keySet = getParameters().keySet();
		Set<String> result = new HashSet<String>(keySet.size());
		// convert from objects to strings
		for (Iterator iter = keySet.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			result.add(key);
		}
		return result;
	}

	/**
	 * Liefert das Set mit allen Keynamen des Parametermappings sortiert zurück.
	 */
	public java.util.SortedSet<String> getKeySetSorted() throws RQLException {

		return new TreeSet<String>(getKeySet());
	}

	/**
	 * RQL-seitige Prüfung ob ein User in einer Gruppe ist.
	 * <p>
	 * Da normale Autoren keine Benutzergruppe/User aus dem ServerManager lesen können, wird die Zulassung eines Templates für die Prüfung mißbraucht.
	 * <p>
	 * Liefert true, falls das Template mit dem Namen UserGroupCheck_<userGroupName> im Ordner rql_templates für den angemeldeten Benutzer sichtbar
	 * ist.
	 */
	public boolean isConnectedUserInUserGroup(String userGroupName) throws RQLException {

		return getUserGroupCheckTemplateFolder().containsByName(buildUserGroupCheckTemplateName(userGroupName));
	}

	/**
	 * Liefert den TemplateNamen, mit dem geprüft wird, ob ein User in einer Gruppe ist.
	 * <p>
	 */
	private String buildUserGroupCheckTemplateName(String userGroupName) throws RQLException {

		return USER_GROUP_CHECK_TEMPLATE_NAME_PREFIX + userGroupName;
	}

	/**
	 * Liefert den TemplateFolder mit dem geprüft wird, ob ein User in einer Gruppe ist.
	 * <p>
	 */
	private TemplateFolder getUserGroupCheckTemplateFolder() throws RQLException {

		return getProject().getTemplateFolderByName(USER_GROUP_CHECK_TEMPLATE_FOLDER_NAME);
	}

	/**
	 * Ersetzt in allen Parameterwerten aller Parameter den gegebenen Wert find mit replace, falls find gefunden wurde.
	 * <p>
	 * Geänderte Seiten werden automatisch bestätigt.
	 * TODO eine private Klasse verwenden, um die Auslösung key - value zu kapseln
	 */
	public void replaceInAllParameterValues(String findValue, String replaceValue) throws RQLException {
		PageArrayList parms = scriptPage.getContainerChildPages(PARAMETERS_TMPLT_ELEM_NAME);
		String key = null;
		String value = null;
		String newValue = null;
		for (Iterator iterator = parms.iterator(); iterator.hasNext();) {
			Page parameterPg = (Page) iterator.next();
			String headline = parameterPg.getHeadline();
			if (headline.indexOf(DELIMITER) < 0) {
				// = not found, read value from ascii text field
				key = headline;
				value = parameterPg.getTextValue(VALUE_TMPLT_ELEM_NAME);
				// replace
				newValue = StringHelper.replace(value, findValue, replaceValue);
				if (!newValue.equals(value)) {
					parameterPg.setTextValue(VALUE_TMPLT_ELEM_NAME, newValue);
					if (parameterPg.isInStateSavedAsDraft()) {
						parameterPg.submitToWorkflow();
					}
					// force re-read of parameters
					parameters = null;
				}
			} else {
				// keep the easy style via the headline for < 256 chars both together
				String[] pair = StringHelper.split(headline, DELIMITER);
				key = pair[0];
				value = pair[1];
				// replace
				newValue = StringHelper.replace(value, findValue, replaceValue);
				if (!newValue.equals(value)) {
					parameterPg.setHeadline(key + DELIMITER + newValue);
					if (parameterPg.isInStateSavedAsDraft()) {
						parameterPg.submitToWorkflow();
					}
					// force re-read of parameters
					parameters = null;
				}
			}
		}
	}

	/**
	 * Liefert das aktuelle Projekt.
	 */
	public Project getProject() throws RQLException {

		return scriptPage.getProject();
	}

	/**
	 * Liefert die map mit allen Parametern des Scripts zurück.
	 */
	private Map<String, String> getParameters() throws RQLException {

		if (parameters == null) {
			// read and save all parameters from CMS
			parameters = new HashMap<String, String>();
			Container parametersCtr = scriptPage.getContainer(PARAMETERS_TMPLT_ELEM_NAME);
			PageArrayList childList = parametersCtr.getChildPages();
			String key = null;
			String value = null;
			for (int i = 0; i < childList.size(); i++) {
				Page parameterPg = (Page) childList.get(i);
				String headline = parameterPg.getHeadline();
				if (headline.indexOf(DELIMITER) < 0) {
					// = not found, read value from ascii text field
					key = headline;
					value = parameterPg.getTextValue(VALUE_TMPLT_ELEM_NAME);
				} else {
					// keep the easy style via the headline for < 256 chars both together
					String[] pair = StringHelper.splitAt1stOccurenceFromLeft(headline, DELIMITER);
					key = pair[0];
					value = pair[1];
				}
				// for both
				parameters.put(key, value);
			}
		}

		return parameters;
	}
}
