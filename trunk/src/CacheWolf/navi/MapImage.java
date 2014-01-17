/*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
	See http://www.cachewolf.de/ for more information.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package CacheWolf.navi;

import CacheWolf.Preferences;
import CacheWolf.utils.Common;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Image;
import ewe.fx.ImageDecodingException;
import ewe.fx.ImageNotFoundException;
import ewe.fx.Point;
import ewe.fx.UnsupportedImageFormatException;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.io.BufferedInputStream;
import ewe.io.FileInputStream;
import ewe.io.IOException;
import ewe.util.ByteArray;

/**
 * class that can be used with any x and any y
 * it will save that location and
 * make itself automatically invisible if it is not on the screen.
 * Call setscreensize to set the screensize
 * 
 * @author pfeffer
 * 
 */
public class MapImage extends AniImage {
    // contains the theoretical location even if it the location is out of the screen. 
    // if the image is on the screen, it contains the same as location
    public Point locAlways = new Point();
    public static Dimension screenDim;
    boolean hidden = false;

    public MapImage() {
	super();
	if (screenDim == null)
	    screenDim = new Dimension(0, 0);
    }

    public MapImage(String f) throws ImageDecodingException, UnsupportedImageFormatException, ImageNotFoundException, ewe.sys.SystemResourceException {
	// f kommt aus MapInfoObject.getImagePathAndName
	if (screenDim == null)
	    screenDim = new Dimension(0, 0);
	Preferences.itself().log("create MapImage from: " + f);
	if (f.indexOf("!") < 0) {
	    // the following code is only necessary because of an Bug in ewe 1.49, which doesn't read from a fakefilesystem. 
	    // If there were no bug, calling super(f) would be sufficient
	    // super(f); 
	    // copied from super()
	    ewe.io.File file = ewe.sys.Vm.newFileObject();
	    file.set(null, f);
	    try {
		// ByteArray imbytes = ewe.io.IO.readAllBytes(input, knownSize, stopAfterKnownSize);(file, null, true); 
		// this would be possible if ewe 1.49 wouldn't have another bug: 
		// fakefilesystem doesn't implement (override) length(), it only overrides getLenght(),
		// that's why readallBytes will call the original File implementation 
		// and causes a NullpointerException
		setImage(new Image(new FileInputStream(f).toReadableStream(), 0), 0);
		freeSource();
	    } catch (IOException e) {
		throw new ImageNotFoundException(f); // in order to behave the same way as super would have
	    }
	} else {
	    // it is a packfile
	    String p[] = ewe.util.mString.split(f, '!');
	    int bboxMinX = Common.parseInt(p[1]);
	    int bboxMinY = Common.parseInt(p[2]);
	    int bboxStride = Common.parseInt(p[3]);
	    long OffsetToIndex = Long.valueOf(p[4]).longValue();
	    int ZoomWanted = Common.parseInt(p[5]);
	    int xWanted = Common.parseInt(p[6]);
	    int yWanted = Common.parseInt(p[7]);
	    ByteArray ba = null;
	    Image im;
	    try {

		int index = (yWanted - bboxMinY) * bboxStride + (xWanted - bboxMinX) - 1;
		long offset = OffsetToIndex + index * 8;

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(p[0]), 20480);

		long skipped = 0;
		do {
		    skipped = skipped + bis.skip(offset);
		} while (offset - skipped > 0);

		long tileOffset = readReverseLong(bis);
		long nextOffset = readReverseLong(bis);
		int length = (int) (nextOffset - tileOffset);
		Preferences.itself().log(" tileOffset/length for x/y/zoom (" + xWanted + "/" + yWanted + "/" + ZoomWanted + ")= " + tileOffset + "/" + length);

		if (length == 0) {
		    Preferences.itself().log("wanted == 0 (nextOffset - tileOffset)");
		    bis.close();
		    throw new ImageNotFoundException(f); // in order to behave the same way as file
		}

		skipped = 0;
		do {
		    skipped = skipped + bis.skip(tileOffset - offset - 16);
		} while (tileOffset - offset - 16 - skipped > 0);

		byte[] buffer = new byte[length];
		int readTilNow = 0;
		int stillToRead = length;
		do {
		    int anzRead = bis.read(buffer, readTilNow, stillToRead);
		    readTilNow = readTilNow + anzRead;
		    stillToRead = stillToRead - anzRead;
		} while (stillToRead > 0);

		bis.close();

		/*
		// check for support / conversion
		byte[] signature = new byte[]
			{ (byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };
		if (Arrays.equals(signature, get(buffer, 0, 8)))
		{
			// es ist ein png
			byte BitDepth = buffer[24];
			// byte ColourType = buffer[25];
			// byte CompressionMethod = buffer[26];
			// BitDepth not supported by pixmap
			switch (BitDepth)
			{
			case 4:
				// Logger.DEBUG("[PackBase] unsupported png in Pack " + this.Filename + " tile: " + desc);
				InputStream in = new ByteArrayInputStream(buffer);
				BufferedImage img = ImageIO.read(in);
				ByteArrayOutputStream bas = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", bas);
				byte[] data = bas.toByteArray();
				bas.close();
				return data;
				// break;
			case 8:
				// supported
				break;
			default:
				// perhaps supported
				break;
			}
		}
		*/
		ba = new ByteArray(buffer);
		im = new Image(ba, 0);
		setImage(im);
	    } catch (Exception exc) {
		Preferences.itself().log(exc + " Error getting image from pack-file " + p[0] + " for " + p[6] + "/" + p[7] + "/" + p[5] + " Bufferlength " + ba.length);
		// ignore throw new ImageNotFoundException(f); // in order to behave the same way as super would have
	    }
	}
    }

    private long readReverseLong(BufferedInputStream bis) throws IOException {
	int byte8 = bis.read();
	int byte7 = bis.read();
	int byte6 = bis.read();
	int byte5 = bis.read();
	int byte4 = bis.read();
	int byte3 = bis.read();
	int byte2 = bis.read();
	int byte1 = bis.read();
	return (long) (((byte1 & 0xFF) << 56) + ((byte2 & 0xFF) << 48) + ((byte3 & 0xFF) << 40) + ((byte4 & 0xFF) << 32) + ((byte5 & 0xFF) << 24) + ((byte6 & 0xFF) << 16) + ((byte7 & 0xFF) << 8) + (byte8 & 0xFF));
    }

    public MapImage(mImage im) {
	super(im);
	if (screenDim == null)
	    screenDim = new Dimension(0, 0);
    }

    /**
     * Best you call this routine before you make any instance of MapImage
     * If the windows size changes after instantiation call screenDimChanged() for every symbol.
     * 
     */
    public static void setScreenSize(int w, int h) {
	screenDim = new Dimension(w, h);
    }

    public void setImage(Image im, Color c) {
	super.setImage(im, c);
	if (screenDim == null)
	    screenDim = new Dimension(0, 0);
    }

    public void setLocation(int x, int y) {
	locAlways.x = x;
	locAlways.y = y;
	if (!hidden && isOnScreen()) {
	    super.setLocation(x, y);
	    properties &= ~mImage.IsInvisible;
	} else {
	    properties |= mImage.IsInvisible;
	    super.move(0, 0);
	}
    }

    public void move(int x, int y) {
	locAlways.x = x;
	locAlways.y = y;
	if (!hidden && isOnScreen()) {
	    super.move(x, y);
	    properties &= ~mImage.IsInvisible;
	} else {
	    properties |= mImage.IsInvisible;
	    super.move(0, 0);
	}
    }

    public boolean isOnScreen() {
	if ((locAlways.x + location.width > 0 && locAlways.x < screenDim.width) && (locAlways.y + location.height > 0 && locAlways.y < screenDim.height))
	    return true;
	else
	    return false;
    }

    public void screenDimChanged() {
	move(locAlways.x, locAlways.y);
    }

    public void hide() {
	hidden = true;
	properties |= mImage.IsInvisible;
    }

    public void unhide() {
	hidden = false;
	move(locAlways.x, locAlways.y);
    }
}
