package CacheWolf.view.pda;

import ewe.fx.Dimension;
import ewe.ui.ButtonObject;
import ewe.ui.Gui;

public class PDAMenuButtonObject extends ButtonObject{
	private PDAMenuButton pdaMenuButton;
	private Dimension calculateSize;

	public PDAMenuButtonObject(PDAMenuButton pdaListButton) {
		super(pdaListButton);
		this.pdaMenuButton = pdaListButton;
	}

	public Dimension calculateSize(Dimension paramDimension) {
		calculateSize = super.calculateSize(paramDimension);
		if (calculateSize.height < Gui.screenSize.height / 8) {
			calculateSize.height = paramDimension.height = Gui.screenSize.height / 8;
		}
		return calculateSize;
	}

}
