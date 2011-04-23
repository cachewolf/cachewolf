package CacheWolf.view.pda;

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.ui.CellConstants;
import ewe.ui.mButton;

public class PDAMenuButton extends mButton {

	public String fromText;
	public String toText;
	public boolean toLogged;
	public boolean fromLogged;

	public PDAMenuButton(String newText, String newAction) {
		super(newText);
		action = newAction;

		minHeight = 200;
		preferredHeight=200;
		maxHeight=200;
		backGround = Color.Sand;
		foreGround = Color.Black;

		font = new Font(getFont().getName(), Font.BOLD, 40);
		anchor = CellConstants.WEST;
		textPosition = 2;
	}
	
	public void make(boolean paramBoolean) {
		if (this.buttonObject != null)
			return;
		this.buttonObject = new PDAMenuButtonObject(this);
	}
}