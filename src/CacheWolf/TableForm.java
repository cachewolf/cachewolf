package CacheWolf;

import ewe.ui.CellConstants;
import ewe.ui.CellPanel;
import ewe.ui.Editor;

public class TableForm extends Editor {
	public TableForm(TablePanel tp) {
		if (Global.getPref().menuAtTop) {
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(tp,STRETCH,FILL);
		} else {
			this.addLast(tp,STRETCH,FILL);
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		/*
		CellPanel[] menuList = addToolbar();
		menuList[0].addLast(Global.mainForm.mMenu);
		menuList[1].addLast(tp);
		*/
	}
}
