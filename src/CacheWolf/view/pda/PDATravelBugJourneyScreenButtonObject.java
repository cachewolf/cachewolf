package CacheWolf.view.pda;

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.Graphics;
import ewe.fx.Rect;
import ewe.ui.Gui;

public class PDATravelBugJourneyScreenButtonObject extends PDAListButtonObject {

    public PDATravelBugJourneyScreenButtonObject(PDAListButton pdaListButton) {
        super(pdaListButton);
    }

    public void paint(Graphics paramGraphics) {
        if ((this.soft) && (this.control != null))
            this.control.doBackground(paramGraphics);
        if (this.text == null)
            this.text = "";
        drawButton(paramGraphics);
        Rect paramRect = new Rect(this.borderWidth, this.borderWidth, this.size.width - (this.borderWidth * 2), this.size.height - (this.borderWidth * 2));
        Rect localRect1 = paramGraphics.reduceClip(paramRect);
        //On PocketPC2003 sometimes reduceClip returns null. If this happens, the clipping area seems to be determined by its input parameter.
        if (localRect1 == null) localRect1 = paramRect;
        try {
            paramGraphics.setColor(foreground);
            int x = 10;
            if (this.image != null) {
                int y = (size.height - image.getHeight()) / 2;
                this.image.draw(paramGraphics, 10, y, 0);
                x += image.getWidth();
                x += 10;
                localRect1.width -= x;
            }
            font = new Font(font.getName(), Font.BOLD, 40);
            pdaListButton.font = font;
            boolean found = false;
            while (!found) {
                Rect textRect = Gui.getSize(pdaListButton.getFontMetrics(), text, 5, 0);
                if ((textRect.width > localRect1.width || textRect.height > localRect1.height) && font.getSize() > 5) {
                    pdaListButton.font = font = new Font(font.getName(), Font.BOLD, font.getSize() - 1);
                    textRect = Gui.getSize(pdaListButton.getFontMetrics(), text, 5, 0);
                } else {
                    found = true;
                }
            }

            paramGraphics.setFont(this.font);
            paramGraphics.drawText(text, x, 10);
            Font tmpFont = new Font(font.getName(), Font.BOLD, 20);
            paramGraphics.setFont(tmpFont);
            if (pdaListButton.fromText != null) {
                paramGraphics.drawText(pdaListButton.fromText, x + 15, 45);
            }
            if (!pdaListButton.fromLogged) {
                paramGraphics.setColor(new Color(255, 0, 0));
                paramGraphics.fillEllipse(x, 50, 10, 10);
                paramGraphics.setColor(foreground);
            }
            if (pdaListButton.toText != null) {
                paramGraphics.drawText(pdaListButton.toText, x + 15, 70);
            }
            if (!pdaListButton.toLogged) {
                paramGraphics.setColor(new Color(255, 0, 0));
                paramGraphics.fillEllipse(x, 75, 10, 10);
                paramGraphics.setColor(foreground);
            }
        } finally {
            paramGraphics.restoreClip(localRect1);
        }
    }
}
