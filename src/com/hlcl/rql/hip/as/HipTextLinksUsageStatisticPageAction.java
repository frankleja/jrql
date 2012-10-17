package com.hlcl.rql.hip.as;

import java.util.List;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.TextElement;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.TabFileWriter;

/**
 * @author lejafr
 * 
 *         This class collects for a physical page and all child pages all text elements and write out the linked pages and external
 *         links.
 */
public class HipTextLinksUsageStatisticPageAction extends PageAction {

	private TabFileWriter file;
	private int numberOfPages;

	/**
	 * constructor
	 */
	public HipTextLinksUsageStatisticPageAction(TabFileWriter file) throws RQLException {
		super();

		this.file = file;

		numberOfPages = 0;
	}

	/**
	 * Liefert die Anzahl der Seiten, für die diese PageAction aufgerufen wurde.
	 */
	public int getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * Für jede übergebene Seite werden alle notwendigen Werte in eine Zeile des files geschrieben.
	 * 
	 * @see com.hlcl.rql.util.as.PageAction#invoke(com.hlcl.rql.as.Page)
	 */
	@Override
	public void invoke(Page page) throws RQLException {

		// start with header
		if (numberOfPages == 0) {
			writeHeader();
		}

		// physical pages text links for all text elements
		logPageLinks(true, page);

		// child pages text links for all text elements
		PhysicalPage pp = new PhysicalPage(page);
		PageArrayList children = pp.getAllNonPhysicalChildPages();
		for (int k = 0; k < children.size(); k++) {
			Page child = children.getPage(k);
			logPageLinks(false, child);
		}

		// end
		numberOfPages++;
	}

	/**
	 * For the given page write out for all filled HTML text elements all linked pages and all external URLs found.
	 */
	private void logPageLinks(boolean parent, Page page) throws RQLException {
		List<TextElement> elements = page.getFilledHtmlTextElements();
		if (parent || elements.size() > 0) {
			// page header data
			file.append(parent ? "parent" : "child");
			file.append(page.getPageId());
			file.append(page.getHeadline());
			file.append(page.getTemplateName());
			file.append(page.getTemplateFolderName());
			file.newLine();
		}
		for (TextElement te : elements) {
			// check links first
			PageArrayList linkedPages = te.getAllLinkedPages(true);
			List<String> externalLinks = te.getAllExternalLinks();

			// out element name only if children
			if (linkedPages.size() + externalLinks.size() > 0) {
				// child page internal links
				for (int i = 0; i < linkedPages.size(); i++) {
					// empty page attributes
					file.indent(5);
					file.append(te.getName());
					// write out linked pages
					Page lp = linkedPages.getPage(i);
					// check for invalid pages
					String pageInfo = "";
					if (lp.isValid()) {
						pageInfo = lp.getInfoText();
					} else {
						pageInfo = "invalid page GUID " + lp.getPageGuid();
					}
					file.append(pageInfo);
					file.newLine();
				}

				// child page external links
				for (String url : externalLinks) {
					// empty page attributes
					file.indent(5);
					file.append(te.getName());
					// write out url
					file.append(url);
					file.newLine();
				}
			}
		}
	}

	/**
	 * Schreibt die Spaltenüberschriften in das Excelfile.
	 */
	public void writeHeader() throws RQLException {
		String[] header = { "kind", "pageid", "headline", "template", "templateFolder", "textElemName", "child/url" };
		file.append(header);
		file.newLine();
	}
}
