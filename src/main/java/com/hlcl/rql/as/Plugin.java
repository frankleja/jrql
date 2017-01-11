package com.hlcl.rql.as;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Diese Klasse beschreibt ein Plugin.
 * 
 * @author LEJAFR
 */
public class Plugin implements CmsClientContainer {

	private boolean active;
	private CmsClient cmsClient;
	private RQLNode detailsNode; // cache
	private String name;
	private String pluginGuid;
	private String url;

	/**
	 * Vollständiger Konstruktor mit allen Attributen.
	 * 
	 * @param client
	 *            the cms client instance
	 * @param pluginGuid
	 *            the GUID of this plugin
	 * @param active0or1
	 *            =1, if this plugin is currently active, else 0
	 * @param name
	 *            the name of this plugin
	 * @param url
	 *            the url to call, when this plugin gets started
	 */
	public Plugin(CmsClient client, String pluginGuid, String active0or1, String name) {
		super();

		this.cmsClient = client;
		this.active = active0or1.equals("1");
		this.pluginGuid = pluginGuid;
		this.name = name;
		// FIXME: url is missing
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines
	 * Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Löscht alle lokale gecachten Werte.
	 */
	private void deleteCaches() {
		detailsNode = null;
	}

	/**
	 * Kopiert den Bezeichner für die gegebene mainLanguage in alle anderen Oberflächensprachen.
	 * <p>
	 * ACHTUNG: Im save RQL müssen im Tag DESCRIPTIONS alle Sprachen vorkommen, sonst werden diese gelöscht! Weiterhin muss active
	 * mitgegeben werden, sonst werden aktive deaktiviert!
	 */
	public void equalizeLabelsAccordingTo(UserInterfaceLanguage mainLanguage) throws RQLException {
		setLabelToAllUserInterfaceLanguages(getLabel(mainLanguage));
	}

	/**
	 * Ändert den Bezeichner für alle Benutzersprachen auf den gegebenen Wert label.
	 */
	public void setLabelToAllUserInterfaceLanguages(String label) throws RQLException {
		java.util.List<UserInterfaceLanguage> languages = getCmsClient().getUserInterfaceLanguages();
		java.util.List<String> labels = new ArrayList<String>(languages.size());
		Collections.fill(labels, label);
		setLabels(languages, labels);
	}

	/**
	 * Ändert den in Menüs angezeigten Namen dieses Plugins für alle gegebenen Benutzerinterfacesprachen auf die gegebenen Labels. Die
	 * Zuordnung erfolgt dabei über den Index. Die Labels aller nicht gegebenen Sprachen werden gelöscht!
	 * <p>
	 * ACHTUNG: Im save RQL müssen im Tag DESCRIPTIONS alle Sprachen vorkommen, sonst werden diese gelöscht! Weiterhin muss active
	 * mitgegeben werden, sonst werden aktive deaktiviert!
	 */
	private void setLabels(java.util.List<UserInterfaceLanguage> userInterfaceLanguages, java.util.List<String> labels)
			throws RQLException {
		/* 
		 V7.5 request
		<IODATA loginguid='A27CF949C92948BFAB6BE65BD17AC55A'>
		<PLUGINS>
		<PLUGIN action="save" guid="3ECEB43369364449BACD7F1B3E82815F">
		<DESCRIPTIONS>
		<CHS name=""/>
		<HUN name=""/>
		<PTB name=""/>
		<SVE name=""/>
		<PLK name=""/>
		<ESN name=""/>
		<ITA name=""/>
		<FRC name="Cancel waiting jobs - LOCAL"/>
		<DEU name="Cancel waiting jobs - LOCAL"/>
		<CSY name=""/>
		<ENG name="Cancel waiting jobs - LOCAL"/>
		</DESCRIPTIONS>
		</PLUGIN>
		</PLUGINS>
		</IODATA>
		 V7.5 response 
		<IODATA>
		<PLUGINS>
		<PLUGIN action="save" guid="3ECEB43369364449BACD7F1B3E82815F">
		<DESCRIPTIONS>
		<CHS name=""/>
		<HUN name=""/>
		<PTB name=""/>
		<SVE name=""/>
		<PLK name=""/>
		<ESN name=""/>
		<ITA name=""/>
		<FRC name="Cancel waiting jobs - LOCAL"/>
		<DEU name="Cancel waiting jobs - LOCAL"/>
		<CSY name=""/>
		<ENG name="Cancel waiting jobs - LOCAL"/>
		</DESCRIPTIONS>
		</PLUGIN>
		</PLUGINS>
		</IODATA>
		 */

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'><PLUGINS>" + " <PLUGIN action='save' guid='" + getPluginGuid()
				+ "' active='" + StringHelper.convertTo01(active) + "'><DESCRIPTIONS>";
		java.util.List<UserInterfaceLanguage> languages = getCmsClient().getUserInterfaceLanguages();
		for (UserInterfaceLanguage language : languages) {
			String label = "";
			int index = userInterfaceLanguages.indexOf(language);
			if (index >= 0) {
				label = labels.get(index);
			}
			rqlRequest += "<" + language.getLanguageId() + " name='" + label + "'/>";
		}
		rqlRequest += "</DESCRIPTIONS></PLUGIN></PLUGINS></IODATA>";
		// call ignore response
		callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 * <p>
	 * The general contract of <code>hashCode</code> is:
	 * <ul>
	 * <li>Whenever it is invoked on the same object more than once during an execution of a Java application, the <tt>hashCode</tt>
	 * method must consistently return the same integer, provided no information used in <tt>equals</tt> comparisons on the object is
	 * modified. This integer need not remain consistent from one execution of an application to another execution of the same
	 * application.
	 * <li>If two objects are equal according to the <tt>equals(Object)</tt> method, then calling the <code>hashCode</code> method
	 * on each of the two objects must produce the same integer result.
	 * <li>It is <em>not</em> required that if two objects are unequal according to the
	 * {@link java.lang.Object#equals(java.lang.Object)} method, then calling the <tt>hashCode</tt> method on each of the two objects
	 * must produce distinct integer results. However, the programmer should be aware that producing distinct integer results for
	 * unequal objects may improve the performance of hashtables.
	 * </ul>
	 * <p>
	 * As much as is reasonably practical, the hashCode method defined by class <tt>Object</tt> does return distinct integers for
	 * distinct objects. (This is typically implemented by converting the internal address of the object into an integer, but this
	 * implementation technique is not required by the Java<font size="-2"><sup>TM</sup></font> programming language.)
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @see java.util.Hashtable
	 */
	public int hashCode() {

		return getPluginGuid().hashCode();
	}

	/**
	 * Plugins mit der gleichen GUID werden als gleich betrachtet.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		Plugin other = (Plugin) obj;
		return getPluginGuid().equals(other.getPluginGuid());
	}

	/**
	 * Liefert den CmsClient.
	 */
	public CmsClient getCmsClient() {
		return cmsClient;
	}

	/**
	 * Liefert den RQLNode mit weiteren Information zu diesem Plugin zurück.
	 */
	private RQLNode getDetailsNode() throws RQLException {

		/* 
		 V7.5 request
		 <IODATA loginguid="A27CF949C92948BFAB6BE65BD17AC55A">
		 <PLUGINS>
		 <PLUGIN action="load" guid="00CC4D3689CC4DCD95F9E0820C633708"/>
		 </PLUGINS>
		 </IODATA>
		 V7.5 response 
		 <IODATA>
		 <PLUGINS>
		 <PLUGIN action="load" guid="00CC4D3689CC4DCD95F9E0820C633708" name="showPublishedFilename.jsp - TEST" active="0" compatibility="7.5" company="" contact="Frank Leja" telefon="" website="www.hapag-lloyd.com" email="lejafr@hlag.com" url="showPublishedFilename_test.asp">
		 <DESCRIPTIONS>
		 <CHS name=""/>
		 <HUN name=""/>
		 <PTB name=""/>
		 <SVE name=""/>
		 <PLK name=""/>
		 <ESN name=""/>
		 <ELL name=""/>
		 <ITA name=""/>
		 <FRC name=""/>
		 <DEU name="Show filename - TEST"/>
		 <CSY name=""/>
		 <ENG name="Show filename - TEST"/>
		 <JPN name=""/>
		 </DESCRIPTIONS>
		 <TARGETS>
		 <TARGET target="ioTreeProjectPage"/>
		 <TARGET target=""/>
		 <TARGET target=""/>
		 </TARGETS>
		 <ICON source="../ActionMenuIcons/common_information.gif" alt=""/>
		 <OPENWINDOWPARAMETER features="toolbar=0,location=1,directories=0,status=1,menubar=0,scrollbars=1,resizable=1,width=990,height=600,screenX=100,screenY=100,left=100,top=100"/>
		 </PLUGIN>
		 </PLUGINS>
		 </IODATA>
		 */

		// cache the node with details information
		if (detailsNode == null) {
			// call CMS
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'><PLUGINS>" + " <PLUGIN action='load' guid='"
					+ getPluginGuid() + "'/>" + "</PLUGINS></IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			detailsNode = rqlResponse.getNode("PLUGIN");
		}
		return detailsNode;
	}

	/**
	 * Liefert den in Menüs angezeigten Bezeichner dieses Plugins für die gegebene Sprache.
	 * <p>
	 * Liefert einen leeren String, falls kein Bezeichner definiert ist.
	 */
	public String getLabel(UserInterfaceLanguage language) throws RQLException {
		RQLNode langNode = getDetailsNode().getNode("DESCRIPTIONS").getNode(language.getLanguageId());
		return langNode == null ? "" : langNode.getAttribute("name");
	}

	/**
	 * Liefert den in Menüs angezeigten Bezeichner dieses Plugins für die gegebenen Sprachen. Die Anzahl und Reihenfolge der Labels
	 * entspricht der der gegebenen Sprachen.
	 * <p>
	 * Liefert einen leeren String, falls kein Bezeichner definiert ist.
	 */
	public java.util.List<String> getLabels(java.util.List<UserInterfaceLanguage> languages) throws RQLException {
		java.util.List<String> labels = new ArrayList<String>(languages.size());
		for (UserInterfaceLanguage language : languages) {
			labels.add(getLabel(language));
		}
		return labels;
	}

	/**
	 * Liefert die in Menüs angezeigten Bezeichner dieses Plugins für alle Benutzeroberflächensprachen. Die Anzahl und Reihenfolge der
	 * Labels entspricht der Liste alle Benutzoberflächensprachen {@link CmsClient#getUserInterfaceLanguages()}.
	 * <p>
	 * Liefert als Label einen leeren String, falls kein Bezeichner definiert ist.
	 */
	public java.util.List<String> getAllLabels() throws RQLException {
		return getLabels(getCmsClient().getUserInterfaceLanguages());
	}

	/**
	 * Ändert den in Menüs angezeigten Bezeichner dieses Plugins für die gegebene Sprache auf den gegebenen Wert label.
	 */
	public String setLabel(UserInterfaceLanguage language, String label) throws RQLException {
		RQLNode langNode = getDetailsNode().getNode("DESCRIPTIONS").getNode(language.getLanguageId());
		return langNode == null ? "" : langNode.getAttribute("name");
	}

	/**
	 * Liefert die RedDot logon GUID des users unter dem das script läuft.
	 */
	public String getLogonGuid() {
		return getCmsClient().getLogonGuid();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the url of this plugin.
	 * @throws RQLException
	 */
	public String getUrl() throws RQLException {
		if (url == null) {
			url = getDetailsNode().getAttribute("url");
		}
		return url;
	}

	/**
	 * @return Returns the icon source of this plugin, e.g. ../ActionMenuIcons/Common_Delete.gif.
	 * @throws RQLException
	 */
	public String getIconSource() throws RQLException {
		return getDetailsNode().getNode("ICON").getAttribute("source");
	}

	/**
	 * @return Returns the pluginGuid.
	 */
	public String getPluginGuid() {
		return pluginGuid;
	}

	/**
	 * @return Returns the active.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Ändert die Eigenschaft active dieses Plugins.
	 */
	public void setIsActive(boolean isActive) throws RQLException {
		save("active", StringHelper.convertTo01(isActive));
		active = isActive;
	}

	/**
	 * Ändert die URL dieses Plugins.
	 */
	public void setUrl(String url) throws RQLException {
		save("url", url);
		this.url = url;
	}

	/**
	 * Ändert den Namen dieses Plugins.
	 */
	public void setName(String name) throws RQLException {
		save("name", name);
		this.name = name;
	}

	/**
	 * Ersetzt im Namen dieses Plugins find gegen replace.
	 */
	public void replaceName(String find, String replace) throws RQLException {
		String oldName = getName();
		String newName = StringHelper.replace(oldName, find, replace);
		if (!newName.equals(oldName)) {
			setName(newName);
		}
	}

	/**
	 * Ersetzt in der URL dieses Plugins find gegen replace.
	 */
	public void replaceUrl(String find, String replace) throws RQLException {
		String oldUrl = getUrl();
		String newUrl = StringHelper.replace(oldUrl, find, replace);
		if (!newUrl.equals(oldUrl)) {
			setUrl(newUrl);
		}
	}

	/**
	 * Ersetzt in allen Labels dieses Plugins find gegen replace.
	 */
	public void replaceLabelInAllUserInterfaceLanguages(String find, String replace) throws RQLException {
		List<String> allLabels = getAllLabels();
		for (int i = 0; i < allLabels.size(); i++) {
			String oldLabel = allLabels.get(i);
			String newLabel = StringHelper.replace(oldLabel, find, replace);
			allLabels.set(i, newLabel);
		}
		setLabels(getCmsClient().getUserInterfaceLanguages(), allLabels);
	}

	/**
	 * Change on this plugin the given attribute. Repeat tag ICON here needed, otherwise plugin is not visible in menus.
	 */
	private void save(String attributeName, String attributeValue) throws RQLException {
		/* 
		 V9 request
		 <IODATA loginguid="A27CF949C92948BFAB6BE65BD17AC55A">
		  <PLUGINS>
		    <PLUGIN action="save" active="1" guid="BA7DF9E46EB8450C9BF347D47E709DA2">
		    <ICON source="../ActionMenuIcons/common_information.gif" alt=""/>
		    </PLUGIN>
		  </PLUGINS>
		</IODATA>
		 V9 response 
		<IODATA>
		<PLUGINS>
		<PLUGIN action="save" active="1" guid="BA7DF9E46EB8450C9BF347D47E709DA2">
		<ICON source="../ActionMenuIcons/common_information.gif" alt=""/>
		</PLUGIN>
		</PLUGINS>
		</IODATA>
		 */

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'><PLUGINS>" + " <PLUGIN action='save' guid='" + getPluginGuid()
				+ "' " + attributeName + "='" + attributeValue + "'>" + "<ICON source='" + getIconSource()
				+ "' alt=''/></PLUGIN></PLUGINS></IODATA>";
		// call ignore response
		callCmsWithoutParsing(rqlRequest);

		// correct local cache
		deleteCaches();
	}

	/**
	 * Delete this plugin. This object cannot be used afterwards.
	 */
	public void delete() throws RQLException {
		/* 
		 V9 request
		<IODATA loginguid="[!guid_login!]">
		  <PLUGINS>
		    <PLUGIN action="delete" guid="[!guid_plugin!]"/>
		    ...
		  </PLUGINS>
		</IODATA>
		 V9 response 
		<IODATA>
		  <PLUGINS>
		    <PLUGIN action="delete" guid="[!guid_plugin!]">ok</PLUGIN>
		    <PLUGIN action="delete" guid="[!guid_plugin!]">error</PLUGIN>
		    ...
		  </PLUGINS>
		</IODATA>
		 */

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'><PLUGINS><PLUGIN action='delete' guid='" + getPluginGuid()
				+ "'></PLUGIN></PLUGINS></IODATA>";
		// call ignore response
		callCmsWithoutParsing(rqlRequest);

		// invalidate this object
		deleteCaches();
		pluginGuid = null;
		name = null;
		url = null;
		cmsClient = null;
	}

	/**
	 * Sets the assigned projects given to this plugin; works absolute. All projects not included in projectGuids will by removed.
	 * <p>
	 * Automatically activate this plug-in if not alredy activated.
	 * 
	 * @see #setIsActive(boolean)
	 */
	public void assignProjects(String[] projectGuids) throws RQLException {
		/* 
		 V9 request
		<IODATA loginguid="[!guid_login!]">
		  <PLUGINS action="reassign">
		    <PLUGIN guid="[!guid_plugin!]">
		      <PROJECT guid="[!guid_project!]"/>
		      ...
		    </PLUGIN>
		    ...
		  </PLUGINS>
		</IODATA> 
		 V9 response 
		<IODATA>
		  <PLUGINS action="reassign">
		    <PLUGIN guid="[!guid_plugin!]">
		      <PROJECT guid="[!guid_project!]">ok</PROJECT>
		      ...
		    </PLUGIN>
		    ...
		  </PLUGINS>
		</IODATA> 
		 */

		// build request
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'><PLUGINS action='reassign'>" + " <PLUGIN guid='"
				+ getPluginGuid() + "'>";
		// add projects
		for (int i = 0; i < projectGuids.length; i++) {
			String guid = projectGuids[i];
			rqlRequest += "<PROJECT guid='" + guid + "'/>";
		}
		// finish
		rqlRequest += "</PLUGIN></PLUGINS></IODATA>";

		// call ignore response
		callCmsWithoutParsing(rqlRequest);

		// activate if not already active
		if (!isActive()) {
			setIsActive(true);
		}
	}

	/**
	 * For debugging only.
	 * 
	 * @throws RQLException
	 */
	public String toString() {
		return this.getClass().getName() + " (" + getName() + ")";
	}
}
