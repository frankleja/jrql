package com.hlcl.rql.as;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Diese Klasse beschreibt ein Image-Element einer Seite.
 * 
 * @author LEJAFR
 */
public class ImageElement extends FileElement {

	/**
	 * ImageElement constructor comment.
	 *
	 * @param	page	Seite, die diesen Container Link beinhaltet.
	 * @param	templateElement		TemplateElement auf dem dieses Element basiert
	 * @param	name	Name des Elements
	 * @param	elementGuid	GUID dieses Elements
	 * @param	value Wert des Elements, auch Dateiname eines Bildes 
	 */
	public ImageElement(Page page, TemplateElement templateElement, String name, String elementGuid, String value, String folderGuid) {

		super(page, templateElement, name, elementGuid, value, folderGuid);
	}
	/**
	 * Liefert das Bild als Objekt im Speicher - lädt es vom RD CMS ImageCache herunter.
	 * @param runsOnServer
	 *            =true, if used from webapp or batch on CMS server; the domain name will be replace with localhost
	 *            <p>
	 *            =false, if used from any other client; the configured URL will be used unchanged
	 */
	public BufferedImage downloadImage(boolean runsOnServer) throws RQLException {

		URL url = getDownloadUrl(runsOnServer);
		BufferedImage result = null;
		try {
			result = ImageIO.read(url);
		} catch (IOException ioe) {
			throw new DownloadException("Download of file " + url.toString() + " does not work.", ioe);
		}
		return result;
	}
	/**
	 * Erstellt eine RD Referenz von diesem ImageElement (als Source) zum gegebenen Element.<p> 
	 * Achtung: Nur als Administrator aufrufbar! 
	 */
	public void referenceTo(ImageElement targetElement) throws RQLException {
		getProject().referenceElement(getElementGuid(), targetElement.getElementGuid(), "element");
	}
}
