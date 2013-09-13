package com.hlcl.rql.util.as;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hlcl.rql.as.RQLException;

/**
 * @author Frank Leja
 *
 * This class represents an image used for a HTML image map.\n
 * You could painting a circle and a rectangle on it.\n
 * Polygone has no visible representation. 
 * Based on the V1.4 ImageIO class.
 */
public class ImageMapImage {

	private BufferedImage image;
	private Graphics2D g2;

	/**
	 * constructor from a in memory image
	 */
	public ImageMapImage(BufferedImage image) throws RQLException {
		super();
		this.image = image;
		g2 = image.createGraphics();
		// use antialiasing per default
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	/**
	 * Paints a circle to the image (only in memory)
	 */
	public void paintCircle(int centerX, int centerY, int radius, int color_r, int color_g, int color_b) {
		setColor(color_r, color_g, color_b);
		g2.fillOval(center(centerX, radius), center(centerY, radius), radius, radius);
	}
	/**
	 * Paints a rectangle to the image (only in memory)
	 */
	public void paintRectangle(int centerX, int centerY, int radius, int color_r, int color_g, int color_b) {
		setColor(color_r, color_g, color_b);
		g2.fillRect(center(centerX, radius), center(centerY, radius), radius, radius);
	}
	/**
	 * Save the image with the actual manipulations as PNG file to the given path.
	 */
	public void saveAsPngTo(String path) throws RQLException {
		try {
			ImageIO.write(image, "PNG", new File(path));
		} catch (IOException ioe) {
			throw new RQLException("Image could not be saved as PNG to "+path+".", ioe);
		}
	}
	/**
	 * Change the color on the graphics context.
	 */
	private void setColor(int r, int g, int b) {
		g2.setColor(new Color(r, g, b)); 
	}
	/**
	 * Calculate from the given centered coordinate the fillX position.
	 */
	private int center(int x, int radius) {
		return x-radius/2;
	}
	/**
	 * Returns the modified in memory image.
	 */
	public BufferedImage getImage() {
		return image;
	}
}


