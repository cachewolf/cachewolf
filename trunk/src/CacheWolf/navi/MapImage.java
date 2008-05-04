package CacheWolf.navi;
import ewe.fx.*;
import ewe.graphics.*;
import ewe.io.FileInputStream;
import ewe.io.FileReader;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.InputStreamReader;
import ewe.io.Stream;
import ewe.util.ByteArray;
/** 
 * class that can be used with any x and any y
 * it will save taht location and make itself automatically
 * invisible if it is not on the screen. Call setscreensize to
 * set the screensize
 * @author pfeffer
 *
 */
public class MapImage extends AniImage {
	public Point locAlways = new Point(); // contains the theoretical location even if it the location is out of the screen. If the image is on the screen, it contains the same as location
	public static Dimension screenDim;
	boolean hidden = false;
	public MapImage() {
		super();
		if (screenDim == null) screenDim = new Dimension(0,0);
	}

	public MapImage(String f) throws ImageDecodingException, UnsupportedImageFormatException, ImageNotFoundException, ewe.sys.SystemResourceException {
		if (screenDim == null) screenDim = new Dimension(0,0);
		//super(f); the following code is only necessary because of an Bug in ewe 1.49, which doesn't read from a fakefilesystem. If there were no bug, calling super(f) would be sufficient
		ewe.io.File file = ewe.sys.Vm.newFileObject();
		file.set(null, f);
		try {
		//ByteArray imbytes = ewe.io.IO.readAllBytes(input, knownSize, stopAfterKnownSize);(file, null, true); // this would be possible if ewe 1.49 wouldn't have another bug: fakefilesystem doesn't implement (oderride) length(), it only overrides getLenght(), that's why readallBytes will call the original File implementation and cause and NullpointerException
		setImage(new Image(new FileInputStream(f).toReadableStream(), 0), 0); // copied from super() 
		freeSource(); // copied from super()
		} catch (IOException e) { 
			throw new ImageNotFoundException(f); // in order to behave the same way as super would have 
		}
	}

	public MapImage(mImage im) {
		super(im);
		if (screenDim == null) screenDim = new Dimension(0,0);
	}

	/**
	 * Best you call this routine before you make any instance of MapImage
	 * If the windows size changes after instantiation call  screenDimChanged()
	 * for every symbol.
	 * 
	 */
	public static void setScreenSize(int w, int h) {
		screenDim = new Dimension(w, h);
	}

	public void setImage(Image im, Color c) {
		super.setImage(im, c);
		if (screenDim == null) screenDim = new Dimension(0,0);
	}

	public void setLocation (int x, int y) {
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

	public void move (int x, int y) {
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
		if ( (locAlways.x + location.width > 0 && locAlways.x < screenDim.width) && 
				(locAlways.y + location.height > 0 && locAlways.y < screenDim.height) ) return true;
		else return false;
	}

	public void screenDimChanged() {
		move(locAlways.x, locAlways.y);
		//if (!hidden && isOnScreen()) properties &= ~AniImage.IsInvisible;
		//else properties |= AniImage.IsInvisible;
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
