package cachewolf.navi;
import eve.fx.*;
import eve.ui.game.*;
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

	public MapImage(String f) throws ImageDecodingException, UnsupportedImageFormatException, ImageNotFoundException, eve.sys.SystemResourceException {
		super(f);
		if (screenDim == null) screenDim = new Dimension(0,0);
		//TODO Check whether EVE reads correctly from a fake filesystem (EWE 1.49 did not)
	}

	public MapImage(Picture im) {
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
		super.setImage(im.getImageData());
		if (screenDim == null) screenDim = new Dimension(0,0);
	}

	public void setLocation (int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (!hidden && isOnScreen()) {
			super.setLocation(x, y);
			properties &= ~AniImage.IsInvisible;
		} else {
			properties |= AniImage.IsInvisible;
			super.move(0, 0);
		}
	}

	public void move (int x, int y) {
		locAlways.x = x;
		locAlways.y = y;
		if (!hidden && isOnScreen()) {
			super.move(x, y);
			properties &= ~AniImage.IsInvisible;
		} else {
			properties |= AniImage.IsInvisible;
			super.move(0, 0);
		}
	}

	public boolean isOnScreen() {
		if ( (locAlways.x + location.width > 0 && locAlways.x < screenDim.width) &&
				(locAlways.y + location.height > 0 && locAlways.y < screenDim.height) ) return true;
		return false;
	}

	public void screenDimChanged() {
		move(locAlways.x, locAlways.y);
		//if (!hidden && isOnScreen()) properties &= ~AniImage.IsInvisible;
		//else properties |= AniImage.IsInvisible;
	}


	public void hide() {
		hidden = true;
		properties |= AniImage.IsInvisible;
	}
	public void unhide() {
		hidden = false;
		move(locAlways.x, locAlways.y);
	}
}
