/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */

package de.uni_siegen.wineme.come_in.thumbnailer.thumbnailers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import de.uni_siegen.wineme.come_in.thumbnailer.FileDoesNotExistException;
import de.uni_siegen.wineme.come_in.thumbnailer.ThumbnailerException;
import de.uni_siegen.wineme.come_in.thumbnailer.util.ResizeImage;

/**
 * Renders the first page of a PDF file into a thumbnail.
 * 
 * Performance note: This takes about 2-3 seconds per file.
 * Depends on:
 * <li>PDFBox (>= 2.0.4)
 */
public class PDFBoxThumbnailer extends AbstractThumbnailer {

	@Override
	public void generateThumbnail(File input, File output) throws IOException,
			ThumbnailerException {
		FileDoesNotExistException.check(input);
		if (input.length() == 0)
			throw new FileDoesNotExistException("File is empty");
		FileUtils.deleteQuietly(output);

		PDDocument document = null;
		try
		{
			try {
				document = PDDocument.load(input);
			} catch (IOException e) {
				throw new ThumbnailerException("Could not load PDF File", e);
			}

			BufferedImage tmpImage = writeImageFirstPage(document, ImageType.RGB);

			if (tmpImage.getWidth() == thumbWidth)
			{
				ImageIO.write(tmpImage, "PNG", output);
			}
			else
			{
				ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
				resizer.resizeMethod = ResizeImage.RESIZE_FIT_BOTH_DIMENSIONS;
				resizer.setInputImage(tmpImage);
				resizer.writeOutput(output);				
			}
		}
		finally
		{
			if( document != null )
			{
				try {
					document.close();
				} catch (IOException e)  {}
			}
		}
	}
	
	/**
	 * Loosely based on the commandline-Tool PDFImageWriter
	 * @param document
	 * @param imageType
	 * @return
	 * @throws IOException
	 */
    private BufferedImage writeImageFirstPage(PDDocument document, ImageType imageType)
    throws IOException
    {
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		PDPage page = document.getPage(0);
		PDRectangle mBox = page.getMediaBox();
		float widthPt = mBox.getWidth();
		float scaling = thumbWidth / widthPt; // resolution / 72.0F;
		BufferedImage image =  pdfRenderer.renderImage(0, scaling, imageType);
    	return image;
    }

    /**
     * Get a List of accepted File Types.
     * Only PDF Files are accepted.
     * 
     * @return MIME-Types
     */
	public String[] getAcceptedMIMETypes()
	{
		return new String[]{
				"application/pdf"
		};
	}

    
}
