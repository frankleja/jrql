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
public class ExportAndImportExportOfficeContacts2Excel {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws RQLException {

		String logonGuid="1093C0DDCA724FFBAA53C43518039BD1";
		String sessionKey="3DD168EB3F9746D0BA1E28F36298708D";
		String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);
		Project project = client.getProject(sessionKey, projectGuid);

		// prepare csv file
		TabFileWriter out = new TabFileWriter("c:\\data\\rd_data\\office_contacts.tab", "UTF-8");
		// column header
		String[] headers = {"continent", "office", "department", "function", "name", "mail", "phone", "fax", "mobile"};
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
				PageArrayList contactsPages = officePg.getListChildPages("contacts_list");
				for (int k = 0; k < contactsPages.size(); k++) {
					Page contactPg = contactsPages.getPage(k);
					// out
					out.append(continentPg.getHeadline());
					out.append(officePg.getHeadline());
					out.append(contactPg.getStandardFieldTextValue("department"));
					out.append(contactPg.getStandardFieldTextValue("function"));
					out.append(contactPg.getStandardFieldTextValue("name"));
					out.append(contactPg.getStandardFieldUserDefinedValue("e_mail"));
					out.append(contactPg.getStandardFieldUserDefinedValue("phone"));
					out.append(contactPg.getStandardFieldUserDefinedValue("fax"));
					out.append(contactPg.getStandardFieldUserDefinedValue("cell_phone"));
					out.newLine();
					contactPg.freeOccupiedMemory();
				}
				officePg.freeOccupiedMemory();
			}
		}
		out.close();
	}
}
