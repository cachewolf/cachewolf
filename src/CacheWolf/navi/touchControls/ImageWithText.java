package CacheWolf.navi.touchControls;

import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Rect;
import ewe.graphics.AniImage;
import ewe.ui.MainWindow;
import ewe.util.mString;

public class ImageWithText extends AniImage {

	String[] text = null;

	final Font imageFont;

	private FontMetrics fm;

	private int textHeight;

	private int textWidth = 0;

	private int startlineWidth = 0;

	private int xProperties = 0;;

	public ImageWithText(Image imageSrc, int fontsize) {
		super(imageSrc);
		imageFont = new Font("Helvetica", Font.BOLD,
				fontsize);
		
		MainWindow win = MainWindow.getMainWindow();
		fm = win.getFontMetrics(imageFont);
	}

	public synchronized void draw(Graphics g, int x, int y, int options) {
		super.draw(g, x, y, options);
		g.setFont(imageFont);
		g.setColor(Color.Black);

		int completeWidth = textWidth + 5 + startlineWidth;
		int completeHight = textHeight;

		int startX = x;
		int startY = y;
		if ((xProperties & MovingMapControlItem.ICON_TEXT_HORIZONTAL_CENTER) != 0) {
			startX = x + (getWidth() - completeWidth) / 2;
		} else if ((xProperties & MovingMapControlItem.ICON_TEXT_RIGHT) != 0) {
			startX = x + getWidth() - completeWidth-5;

		}

		if ((xProperties & MovingMapControlItem.ICON_TEXT_VERTICAL_CENTER) != 0) {
			startY = y + (getHeight() - completeHight) / 2;
		} else if ((xProperties & MovingMapControlItem.ICON_TEXT_BOTTOM) != 0) {
			startY = y + getHeight() - completeHight;
		}

		if (startlineWidth > 0) {
			int startliney = startY + completeHight / 2 - 2;
			g.drawRect(startX, startliney, startlineWidth, 4);
			g.fillRect(startX, startliney,
					startlineWidth /2, 4);
			
		}
		if (text != null) {
			g.drawText(fm,text,new Rect((startX + 2 + startlineWidth), startY,textWidth,textHeight),Graphics.CENTER,1);
		}
	}

	public void setText(String text) {
		Dimension size = new Dimension();
		Graphics.getSize(fm, text, size);
		if (size.width>getWidth()*0.7) {
			this.text = new String[]{"",""};
			String[] split= mString.split(text, ' ');
			int lineLength=0;
			for (int i = 0; i < split.length; i++) {
				String part = split[i];
				if (lineLength>text.length()/2) {
					this.text[1]+=part+" ";
				}else{
					this.text[0]+=part+" ";
					lineLength++;
					lineLength+=part.length();
				}
			}
			Graphics.getSize(fm, this.text, 0, this.text.length, size);
		}else{
			this.text = new String[]{text};
		}
		textHeight = size.height;
		textWidth = size.width;
		
	}

	public void setStartlineWitdth(int startlineWidth) {
		this.startlineWidth = startlineWidth;
	}

	public void setProperties(int xProperties) {
		this.xProperties = xProperties;

	}
}
