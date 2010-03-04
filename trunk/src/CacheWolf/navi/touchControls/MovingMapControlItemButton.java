package CacheWolf.navi.touchControls;


import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;

public class MovingMapControlItemButton extends MovingMapControlItem {

	private String command;
	private AniImageGrayScaled aniImage;
	
	public MovingMapControlItemButton(String source,String iconSrc, String actionCommand,
			int alpha) {
		Image image = MovingMapControlItem.createImage(source, iconSrc, alpha);
		aniImage = new AniImageGrayScaled(image);
		aniImage.freeSource();
		command = actionCommand;
		aniImage.properties|= mImage.AlwaysOnTop;
		xProperties|=IS_ICON_WITH_COMMAND;
	}

	public String getActionCommand() {
		return command;
	}

	
	public int getWidth() {
		return aniImage.getWidth();
	}

	public int getHeight() {
		return aniImage.getHeight();
	}

	public AniImage getImage() {
		return aniImage;
	}

	public void setText(String text) {

	}

}
