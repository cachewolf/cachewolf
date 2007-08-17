package CacheWolf;

import ewe.ui.CellPanel;
import ewe.ui.Editor;

public class TableForm extends Editor {
	public TableForm(TablePanel tp) {
		CellPanel[] menuList = addToolbar();
		menuList[0].addLast(Global.mainForm.mMenu);
		menuList[1].addLast(tp); 
	}
}
