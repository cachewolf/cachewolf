package CacheWolf.view.pda;

import ewe.fx.Color;
import ewe.fx.Font;
import ewe.ui.CellConstants;
import ewe.ui.mButton;

public class PDAListButton extends mButton {

	public String fromText;
	public String toText;
	public boolean toLogged;
	public boolean fromLogged;
	
	public PDAListButton(String newText, String newAction) {
		super(newText);
		action = newAction;
		buttonObject = new PDAListButtonObject(this);

		backGround = Color.White;
		foreGround = Color.Black;

		font = new Font(getFont().getName(), Font.BOLD, 40);
		anchor = CellConstants.WEST;
		textPosition=2;
	}

	public void make(boolean paramBoolean) {
		if (this.buttonObject != null)
			return;
		this.buttonObject = new PDAListButtonObject(this);
	}
	
}
