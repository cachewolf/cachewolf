package CacheWolf.view.pda;

import ewe.fx.Dimension;
import ewe.ui.ButtonObject;

public class PDAMenuButtonObject extends ButtonObject{
	private PDAMenuButton pdaMenuButton;
	private Dimension calculateSize;

	public PDAMenuButtonObject(PDAMenuButton pdaListButton) {
		super(pdaListButton);
		this.pdaMenuButton = pdaListButton;
	}

	  public Dimension calculateSize(Dimension paramDimension){
		  calculateSize = super.calculateSize(paramDimension);
		  if (calculateSize.height < 100){
			  calculateSize.height=100;
			  paramDimension.height=100;
		  }
		  return calculateSize;
	  }

}
