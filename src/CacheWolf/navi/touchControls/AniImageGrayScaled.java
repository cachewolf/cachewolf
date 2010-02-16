package CacheWolf.navi.touchControls;

import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;

public class AniImageGrayScaled extends AniImage {
	public AniImageGrayScaled(Image image) {
		super(image);
	}

	public void doDraw(Graphics g, int options) {
		if ((properties & mImage.IsNotHot) != 0) {
			options |= DISABLED;
		}
		super.doDraw(g, options);
	}
}
