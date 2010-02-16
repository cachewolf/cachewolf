package CacheWolf.navi.touchControls;


import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;
import ewe.sys.Vm;
import ewe.util.ByteArray;

public class MovingMapControlItemText extends MovingMapControlItem {

	private ImageWithText aniImage;
	private String command;
	private String context;

	public MovingMapControlItemText(final String iconText, String imageSource,String iconSource,
			int alpha, String actionCommand, String context, String alignText,int fontsize) {

		Image image = MovingMapControlItem.createImage(imageSource, iconSource, alpha);
		
		aniImage = new ImageWithText(image,fontsize);

		aniImage.setText(iconText);
		aniImage.freeSource();

		aniImage.properties |= mImage.AlwaysOnTop;

		if (context != null) {
			this.context = context;
			xProperties |= IS_ICON_WITH_TEXT;
			if (context.equals("scale")) {
				xProperties |= IS_ICON_WITH_FRONTLINE;
			}
		}
		if (alignText != null) {
			alignText = alignText.toUpperCase();

			if (alignText.startsWith("T")) {
				xProperties |= ICON_TEXT_TOP;
			} else if (alignText.startsWith("B")) {
				xProperties |= ICON_TEXT_BOTTOM;
			} else
				xProperties |= ICON_TEXT_VERTICAL_CENTER;

			if (alignText.endsWith("L")) {
				xProperties |= ICON_TEXT_LEFT;
			} else if (alignText.endsWith("R")) {
				xProperties |= ICON_TEXT_RIGHT;
			} else
				xProperties |= ICON_TEXT_HORIZONTAL_CENTER;
		} else {
			xProperties |= ICON_TEXT_VERTICAL_CENTER;
			xProperties |= ICON_TEXT_HORIZONTAL_CENTER;
		}
		if (actionCommand != null) {
			command = actionCommand;
			xProperties |= IS_ICON_WITH_COMMAND;
		}

		aniImage.setProperties(xProperties);
	}

	public void setText(String iconText) {
		aniImage.setText(iconText);
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

	public String getCommand() {
		return command;
	}

	public String getContext() {
		return context;
	}

	public void setAdditionalProperty(int prop) {
		aniImage.setStartlineWitdth(prop);
	}

	public String getActionCommand() {
		return command;
	}
}
