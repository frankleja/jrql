package com.hlcl.rql.as;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hlcl.rql.util.as.RqlProfiler;

/**
 * Die Klasse beschreibt einen Client zum RedDot Content Management Server.
 * 
 * @author LEJAFR
 */
public class CmsClient {
	private static final String LDAP_SERVER_NAME_KEY = "LdapServerName";
	private static final String LDAP_SERVER_PORT_KEY = "LdapServerPort";
	private static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout"; // BURMEBJ004A
	private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout"; // BURMEBJ004A

	// mail constants
	private static final String STATISTIC_MAIL_FROM_ADDRESS = "lejafr@hlag.com";

	/**
	 * BURMEBJ002A Diese Methode verschickt eine E-Mail mit Attachements an
	 * mehrere Empfï¿½nger. Ein groï¿½er Teil dieser Methode wurde von der
	 * ursprï¿½nglichen Methode sendMail(String, String[], String, String,
	 * File[]) ï¿½bernommen. Lediglich der letzte Parameter fileNames wurde
	 * ergï¿½nzt.
	 * 
	 * Geï¿½ndert durch: Name Datum: MM-JJ Beschreibung: kurze Erlï¿½uterung der
	 * ï¿½nderung
	 * 
	 * @param from
	 *            ein String mit der E-Mail-Adresse des Absenders
	 * @param to
	 *            ein Array von Strings mit den E-Mail-Adressen der Empfï¿½nger
	 * @param subject
	 *            ein String mit dem Subject der E-Mail
	 * @param msgText
	 *            ein String mit dem Text der E-Mail
	 * @param attachements
	 *            ein Array von Files, die attached werden sollen
	 * @param fileNames
	 *            Ein Array mit den fileNames, die fï¿½r die Attachments in der
	 *            Mail vergeben werden sollen. Dabei wird folgendermaï¿½en
	 *            vorgegangen: Fï¿½r das i. Attachement wird geprï¿½ft, ob der
	 *            i. Eintrag des fileNames-Array von null verschieden ist. Wenn
	 *            ja, wird dieser fileName verwendet, ansonsten der
	 *            ursprï¿½ngliche File-Name des Attachements. (BURMEBJ002A)
	 * @exception javax.mail.MessagingException
	 *                , wenn beim Versenden der E-Mail ein Fehler auftritt
	 */
	static public void sendMail(String from, String[] to, String subject,
			String msgText, java.io.File[] attachements, String[] fileNames)
			throws MessagingException {
		//
		// Der SMTP-Host wird aus einer properties-Datei gelesen:
		//
		String smtpHost = "";
		PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle
				.getBundle("com.hlcl.rql.as.SMTPHost");
		smtpHost = bundle.getString("smtpHost").trim();
		//
		// Erzeugen eines Properties-Objektes mit dem SMTP-Host:
		//
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);

		// BURMEBJ004A begin
		setOptionalProperty(MAIL_SMTP_TIMEOUT, bundle, props);
		setOptionalProperty(MAIL_SMTP_CONNECTIONTIMEOUT, bundle, props);
		// BURMEBJ004A end

		//
		// Der Debug-Modus wird eingestellt.
		//
		// BURMEBJ005M begin
		// BURMEBJ005M end
		//
		// Erzeugen einer Default-Session:
		//
		Session session = Session.getDefaultInstance(props, null);
		//
		// Erzeugen einer MimeMessage:
		//
		Message msg = new MimeMessage(session);
		//
		// Erzeugen der Absender-Adresse der Message:
		//
		InternetAddress fromAddress = new InternetAddress(from);
		msg.setFrom(fromAddress);
		//
		// Erzeugen der Empfï¿½nger-Adresse der Message:
		//
		int max = to.length;
		InternetAddress[] toAddress = new InternetAddress[max];
		for (int i = 0; i < max; i++) {
			toAddress[i] = new InternetAddress(to[i]);
		}
		msg.setRecipients(Message.RecipientType.TO, toAddress);
		//
		// Setzen des Subjects der Message:
		//
		msg.setSubject(subject);

