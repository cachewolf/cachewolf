package CacheWolf.view.pda;

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.fx.Graphics;
import ewe.fx.Rect;
import ewe.ui.ButtonObject;

public class PDAListButtonObject extends ButtonObject {
	private PDAListButton pdaListButton;

	public PDAListButtonObject(PDAListButton pdaListButton) {
		super(pdaListButton);
		this.pdaListButton = pdaListButton;
	}

	public void paint(Graphics paramGraphics) {
		if ((this.soft) && (this.control != null))
			this.control.doBackground(paramGraphics);
		if (this.text == null)
			this.text = "";
		drawButton(paramGraphics);
		Rect localRect1 = paramGraphics.reduceClip(new Rect(this.borderWidth, this.borderWidth, this.size.width
				- (this.borderWidth * 2), this.size.height - (this.borderWidth * 2)));
		try {
			paramGraphics.setColor(foreground);
			int x = 10;
			if (this.image != null) {
				int y = (size.height - image.getHeight()) / 2;
				this.image.draw(paramGraphics, 10, y, 0);
				x += image.getWidth();
				x += 10;
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
