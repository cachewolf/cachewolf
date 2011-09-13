    /*
    GNU General Public License
    CacheWolf is a software for PocketPC, Win and Linux that
    enables paperless caching.
    It supports the sites geocaching.com and opencaching.de

    Copyright (C) 2006  CacheWolf development team
    See http://developer.berlios.de/projects/cachewolf/
    for more information.
    Contact: 	bilbowolf@users.berlios.de
    			kalli@users.berlios.de

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
package CacheWolf.navi.touchControls;

import CacheWolf.navi.touchControls.MovingMapControlItemText.TextOptions;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.FontMetrics;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Rect;
import ewe.graphics.AniImage;
import ewe.ui.MainWindow;

public class ImageWithText extends AniImage {
	String[] text = null;
	final Font imageFont;
	private FontMetrics fm;
	private int textHeight;
	private int textWidth = 0;
	private int startlineWidth = 0;
	private int xProperties = 0;
	private TextOptions tOptions;;
	public ImageWithText(Image imageSrc, TextOptions tOptions) {
		super(imageSrc);
		imageFont = new Font("Helvetica", Font.BOLD, tOptions.getFontSize());
		this.tOptions = tOptions;
		MainWindow win = MainWindow.getMainWindow();
		fm = win.getFontMetrics(imageFont);
	}

	public synchronized void draw(Graphics g, int x, int y, int options) {
		super.draw(g, x, y, options);
		g.setFont(imageFont);
		g.setColor(Color.Black);

		int completeWidth = textWidth + 5 + startlineWidth;
		int completeHight = textHeight;

		int startX = x+tOptions.getTextFromLeft();
		int startY = y+tOptions.getTextFromTop();
		if ((xProperties & MovingMapControlItem.ICON_TEXT_HORIZONTAL_CENTER) != 0) {
			startX = x + tOptions.getTextFromLeft()+ (getWidth() - completeWidth-tOptions.getTextFromLeft()) / 2;
		} else if ((xProperties & MovingMapControlItem.ICON_TEXT_RIGHT) != 0) {
			startX = x + getWidth() - completeWidth - tOptions.getTextFromRight();

		}

		if ((xProperties & MovingMapControlItem.ICON_TEXT_VERTICAL_CENTER) != 0) {
			startY = y +tOptions.getTextFromTop() + (getHeight() - completeHight) / 2;
		} else if ((xProperties & MovingMapControlItem.ICON_TEXT_BOTTOM) != 0) {
			startY = y + getHeight() - completeHight - tOptions.getTextFromBottom();
		}

		if (startlineWidth > 0) {
			int startliney = startY + completeHight / 2 - 2;
			g.drawRect(startX, startliney, startlineWidth, 4);
			g.fillRect(startX, startliney, startlineWidth / 2, 4);

		}
		if (text != null) {
			g.drawText(fm, text, new Rect((startX + 2 + startlineWidth),
					startY, textWidth, textHeight), Graphics.CENTER, 1);
		}
	}

	public void setText(String text) {
		Dimension size = new Dimension();
		Graphics.getSize(fm, text, size);
		int offsets = 0;
		offsets += tOptions.getTextFromLeft();
		offsets += tOptions.getTextFromRight();

		if (size.width > getWidth() - offsets) {
			char[] chars = text.toCharArray();
			int splitindex = 0;
			int half = chars.length / 2;
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (c == ' ') {
					if (i <= half / 2) {
						splitindex = i;
					} else {
						if (splitindex <= half && half - splitindex > i - half) {
							splitindex = i;
							break;
						}
					}
				}
			}

			if (splitindex > 0) {
				this.text = new String[] { text.substring(0, splitindex),
						text.substring(splitindex) };
				Graphics.getSize(fm, this.text, 0, this.text.length, size);
			}

		} else {
			this.text = new String[] { text };
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
