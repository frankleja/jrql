package com.hlcl.rql.hip.as;

import java.util.List;

import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.TabFileWriter;

/**
 * @author lejafr
 * 
 *         Diese Klasse schreibt die Änderungshistorie in die gegebene Exceldatei.
 */
public class HipChangeLogStatisticPageAction extends PageAction {

	private TabFileWriter file;
	private int numberOfPages;

	/**
	 * ConvertResponsibilityPageAction constructor
	 */
	public HipChangeLogStatisticPageAction(TabFileWriter file) throws RQLException {
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

		// always possible
		file.append(page.getPageId());
		file.append(page.getHeadline());
		file.append(page.getTemplateName());
		file.append(page.getTemplateFolderName());
		file.append(page.getCreatedOnAsyyyyMMdd());
		file.append(page.getCreatedByUserName());
		file.append(page.getLastChangedOnAsyyyyMMdd());
		file.append(page.getLastChangedByUserName());

		// old responsible values
		ContentPage cp = new ContentPage(page);
		file.append(cp.getUpdatedOnIfAvailable(""));

		// latest change comment
		ChangeLogPage clp = new ChangeLogPage(page);
		List<ChangeLogEntry> changes = clp.getChangesOrEmpty();
		if (changes.isEmpty()) {
			file.newLine();
		} else {
			// some changes available
			// add to existing line
			for (int i = 0; i < 1; i++) {
				ChangeLogEntry cle = changes.get(i);
				file.append(cle.getTimestampAsyyyyMMdd());
				file.append(cle.getUserName());
				file.append(cle.getComment());
				file.newLine();
			}
			// additional lines, if needed
			for (int i = 1; i < changes.size(); i++) {
				ChangeLogEntry cle = changes.get(i);
				// empty column values for page values above
				for (int j = 0; j < 9; j++) {
					file.append("");
				}
				file.append(cle.getTimestampAsyyyyMMdd());
				file.append(cle.getUserName());
				file.append(cle.getComment());
				file.newLine();
			}
		}

		// end
		numberOfPages++;
	}

	/**
	 * Schreibt die Spaltenüberschriften in das Excelfile.
	 */
	public void writeHeader() throws RQLException {
		String[] header = { "pageid", "headline", "template", "templateFolder", "createdOn", "createdBy", "changedOn", "changedBy",
				"updatedOn", "changeLogAt", "changeLogBy", "changeLogComment" };
		file.append(header);
		file.newLine();
	}
}
