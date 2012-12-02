package com.hlcl.rql.as;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Helper-Klasse mit verschiedenen statischen Methoden für RQL-Anfragen an das
 * CMS.
 * 
 * @author BURMEBJ
 * 
 * Change history:
 * 
 * 14.04.2004  BURMEBJ001  Auf bestimmte Anfragen antwortet der CMS-Server nicht
 *                         mit einer gültigen XML-Struktur: Die Antwort enthält 
 *                         keine Tags. Für diesen Fall wird eine zweite Methode
 *                         geschaffen, die die Antwort direkt ohne Struktur liefert.
 *                         Zu diesem Zweck wurde ein Teil der Methode callCMS() aus-
 *                         gelagert in eine private Methode getCMSResultAsStream().
 *                         Sowohl callCMS() als auch die neue Methode callCMSWithout-
 *                         Parsing() verwenden diese Methode.
 */

public class RQLHelperTest {
	private static final String HOST = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString("host");
	private static final String FILE = ResourceBundle.getBundle("com.hlcl.rql.as.rql_fw").getString("file");

	/**
	 * Diese Methode führ eine RQL-Anfrage mit der übergebenen rqlQuery an das CMS aus.
	 * Das Ergebnis wird in Form eines RQLNode ("<IODATA>...</IODATA>") zurückgegeben.
	 * Wenn es zu Problemen kommt, wird eine RQLException geworfen.
	 * 
	 * @param rqlQuery String: s.o.
	 * @return RQLNode: s.o.
	 * @throws RQLException: s.o.
	 */
	public static RQLNode callCMS(String rqlQuery) throws RQLException {
		InputStream is = null; // BURMEBJ001A, um im finally ein close() zu machen
		RQLNode root = null;

		try {
			// BURMEBJ001D: eigentlicher CMS-Aufruf in getCMSResultAsStream() 
			//              ausgelagert (s.u.).

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = dbf.newDocumentBuilder();

			is = getCMSResultAsStream(rqlQuery); // BURMEBJ001A

			root = buildTree(db.parse(is).getDocumentElement()); // BURMEBJ001M
		} catch (ParserConfigurationException pce) {
			throw new RQLException("RQLHelper.callCMS()", pce);
		} catch (SAXException se) {
			throw new RQLException("RQLHelper.callCMS()", se);
		} catch (IOException ioe) {
			throw new RQLException("RQLHelper.callCMS()", ioe);
		} finally {
			// BURMEBJ001M begin
			// Das bisherige Schließen des OutputStreamWriter wurde 
			// natürlich ebenfalls in die Methode getCMSResultAsStream()
			// verlagert. Hier erfolgt nun zusätzlich noch ein close() 
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

	// BURMEBJ001A
	/**
	 * Diese Methode führ eine RQL-Anfrage mit der übergebenen rqlQuery an das CMS aus.
	 * Das Ergebnis wird als String zurückgegeben. Es wird davon ausgegangen, daß die
	 * 1. Zeile von der Form <?xml ... ?> ist. In diesem Fall wird diese erste Zeile
	 * weggelassen. Andernfalls kommt es zu einer Exception.
	 *
	 * @param rqlQuery String: s.o.
	 * @return String: s.o.
	 * @throws RQLException: s.o.
	 */
	public static String callCMSWithoutParsing(String rqlQuery) throws RQLException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(getCMSResultAsStream(rqlQuery)));

			StringBuffer firstLine = new StringBuffer(); // für die erste Zeile der Antwort
			StringBuffer content = new StringBuffer(); // für die restlichen Zeilen der Antwort
			byte state = 0; // 0: erste Zeile, 1: Zeilenumbrüche am Ende der ersten Zeile, 2: weitere Zeilen
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
			if (!(firstLineString.startsWith("<?xml") && firstLineString.endsWith("?>")))
				throw new RQLException("RQLHelper.callCMSWithoutParsing(): first line not valid: " + firstLineString);

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

	// BURMEBJ001A
	/**
	 * Diese Methode schickt die übergebene rqlQuery an das CMS und liefert das
	 * Ergebnis als InputStream zurück. Die aufrufende Methode muß sich darum
	 * kümmern, den Stream wieder zu schließen.
	 * 
	 * @param rqlQuery: s.o.
	 * @return InputStream: s.o.
	 * @throws RQLException
	 */
	private static InputStream getCMSResultAsStream(String rqlQuery) throws RQLException {
		OutputStreamWriter osr = null;

		try {
			URL url = new URL("http", HOST, FILE);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			osr = new OutputStreamWriter(conn.getOutputStream());
			osr.write(rqlQuery);
			osr.flush();

			return conn.getInputStream();
		} catch (IOException ioe) {
			throw new RQLException("IO Exception reading result from server", ioe);
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
	 * Diese Methode liefert den übergebenen Node mit all seinen Nachkommen
	 * in einen RQLNode mit den entsprechenden Nachkommen um. Der RQL-Node,
	 * der dem übergebenen Node entspricht, wird zurückgegeben.
	 */
	private static RQLNode buildTree(Node root) {
		if (root == null)
			return null;

		RQLNode rqlRoot = null;

		if (root.getNodeType() == Node.ELEMENT_NODE) {
			rqlRoot = new RQLTagNode(root.getNodeName());

			NamedNodeMap nnm = root.getAttributes();
			for (int i = 0; i < (nnm != null ? nnm.getLength() : 0); i++) {
				Node attr = nnm.item(i);
				if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
					((RQLTagNode) rqlRoot).addAttribute(attr.getNodeName(), attr.getNodeValue());
				}
			}

			NodeList children = root.getChildNodes();
			for (int i = 0; i < (children != null ? children.getLength() : 0); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.TEXT_NODE) {
					RQLNode rqlChild = buildTree(child);
					((RQLTagNode) rqlRoot).addChild(rqlChild);
				}
			}
		} else if (root.getNodeType() == Node.TEXT_NODE) {
			rqlRoot = new RQLTextNode(root.getNodeValue());
		}

		return rqlRoot;
	}
}
