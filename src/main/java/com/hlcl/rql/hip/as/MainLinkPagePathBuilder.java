package com.hlcl.rql.hip.as;

import java.util.Collections;
import java.util.ResourceBundle;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.List;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PageContainer;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.RQLNode;
import com.hlcl.rql.as.StringHelper;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.UnlinkedPageException;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.ScriptParameters;

/**
 * @author lejafr
 * 
 * This class collects all parent page (via main link) until the project's root.
 */
public class MainLinkPagePathBuilder implements PageContainer {

	private ScriptParameters parms;
	private Page page;

	private java.util.List<Template> nodeTemplates; // cache

	final int PAGE_TYPE_BLOCK = 0;
	final int PAGE_TYPE_PHYSICAL = 1;
	final int PAGE_TYPE_NODE = 2;

	/**
	 * Constructor
	 */
	public MainLinkPagePathBuilder(Page page) {
		super();
		this.page = page;
	}

	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten. <p>
	 * Per default werden nur die physical pages zurückgegeben. Die blöcke und nodes werden hinzugefügt, wenn gewünscht.
	 * <p>
	 * Erste Seite der Liste ist eine der gegebenen startPages (falls im Pfad vorhanden) und letzte ist die Seite aus dem Konstruktor.
	 * 
	 * @param startPageIds
	 *            path construction ends at one of the given pages, otherwise path go back to project's start page
	 */
	public PageArrayList getMainLinkPath(String startPageIds, String separator, boolean includeBlocks, boolean includeNodes) throws RQLException {
		return getPath(startPageIds, separator, includeBlocks, includeNodes);
	}
	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten. <p>
	 * Per default werden nur die physical pages zurückgegeben. Die blöcke und nodes werden hinzugefügt, wenn gewünscht.
	 * <p>
	 * Erste Seite der Liste ist die Projetstartseite und letzte ist die Seite aus dem Konstruktor.
	 * 
	 * @param startPageIds
	 *            path construction ends at one of the given pages, otherwise path go back to project's start page
	 */
	public PageArrayList getMainLinkPath(boolean includeBlocks, boolean includeNodes) throws RQLException {
		return getPath(null, ",", includeBlocks, includeNodes);
	}
	/**
	 * Liefert eine Liste mit allen über MainLink verknüpften Seiten. <p>
	 * Per default werden nur die physical pages zurückgegeben. Die blöcke und nodes werden hinzugefügt, wenn gewünscht.
	 * <p>
	 * Erste Seite der Liste ist eine der gegebenen startPages (falls im Pfad vorhanden) und letzte ist die Seite aus dem Konstruktor.
	 * 
	 * @param startPageIds
	 *            path construction ends at one of the given pages, otherwise path go back to project's start page
	 */
	private PageArrayList getPath(String startPageIdsOrNull, String separator, boolean includeBlocks, boolean includeNodes) throws RQLException {

		PageArrayList result = new PageArrayList();
		// collect going backwards until project start page
		Page page = this.getPage();
		boolean loop = true;
		try {
			while (loop) {
				int type = getPageType(page);
				if (type == PAGE_TYPE_PHYSICAL) {
					result.add(page);
				} else if (type == PAGE_TYPE_BLOCK) {
					if (includeBlocks) {
						result.add(page);
					}
				} else if (type == PAGE_TYPE_NODE) {
					if (includeNodes) {
						if (isContentNode(page)) {
							List pseudoList = page.getList(getParameter("nodePagePseudoListTmpltElemName"));
							if (pseudoList != null && pseudoList.hasChildPages()) {
								Page firstChild = pseudoList.getFirstChildPage();
								if (firstChild != null) {
									result.add(firstChild);
								}
							}
						} else {
							result.add(page);
						}
					}
				}
				// always
				page = page.getMainLinkParentPage();
				// check optional end condition by given page
				if (startPageIdsOrNull != null && StringHelper.contains(startPageIdsOrNull, page.getPageId(), true)) {
					// add last page itself 
					result.add(page);
					loop = false;
				}
			}
		} catch (UnlinkedPageException upe) {
			// simply end iteration
		}

		// reverse and return
		Collections.reverse(result);
		
		return result;
	}

	/**
	 * Return the script parameter value from CMS.
	 */
	private String getParameter(String parameterName) throws RQLException {
		return getParms().get(parameterName);
	}

	/**
	 * Return the page type for the encapsulated page.
	 */
	private int getPageType(Page page) throws RQLException {
		Template template = page.getTemplate();
		if (getNodeTemplates().contains(template)) {
			return PAGE_TYPE_NODE;
		}
		if (template.contains(getParameter("isPhysicalPageTmpltElemName"))) {
			return PAGE_TYPE_PHYSICAL;
		} else {
			return PAGE_TYPE_BLOCK;
		}
	}

	private boolean isContentNode(Page page) throws RQLException {
		if (getPageType(page) == PAGE_TYPE_NODE) {
			return (page.getTemplate().contains(getParameter("nodePagePseudoListTmpltElemName")));
		}
		return false;
	}
	
	/**
	 * Return true, if the encapsulated page is a block.
	 */
	public boolean isBlock() throws RQLException {
		return getPageType(getPage()) == PAGE_TYPE_BLOCK;
	}

	/**
	 * Return true, if the encapsulated page is a physical page.
	 */
	public boolean isPhysicalPage() throws RQLException {
		return getPageType(getPage()) == PAGE_TYPE_PHYSICAL;
	}

	/**
	 * Return true, if the encapsulated page is a node or content node.
	 */
	public boolean isNode() throws RQLException {
		return getPageType(getPage()) == PAGE_TYPE_NODE;
	}

	/**
	 * Returns the page id with all parameters. Would be read in with ScriptParameters.
	 */
	private String getParametersPageId() {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.hip.as.hip_parmPageIds");
		return b.getString(this.getClass().getName());
	}

	/**
	 * Returns the templates used for nodes.
	 * 
	 * @throws RQLException
	 */
	private java.util.List<Template> getNodeTemplates() throws RQLException {
		if (nodeTemplates == null) {
			nodeTemplates = getParms().getTemplatesByGuid("nodeTmpltGuids", ",");
		}
		return nodeTemplates;
	}

	/**
	 * Return the script parameters always from the cached parameters in project to speed up.
	 */
	private ScriptParameters getParms() throws RQLException {
		if (parms == null) {
			parms = getProject().getParameters(getParametersPageId(), this.getClass().getName());
		}
		return parms;
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine geparste Antwort zurueck.
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		return getCmsClient().callCms(rqlRequest);
	}

	/**
	 * Senden eine Anfrage an das CMS und liefert eine ungeparste Antwort zurueck. Erforderlich für die Ermittlung des Werts eines Textelements.
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		return getCmsClient().callCmsWithoutParsing(rqlRequest);
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {
		return getPage().getLogonGuid();
	}

	/**
	 * Liefert die Seite, zu der diese Bemerkung gehört.
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * Liefert die RedDot GUID der Seite.
	 */
	public String getPageGuid() {
		return getPage().getPageGuid();
	}

	/**
	 * Liefert die RedDot GUID des Projekts.
	 */
	public String getProjectGuid() throws RQLException {
		return getPage().getProjectGuid();
	}

	/**
	 * Liefert den RedDot Session key.
	 */
	public String getSessionKey() {
		return getPage().getSessionKey();
	}

	public Project getProject() {
		return getPage().getProject();
	}

	public CmsClient getCmsClient() {
		return getProject().getCmsClient();
	}
}
