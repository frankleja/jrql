package com.hlag.jrql.examples;

/**
 * 
 */

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;
import com.hlcl.rql.util.as.TabFileWriter;

/**
 * @author lejafr
 * 
 */
public class ExportAndImportExportOfficeDetails2Excel {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid="4D522ED5406E49838F2A77F8F4E6AA97";
		String sessionKey="84289ADEBB3C4E0EB3BE5A15DBE6145E";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// prepare csv file
		TabFileWriter out = new TabFileWriter("c:\\data\\rd_data\\office_details.tab", "UTF-8");
		// column header
		String[] headers = {"continent", "office", "org place ID"};
		out.append(headers);
		out.newLine();
		
		// all offices contacts
		Page officesPg = project.getPageById("258");
		PageArrayList continentPages = officesPg.getListChildPages("navi_list", "offices_continent_page");
		for (int i = 0; i < continentPages.size(); i++) {
			Page continentPg = continentPages.getPage(i);
			PageArrayList officePages = continentPg.getListChildPages("navi_list", "office_details_page");
			for (int j = 0; j < officePages.size(); j++) {
				Page officePg = officePages.getPage(j);
				System.out.println(officePg.getHeadline());
				
				// get org place id
				PageArrayList parms = officePg.getListChildPages("portlet_init_parameters_list");
				Page parmPg = parms.findByHeadlineStartsWith("FDO_IOrganisationPlace");
				
				// out
				out.append(continentPg.getHeadline());
				out.append(officePg.getHeadline());
				out.append(parmPg.getStandardFieldTextValue("init_parm_value"));
				out.newLine();
				
				officePg.freeOccupiedMemory();
			}
		}
		out.close();
	}
}
