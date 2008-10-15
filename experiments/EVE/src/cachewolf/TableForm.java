package cachewolf;

import eve.ui.CellConstants;
import eve.ui.Form;

public class TableForm extends Form {
	public TableForm(TablePanel tp) {
		if (Global.getPref().menuAtTop) {
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
			this.addLast(tp,STRETCH,FILL);
		} else {
			this.addLast(tp,STRETCH,FILL);
			this.addLast(Global.mainForm.mMenu,CellConstants.DONTSTRETCH, CellConstants.FILL);
		}
		this.firstFocus = tp; // give the first fokus to the list of caches, not to the main menu
		/*
		CellPanel[] menuList = addToolbar();
		menuList[0].addLast(Global.mainForm.mMenu);
		menuList[1].addLast(tp);
		*/
	}
}
