package com.hlcl.rql.hip.as;

import com.hlcl.rql.as.MissingMainLinkException;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.TabFileWriter;

/**
 * @author lejafr
 * 
 * Diese Klasse schreibt alle für die HIP Statistik notwendigen Werte in die gegebene Exceldatei.
 */
public class HipStatisticsPageAction extends PageAction {

	private TabFileWriter file;
	private String publishedFilenameProjectVariantGuid;
	private int numberOfPages;

	/**
	 * ConvertResponsibilityPageAction constructor
	 */
	public HipStatisticsPageAction(TabFileWriter file, String publishedFilenameProjectVariantGuid) throws RQLException {
		super();

		this.file = file;
		this.publishedFilenameProjectVariantGuid = publishedFilenameProjectVariantGuid;
		
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

        // published filename
        try {
            file.append(page.getPublishedFilename(publishedFilenameProjectVariantGuid));
        } catch (MissingMainLinkException e) {
            file.append("not available, missing main link");
        } catch (RQLException e) {
            file.append("not available, rql exception");
        }
        
        // old responsible values
        ContentPage cp = new ContentPage(page);
		file.append(cp.getResponsibleUserNameIfAvailable(""));
		file.append(cp.getResponsibleUserIdIfAvailable(""));
		file.append(cp.getUpdatedOnIfAvailable(""));
		file.append(cp.getAuthorizationPackageNameWithoutPrefix());
		
		// latest change comment
		ChangeLogPage clp = new ChangeLogPage(page);
		file.append(clp.getLatestChangeTimestampAsyyyyMMdd(""));
		file.append(clp.getLatestChangeUserName(""));
		file.append(clp.getLatestChangeComment("")); 
		
		// new responsibility values and check results
		String[] respValues = {"", "", "", "", "", ""};
		if (cp.hasResponsibleList()) {
			respValues[0] = cp.getRequesterUserId("");
			respValues[1] = cp.getResponsibleIdType();
			respValues[2] = cp.getResponsibleDepartmentNumberAndNameIfAvailable(" ", "not linked");
			respValues[3] = cp.isResponsibleDepartmentValid() ? "true" : "false";
			respValues[4] = cp.checkResponsibleId();
			respValues[5] = cp.checkResponsibleName();
		} 
		file.append(respValues);
		
		// end 
		file.newLine();
		numberOfPages++;
	}
	/**
	 * Schreibt die Spaltenüberschriften in das Excelfile.
	 */
	public void writeHeader() throws RQLException {
		String[] header = { "pageid", "headline", "template", "templateFolder", "createdOn", "createdBy", "changedOn", "changedBy", "filename", "responsibleName",
				"responsibleId", "updatedOn", "workarea", "changeCommentDate", "changeCommentUser", "changeComment", 
				"requester", "responsibleIdType", "responsibleDepartment", "responsibleDepartmentValid", "checkResponsibleId", "checkResponsibleName"};
		file.append(header);
		file.newLine();
	}
}