		// BURMEBJ003A begin
		if (attachements == null) {
			//
			// Setzen des Message-Contents:
			//
			msg.setContent(msgText, "text/plain; charset=ISO-8859-1");
		}
		// BURMEBJ003A end
		else {
			// BURMEBJ003 moved begin
			//
			// Erzeugen eines MimeMultipart:
			//
			MimeMultipart mp = new MimeMultipart();
			//
			// Anhängen eines MimeBodyParts für den einleitenden Text:
			//
			MimeBodyPart text = new MimeBodyPart();
			text.setDisposition(Part.INLINE);
			text.setContent(msgText, "text/plain; charset=ISO-8859-1");
			mp.addBodyPart(text);
			//
			// Anhängen der Attachements:
			//
			// BURMEBJ003 moved end
			// @BURMEBJ002A begin
			int numberOfFileNames = 0;
			if (fileNames != null) {
				numberOfFileNames = fileNames.length;
			}
			// @BURMEBJ002A end

			for (int i = 0; i < attachements.length; i++) {
				MimeBodyPart file_part = new MimeBodyPart();
				java.io.File file = attachements[i];
				FileDataSource fds = new FileDataSource(file);
				DataHandler dh = new DataHandler(fds);
				// @BURMEBJ002M begin
				String fileName = file.getName();
				if (i < numberOfFileNames && fileNames[i] != null) {
					fileName = fileNames[i];
				}
				file_part.setFileName(fileName);
				// @BURMEBJ002M end
				file_part.setDisposition(Part.ATTACHMENT);
				file_part.setDescription("Attached file: " + file.getName());
				file_part.setDataHandler(dh);
				mp.addBodyPart(file_part);
			}
			msg.setContent(mp);
		}
		//
		// Abschicken der Message:
		//
		Transport.send(msg);
	}

	// BURMEBJ004A
	static private void setOptionalProperty(String key, ResourceBundle bundle,
			Properties props) {
		try {
			String value = bundle.getString(key);
			if (value != null) {
				value = value.trim();
				if (value.length() > 0) {
					props.put(key, value);
				}
			}
		} catch (MissingResourceException mre) {
			// ignorieren, property ist optional, muß also nicht konfiguriert
			// werden.
		}
	}

	private RQLNodeList allLocalesNodeListCache;
	private RQLNodeList allPluginsNodeListCache;
	private RQLNodeList allUserInterfaceLanguagesNodeListCache;
	private RQLNodeList allUsersNodeListCache;

	// actual connected user
	private User connectedUser;

	// only one project could be selected at a time
	private Project currentProject;
	// remember open ldap context
	private DirContext ldapContext;

	private String logonGuid;

	// caches
	private RQLNodeList projectsNodeListCache;

	// URL of remote RQL bridge ASP file
	// determine to which CMS server it would be connected, from constructor or
	// default from rql_fw.properties
	// like http://reddot.hlcl.com/cms/hlclRemoteRQL.asp
	private URL cmsServerConnectionUrl;

	
	/**
	 * RQL Request/response-Logging is enabled.
	 */
	public final boolean debugRql;

	
	/**
	 * Helps with the XML work.
	 */
	private final DocumentBuilderFactory dbf;
	
	
	/**
	 * If set, wer are profiling individual calls.
	 */
	protected RqlProfiler rqlProfiler = null;
	
	
	/**
	 * Common constructor
	 */
	private CmsClient() {
		this.debugRql = Boolean.valueOf(System.getProperty("debugRql"));
		this.dbf = DocumentBuilderFactory.newInstance();
		this.dbf.setIgnoringElementContentWhitespace(true);
	}
	
	
	/**
	 * Erzeugt einen CmsServer, indem ein neuer User am CMS angemeldet wird.
	 * 
	 * @param passwordAuthentication
	 * @throws UserAlreadyLoggedInException
	 */
	public CmsClient(PasswordAuthentication passwordAuthentication)
			throws RQLException {

		this();
		cmsServerConnectionUrl = null;
		login(passwordAuthentication);
	}

	/**
	 * Erzeugt einen CmsServer, indem ein neuer User am CMS angemeldet wird.
	 * 
	 * @param passwordAuthentication
	 * @throws UserAlreadyLoggedInException
	 */
	public CmsClient(PasswordAuthentication passwordAuthentication,
			URL cmsServerConnectionUrl) throws RQLException {

		this();
		this.cmsServerConnectionUrl = cmsServerConnectionUrl;

		login(passwordAuthentication);
	}

	/**
	 * Erzeugt einen CmsClient fï¿½r die gegebene logonGuid.
	 * 
	 * @param logonGuid
	 *            Anmelde-GUID des angemeldeten Nutzers
	 */
	public CmsClient(String logonGuid) throws RQLException {
		this();

		this.logonGuid = logonGuid;
		cmsServerConnectionUrl = null;
		connectedUser = null;
	}

	/**
	 * Erzeugt einen CmsClient fï¿½r die gegebene logonGuid. Gleichzeitig wird
	 * der angemeldete Benutzer initialisiert.
	 * 
	 * @param logonGuid
	 *            Anmelde-GUID des angemeldeten Nutzers
	 * @param connectedUserGuid
	 *            GUID des angemeldeten Nutzers
	 */
	public CmsClient(String logonGuid, String connectedUserGuid)
			throws RQLException {
		this();

		this.logonGuid = logonGuid;
		cmsServerConnectionUrl = null;
		connectedUser = new User(this, connectedUserGuid);
	}

	/**
	 * Erzeugt einen CmsClient fï¿½r die gegebene logonGuid und die gegebene URL
	 * zur CMS Server hlclRemoteRQL.asp.
	 * 
	 * @param logonGuid
	 *            Anmelde-GUID des angemeldeten Nutzers
	 */
	public CmsClient(String logonGuid, URL cmsServerConnectionUrl)
			throws RQLException {
		this();

		this.logonGuid = logonGuid;
		// remember the CMS server to bind to
		this.cmsServerConnectionUrl = cmsServerConnectionUrl;
		connectedUser = null;
	}

	/**
	 * Diese Methode liefert den ï¿½bergebenen Node mit all seinen Nachkommen in
	 * einen RQLNode mit den entsprechenden Nachkommen um. Der RQL-Node, der dem
	 * ï¿½bergebenen Node entspricht, wird zurï¿½ckgegeben.
	 */
	private RQLNode buildTree(Node root) {
		if (root == null)
			return null;

		RQLNode rqlRoot = null;

		if (root.getNodeType() == Node.ELEMENT_NODE) {
			rqlRoot = new RQLTagNode(root.getNodeName());

			NamedNodeMap nnm = root.getAttributes();
			for (int i = 0; i < (nnm != null ? nnm.getLength() : 0); i++) {
				Node attr = nnm.item(i);
				if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
					((RQLTagNode) rqlRoot).addAttribute(attr.getNodeName(),
							attr.getNodeValue());
				}
			}

			NodeList children = root.getChildNodes();
			for (int i = 0; i < (children != null ? children.getLength() : 0); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE
						|| child.getNodeType() == Node.TEXT_NODE) {
					RQLNode rqlChild = buildTree(child);
					((RQLTagNode) rqlRoot).addChild(rqlChild);
				}
			}
		} else if (root.getNodeType() == Node.TEXT_NODE) {
			rqlRoot = new RQLTextNode(root.getNodeValue());
		}

		return rqlRoot;
	}

	/**
	 * Erzeugt ein Userobjekt aus dem gegebenen Usernode.
	 * 
	 * @param node
	 *            node des tags <user ...
	 */
	User buildUser(RQLNode node) {

		User user = null;
		String loginGuidOrNull = node.getAttribute("loginguid");
		if (loginGuidOrNull == null) {
			user = new User(this, node.getAttribute("name"),
					node.getAttribute("guid"), node.getAttribute("id"),
					node.getAttribute("fullname"), node.getAttribute("email"));
		} else {
			user = new User(this, node.getAttribute("name"),
					node.getAttribute("guid"), node.getAttribute("id"),
					node.getAttribute("fullname"), node.getAttribute("email"),
					loginGuidOrNull);
		}
		return user;
	}

	/**
	 * Wandelt den RQLNode fï¿½r eine user interface language in ein Object.
	 */
	private UserInterfaceLanguage buildUserInterfaceLanguage(
			RQLNode languageNode) {
		return new UserInterfaceLanguage(this,
				languageNode.getAttribute("rfclanguageid"),
				languageNode.getAttribute("id"),
				languageNode.getAttribute("country"),
				languageNode.getAttribute("language"));
	}

	/**
	 * Sendet einen RQL request an das CMS und gibt die geparste Antwort
	 * zurï¿½ck.
	 * <p>
	 * Leitet den Aufruf an den RQLHelper weiter.
	 * 
	 * @param rqlRequest
	 *            String
	 * @return RQLNode
	 * @throws RQLException
	 */
	public RQLNode callCms(String rqlRequest) throws RQLException {
		URL url = getCmsServerConnectionUrl();

		if (url.toString().endsWith("/RqlWebService.svc")) {
			return callCms_SOAP(url, rqlRequest);
		} else {
			return callCms_ASP(url, rqlRequest);
		}
	}


	private RQLNode callCms_SOAP(URL url, String rqlQuery) throws RQLException {
		try {
			String s = callCmsWithoutParsing_SOAP(url, rqlQuery);
			DocumentBuilder db = dbf.newDocumentBuilder();
			return buildTree(db.parse(new InputSource(new StringReader(s))).getDocumentElement());
		} catch (SAXException e) {
			throw new RQLException(e.toString(), e);
		} catch (IOException e) {
			throw new RQLException(e.toString(), e);
		} catch (ParserConfigurationException e) {
			throw new RQLException(e.toString(), e);
		}
	}
	

	/**
	 * Diese Methode fï¿½hr eine RQL-Anfrage mit der ï¿½bergebenen rqlQuery an
	 * das CMS aus. Das Ergebnis wird in Form eines RQLNode
	 * ("<IODATA>...</IODATA>") zurï¿½ckgegeben. Wenn es zu Problemen kommt,
	 * wird eine RQLException geworfen.
	 * 
	 * @param rqlQuery
	 *            String: s.o.
	 * @return RQLNode: s.o.
	 * @throws RQLException
	 *             : s.o.
	 */
	private RQLNode callCms_ASP(URL url, String rqlQuery) throws RQLException {
		InputStream is = null;
		final RQLNode root;

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			long before = 0;
			long after = 0;
			
			
            if (debugRql) {
                System.out.println(">------ RQL request ----->\n" + StringHelper.prettyPrintXml(rqlQuery, 2));
                before = System.currentTimeMillis();
            }

			is = getCMSResultAsStream_ASP(url, rqlQuery); // the actual HTTP call

			final InputSource source;
            if (debugRql) {
            	after = System.currentTimeMillis();
            	long delta = after - before;
            	
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) > -1 ) {
                    baos.write(buffer, 0, len);
                }
                baos.close();

                InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                source = new InputSource(is1);

                String rqlResponse = new String(baos.toByteArray(), getResponseReaderEncoding());
                System.out.println("<------ RQL response ("+delta+" ms)------<\n" + StringHelper.prettyPrintXml(rqlResponse, 2) );
            } else {
                source = new InputSource(is);
            }
            // how it's related to the encoding within xml document?
            source.setEncoding(getResponseReaderEncoding());
            root = buildTree(db.parse(source).getDocumentElement());

		} catch (ParserConfigurationException pce) {
			throw new RQLException("RQLHelper.callCMS()", pce);
		} catch (SAXException se) {
			throw new RQLException("RQLHelper.callCMS()", se);
		} catch (IOException ioe) {
			throw new RQLException("RQLHelper.callCMS()", ioe);
        } finally {
			// BURMEBJ001M begin
			// Das bisherige Schlieï¿½en des OutputStreamWriter wurde
			// natï¿½rlich ebenfalls in die Methode getCMSResultAsStream()
			// verlagert. Hier erfolgt nun zusï¿½tzlich noch ein close()
			// des InputStreams:
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
				}
			}
			// BURMEBJ001M end
		}

		if ("ERROR".equals(root.getName())) {
			throw new RQLException(root.getText(), null);
		}

		return root;
	}

	/**
	 * Sendet einen RQL request an das CMS und gibt die ungeparste Antwort
	 * zurï¿½ck. Leitet den Aufruf an den RQLHelper weiter.
	 * 
	 * @param rqlRequest
	 *            String
	 * @return XML String
	 * @throws RQLException
	 * @see RQLNode
	 */
	public String callCmsWithoutParsing(String rqlRequest) throws RQLException {
		URL url = getCmsServerConnectionUrl();

		if (url.toString().endsWith("/RqlWebService.svc")) {
			return callCmsWithoutParsing_SOAP(url, rqlRequest);
		} else {
			return callCmsWithoutParsingPrimitive_ASP(url, rqlRequest);
		}
	}


	/**
	 * Diese Methode fï¿½hr eine RQL-Anfrage mit der ï¿½bergebenen rqlQuery an
	 * das CMS aus. Das Ergebnis wird als String zurï¿½ckgegeben. Es wird davon
	 * ausgegangen, daï¿½ die 1. Zeile von der Form <?xml ... ?> ist. In diesem
	 * Fall wird diese erste Zeile weggelassen. Andernfalls kommt es zu einer
	 * Exception.
	 * 
	 * @param rqlQuery
	 *            String: s.o.
	 * @return String: s.o.
	 * @throws RQLException
	 *             : s.o.
	 */
	private String callCmsWithoutParsingPrimitive_ASP(URL url, String rqlQuery)
			throws RQLException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(
					new InputStreamReader(getCMSResultAsStream_ASP(url, rqlQuery),
							getResponseReaderEncoding()));

			StringBuilder firstLine = new StringBuilder(128); // fï¿½r die erste
															// Zeile der Antwort
			StringBuilder content = new StringBuilder(1024); // fï¿½r die restlichen
														// Zeilen der Antwort
			byte state = 0; // 0: erste Zeile, 1: Zeilenumbrï¿½che am Ende der
							// ersten Zeile, 2: weitere Zeilen
			int next = -1;

			while ((next = br.read()) != -1) {
				if (state == 0) {
					if (next == '\n' || next == '\r')
						state = 1;
					else
						firstLine.append((char) next);
				} else if (state == 1) {
					if (next != '\n' && next != '\r')
						state = 2;
				}

				if (state == 2) {
					content.append((char) next);
				}
			}

			String firstLineString = firstLine.toString();
			if (!(firstLineString.startsWith("<?xml") && firstLineString
					.endsWith("?>")))
				throw new RQLException(
						"RQLHelper.callCMSWithoutParsing(): first line not valid: "
								+ firstLineString);

			return content.toString();
		} catch (IOException ioe) {
			throw new RQLException("RQLHelper.callCMSWithoutParsing()", ioe);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	/**
	 * Wechselt das aktuelle Projekt.
	 * 
	 * @param projectGuid
	 *            Guid des neuen Projektes
	 * @return Project
	 */
	public Project changeCurrentProjectByGuid(String projectGuid)
			throws RQLException {

		return getProjectByGuid(projectGuid);
	}

	/**
	 * Wechselt das aktuelle Projekt.
	 * 
	 * @param projectName
	 *            Name des neuen Projektes, z.b. hip.hlcl.com
	 * @return Project
	 */
	public Project changeCurrentProjectByName(String projectName)
			throws RQLException {

		return getProjectByName(projectName);
	}

	/**
	 * Schlieï¿½t den angeforderten LDAP directory service context wieder.
	 * 
	 * @see #openLdapContext()
	 */
	public void closeLdapContext() throws RQLException {
		try {
			ldapContext.close();
		} catch (NamingException ex) {
			throw new RQLException("Could not close LDAP context", ex);
		}
	}

	/**
	 * Liefert alle Mailadressen aller User zurï¿½ck, die fï¿½r die gegebenen
	 * Projekte zugelassen sind.
	 * <p>
	 * Diese Abfrage kann nur ein Administrator ausfï¿½hren. Wiederholungen
	 * werden durch ein Set vermieden. User ohne Mailadresse werden ausgelassen.
	 * 
	 * @param sessionKey
	 *            aktueller Session Key
	 * @param projectGuids
	 *            Strings mit den GUIDs der Projekte
	 */
	public java.util.List<String> collectUserMailAddressesForProjects(
			String sessionKey, String[] projectGuids) throws RQLException {

		// remember current project
		Project oldProject = currentProject;

		// for all given project guids do
		SortedSet<String> addresses = new TreeSet<String>();
		Iterator<User> iter = null;
		for (int i = 0; i < projectGuids.length; i++) {
			String projectGuid = projectGuids[i];
			Project project = getProject(sessionKey, projectGuid);
			java.util.List<User> projectUsers = project.getAllUsers();
			iter = projectUsers.iterator();
			while (iter.hasNext()) {
				User user = (User) iter.next();
				String address = user.getEmailAddress().trim();
				if (address.length() != 0) {
					addresses.add(address.toLowerCase());
				}
			}
		}

		// restore project
		currentProject = oldProject;

		return new ArrayList<String>(addresses);
	}

	/**
	 * Meldet diesen Client vom CMS ab. Danach kann dieses Object nicht mehr
	 * benutzt werden.
	 */
	public void disconnect() throws RQLException {

		logout(getLogonGuid());

		// invalidate this object
		logonGuid = null;
		projectsNodeListCache = null;
		allUsersNodeListCache = null;
		connectedUser = null;
	}

	/**
	 * Sperrt alle Projekte dieses Servers und meldet alle aktiven Benutzer
	 * (auï¿½er dem, der das Script gestartet hat) ab.
	 */
	public void enterOutage(String outageMessage, boolean isTest)
			throws RQLException {

		java.util.List<Project> projects = isTest ? getTestProjects()
				: getAllProjects();
		for (int i = 0; i < projects.size(); i++) {
			Project project = (Project) projects.get(i);
			project.lock(outageMessage);
			project.logoutActiveUsers();
		}
	}

	/**
	 * Entsperrt alle Projekt dieses CMSServers.
	 */
	public void exitOutage(boolean isTest) throws RQLException {

		java.util.List<Project> projects = isTest ? getTestProjects()
				: getAllProjects();
		for (int i = 0; i < projects.size(); i++) {
			Project project = (Project) projects.get(i);
			project.unlock();
		}
	}

	/**
	 * Lifert den RQLNode fï¿½r das gegebene Projekt oder null zurï¿½ck. Null
	 * signalisiert, dass dieses Projekt nicht fï¿½r den User zugelassen ist,
	 * oder die GUID falsch ist.
	 * 
	 * @param projectGuid
	 *            GUID des Projectes, an den sich der User anmelden will.
	 * @return <code>RQLNode</code> or null
	 */
	private RQLNode findUserProjectNodeByGuid(String projectGuid)
			throws RQLException {

		RQLNodeList nodes = getProjectsNodeList(getConnectedUser());

		// find
		RQLNode node = null;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);

			if (node.getAttribute("guid").equals(projectGuid)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Liefert den RQLNode fï¿½r das gegebene Projekt oder null zurï¿½ck. Null
	 * signalisiert, dass dieses Projekt nicht fï¿½r den User zugelassen ist,
	 * oder die GUID falsch ist.
	 * 
	 * @param projectName
	 *            GUID des Projectes, an den sich der User anmelden will.
	 * @return <code>RQLNode</code> or null
	 */
	private RQLNode findUserProjectNodeByName(String projectName)
			throws RQLException {

		RQLNodeList nodes = getProjectsNodeList(getConnectedUser());

		// find
		RQLNode node = null;
		for (int i = 0; i < nodes.size(); i++) {
			node = nodes.get(i);

			if (node.getAttribute("name").equals(projectName)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Liefert alle gerade am CMS angemeldeten Benutzer.
	 */
	public java.util.List<User> getAllActiveUsers() throws RQLException {

		java.util.List<User> activeUsers = new ArrayList<User>();
		java.util.List<Project> projects = getAllProjects();
		for (int i = 0; i < projects.size(); i++) {
			Project p = (Project) projects.get(i);
			activeUsers.addAll(p.getActiveUsers());
		}
		return activeUsers;
	}

	/**
	 * Liefert alle auf diesem RD Server eingerichteten Plugins (aktive und
	 * inaktive) unabhï¿½ngig von der Projektzuweisung.
	 */
	public java.util.List<Plugin> getAllPlugins() throws RQLException {

		RQLNodeList nodes = getPluginsNodeList();
		java.util.List<Plugin> plugins = new ArrayList<Plugin>();
		// check if any plugins at all
		if (nodes == null) {
			return plugins;
		}

		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode node = nodes.get(i);
				plugins.add(buildPlugin(node));
			}
		}

		return plugins;
	}

	/**
	 * Liefert alle Projekt auf diesem CMS Server, unabhï¿½ngig von den Rechten
	 * des angemeldeten Users.
	 */
	public java.util.List<Project> getAllProjects() throws RQLException {

		return wrapProjectNodes(getProjectsNodeList());
	}

	/**
	 * Liefert alle User zurï¿½ck, die auf diesem CMS Server konfiguriert sind.
	 * Diese Abfrage kann nur ein Administrator ausfï¿½hren.
	 */
	public java.util.List<User> getAllUsers() throws RQLException {

		return wrapUserNodes(getAllUsersNodeList());
	}

	/**
	 * Liefert die RQLNodeList aller User zurï¿½ck, die auf diesem CMS Server
	 * konfiguriert sind. Diese Abfrage kann nur ein Administrator ausfï¿½hren.
	 */
	private RQLNodeList getAllUsersNodeList() throws RQLException {
		/*
		 * V5 request <IODATA loginguid="[!guid_login!]"> <ADMINISTRATION>
		 * <USERS action="list"/> </ADMINISTRATION> </IODATA> V5 response
		 * <IODATA> <USERS> <USER guid="CF4776E0819043C393367B63D7512A36"
		 * id="247" name="abeelan" fullname="Andy Van den Abeele" flags1="0"
		 * flags2="0" email="abeelan@hlcl.com" maxlevel="4"
		 * dialoglanguageid="ENG" loginguid="8361A28AFE4A42E4989766F46F3FF136"
		 * logindate="38048.6737731481"/> ... </USERS> </IODATA>
		 */

		// call CMS
		if (allUsersNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
					+ "  <ADMINISTRATION>" + "   <USERS action='list'/>"
					+ "  </ADMINISTRATION>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			allUsersNodeListCache = rqlResponse.getNodes("USER");
		}

		return allUsersNodeListCache;
	}


	/**
	 * Diese Methode schickt die Ã¼bergebene rqlQuery an das CMS und liefert das
	 * Ergebnis als InputStream zurï¿½ck. Die aufrufende Methode muï¿½ sich
	 * darum kï¿½mmern, den Stream wieder zu schlieï¿½en.
	 * 
	 * @param rqlQuery
	 *            : s.o.
	 * @return InputStream: s.o.
	 * @throws RQLException
	 */
	private InputStream getCMSResultAsStream_ASP(URL url, String rqlQuery)
			throws RQLException {
		OutputStreamWriter osr = null;

		try {
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			osr = new OutputStreamWriter(conn.getOutputStream(),
					getRequestWriterEncoding());
			osr.write(rqlQuery);
			osr.flush();

			return conn.getInputStream();
		} catch (IOException ioe) {
			throw new RQLException(ioe.toString(), ioe);
		} finally {
			if (osr != null) {
				try {
					osr.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
	
	
	/**
	 * Poor man's SOAP call.
	 * 
	 * @param rqlQuery
	 * @return
	 * @throws RQLException
	 */
	private String callCmsWithoutParsing_SOAP(URL url, String rqlQuery) throws RQLException
	{
		try {
			StringBuilder soapBody = new StringBuilder(2048);
			soapBody.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			.append("<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mes=\"http://tempuri.org/RDCMSXMLServer/message/\">")
			.append("<soapenv:Header/><soapenv:Body>")
			.append("<mes:Execute soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">")
			.append("<sParamA xsi:type=\"xsd:string\"><![CDATA[")
			.append(rqlQuery) // TBI: Falls da CDATAs drinstehen, mussen wir evtl. was tun
			// http://en.wikipedia.org/wiki/CDATA#Nesting
			.append("]]></sParamA></mes:Execute>")
			.append("</soapenv:Body></soapenv:Envelope>");
			
			final long before;
			final long after;
			final long delta;
			
            if (debugRql) {
                System.out.println(">------ RQL SOAP request ----->\n" + StringHelper.prettyPrintXml(rqlQuery, 2));
            }

            before = System.currentTimeMillis();
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
			conn.setRequestProperty("SOAPAction", "http://tempuri.org/RDCMSXMLServer/action/XmlServer.Execute");
			OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			osr.write(soapBody.toString());
			osr.close();

			// String ctype = conn.getContentType(); // TBI: Use this encoding, don't assume UTF-8
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource source = new InputSource(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			//InputSource source = new InputSource(conn.getInputStream()); // there is no XML PI-header
            Document soapResponse = db.parse(source);

            // Error signaling (body may be empty)
            String rqlError = item0FirstChildValue(soapResponse.getElementsByTagName("sErrorA"), "");
            String rqlInfo = item0FirstChildValue(soapResponse.getElementsByTagName("sResultInfoA"), "");
            String rqlResponse = item0FirstChildValue(soapResponse.getElementsByTagName("Result"), "");
            
            // Consume the first line with the superflous XML-Header (if any)
			String firstLine = new BufferedReader(new StringReader(rqlResponse)).readLine(); // peek ahead
			if (firstLine != null && firstLine.startsWith("<?xml") && firstLine.endsWith("?>")) {
				System.out.println("* RQL firstline was: " + firstLine);
				rqlResponse = rqlResponse.substring(firstLine.length());
			}

			after = System.currentTimeMillis();
			delta = after - before;
			
            if (debugRql) {
            	System.out.println("<------ RQL SOAP response ("+delta+" ms)------<"+rqlError+"<"+rqlInfo+"<\n" + (rqlResponse.isEmpty() ? "" : StringHelper.prettyPrintXml(rqlResponse, 2)));
            }
            
            if (rqlProfiler != null) {
            	String key = this.parseAction(rqlQuery);
            	rqlProfiler.add(key, delta);
            }
            
            if (!rqlError.isEmpty()) {
            	// FIXME: Das koennte man vernuenftig in RQLException aufheben
            	throw new RQLException(rqlError + ": " + rqlInfo);
            }
            
            return rqlResponse;
		} catch (ParserConfigurationException e) {
			throw new RQLException(e.toString(), e);
		} catch (SAXException e) {
			throw new RQLException(e.toString(), e);
		} catch (IOException e) {
			throw new RQLException(e.toString(), e);
		} catch (Error e) {
			e.printStackTrace(System.out); // Debug
			throw e;
		}
	}
	
	
	/**
	 * Build some kind of key for an RQL query.
	 * 
	 * @param rqlQuery 
	 * @return something that tells us what is going on.
	 */
	private String parseAction(String rqlQuery) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = dbf.newDocumentBuilder();
		RQLNode root = buildTree(db.parse(new InputSource(new StringReader(rqlQuery))).getDocumentElement());
		StringBuilder sb = new StringBuilder(256);
		Queue<RQLNode> q = new LinkedList<RQLNode>();
		q.add(root); // IODATA

		while (true) {
			RQLNode n = q.poll();
			if (n == null) break;

			// Name of this node
			String name = n.getName();
			if (name == null) continue; // text node

			// ignore duplicates, no matter the depth
			if (sb.toString().endsWith(name)) { // hooray for garbage
				sb.append("...");
			} else { 			// Append it
				if (!name.equals("IODATA")) {
					if (sb.length() != 0)
						sb.append(" / ");
					sb.append(n.getName());
				}
			}
			
			// we need to know the action
			String action = n.getAttribute("action");
			if (action != null) {
				sb.append(" ").append(action);
			}
			
			// children
			RQLNodeList l = n.getChildren();
			if (l == null) continue;
			
			for (RQLNode c : l) {
				q.add(c);
			}
		}
		
		return sb.toString();
	}
	
	
	/**
	 * We all love XML and DOM: Extract the first usable string from a number of nodes.
	 * @return the defValue if the structure is not as expected.
	 */
	private String item0FirstChildValue(NodeList l, String defValue) {
		if (l.getLength() < 1)
			return defValue;
		
		Node e = l.item(0).getFirstChild();
		if (e == null)
			return defValue;
		
		String out = e.getNodeValue();
		return out == null ? defValue : out;
	}
	

	/**
	 * Liefert die URL zu dem CMS Server zurück, mit der sich dieser CmsClient
	 * verbindet.
	 * <p>
	 * Wird er nicht im Konstruktor gesetzt, wird er per default aus
	 * rql_fw.properties gelesen.
	 * 
	 * @throws RQLException
	 */
	public URL getCmsServerConnectionUrl() throws RQLException {
		if (cmsServerConnectionUrl == null) {
			setCmsServerConnectionUrl(ResourceBundle.getBundle(
					"com.hlcl.rql.as.rql_fw").getString(
					"cmsServerConnectionUrl"));
		}
		return cmsServerConnectionUrl;
	}

	/**
	 * Returns the encoding which is used to read the response from the bridge
	 * asp.
	 */
	private String getResponseReaderEncoding() throws RQLException {
		return ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString(
				"responseReaderEncoding");
	}

	/**
	 * Returns the protocol, host and port number from the cms server connection
	 * URL. It is used to build further URLs, like the download URL for the
	 * correct host. Do not have a / at the end.
	 * 
	 * @return protocol, host name and port number (optional), e.g.
	 *         http://reddot.hlcl.com or http://reddot.hlcl.com:8080
	 */
	private String getCmsServerConnectionUrlHost() throws RQLException {
		URL url = getCmsServerConnectionUrl();
		// assumes that no query parameters are used!
		// simply remove the path /cms/hlclRemote.asp from the url string
		return StringHelper.replace(url.toString(), url.getPath(), "");
	}

	/**
	 * Returns the file download URL pattern
	 * http://<cms-server>/cms/ImageCache/{0}/{1}/{2}/{3}. The cms-server
	 * variable is retrieved from the {@link #getCmsServerConnectionUrl()}. The
	 * Variables are: # 0 = project guid, # 1 = folder guid, # 2 = 2 letter
	 * prefix, # 3 = filename
	 * 
	 * @see FileElement#getDownloadUrl(boolean)
	 * @return file download URL pattern, e.g.
	 *         http://reddot.hlcl.com/cms/ImageCache/{0}/{1}/{2}/{3}
	 */
	String getFileDownloadUrlPattern() throws RQLException {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String downloadPath = b.getString("downloadPath");
		return getCmsServerConnectionUrlHost() + downloadPath;
	}

	/**
	 * Returns the page preview URL pattern
	 * http://<cms-server>/cms/ioRD.asp?Action
	 * =RedDot&amp;Mode=0&amp;OnLoad=0&amp;PageGuid={0}&amp;PreviewType=2. The
	 * variable cms-server is retrieved from this client's server connection
	 * URL. &amp; needed, because MessageFormat.format() converts it back into &.
	 * 
	 * @return page preview url pattern, e.g.
	 *         http://reddot.hlcl.com/cms/ioRD.asp
	 *         ?Action=RedDot&amp;Mode=0&amp;OnLoad
	 *         =0&amp;PageGuid={0}&amp;PreviewType=2
	 */
	String getPagePreviewUrlPattern() throws RQLException {
		ResourceBundle b = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw");
		String pagePreviewPath = b.getString("pagePreviewPath");
		return getCmsServerConnectionUrlHost() + pagePreviewPath;
	}

	/**
	 * Returns the encoding which is used to write the request to the bridge
	 * asp.
	 */
	private String getRequestWriterEncoding() throws RQLException {
		return ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString(
				"requestWriterEncoding");
	}

	/**
	 * Liefert den angemeldeten Benutzer, falls vorhanden.
	 * <p>
	 * Liegt keine User GUID vor, wird versucht diese aus dem sessionKey zu
	 * ermitteln.
	 * <p>
	 * Dazu muss vorher ein Projekt gewï¿½hlt worden sein.
	 */
	public User getConnectedUser() throws RQLException {

		if (connectedUser != null) {
			return connectedUser;
		} else if (currentProject == null) {
			throw new MissingGuidException(
					"The GUID for the connected user is missing. You created this CMS client by logon GUID only and did not select a project via session key, therefore no user GUID is available. Select a project via the session key, create a client by user name and password or create this CMS client with user GUID.");
		} else {
			return new User(this, getConnectedUserGuid(getCurrentProject()
					.getSessionKey()));
		}
	}

	/**
	 * Liefert die Locale des angemeldeten Benutzers zurï¿½ck.
	 */
	public Locale getConnectedUserLocale() throws RQLException {
		return getConnectedUser().getLocale();
	}

	/**
	 * Liefert die EmailAddresse des angemeldeten Benutzers zurï¿½ck.
	 */
	public String getConnectedUserEmailAddress() throws RQLException {
		return getConnectedUser().getEmailAddress();
	}

	/**
	 * Liefert den angemeldeten Benutzer ï¿½ber eine beliebige Seite des
	 * aktuellen Projektes. Skurilerweise liefert das CMS die GUID des
	 * angemeldeten Benuter ï¿½ber die Seite zurï¿½ck. Und die meisten Scripte
	 * beziehen sich auf eine Seite, diese ist also vorhanden. Das funktioniert
	 * auch bereits in V5.6!
	 */
	public User getConnectedUser(Page page) throws RQLException {

		connectedUser = new User(this, page.getConnectedUserGuid());
		return connectedUser;
	}

	/**
	 * Liefert den angemeldeten Benutzer fï¿½r den gegebenen session key. Erst
	 * ab V6!.
	 */
	public User getConnectedUser(String sessionKey) throws RQLException {

		connectedUser = new User(this, getConnectedUserGuid(sessionKey));
		return connectedUser;
	}

	/**
	 * Liefert die GUID des angemeldeten Benutzers aus dem SessionKey des
	 * aktuellen Projektes.
	 */
	private String getConnectedUserGuid(String sessionKey) throws RQLException {
		return getConnectedUserNode(sessionKey).getAttribute("guid");
	}

	/**
	 * Liefert den RQLNode fï¿½r den angemeldeten Benutzer aus dem SessionKey
	 * des aktuellen Projektes.
	 */
	private RQLNode getConnectedUserNode(String sessionKey) throws RQLException {

		/*
		 * V6 request <IODATA> <PROJECT sessionkey="[!key!]"> <USER
		 * action="sessioninfo"/> </PROJECT> </IODATA> V6 response <IODATA>
		 * <USER action="sessioninfo" languagevariantid="DEU"
		 * dialoglanguageid="DEU" guid="[!guid_user!]"
		 * projectguid="[!guid_project!]" name="admin" flag2="32768" id="1"
		 * projectlevel="1"/> </IODATA>
		 */
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
				+ "  <PROJECT sessionkey='" + sessionKey + "'>"
				+ "   <USER action='sessioninfo'/>" + "  </PROJECT>"
				+ "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNode("USER");
	}

	/**
	 * Liefert das aktuell gewï¿½hlte Projekt, falls vorher ein Projekt geholt
	 * wurde; sonst null.
	 * 
	 * @see <code>getProject</code>
	 */
	public Project getCurrentProject() {

		return currentProject;
	}

	/**
	 * Liefert die GUID des aktuell gewï¿½hlten Projektes, falls vorher ein
	 * Projekt geholt wurde; sonst null.
	 * 
	 * @see <code>getProject</code>
	 */
	public String getCurrentProjectGuid() {

		return currentProject.getProjectGuid();
	}

	/**
	 * Liefert die Locale fï¿½r die gegebene locale ID (z.b. Germany = 1031)
	 * zurï¿½ck.
	 * 
	 * @throws ElementNotFoundException
	 *             if locale cannot be found
	 */
	public Locale getLocaleByLcid(String localeId) throws RQLException {

		RQLNodeList localeNodeList = getLocaleNodeList();

		for (int i = 0; i < localeNodeList.size(); i++) {
			RQLNode localeNode = localeNodeList.get(i);
			String lcid = localeNode.getAttribute("lcid");
			if (lcid.equals(localeId)) {
				return new Locale(this, lcid, localeNode.getAttribute("id"),
						localeNode.getAttribute("country"),
						localeNode.getAttribute("language"));
			}
		}

		throw new ElementNotFoundException(
				"The locale for the given locale ID " + localeId
						+ " cannot be found.");
	}

	/**
	 * Liefert die RQLNodeList fï¿½r alle Locale des RD Servers.
	 */
	private RQLNodeList getLocaleNodeList() throws RQLException {

		/*
		 * V7.5 request <IODATA loginguid="FA6DBAC63F8D45CAB6B293379337A323">
		 * <LANGUAGE action="list"/> </IODATA> V6 response <IODATA> <LANGUAGES>
		 * ... <LIST id="DEU" country="Germany" language="German"
		 * standardlanguage="0" lcid="1031" rfclanguageid="de-de"/> ... <LIST
		 * id="ENG" country="United Kingdom" language="English"
		 * standardlanguage="1" lcid="2057" rfclanguageid="en-gb"/> <LIST
		 * id="ENU" country="United States" language="English"
		 * standardlanguage="0" lcid="1033" rfclanguageid="en-us"/> <LIST
		 * id="ESY" country="Uruguay" language="Spanish" standardlanguage="0"
		 * lcid="14346" rfclanguageid="es-uy"/> <LIST id="ESV"
		 * country="Venezuela" language="Spanish" standardlanguage="0"
		 * lcid="8202" rfclanguageid="es-ve"/> <LIST id="VIT" country="Viet Nam"
		 * language="Vietnamese" standardlanguage="0" lcid="1066"
		 * rfclanguageid="vi"/> <LIST id="ARY" country="Yemen" language="Arabic"
		 * standardlanguage="0" lcid="9217" rfclanguageid="ar-ye"/> </LANGUAGES>
		 * </IODATA>
		 */
		// call CMS
		if (allLocalesNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
					+ "  <LANGUAGE action='list'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			allLocalesNodeListCache = rqlResponse.getNodes("LIST");
		}
		return allLocalesNodeListCache;
	}

	/**
	 * Liefert die RedDot logon GUID.
	 */
	public String getLogonGuid() {

		return logonGuid;
	}

	/**
	 * Liefert das Plugin mit dem gegebenen Namen oder null, falls keines
	 * gefunden werden kann.
	 * <p>
	 * Check with equals().
	 */
	public Plugin getPluginByName(String pluginName) throws RQLException {

		RQLNodeList nodes = getPluginsNodeList();
		// check if any plugins at all
		if (nodes == null) {
			return null;
		}

		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode node = nodes.get(i);
				if (node.getAttribute("name").equals(pluginName)) {
					return buildPlugin(node);
				}
				;
			}
		}
		return null;
	}

	/**
	 * Liefert alle Plugins, deren Name mit dem gegebenen Prefix beginnt oder
	 * eine leere Liste, falls keines gefunden werden kann.
	 * <p>
	 * Check with startsWith().
	 */
	public Set<Plugin> getPluginsByNamePrefix(String pluginNamePrefix)
			throws RQLException {

		RQLNodeList nodes = getPluginsNodeList();
		// check if any plugins at all
		if (nodes == null) {
			return null;
		}

		// collect
		HashSet<Plugin> result = new HashSet<Plugin>();
		if (nodes != null) {
			for (int i = 0; i < nodes.size(); i++) {
				RQLNode node = nodes.get(i);
				if (node.getAttribute("name").startsWith(pluginNamePrefix)) {
					result.add(buildPlugin(node));
				}
				;
			}
		}
		return result;
	}

	/**
	 * Builds a plugin object from given node.
	 */
	Plugin buildPlugin(RQLNode node) {
		return new Plugin(this, node.getAttribute("guid"),
				node.getAttribute("active"), node.getAttribute("name"));
	}

	/**
	 * Liefert die RQLNode List fï¿½r alle Plugins vom RD Server.
	 */
	private RQLNodeList getPluginsNodeList() throws RQLException {

		/*
		 * V7.5 request <IODATA loginguid="B55B02ADF81E4C2E86C095A1BCF93AF8">
		 * <PLUGINS action="list" byproject="0"/> </IODATA> V7.5 response
		 * <IODATA> <PLUGINS action="list" byproject="0"> <PLUGIN
		 * guid="002469BC78574E62A271A623CD514720" name="editLinking.jsp LOCAL"
		 * active="0" compatibility="6.5"> <PROJECT
		 * guid="06BE79A1D9F549388F06F6B649E27152" name="hip.hlcl.com"/>
		 * </PLUGIN> <PLUGIN guid="03E123F183BB4B4DAD72140595FDC61F"
		 * name="showChildDetails.jsp - LOCAL" active="0" compatibility="6.5">
		 * <PROJECT guid="268F46EF5EB74A75824856D3DA1C6597" name="fishelp"/>
		 * <PROJECT guid="06BE79A1D9F549388F06F6B649E27152"
		 * name="hip.hlcl.com"/> </PLUGIN> <PLUGIN
		 * guid="03EE3E6E6F6A475386FD7BE3D5B048C5" name="Show All Projects"
		 * active="0" compatibility=""> <PROJECT
		 * guid="031846B2E1A94FAFB52ACDC38B92513E" name="hlag_relaunch_2004"/>
		 * <PROJECT guid="4EA3E533EEAF4C8794348D61602C80FB" name="processes"/>
		 * <PROJECT guid="5256C671655D4CE696F663C73CE3E526"
		 * name="www.hapag-lloyd.com"/> <PROJECT
		 * guid="126149CE860E4BF6BA5D36248E0ED7C0" name="Manuals"/> <PROJECT
		 * guid="EA851692656044EEB27D9C482C7F0878" name="SYSHAND"/> <PROJECT
		 * guid="268F46EF5EB74A75824856D3DA1C6597" name="fishelp"/> <PROJECT
		 * guid="06BE79A1D9F549388F06F6B649E27152" name="hip.hlcl.com"/>
		 * <PROJECT guid="E62CF0C8E4EC4D018C3E392C42A12161" name="Up-And-Away"/>
		 * </PLUGIN> ... </PLUGINS> </IODATA>
		 */
		// call CMS
		if (allPluginsNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
					+ "  <PLUGINS action='list'  byproject='0'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			allPluginsNodeListCache = rqlResponse.getNodes("PLUGIN");
		}
		return allPluginsNodeListCache;
	}

	/**
	 * Creates a new plugin from the the Plugins XML file, which has to be a
	 * local path on the CMS server itself. Returns the imported plugin. Same
	 * functionality as @link {@link #addPlugin(String)}.
	 */
	public Plugin importPlugin(String definitionXmlPathOnCmsServer)
			throws RQLException {
		return addPlugin(definitionXmlPathOnCmsServer);
	}

	/**
	 * Creates a new plugin from the the Plugins XML file, which has to be a
	 * local path on the CMS server itself. Returns the imported plugin. Same
	 * functionality as {@link #importPlugin(String)}.
	 */
	public Plugin addPlugin(String definitionXmlPathOnCmsServer)
			throws RQLException {

		/*
		 * V9 request <IODATA loginguid="DA524177C6FB4C798379EA4696D10F55">
		 * <PLUGINS action="import"
		 * source="E:\Server\CMS\ASP\PlugIns\showNotes.xml"/> </IODATA> V9
		 * response <IODATA> <REPORT> <INFO caption="showNotes.jsp - ???"
		 * text="The plug-in has been imported."/> </REPORT> </IODATA>
		 */
		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
				+ "  <PLUGINS action='import' source='"
				+ definitionXmlPathOnCmsServer + "'/>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);

		// reset cache
		allPluginsNodeListCache = null;

		return getPluginByName(rqlResponse.getNode("INFO").getAttribute(
				"caption"));
	}

	/**
	 * Erzeugt ein Project aus dem gegebenen sessionKey. Die GUID des Projektes
	 * wird ermittelt.
	 * <p>
	 * Das aktuell gewï¿½hlte Projekt wird festgehalten.
	 * 
	 * @param sessionKey
	 *            aktueller Sessionkey
	 * @return Project
	 */
	public Project getProject(String sessionKey) throws RQLException {
		return getProject(sessionKey, getProjectGuid(sessionKey));
	}

	/**
	 * Erzeugt ein Project aus dem gegebenen sessionKey und der GUID des
	 * Projektes.
	 * <p>
	 * Das aktuell gewï¿½hlte Projekt wird festgehalten.
	 * 
	 * @param sessionKey
	 *            aktueller Sessionkey
	 * @param projectGuid
	 *            Guid des Projektes
	 * @return Project
	 */
	public Project getProject(String sessionKey, String projectGuid)
			throws RQLException {

		if (currentProject == null
				|| !currentProject.getProjectGuid().equals(projectGuid)) {
			// prevent projectGuid == null
			if (projectGuid == null) {
				projectGuid = getProjectGuid(sessionKey);
			}
			setCurrentProject(new Project(this, sessionKey, projectGuid));
			// validate against project
			currentProject.validate();
		}

		return currentProject;
		// ab V6
		// aus sessionKey kann projectGUID + userGUID ermittelt werden
		/*
		 * request <IODATA> <PROJECT sessionkey="[!key!]"> <USER
		 * action="sessioninfo"/> </PROJECT> </IODATA> response <IODATA> <USER
		 * action="sessioninfo" languagevariantid="DEU" dialoglanguageid="DEU"
		 * guid="[!guid_user!]" projectguid="[!guid_project!]" name="admin"
		 * flag2="32768" id="1" projectlevel="1"/> </IODATA>
		 */
	}

	/**
	 * Erzeugt ein Project aus der gegebenen GUID des Projektes. Das aktuell
	 * gewï¿½hlte Projekt wird festgehalten.
	 * 
	 * @param projectGuid
	 *            Guid des Projektes
	 * @return Project
	 */
	public Project getProjectByGuid(String projectGuid) throws RQLException {

		if (currentProject == null
				|| !currentProject.getProjectGuid().equals(projectGuid)) {
			RQLNode projectNodeOrNull = findUserProjectNodeByGuid(projectGuid);
			if (projectNodeOrNull == null) {
				throw new ProjectNotFoundException(
						"Project with GUID "
								+ projectGuid
								+ " could not be found, or the user could not access this project.");
			}
			setCurrentProject(new Project(this, projectGuid));
			// validate against project
			currentProject.validate();
		}

		return currentProject;
	}

	/**
	 * Erzeugt ein Project mit dem gegebenen Namen. Der Benutzer muss auf dieses
	 * Projekt berechtigt sind. Das aktuell gewï¿½hlte Projekt wird
	 * festgehalten.
	 * 
	 * @param projectName
	 *            Name des Projektes, z.b. hip.hlcl.com
	 * @return Project
	 */
	public Project getProjectByName(String projectName) throws RQLException {

		if (currentProject == null
				|| !currentProject.getName().equals(projectName)) {
			RQLNode projectNodeOrNull = findUserProjectNodeByName(projectName);
			if (projectNodeOrNull == null) {
				throw new ProjectNotFoundException(
						"Project with name "
								+ projectName
								+ " could not be found, or the user could not access this project.");
			}
			setCurrentProject(new Project(this,
					projectNodeOrNull.getAttribute("guid")));
			// validate against project
			currentProject.validate();
		}

		return currentProject;
	}

	/**
	 * Liefert die GUID des aktuellen Projektes aus dem session key.
	 */
	private String getProjectGuid(String sessionKey) throws RQLException {
		return getConnectedUserNode(sessionKey).getAttribute("projectguid");
	}

	/**
	 * Liefert die RQLNodeList mit den Projekten auf diesem CMS Server.
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	private RQLNodeList getProjectsNodeList() throws RQLException {
		/*
		 * V5 request <IODATA loginguid="7B387EFFCA5D44ADACD45BE9332DB0B5">
		 * <ADMINISTRATION> <PROJECTS action="list"/> </ADMINISTRATION>
		 * </IODATA> V5 response <IODATA> <PROJECTS action="list"> <PROJECT
		 * guid="06BE79A1D9F549388F06F6B649E27152" name="hip.hlcl.com"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="126149CE860E4BF6BA5D36248E0ED7C0"
		 * name="Manuals" servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="4EA3E533EEAF4C8794348D61602C80FB"
		 * name="processes" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="0E9807BAD7D14B738038214E793EC9A8"
		 * name="Projekt orgaweb" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="17" lockinfo=
		 * "RedDot for orgaweb not available from 21 July 2004 until 27 July 2004. rgds, Antje Kiessig"
		 * checkdatabase="002.043" lockedbysystem="0"/> <PROJECT
		 * guid="268F46EF5EB74A75824856D3DA1C6597" name="projekt_fishelp"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="094C0E751B524A83BD9707889135B5DD"
		 * name="projekt_hlag" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="7CF5EBA32CB34E55A329CA181D5D150D"
		 * name="projekt_hlcln" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="EA851692656044EEB27D9C482C7F0878"
		 * name="SYSHAND" servername="khh30006" server="khh30006"
		 * versioning="-1" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/> <PROJECT guid="E62CF0C8E4EC4D018C3E392C42A12161"
		 * name="Up-And-Away" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0"/></PROJECTS> </IODATA>
		 */

		// call CMS
		if (projectsNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
					+ " <ADMINISTRATION>" + "   <PROJECTS action='list'/>"
					+ " </ADMINISTRATION>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			projectsNodeListCache = rqlResponse.getNodes("PROJECT");
		}

		return projectsNodeListCache;
	}

	/**
	 * Liefert die RQLNodeList mit allen Projekt-Nodes des gegebenen Users
	 * zurï¿½ck.
	 * 
	 * @return <code>RQLNodeList</code>
	 */
	RQLNodeList getProjectsNodeList(User user) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="40C426B57BF9485D964B0B2694DB41C2">
		 * <ADMINISTRATION> <USER guid="8898998310DD4513BB8CC1771FFD00BC">
		 * <PROJECTS action="list"/> </USER> </ADMINISTRATION> </IODATA> V5
		 * response <IODATA> <USER guid="8898998310DD4513BB8CC1771FFD00BC">
		 * <PROJECTS action="list" parentguid="8898998310DD4513BB8CC1771FFD00BC"
		 * parenttype="USR"> <PROJECT guid="06BE79A1D9F549388F06F6B649E27152"
		 * name="hip.hlcl.com" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="126149CE860E4BF6BA5D36248E0ED7C0" name="Manuals"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="4EA3E533EEAF4C8794348D61602C80FB" name="processes"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="0E9807BAD7D14B738038214E793EC9A8" name="Projekt orgaweb"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="17" lockinfo=
		 * "RedDot for orgaweb not available from 21 July 2004 until 27 July 2004. rgds, Antje Kiessig"
		 * checkdatabase="002.043" lockedbysystem="0" inhibit="1" userlevel="4"
		 * templateeditorright="0" languagemanagerright="0" istranslator="0"/>
		 * <PROJECT guid="268F46EF5EB74A75824856D3DA1C6597"
		 * name="projekt_fishelp" servername="khh30006" server="khh30006"
		 * versioning="0" inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="094C0E751B524A83BD9707889135B5DD" name="projekt_hlag"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="7CF5EBA32CB34E55A329CA181D5D150D" name="projekt_hlcln"
		 * servername="khh30006" server="khh30006" versioning="0"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> <PROJECT
		 * guid="EA851692656044EEB27D9C482C7F0878" name="SYSHAND"
		 * servername="khh30006" server="khh30006" versioning="-1"
		 * inhibitlevel="0" lockinfo="" checkdatabase="002.043"
		 * lockedbysystem="0" inhibit="0" userlevel="4" templateeditorright="0"
		 * languagemanagerright="0" istranslator="0"/> </PROJECTS> </USER>
		 * </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
				+ " <ADMINISTRATION>" + "  <USER guid='" + user.getUserGuid()
				+ "'>" + "   <PROJECTS action='list'/>" + "  </USER>"
				+ " </ADMINISTRATION>" + "</IODATA>";
		RQLNode rqlResponse = callCms(rqlRequest);
		return rqlResponse.getNodes("PROJECT");
	}

	/**
	 * Liefert eine List mit Projekte fï¿½r den Test.
	 */
	public java.util.List<Project> getTestProjects() {

		java.util.List<Project> tps = new ArrayList<Project>(1);
		// syshand + up and away
		tps.add(new Project(this, "EA851692656044EEB27D9C482C7F0878"));
		tps.add(new Project(this, "E62CF0C8E4EC4D018C3E392C42A12161"));
		return tps;
	}

	/**
	 * Liefert ein paar User zurï¿½ck, an die testweise ein mail versendet
	 * werden kann.
	 */
	public java.util.List<User> getTestUsers() {

		java.util.List<User> testUsers = new ArrayList<User>();
		testUsers.add(new User(this, "lejafr4",
				"198C466E5362482EBBD0AEE77BF141C3", "user id", "Frank Leja",
				"lejafr@hlcl.com"));

		return testUsers;
	}

	/**
	 * Liefert den User mit dem gegebenen Namen zurï¿½ck.
	 * <p>
	 * Nur mit Administratorrechten benutzbar.
	 */
	public User getUserByName(String userName) throws RQLException {

		// find container
		RQLNodeList usersList = getAllUsersNodeList();
		RQLNode userNode = null;

		for (int i = 0; i < usersList.size(); i++) {
			userNode = usersList.get(i);

			if (userNode.getAttribute("name").equals(userName)) {
				return buildUser(userNode);
			}
		}
		throw new ElementNotFoundException("User with name " + userName
				+ " is not configured at the CMS server.");
	}

	/**
	 * Liefert die UserInterfaceLanguage fï¿½r die gegebene language ID (z.B.
	 * DEU, ENG) zurï¿½ck.
	 * 
	 * @throws ElementNotFoundException
	 *             if language cannot be found
	 */
	public UserInterfaceLanguage getUserInterfaceLanguageByLanguageId(
			String languageId) throws RQLException {

		RQLNodeList languageNodeList = getUserInterfaceLanguageNodeList();

		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode languageNode = languageNodeList.get(i);
			String id = languageNode.getAttribute("id");
			if (id.equals(languageId)) {
				return buildUserInterfaceLanguage(languageNode);
			}
		}

		throw new ElementNotFoundException(
				"The user interface language for the given language ID "
						+ languageId + " cannot be found.");
	}

	/**
	 * Liefert die UserInterfaceLanguage fï¿½r die gegebene RFC language ID
	 * (z.B. de-de, fr-ca) zurï¿½ck.
	 * 
	 * @throws ElementNotFoundException
	 *             if language cannot be found
	 */
	public UserInterfaceLanguage getUserInterfaceLanguageByRfcId(
			String rfcLanguageId) throws RQLException {

		RQLNodeList languageNodeList = getUserInterfaceLanguageNodeList();

		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode languageNode = languageNodeList.get(i);
			String id = languageNode.getAttribute("rfclanguageid");
			if (id.equals(rfcLanguageId)) {
				return buildUserInterfaceLanguage(languageNode);
			}
		}

		throw new ElementNotFoundException(
				"The user interface language for the given RFC language ID "
						+ rfcLanguageId + " cannot be found.");
	}

	/**
	 * Liefert die RQLNodeList fï¿½r alle Oberflï¿½chensprachen des RD Servers.
	 */
	private RQLNodeList getUserInterfaceLanguageNodeList() throws RQLException {

		/*
		 * V7.5 request <IODATA loginguid="D866C366A1EF4D81B4A7255CEFA41975">
		 * <DIALOG action="listlanguages"/> </IODATA> V7.5 response <IODATA>
		 * <LANGUAGES> <LIST id="PTB" country="Brazil" language="Portuguese"
		 * standardlanguage="0" lcid="1046" disabled="0" rfclanguageid="pt-br"/>
		 * <LIST id="FRC" country="Canada" language="French"
		 * standardlanguage="0" lcid="3084" disabled="0" rfclanguageid="fr-ca"/>
		 * <LIST id="CSY" country="Czech Republic" language="Czech"
		 * standardlanguage="0" lcid="1029" disabled="0" rfclanguageid="cs"/>
		 * <LIST id="DEU" country="Germany" language="German"
		 * standardlanguage="0" lcid="1031" disabled="0" rfclanguageid="de-de"/>
		 * <LIST id="ELL" country="Greece" language="Greek" standardlanguage="0"
		 * lcid="1032" disabled="0" rfclanguageid="el"/> <LIST id="HUN"
		 * country="Hungary" language="Hungarian" standardlanguage="0"
		 * lcid="1038" disabled="0" rfclanguageid="hu"/> <LIST id="ITA"
		 * country="Italy" language="Italian" standardlanguage="0" lcid="1040"
		 * disabled="0" rfclanguageid="it-it"/> <LIST id="JPN" country="Japan"
		 * language="Japanese" standardlanguage="0" lcid="1041" disabled="0"
		 * rfclanguageid="ja"/> <LIST id="PLK" country="Poland"
		 * language="Polish" standardlanguage="0" lcid="1045" disabled="0"
		 * rfclanguageid="pl"/> <LIST id="CHS" country="PRC" language="Chinese"
		 * standardlanguage="0" lcid="2052" disabled="0" rfclanguageid="zh-cn"/>
		 * <LIST id="ESN" country="Spain" language="Spanish"
		 * standardlanguage="0" lcid="1034" disabled="0" rfclanguageid="es-es"/>
		 * <LIST id="SVE" country="Sweden" language="Swedish"
		 * standardlanguage="0" lcid="1053" disabled="0" rfclanguageid="sv-se"/>
		 * <LIST id="ENG" country="United Kingdom" language="English"
		 * standardlanguage="1" lcid="2057" disabled="0" rfclanguageid="en-gb"/>
		 * </LANGUAGES> </IODATA>
		 */
		// call CMS
		if (allUserInterfaceLanguagesNodeListCache == null) {
			String rqlRequest = "<IODATA loginguid='" + getLogonGuid() + "'>"
					+ "  <DIALOG action='listlanguages'/>" + "</IODATA>";
			RQLNode rqlResponse = callCms(rqlRequest);
			allUserInterfaceLanguagesNodeListCache = rqlResponse
					.getNodes("LIST");
		}
		return allUserInterfaceLanguagesNodeListCache;
	}

	/**
	 * Liefert alle UserInterfaceLanguages zurï¿½ck.
	 */
	public java.util.List<UserInterfaceLanguage> getUserInterfaceLanguages()
			throws RQLException {

		RQLNodeList languageNodeList = getUserInterfaceLanguageNodeList();

		// wrap all
		java.util.List<UserInterfaceLanguage> result = new ArrayList<UserInterfaceLanguage>(
				languageNodeList.size());
		for (int i = 0; i < languageNodeList.size(); i++) {
			RQLNode languageNode = languageNodeList.get(i);
			result.add(buildUserInterfaceLanguage(languageNode));
		}
		return result;
	}

	/**
	 * Do all steps needed to login from given user name and password.
	 * 
	 * @param passwordAuthentication
	 * @throws UserAlreadyLoggedInException
	 * @throws RQLException
	 * @throws UnknownUserOrWrongPasswordException
	 */
	private void login(PasswordAuthentication passwordAuthentication)
			throws UserAlreadyLoggedInException, RQLException,
			UnknownUserOrWrongPasswordException {
		// extract wrapped credentials
		String userName = passwordAuthentication.getUserName();
		String password = passwordAuthentication.getPassword();

		/*
		 * V5 request <IODATA> <ADMINISTRATION action="login" name="user_name"
		 * password="pw"/> </IODATA>
		 * 
		 * V5 response <IODATA> <LOGIN guid="[!guid_login!]" server="svr001"
		 * serverguid="[!guid_server!]" translationeditorserverlicense="1"/>
		 * <USER guid="[!guid_user!]" id="3" flags1="0" flags2="0" maxlevel="1"
		 * dialoglanguageid="DEU" languageid="DEU" isservermanager="-1" te="-1"
		 * lm="0" showstarthelp="0" projectcount="5" lcid="1031"/> <PROJECT
		 * guid="[!guid_project!]" name="project_name" description=""
		 * versioning="-1" speciallock="0" inhibitlevel="0" lockinfo=""
		 * inhibit="0" userlevel="1" templateeditorright="-1"
		 * languagemanagerright="0" projectversion="002.015"
		 * reddotstartpageguid="" lockedbysystem="0"/> </IODATA>
		 */

		// call CMS
		RQLNode rqlResponse = null;
		try {
			String rqlRequest = "<IODATA>"
					+ " <ADMINISTRATION action='login' name='" + userName
					+ "' password='" + password + "'/>" + "</IODATA>";
			rqlResponse = callCms(rqlRequest);
		} catch (RQLException rqle) {
			// check if already logged in
			if (rqle.getMessage().indexOf("#RDError101") > 0) {
				throw new UserAlreadyLoggedInException(
						"The user with name "
								+ userName
								+ " is already logged in. Please logout this user and try again.");
			}
			// throw
			throw rqle;
		}

		// check if login was successful
		logonGuid = rqlResponse.getNode("LOGIN").getAttribute("guid");
		if (logonGuid == null || logonGuid.length() == 0) {
			throw new UnknownUserOrWrongPasswordException("The user with name "
					+ userName
					+ " is unknown or the given password is not correct.");
		}
		// save login results
		connectedUser = new User(this, rqlResponse.getNode("USER")
				.getAttribute("guid"), logonGuid);
	}

	/**
	 * Meldet den User mit der gegebenen logon GUID vom CMS ab.
	 */
	void logout(String logonGuid) throws RQLException {
		/*
		 * V5 request <IODATA loginguid="98AF5E2CA5E1480ABA2BD39181234394">
		 * <ADMINISTRATION> <LOGOUT guid="98AF5E2CA5E1480ABA2BD39181234394" />
		 * </ADMINISTRATION> </IODATA> V5 response <IODATA> </IODATA>
		 */

		// call CMS
		String rqlRequest = "<IODATA loginguid='" + logonGuid + "'>"
				+ "  <ADMINISTRATION>" + "   <LOGOUT guid='" + logonGuid
				+ "'/>" + "  </ADMINISTRATION>" + "</IODATA>";
		callCms(rqlRequest);
	}

	/**
	 * Liefert einen LDAP directory service context zurï¿½ck. Muss mit
	 * closeLdapContext() geschlossen werden.
	 * 
	 * @see #closeLdapContext()
	 */
	public DirContext openLdapContext() throws RQLException {
		// get LDAP server properties
		PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle
				.getBundle("com.hlcl.rql.as.rql_fw");
		String ldapServerName = bundle.getString(LDAP_SERVER_NAME_KEY).trim();
		String ldapServerPort = bundle.getString(LDAP_SERVER_PORT_KEY).trim();
		// prepare context creation
		Hashtable<String, String> environment = new Hashtable<String, String>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory"); // fix LDAP
		environment.put(Context.PROVIDER_URL, "ldap://" + ldapServerName + ":"
				+ ldapServerPort);
		// create context
		try {
			ldapContext = new InitialDirContext(environment);
		} catch (NamingException ex) {
			throw new RQLException("Could not create LDAP context", ex);
		}
		return ldapContext;
	}

	/**
	 * Sendet eine Mail an alle gegebenen User.
	 */
	public void sendMail(java.util.List<User> users, String from,
			String subject, String message) throws RQLException {

		// convert receivers
		String[] to = new String[users.size()];
		Iterator<User> it = users.iterator();
		int i = 0;
		User user = null;
		while (it.hasNext()) {
			user = (User) it.next();
			to[i++] = user.getEmailAddress();
		}

		// send mail
		sendMail(from, to, subject, message);
	}

	/**
	 * Sendet eine Mail an einen Empfï¿½nger.
	 */
	public void sendMail(String from, String to, String subject, String message)
			throws RQLException {

		String[] toAddresses = new String[1];
		toAddresses[0] = to;

		sendMail(from, toAddresses, subject, message);
	}

	/**
	 * Sends an e-mail to the given list of addresses.
	 */
	public void sendMail(String from, String toAddresses, String delimiter,
			String subject, String message) throws RQLException {
		sendMail(from, StringHelper.split(toAddresses, delimiter), subject,
				message);
	}

	/**
	 * Anbindung an den DS MailService.
	 */
	public void sendMail(String from, String[] toAddresses, String subject,
			String message) throws RQLException {

		// send mail
		try {
			CmsClient.sendMail(from, toAddresses, subject, message, null, null);
		} catch (MessagingException me) {
			throw new RQLException("The e-mail '" + subject
					+ "' could not be send.", me);
		}
	}

	/**
	 * Sendet eine Mail an alle eingerichteten User.
	 */
	public void sendMailToAllUsers(String from, String subject, String message,
			boolean isTest) throws RQLException {

		sendMail(isTest ? getTestUsers() : getAllUsers(), from, subject,
				message);
	}

	/**
	 * Sendet eine Mail mit Statistikinformationen (Dauer, Zeitpunkt...) im CSV
	 * Format an statisticReceiver.
	 * <p>
	 * 
	 * @param statisticReceiver
	 *            Zieladresse
	 * @param sourceId
	 *            ID des Scriptes für das die Informationen sind
	 * @param start
	 *            Startzeitpunkt in 1/1000 s
	 * @param end
	 *            Endzeitpunkt in 1/1000s
	 * @param additionalHeader
	 *            zusätzliche Headerfelder (mit ; getrennt)
	 * @param additionalData
	 *            zusätzliche Datenfelder (mit ; getrennt)
	 * @throws RQLException
	 */
	public void sendStatisticMail(String statisticReceiver, String sourceId,
			long start, long end, String additionalHeader, String additionalData)
			throws RQLException {

		StringBuilder buffer = new StringBuilder();
		// header
		buffer.append("sourceId;date;time;duration in s;" + additionalHeader
				+ "\n");
		// data
		buffer.append(sourceId + ";");
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		buffer.append(df.format(d) + ";");
		SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
		buffer.append(tf.format(d) + ";");
		buffer.append((end - start) / 1000 + ";");
		buffer.append(additionalData + "\n");
		sendMail(STATISTIC_MAIL_FROM_ADDRESS, statisticReceiver, sourceId
				+ " statistic", buffer.toString());
	}

	/**
	 * @param cmsServerConnectionUrl
	 *            the cmsServerConnectionUrl to set
	 */
	private void setCmsServerConnectionUrl(String cmsServerConnectionUrl)
			throws RQLException {
		try {
			this.cmsServerConnectionUrl = new URL(cmsServerConnectionUrl);
		} catch (MalformedURLException ex) {
			throw new RQLException(
					"URL of CMS server connection "
							+ cmsServerConnectionUrl
							+ " is not valid. Has to be similar to http://reddot.hlcl.com/cms/hlclRemoteRQL.asp",
					ex);
		}
	}

	/**
	 * Ändert den Cache für das Project auf das gegebenen Project. Ein eventuell
	 * vorhandenes altes Projekt-Objekt wird unbenutzbar gemacht.
	 */
	private void setCurrentProject(Project project) {
		// same login GUID didn't work in 2 projects at the same time
		// so always 2 clients needed
		if (currentProject != null) {
			currentProject.invalidate();
		}

		currentProject = project;
	}


	/**
	 * Pretend that no project is currently selected, so that switching the project
	 * performs a validation.
	 */
	public void noCurrentProject() {
		setCurrentProject(null);
	}
	
	/**
	 * Hält die Ausführung für die gegebenen Sekunden an.
	 */
	public void wait(int seconds) throws RQLException {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException ie) {
			throw new RQLException("Waiting for " + seconds
					+ " seconds were interrupted.", ie);
		}
	}

	/**
	 * Hält die Ausführung für die gegebenen Sekunden an.
	 */
	public void wait(String seconds) throws RQLException {
		wait(Integer.parseInt(seconds));
	}

	/**
	 * Erzeugt für alle gegebenen Projekt-Nodes Projekte und liefert sie
	 * gesammelt in einer Liste zurück.
	 */
	java.util.List<Project> wrapProjectNodes(RQLNodeList projectsNodeList)
			throws RQLException {

		java.util.List<Project> projects = new ArrayList<Project>(
				projectsNodeList.size());

		for (int i = 0; i < projectsNodeList.size(); i++) {
			RQLNode node = projectsNodeList.get(i);
			projects.add(new Project(this, node.getAttribute("guid")));
		}
		return projects;
	}

	/**
	 * Wandelt alle gegebenen user nodes in eine Liste mit User-Objekten um.
	 * 
	 * @param userNodeList
	 *            liste der umzuwandelden user nodes
	 */
	private java.util.List<User> wrapUserNodes(RQLNodeList userNodeList) {

		RQLNode node = null;
		java.util.List<User> users = new ArrayList<User>();

		if (userNodeList != null) {
			for (int i = 0; i < userNodeList.size(); i++) {
				node = userNodeList.get(i);
				users.add(buildUser(node));
			}
		}

		return users;
	}

	/**
	 * Setzt alle Plugins, die namePart im Namen haben, auf active=true. Returns
	 * all changed plugins.
	 */
	public java.util.List<Plugin> enablePluginsByNameContains(String namePart,
			boolean ignoreCase) throws RQLException {
		java.util.List<Plugin> result = new ArrayList<Plugin>();
		for (Plugin plugin : getPluginsByNameContains(namePart, ignoreCase)) {
			plugin.setIsActive(true);
			result.add(plugin);
		}
		return result;
	}

	/**
	 * Löscht alle Plugins, die namePart im Namen haben. Returns the number of
	 * deleted plug-ins.
	 */
	public int deletePluginsByNameContains(String namePart, boolean ignoreCase)
			throws RQLException {
		List<Plugin> plugins = getPluginsByNameContains(namePart, ignoreCase);
		for (Plugin plugin : plugins) {
			plugin.delete();
		}
		return plugins.size();
	}

	/**
	 * Setzt alle Plugins, die namePart im Namen haben, auf active=false.
	 * Returns all changed plugins.
	 */
	public java.util.List<Plugin> disablePluginsByNameContains(String namePart,
			boolean ignoreCase) throws RQLException {
		java.util.List<Plugin> result = new ArrayList<Plugin>();
		for (Plugin plugin : getPluginsByNameContains(namePart, ignoreCase)) {
			plugin.setIsActive(false);
			result.add(plugin);
		}
		return result;
	}

	/**
	 * Returns all plug-ins which name contains given namePart. Check with
	 * contains; case depending on given ignoreCase.
	 */
	public java.util.List<Plugin> getPluginsByNameContains(String namePart,
			boolean ignoreCase) throws RQLException {
		java.util.List<Plugin> result = new ArrayList<Plugin>();
		for (Plugin plugin : getAllPlugins()) {
			// check namePart case dependent on given value
			if (StringHelper.contains(plugin.getName(), namePart, ignoreCase)) {
				result.add(plugin);
			}
		}
		return result;
	}

	/**
	 * Returns all active plug-ins which name contains given namePart. Check
	 * with contains; case depending on given ignoreCase.
	 */
	public java.util.List<Plugin> getActivePluginsByNameContains(
			String namePart, boolean ignoreCase) throws RQLException {
		java.util.List<Plugin> result = new ArrayList<Plugin>();
		for (Plugin plugin : getPluginsByNameContains(namePart, ignoreCase)) {
			if (plugin.isActive()) {
				result.add(plugin);
			}
		}
		return result;
	}


    /**
     * Leifert einen ReddotMailer für diese CMS-Verbindung
     *
     * @return
     */
    public ReddotMailer getCmsMailer(){

        return ReddotMailer.forCmsClient(this);
    }

    
    
    /**
     * Replace the current rql profiler with the given one
     * 
     * @param context some kind of marker what we are profiling here.
     */
    public void startProfiling(String context) {
    	this.rqlProfiler = new RqlProfiler(context);
    }
    
    
    /**
     * Stop profiling this client
     * 
     * @return the collected data so far, null when not profiling.
     */
    public RqlProfiler stopProfiling() {
    	RqlProfiler out = this.rqlProfiler;
    	this.rqlProfiler = null;
    	if (out != null)
    		out.after = System.currentTimeMillis();
    	return out;
    }
    
}
