package com.hlcl.rql.util.as;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.ElementNotFoundException;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.RQLException;

/**
 * Diese Klasse repräsentiert ein conf-File, wie es von RedDot am Ende einer Publizierung angelegt wird. Der FileName hat folgende Struktur:
 * 
 * yyyyMMdd_HHmmss_RDPublishing_<projectName>.conf
 * 
 * Der Inhalt sieht z.B. so aus:
 * 
 * <CONFIGURATION>
 * <SETTINGS>
 * <SERVERNAME> kswfrd02</SERVERNAME>
 * <SERVERIP> 193.17.50.26</SERVERIP>
 * <SERVERIP> 172.17.52.14</SERVERIP>
 * </SETTINGS>
 * <LOGS>
 * <LOG><![CDATA[E:\server\CMS\ASP\log\Publishing\20090714_0621081_RDPublishingJob_hip.hlcl.com_ENG_PROD_media_constraints_XML.xml]]></LOG>
 * <LOG><![CDATA[E:\server\CMS\ASP\log\Publishing\20090714_0621082_RDPublishingJob_hip.hlcl.com_ENG_PROD_pages_HTML.xml]]></LOG>
 * <LOG><![CDATA[E:\server\CMS\ASP\log\Publishing\20090714_0621083_RDPublishingJob_hip.hlcl.com_ENG_PROD_structure_XML.xml]]></LOG>
 * </LOGS></CONFIGURATION>
 */
public class PublishingConfFile {
	
	private static final String _RD_PUBLISHING_ = "_RDPublishing_";
	private static final String CONF = "conf";
	private static final String XML = "xml";

	private static final String LOG_LINE_START = "<LOG><![CDATA[";
	private static final String LOG_LINE_END = "]]></LOG>";

	private static final String DATE_PATTERN = "yyyyMMdd_HHmmss";
	
	private CmsClient cmsClient = null;
	private java.io.File confFile = null;
	private Date date = null;
	private Project project = null;
	
	private Log[] logs = null;
	
	public PublishingConfFile(CmsClient cmsClient, String confFileName) throws Exception {
		super();
		this.cmsClient = cmsClient;
		this.confFile = new java.io.File(confFileName);
		init();		
	}
	
	private void init() throws IOException, ParseException, RQLException {
		String confFileName = confFile.getName();
		int ndx = confFileName.indexOf(_RD_PUBLISHING_);
		String dateString = confFileName.substring(0, ndx);
		date = (new SimpleDateFormat(DATE_PATTERN)).parse(dateString);

		String projectName = confFileName.substring(ndx + _RD_PUBLISHING_.length(), confFileName.length() - CONF.length() - 1);
		project = cmsClient.getProjectByName(projectName);
		
		java.util.List<Log> logList = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(confFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith(LOG_LINE_START) && line.endsWith(LOG_LINE_END)) {
					String logFileName = line.substring(LOG_LINE_START.length(), line.length() - LOG_LINE_END.length());
					if (logList == null) {
						logList = new ArrayList<Log>();
					}
					logList.add(new Log(project, logFileName));
				}
			}
			if (logList != null) {
				this.logs = logList.toArray(new Log[logList.size()]);
			}
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException ioe) {
					// ignore
				}
			}
		}
	}
	
	public Date getDate() {
		return date;
	}
	
	public Project getProject() {
		return project;
	}
	
	public Log[] getLogs() {
		return (logs != null ? logs.clone() : null);
	}
	
	public File getConfFile() {
		return confFile;
	}
	
	public static class Log {

		private java.io.File logFile = null;
		private String projectVariantNameFromFileName = null;
		
		private Project project = null;
		private LanguageVariant languageVariant = null;
		private ProjectVariant projectVariant = null;
		
		private Log(Project project, String logFileName) throws RQLException {
			super();
			this.project = project;
			this.logFile = new java.io.File(logFileName);
			init();
		}
		
		private void init() throws RQLException {
			String logFileName = logFile.getName();
			String projectName = project.getName();
			int projectNdx = logFileName.indexOf(projectName);
			String languageAndProjectVariant = logFileName.substring(projectNdx + projectName.length() + 1, logFileName.length() - XML.length() - 1);
			int ndx = languageAndProjectVariant.indexOf('_');
			languageVariant = project.getLanguageVariantByLanguage(languageAndProjectVariant.substring(0, ndx));
			projectVariantNameFromFileName = languageAndProjectVariant.substring(ndx + 1);
			try {
				projectVariant = project.getProjectVariantByName(projectVariantNameFromFileName);
			}
			catch (ElementNotFoundException enfe) {
				String _DRAFT = "_Draft";
				if (projectVariantNameFromFileName.endsWith(_DRAFT)) {
					// "_Draft" entfernen und die non-Draft-Projekt-Variante verwenden:
					String reducedProjectVariantName = projectVariantNameFromFileName.substring(0, projectVariantNameFromFileName.length() - _DRAFT.length());
					projectVariant = project.getProjectVariantByName(reducedProjectVariantName);
				}
				else {
					throw enfe;
				}
			}
		}
		
		public LanguageVariant getLanguageVariant() {
			return languageVariant;
		}
		
		public ProjectVariant getProjectVariant() {
			return projectVariant;
		}

		public Project getProject() {
			return project;
		}
		
		public java.io.File getLogFile() {
			return logFile;
		}
		
		public String getProjectVariantNameFromFileName() {
			return projectVariantNameFromFileName;
		}
	}
}
