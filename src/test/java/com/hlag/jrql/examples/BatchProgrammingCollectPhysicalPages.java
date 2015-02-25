package com.hlag.jrql.examples;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.hip.as.PhysicalPagesWalker;
import com.hlcl.rql.util.as.PageAction;
import com.hlcl.rql.util.as.PageListener;
import com.hlcl.rql.util.as.SimulateSmartEditUsagePageAction;

/**
 * @author lejafr
 */
public class BatchProgrammingCollectPhysicalPages {

	/**
	 * @param args
	 * @throws RQLException
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws RQLException, ClassNotFoundException, SQLException {

		String logonGuid = "0B1FBC04A6D94A45A6C5E2AC8915B698";
		String sessionKey = "C26CF959E1434E31B7F9DA89829369B4";
		String projectGuid = "73671509FA5C43ED8FC4171AD0298AD2";

		CmsClient client = new CmsClient(logonGuid);

		Project project = client.getProject(sessionKey, projectGuid);

		String startPageId = "4712";
		
		// create walker
		Class.forName("org.hsqldb.jdbcDriver");
		Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:dafpc", "sa", "");
		PhysicalPagesWalker walker = new PhysicalPagesWalker(connection, "tableName");

//		// init application logger
//		Logger logger = Logger.getLogger("BatchProgrammingCollectPhysicalPages");
//		PropertyConfigurator.configure("log4j.properties");
//
//		// use log4j's debug listener to walker
//		class WalkerPageListener implements PageListener {
//			private Logger logger;
//
//			public WalkerPageListener(Logger logger) {
//				super();
//				this.logger = logger;
//			}
//
//			public void update(Page currentPage) throws RQLException {
//				logger.debug("  " + currentPage.getHeadlineAndId());
//			} // end update method
//		} // end listener class
//		walker.setListener(new WalkerPageListener(logger));

		// use system out listener
		class WalkerPageListener2 implements PageListener {

			public WalkerPageListener2() {
				super();
			}

			public void update(Page currentPage) throws RQLException {
				System.out.println("  " + currentPage.getHeadlineAndId());
			} // end update method
		} // end listener class
		walker.setListener(new WalkerPageListener2());

		// prepare page action called on all physical pages
		PageAction simulateSmartEditUsage = new SimulateSmartEditUsagePageAction();

		// walk through all physical child pages for all given start pages
		Page startPg = project.getPageById(startPageId);
		walker.walk(startPg, simulateSmartEditUsage);
		
	}
}
