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
package CacheWolf.navi.touchControls;


import ewe.fx.Image;
import ewe.fx.mImage;
import ewe.graphics.AniImage;

public class MovingMapControlItemText extends MovingMapControlItem {

    private ImageWithText aniImage;
    private int command;
    private String content;
    private String iconText;

    public MovingMapControlItemText(final String iconText, String imageSource, String iconSource,
                                    int alpha, int actionCommand, String content, String alignText, TextOptions tOptions) {

        Image image = MovingMapControlItem.createImage(imageSource, iconSource, alpha);
        aniImage = new ImageWithText(image, tOptions);
        this.iconText = iconText;
        aniImage.setText(iconText);
        aniImage.freeSource();
        aniImage.properties |= mImage.AlwaysOnTop;
        if (content != null) {
            this.content = content;
            xProperties |= IS_ICON_WITH_TEXT;
            if (content.equals("scale")) {
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
        if (actionCommand != -1) {
            command = actionCommand;
            xProperties |= IS_ICON_WITH_COMMAND;
        }
        aniImage.setProperties(xProperties);
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

    public int getCommand() {
        return command;
    }

    public String getContent() {
        return content;
    }

    public String getText() {
        return iconText;
    }

    public void setText(String iconText) {
        aniImage.setText(iconText);
    }

    public void setAdditionalProperty(int prop) {
        aniImage.setStartlineWitdth(prop);
    }

    public int getActionCommand() {
        return command;
    }

    public static class TextOptions {
        private final int fontSize;
        private final int textFromLeft;
        private final int textFromRight;
        private final int textFromTop;
        private final int textFromBottom;

        public TextOptions(int fontSize, int textFromLeft, int textFromRight, int textFromTop, int textFromBottom) {
            super();
            this.fontSize = fontSize;
            this.textFromLeft = textFromLeft;
            this.textFromRight = textFromRight;
            this.textFromTop = textFromTop;
            this.textFromBottom = textFromBottom;
        }

        public int getFontSize() {
            return fontSize;
        }

        public int getTextFromLeft() {
            return textFromLeft;
        }

        public int getTextFromRight() {
            return textFromRight;
        }

        public int getTextFromTop() {
            return textFromTop;
        }

        public int getTextFromBottom() {
            return textFromBottom;
        }
    }

}
