package CacheWolf;

import ewe.ui.CellPanel;
import ewe.ui.ControlConstants;
import ewe.ui.Gui;
import ewe.ui.IKeys;
import ewe.ui.Window;
import ewe.ui.mButton;

public class ExecutePanel extends CellPanel {
    public final static int LEFT = 1, MIDDLE = 2, RIGHT = 4, CANCEL = LEFT, APPLY = RIGHT; // flags
    public mButton leftButton, middleButton, rightButton;
    public mButton cancelButton, refuseButton, applyButton;
    private int flags;

    public ExecutePanel(CellPanel panel) {
	this.equalWidths = true;
	cancelButton = GuiImageBroker.getButton(MyLocale.getMsg(614, "Cancel"), "cancel");
	cancelButton.setHotKey(0, IKeys.ESCAPE);
	this.addNext(cancelButton);
	applyButton = GuiImageBroker.getButton(MyLocale.getMsg(615, "Apply"), "apply");
	applyButton.setHotKey(0, IKeys.ACTION);
	applyButton.setHotKey(0, IKeys.ENTER);
	this.addNext(applyButton);
	panel.addLast(this, DONTSTRETCH, FILL);
	leftButton = cancelButton;
	rightButton = applyButton;
	flags = LEFT & RIGHT;
    }

    /** Cancel buttons should come before OK buttons. */
    private static boolean reverse = (Gui.getGuiFlags() & Window.GUI_FLAG_REVERSE_OK_CANCEL) != 0;

    public ExecutePanel(CellPanel panel, int which) {
	/*
		boolean first = !reverse;
		
		if ((which & (FormBase.YESB|FormBase.NOB)) == (FormBase.YESB|FormBase.NOB))
			if ((which & (FormBase.CANCELB|FormBase.DEFCANCELB)) != 0)
				if (!first){
				    cancelButton = addButton(which,FormBase.DEFCANCELB|FormBase.CANCELB,"Cancel",cross,IKeys.ESCAPE);
					no = addButton(which,NOB,"No",stop,'n');
					applyButton = addButton(which,YESB,"Yes",tick,IKeys.ENTER);
				}
		for (int i = 0; i<2; i++){
			if (first){
				if (ok == null) ok = addButton(which,OKB,"OK",tick,'o');
				if (ok == null) ok = addButton(which,DEFOKB,"OK",tick,IKeys.ENTER);
				if (yes == null) yes = addButton(which,YESB,"Yes",tick,reverse ? IKeys.ENTER : 'y');
			}else{
				if (no == null) no = addButton(which,NOB,"No",stop,reverse ? IKeys.ESCAPE : 'n');
				if (cancel == null) cancel = addButton(which,CANCELB,"Cancel",cross,'c');
				if (cancel == null) cancel = addButton(which,DEFCANCELB,"Cancel",cross,IKeys.ESCAPE);
			}
			first = !first;
		}
		if ((which & CANCELB) == 0)
		if (back == null) back = addButton(which,BACKB,"<< Back",null,'b');
	*/
	this.equalWidths = true;

	middleButton = GuiImageBroker.getButton(MyLocale.getMsg(1605, "OK"), "OK");
	middleButton.setHotKey(0, IKeys.ACTION);
	middleButton.setHotKey(0, IKeys.ENTER);
	middleButton.setHotKey(0, IKeys.ESCAPE);
	this.addNext(middleButton);
	panel.addLast(this, DONTSTRETCH, FILL);
	applyButton = middleButton;
	cancelButton = middleButton;
	flags = MIDDLE;
    }

    public ExecutePanel(CellPanel panel, String leftText, String middleText, String rightText, String leftIcon, String middleIcon, String rightIcon) {
	this.equalWidths = true;
	if (leftText != null) {
	    leftButton = GuiImageBroker.getButton(leftText, leftIcon);
	    this.addNext(leftButton);
	}
	if (middleText != null) {
	    middleButton = GuiImageBroker.getButton(middleText, middleIcon);
	    this.addNext(middleButton);
	}
	if (leftText != null) {
	    rightButton = GuiImageBroker.getButton(rightText, rightIcon);
	    this.addLast(rightButton);
	}
	panel.addLast(this, DONTSTRETCH, FILL);
    }

    public void enable(int button) {
	if ((flags & button & LEFT) != 0) {
	    leftButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & MIDDLE) != 0) {
	    middleButton.set(ControlConstants.Disabled, false);
	}
	if ((flags & button & RIGHT) == 0) {
	    rightButton.set(ControlConstants.Disabled, false);
	}
	repaint();
    }

    public void disable(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, false);
	leftButton.set(ControlConstants.Disabled, true);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, false);
	middleButton.set(ControlConstants.Disabled, true);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, false);
	rightButton.set(ControlConstants.Disabled, true);
	repaint();
    }

    public void show(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, false);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, false);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, false);
	repaint();
    }

    public void hide(int button) {
	if ((flags & button & LEFT) != 0)
	    leftButton.set(ControlConstants.Invisible, true);
	if ((flags & button & MIDDLE) != 0)
	    middleButton.set(ControlConstants.Invisible, true);
	if ((flags & button & RIGHT) == 0)
	    rightButton.set(ControlConstants.Invisible, true);
	repaint();
    }

}
