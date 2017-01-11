package com.hlcl.rql.hip.as;

import java.io.*;

import org.apache.commons.io.IOUtils;

import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.StringHelper;

/**
 * @author lejafr
 *
 * This class represents a deletion request (a xml file) for the RD Live Server. 
 */
public class LiveServerDeleteRequest {

	private String filename;
	private static final String TEMPLATE_RESOURCE_NAME = "/com/hlcl/rql/hip/as/deletionFile.template";
	private static final String VARIABLE_NAME = "FILENAME_VARIABLE";
	private static final String EXTENDER = ".del";

	private static final String TEMPLATE;
	static {
		InputStream in = LiveServerDeleteRequest.class.getResourceAsStream(TEMPLATE_RESOURCE_NAME);
		try {
			TEMPLATE = IOUtils.toString(in);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot load deletion file template", ex);
		}
	}

	/**
	 * LiveServerDeletionFile constructor comment.
	 */
	public LiveServerDeleteRequest(String filename) {
		super();
		this.filename = filename;
	}

	/**
	 * Liefert den zu löschen Dateinamen zurück. 
	 */
	public String getFilenameToDelete() {
		return filename;
	}

	/**
	 * Liefert den Dateinamen der Löschdatei zurück, die in das Importverzeichnis des LS übertragen werden muss.<p>
	 * Es wird an den zu löschenden Dateinamen einfach .xml angehängt. 
	 */
	public String getFtpFilename() {
		return getFilenameToDelete() + EXTENDER;
	}

	/**
	 * Return the deletion file template
	 */
	private String getTemplate() throws RQLException {
		return TEMPLATE;
	}

	/**
	 * Schreibt dieses deletion file auf den gegebenen stream. Der out stream wird geschlossen.
	 * @throws RQLException 
	 */
	public void writeOn(OutputStream out) throws RQLException {
		String template = getTemplate();
		// merge filename into template
		String content = StringHelper.replace(template, VARIABLE_NAME, getFilenameToDelete());
		try {
			IOUtils.write(content, out);
		} catch (IOException ex) {
			throw new RQLException("IOException writing deletion file content to the given out stream", ex);
		}
	}

}
