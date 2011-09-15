package CacheWolf.view.pda;

import CacheWolf.MyLocale;
import ewe.ui.ControlEvent;
import ewe.ui.Form;

public abstract class PDAMenu extends Form {

	protected static final String CANCEL = "__Cancel_Exit__";

	public abstract void actionPerformed(String actionCommand);

	public PDAMenu(){
		setPreferredSize(MyLocale.getScreenWidth(), MyLocale.getScreenHeight());
	}
	
	public void onControlEvent(ControlEvent paramEvent) {
		switch (paramEvent.type) {
		case ControlEvent.PRESSED:
			if (paramEvent.action.equals(CANCEL)) {
				exit(0);
				
			} else {
				actionPerformed(paramEvent.action);
			}
		}
		super.onControlEvent(paramEvent);
	}

	protected void buildMenu() {
		PDAMenuButton button = new PDAMenuButton(MyLocale.getMsg(6057, "Back"), CANCEL);
		addLast(button);
	}

	protected void addMenuItem(String item, String actionCommand) {
		PDAMenuButton button = new PDAMenuButton(item, actionCommand);
		addLast(button);
	}

}
