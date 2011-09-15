package CacheWolf.view.pda;

import CacheWolf.Global;
import ewe.fx.Font;
import ewe.fx.Graphics;
import ewe.fx.Rect;
import ewe.ui.ButtonObject;
import ewe.ui.Gui;

public class PDAListButtonObject extends ButtonObject {
	protected PDAListButton pdaListButton;

	public PDAListButtonObject(PDAListButton pdaListButton) {
		super(pdaListButton);
		this.pdaListButton = pdaListButton;
	}

	public void paint(Graphics paramGraphics) {
		try{
		if ((this.soft) && (this.control != null))
			this.control.doBackground(paramGraphics);
		if (this.text == null)
			this.text = "";
		drawButton(paramGraphics);
		Rect paramRect = new Rect(this.borderWidth, this.borderWidth, this.size.width - (this.borderWidth * 2), this.size.height - (this.borderWidth * 2));
		Rect localRect1 = paramGraphics.reduceClip(paramRect);
		//On PocketPC2003 sometimes reduceClip returns null. If this happens, the clipping area seems to be determined by its input parameter.
		if (localRect1 == null) localRect1=paramRect;
		try {
			paramGraphics.setColor(foreground);
			int x = 10;
			if (this.image != null) {
				int y = (size.height - image.getHeight()) / 2;
				this.image.draw(paramGraphics, 10, y, 0);
				x += image.getWidth();
				x += 10;
			}
			
			int fontSize = 40;
			font = new Font(font.getName(), Font.BOLD, fontSize);
			boolean found = false;
			while (!found) {
				Rect textRect = Gui.getSize(pdaListButton.getFontMetrics(), text, 5, 0);
				if (textRect.width > localRect1.width && textRect.height > localRect1.height && fontSize > 5) {
					fontSize--;
					Font tmpFont = new Font(font.getName(), Font.BOLD, fontSize);
					if (tmpFont != null){
						font = tmpFont;
					}
					textRect = Gui.getSize(pdaListButton.getFontMetrics(), text, 5, 0);
				} else {
					found = true;
				}
			}
			paramGraphics.setFont(this.font);
			paramGraphics.drawText(text, x, 10);
			Font tmpFont = new Font(font.getName(), Font.BOLD, 20);
			paramGraphics.setFont(tmpFont);
		} finally {
			paramGraphics.restoreClip(localRect1);
		}
		}
		catch(Exception e){
			Global.getPref().log("Mysterious Exception caught!", e, true);
		}
	}
}
